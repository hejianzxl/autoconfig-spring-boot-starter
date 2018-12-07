package com.autoConfig.client.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public class SerializeUtils {

	private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

	public static <T> byte[] serialize2(T t, Class<T> clazz) {
		return ProtobufIOUtil.toByteArray(t, RuntimeSchema.createFrom(clazz),
				LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
	}

	/**
	 * 序列化（对象 -> 字节数组）
	 */
	@SuppressWarnings("unchecked")
	public static <T> byte[] serialize(T obj) {
		Class<T> cls = (Class<T>) obj.getClass();
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try {
			Schema<T> schema = getSchema(cls);
			return ProtobufIOUtil.toByteArray(obj, schema, buffer);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			buffer.clear();
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Schema<T> getSchema(Class<T> cls) {
		Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
		if (schema == null) {
			schema = RuntimeSchema.createFrom(cls);
			if (schema != null) {
				cachedSchema.put(cls, schema);
			}
		}
		return schema;
	}

	public static <T> T deSerialize(byte[] data, Class<T> clazz) {
		RuntimeSchema<T> runtimeSchema = RuntimeSchema.createFrom(clazz);
		T t = runtimeSchema.newMessage();
		ProtobufIOUtil.mergeFrom(data, t, runtimeSchema);
		return t;
	}

}
