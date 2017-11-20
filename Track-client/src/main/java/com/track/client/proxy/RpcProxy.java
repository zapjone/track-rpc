package com.track.client.proxy;

import com.track.client.RpcClient;
import com.track.common.trans.RpcRequest;
import com.track.common.trans.RpcResponse;
import com.track.client.discover.ServiceDiscovery;

import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * Rpc客户端代理
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class RpcProxy {

    private ServiceDiscovery serviceDiscovery;
    private String serverAddr;

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T newProxy(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass},
                (proxy, method, args) -> {
                    RpcRequest request = new RpcRequest();
                    request.setId(UUID.randomUUID().toString());
                    request.setInterfaceName(interfaceClass.getName());
                    request.setMethodName(method.getName());
                    request.setParamTypes(method.getParameterTypes());
                    request.setParamVals(args);

                    // 发现服务
                    if (null != serviceDiscovery) {
                        serverAddr = serviceDiscovery.discover();
                    }

                    String[] hostAndPort = serverAddr.split(":");
                    String host = hostAndPort[0];
                    int port = Integer.parseInt(hostAndPort[1]);

                    RpcClient client = new RpcClient(host, port);
                    RpcResponse response = client.send(request);

                    // 检测是否有异常
                    if (response.isError()) {
                        throw response.getError();
                    } else {
                        return response.getResult();
                    }
                });
    }

}
