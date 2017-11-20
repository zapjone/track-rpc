package com.track.client.discover;

import com.track.common.util.Constant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rpc客户端服务自动发现
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class ZkServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(ZkServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);
    private volatile Set<String> serverSet = new HashSet<>();

    private String registryAddress;

    public ZkServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;

        // 连接zk进行服务发现
        try {
            ZooKeeper zk = connectZk();
            if (zk != null) {
                watchNode(zk);
            }
        } catch (IOException | InterruptedException | KeeperException e) {
            LOG.error("发现服务出现异常:", e);
        }
    }

    @Override
    public String discover() {
        if (serverSet.size() <= 0) {
            return null;
        }

        String data = null;
        int size = serverSet.size();
        if (size == 1) {
            data = serverSet.iterator().next();
        } else {
            Iterator iter = serverSet.iterator();
            int solt = ThreadLocalRandom.current().nextInt(size);
            int i = 0;
            while (iter.hasNext()) {
                if (i == solt) {
                    data = (String) iter.next();
                    break;
                }
                i++;
            }
        }

        LOG.debug("using random data: {}", data);
        return data;
    }

    /**
     * 连接zookeeper
     */
    private ZooKeeper connectZk() throws IOException, InterruptedException {
        ZooKeeper zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, event -> {
            if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                latch.countDown();
            }
        });

        latch.await();

        return zk;
    }

    /**
     * 监听目录，发现服务
     *
     * @param zk Zookeeper实例
     */
    private void watchNode(ZooKeeper zk) throws KeeperException, InterruptedException {
        List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, event -> {
            if (Watcher.Event.EventType.NodeChildrenChanged == event.getType()) {
                try {
                    watchNode(zk);
                } catch (KeeperException | InterruptedException e) {
                    LOG.error("监听服务目录出现异常:", e);
                }
            }
        });

        // 获取节点数据
        nodeList.forEach(node -> {
            try {
                byte[] znodeData = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                this.serverSet.add(new String(znodeData, 0, znodeData.length));
            } catch (KeeperException | InterruptedException e) {
                LOG.error("获取服务节点数据失败:", e);
            }
        });
    }

}
