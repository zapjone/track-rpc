package com.track.server.registry;

import com.track.common.util.Constant;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 使用zk来注册服务
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class ZkServiceRegistry implements ServiceRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(ZkServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    public ZkServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }


    @Override
    public void register(String data) {
        if (data != null) {
            try {
                ZooKeeper zk = connectZk();
                if (zk != null) {
                    createNode(zk, data);
                }
            } catch (IOException | InterruptedException | KeeperException e) {
                LOG.error("注册服务出现异常:", e);
            }
        }
    }

    /**
     * 连接zk
     */
    private ZooKeeper connectZk() throws IOException, InterruptedException {
        ZooKeeper zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (Event.KeeperState.SyncConnected == event.getState()) {
                    latch.countDown();
                }
            }
        });
        latch.await();

        return zk;
    }

    /**
     * 向zk注册节点
     */
    private void createNode(ZooKeeper zk, String data) throws KeeperException, InterruptedException {

        // 检查父目录并创建
        checkParent(zk, Constant.ZK_DATA_PATH);

        String path = zk.create(Constant.ZK_DATA_PATH, data.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        LOG.debug("created zookeeper node ({} => {})", path, data);
    }

    private void checkParent(ZooKeeper zk, String path) throws KeeperException, InterruptedException {
        if (null == zk.exists(path, false)) {
            int lastSep = path.lastIndexOf("/");
            if (lastSep < 1) {
                return;
            }
            String parentPath = path.substring(0, lastSep);

            checkParent(zk, parentPath);

            if (null == zk.exists(parentPath, false)) {
                zk.create(parentPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }
    }

}
