package com.autoConfig.client.name.factory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * name 工厂
 * @author hejian
 *
 */
public class NameFactory {
	private AtomicInteger	atomicInteger	= new AtomicInteger();

	private static String	DEFAULT			= "_default";

	private NameFactory() {
		// ignore
	}

	public static NameFactory init() {
		return ChannelHandler.INSTANCE;
	}

	private static class ChannelHandler {
		private static final NameFactory INSTANCE = new NameFactory();
	}
	public String create() {
		return UUID.randomUUID() + DEFAULT + atomicInteger.getAndIncrement();
	}
}	
