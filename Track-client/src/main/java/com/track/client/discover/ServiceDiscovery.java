package com.track.client.discover;

/**
 * Rpc客户端服务发现
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public interface ServiceDiscovery {

    /**
     * 服务发现
     *
     * @return 服务器地址
     */
    String discover();

}
