package com.track.test.client;

import com.track.client.annotation.RpcAutowired;
import com.track.client.proxy.RpcProxy;
import com.track.test.service.HelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author zap
 * @version 1.0, 2017/11/20
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class RpcClientTest {

    @Autowired
    private RpcProxy proxy;

    @RpcAutowired
    private HelloService helloService;

    /**
     * 测试RPC远程调用,0.1版本
     */
    @Test
    public void testRpcCall() {
        HelloService helloService = proxy.newProxy(HelloService.class);
        String rpcResult = helloService.sendStr("hello rpc");
        System.out.println(rpcResult);
    }

    /**
     * 使用注解自动注入
     */
    @Test
    public void testRpcCall2() {
        String result = helloService.sendStr("call rpc2");
        System.out.println("sencod result: " + result);
    }

}
