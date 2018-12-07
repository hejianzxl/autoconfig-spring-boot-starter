package com.autoConfig.client.netty.proxy;


/**
 * 代理接口
 * @author hejian
 * @param <T>
 *
 */
public interface Proxy<T> {
	public T create(Class<?> interfaceClass);
}
