package com.track.server;

import com.track.common.codec.RpcDecoder;
import com.track.common.codec.RpcEncoder;
import com.track.common.serializer.Codec;
import com.track.common.serializer.ProtostuffCodec;
import com.track.common.trans.RpcRequest;
import com.track.common.trans.RpcResponse;
import com.track.common.util.Addressing;
import com.track.server.annotation.RpcService;
import com.track.server.handler.RpcHandler;
import com.track.server.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * Rpc服务端
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(RpcServer.class);

    /**
     * 消息的序列化和反序列化器
     */
    private Codec codec = new ProtostuffCodec();
    private Map<String, Object> handlerMap = new HashMap<>();

    private String serverAddressPort;
    private ServiceRegistry serviceRegistry;

    public RpcServer(String serverAddressPort) {
        this.serverAddressPort = serverAddressPort;
    }

    public RpcServer(String serverAddressPort, ServiceRegistry serviceRegistry) {
        this.serverAddressPort = serverAddressPort;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Spring容器初始化后将所有使用了RpcService注解的类进行记录
     */
    @SuppressWarnings("all")
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serverMap = ctx.getBeansWithAnnotation(RpcService.class);

        if (MapUtils.isNotEmpty(serverMap)) {
            serverMap.values().forEach(serverBean -> {
                String interfaceName = serverBean.getClass().getAnnotation(
                        RpcService.class).value().getName();
                handlerMap.put(interfaceName, serverBean);
            });
        }

    }

    /**
     * Bean初始化完成后
     *
     * @throws Exception Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 启动Netty服务器
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LoggingHandler());
                            pipeline.addLast(new RpcDecoder(RpcRequest.class, codec));
                            pipeline.addLast(new RpcEncoder(RpcResponse.class, codec));
                            pipeline.addLast(new RpcHandler(handlerMap));
                        }
                    });

            // 得到服务器地址
            String host = Addressing.getIp4Address().getHostAddress();
            int port = Integer.parseInt(serverAddressPort);

            // 绑定端口启动
            ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
            LOG.debug("server started on port {}", port);

            // 注册服务
            if (null != serviceRegistry) {
                serviceRegistry.register(host + ":" + port);
            }

            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

}
