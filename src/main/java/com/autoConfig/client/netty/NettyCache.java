package com.autoConfig.client.netty;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

/**
 * 通道缓存
 * @author hejian
 *
 */
public interface NettyCache {
	public static final ConcurrentHashMap<Integer, Channel> SERVER_CHANNEL = new ConcurrentHashMap<>(16);
}
