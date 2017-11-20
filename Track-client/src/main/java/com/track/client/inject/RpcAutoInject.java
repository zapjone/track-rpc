package com.track.client.inject;

import com.track.client.annotation.RpcAutowired;
import com.track.client.proxy.RpcProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;

/**
 * 使用RpcAutowired自动注入
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class RpcAutoInject implements ApplicationContextAware, BeanPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RpcAutoInject.class);

    private ApplicationContext ctx;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);

            RpcAutowired rpcAutowired = field.getAnnotation(RpcAutowired.class);
            if (null == rpcAutowired) {
                continue;
            }

            Class<?> fieldType = field.getType();
            if (!fieldType.isInterface()) {
                LOG.error("RPC服务的属性不能接口类型:{}，自动注入失败", field.getName());
                continue;
            }

            RpcProxy rpcProxy = ctx.getBean(RpcProxy.class);
            Object proxyObj = rpcProxy.newProxy(fieldType);

            try {
                field.set(bean, proxyObj);
            } catch (IllegalAccessException e) {
                LOG.error("属性[{}]注入代理实例失败:", field.getName(), e);
            }

        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
