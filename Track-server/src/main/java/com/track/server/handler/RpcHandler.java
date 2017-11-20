package com.track.server.handler;

import com.track.common.trans.RpcRequest;
import com.track.common.trans.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * RPC服务器接收接受处理
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setId(request.getId());
        try {
            // 处理并设置返回结果
            Object result = handle(request);
            response.setResult(result);
        } catch (Throwable t) {
            response.setError(t);
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RpcRequest request) throws InvocationTargetException {
        String interfaceName = request.getInterfaceName();
        String methodName = request.getMethodName();
        Class<?>[] paramTypes = request.getParamTypes();
        Object[] args = request.getParamVals();

        // 获取服务接口实现类
        Object serviceBean = handlerMap.get(interfaceName);

        // 获取服务接口实现类型
        Class<?> serviceClass = serviceBean.getClass();

        // 获取方法
        FastMethod serviceMethod = FastClass.create(serviceClass).getMethod(methodName, paramTypes);

        // 执行方法
        return serviceMethod.invoke(serviceBean, args);
    }


}
