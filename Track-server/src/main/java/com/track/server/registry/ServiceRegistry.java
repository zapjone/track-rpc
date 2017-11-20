package com.track.server.registry;

/**
 * @author zap
 * @version 1.0, 2017/11/20
 */
public interface ServiceRegistry {

    /**
     * 服务器注册
     *
     * @param data 注册数据
     */
    void register(String data);

}
