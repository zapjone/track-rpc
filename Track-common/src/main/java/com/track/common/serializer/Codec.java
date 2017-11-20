package com.track.common.serializer;

/**
 * 序列化和反序列化
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public interface Codec {

    /**
     * 序列化
     *
     * @param t 对象
     * @return 序列化后的字节数组
     */
    <T> byte[] serializer(T t);

    /**
     * 反序列化
     *
     * @param data   字节数组
     * @param tClass 反序列化的类型
     * @return 对象实例
     */
    <T> T deserializer(byte[] data, Class<T> tClass);

}
