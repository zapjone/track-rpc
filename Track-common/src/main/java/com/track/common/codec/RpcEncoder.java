package com.track.common.codec;

import com.track.common.serializer.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 字节到消息的编码器
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Codec codec;
    private Class<?> clazz;

    public RpcEncoder(Class<?> clazz, Codec codec) {
        this.clazz = clazz;
        this.codec = codec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (clazz.isInstance(msg)) {
            byte[] data = codec.serializer(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
