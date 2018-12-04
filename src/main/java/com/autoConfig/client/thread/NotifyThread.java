package com.autoConfig.client.thread;

import org.springframework.context.ApplicationContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * 通过Redis TCP通道
 * 订阅对应的配置信息
 * @author hejian
 *
 */
public class NotifyThread extends BaseThread {
	
	private String groupId;
	
	private ApplicationContext applicationContext;

	public NotifyThread(String groupId, Jedis jedis, ApplicationContext applicationContext) {
		this.groupId = groupId;
		this.applicationContext = applicationContext;
	}

	@Override
	public void execute() {
		// 订阅指定的 SUBSCRIBE channel   TODO
		Jedis jedis = new Jedis("127.0.0.1", 6379);
		jedis.subscribe(new Notifiy(applicationContext), groupId);
	}
}
