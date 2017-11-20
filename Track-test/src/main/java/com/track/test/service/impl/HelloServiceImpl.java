package com.track.test.service.impl;

import com.track.server.annotation.RpcService;
import com.track.test.service.HelloService;

/**
 * Rpc服务
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sendStr(String str) {
        return "hello:" + str;
    }

}
