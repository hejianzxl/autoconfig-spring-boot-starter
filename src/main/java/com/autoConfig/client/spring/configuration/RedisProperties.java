package com.autoConfig.client.spring.configuration;

import javax.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * redis 配置文件加载
 * @author hejian
 *
 */
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {
	// redis host name
	private String host;
	// redis connection port
	private int port;
	private String disConfigHost;
	private int disConfigPort;

	@PostConstruct
	public void init() {
		System.out.println("RedisProperties init start!");
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDisConfigHost() {
		return disConfigHost;
	}

	public void setDisConfigHost(String disConfigHost) {
		this.disConfigHost = disConfigHost;
	}

	public int getDisConfigPort() {
		return disConfigPort;
	}

	public void setDisConfigPort(int disConfigPort) {
		this.disConfigPort = disConfigPort;
	}

}
