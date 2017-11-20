package com.track.common.codec;

import com.track.common.serializer.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Rpc字节到消息的解码器
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> clazz;
    private Codec codec;

    public RpcDecoder(Class<?> clazz, Codec codec) {
        this.clazz = clazz;
        this.codec = codec;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }

        in.markReaderIndex();
        int dataLen = in.readInt();
        if (dataLen < 0) {
            ctx.close();
        }

        if (in.readableBytes() < dataLen) {
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLen];
        in.readBytes(data);

        // 反序列化
        Object obj = codec.deserializer(data, clazz);
        out.add(obj);

    }
}
