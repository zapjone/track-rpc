package com.track.client;

import com.track.common.serializer.ProtostuffCodec;
import com.track.common.codec.RpcDecoder;
import com.track.common.codec.RpcEncoder;
import com.track.common.serializer.Codec;
import com.track.common.trans.RpcRequest;
import com.track.common.trans.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rpc客户端
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class RpcClient {

    private static final Logger LOG = LoggerFactory.getLogger(RpcClient.class);
    /**
     * 消息的序列化和反序列化器
     */
    private Codec codec = new ProtostuffCodec();
    private final Object LOCK = new Object();

    private RpcResponse response;

    private String host;
    private int port;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LoggingHandler());
                            pipeline.addLast(new RpcEncoder(RpcRequest.class, codec));
                            pipeline.addLast(new RpcDecoder(RpcResponse.class, codec));
                            pipeline.addLast(new RpcClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            // 将请求写出去
            LOG.debug("发起rpc调用...");
            channelFuture.channel().writeAndFlush(request).sync();

            synchronized (LOCK) {
                LOCK.wait();
            }

            if (null != response) {
                channelFuture.channel().closeFuture().sync();
            }

            return response;

        } finally {
            group.shutdownGracefully();
        }
    }

    private class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
            response = msg;

            synchronized (LOCK) {
                LOCK.notifyAll();
            }
        }
    }

}
