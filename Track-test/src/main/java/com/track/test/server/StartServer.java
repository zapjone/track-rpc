package com.track.test.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class StartServer {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("classpath:server-spring.xml");
    }

}
