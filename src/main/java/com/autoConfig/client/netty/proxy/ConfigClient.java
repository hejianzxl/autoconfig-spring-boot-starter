package com.autoConfig.client.netty.proxy;

/**
 * config操作client
 * @author hejian
 *
 */
public interface ConfigClient {
	
	/**
	 * 查询配置值
	 * @param groupId
	 * @param key
	 * @return
	 */
	public Object getConfig(String groupId, String key);
}
