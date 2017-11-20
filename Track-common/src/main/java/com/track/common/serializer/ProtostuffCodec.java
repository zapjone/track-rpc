package com.track.common.serializer;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * 使用Protostiff进行序列化和反序列化
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class ProtostuffCodec implements Codec {

    private static Objenesis objenesis = new ObjenesisStd(true);

    @Override
    public <T> byte[] serializer(T t) {
        @SuppressWarnings("unchecked")
        Schema<T> schema = getSchema((Class<T>) t.getClass());

        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return ProtostuffIOUtil.toByteArray(t, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserializer(byte[] data, Class<T> tClass) {

        T tObj = objenesis.newInstance(tClass);
        Schema<T> schema = getSchema(tClass);

        ProtostuffIOUtil.mergeFrom(data, tObj, schema);

        return tObj;
    }

    private <T> Schema<T> getSchema(Class<T> clazz) {
        return RuntimeSchema.getSchema(clazz);
    }

}
