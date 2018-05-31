package com.autoConfig.client.thread;

import org.springframework.context.ApplicationContext;
import redis.clients.jedis.JedisCluster;

/**
 * 通过Redis TCP通道
 * 订阅对应的配置信息
 * @author hejian
 *
 */
public class NotifyThread extends BaseThread {
	
	private String groupId;
	
	private JedisCluster jedisCluster;
	
	private ApplicationContext applicationContext;

	public NotifyThread(String groupId, JedisCluster jedisCluster, ApplicationContext applicationContext) {
		this.groupId = groupId;
		this.jedisCluster = jedisCluster;
		this.applicationContext = applicationContext;
	}

	@Override
	public void execute() {
		// 订阅指定的 SUBSCRIBE channel   TODO
		jedisCluster.subscribe(new Notifiy(applicationContext), groupId);
	}
}
