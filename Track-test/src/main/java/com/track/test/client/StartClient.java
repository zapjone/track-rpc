package com.track.test.client;

import com.track.client.proxy.RpcProxy;
import com.track.test.service.HelloService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class StartClient {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:client-spring.xml");
        RpcProxy proxy = (RpcProxy) ctx.getBean("rpcProxy");
        HelloService service = proxy.newProxy(HelloService.class);
        String result = service.sendStr("cell rpc");
        System.out.println(result);
    }

}
