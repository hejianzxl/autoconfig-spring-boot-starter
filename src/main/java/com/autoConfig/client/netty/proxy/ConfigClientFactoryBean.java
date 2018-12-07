package com.autoConfig.client.netty.proxy;

import org.springframework.beans.factory.FactoryBean;

/**
 * ConfigClient实例类
 * @author hejian
 *
 */
public class ConfigClientFactoryBean implements FactoryBean<ConfigClient> {
	
	private Class<?> targetClass;
	
	public ConfigClientFactoryBean() {
		targetClass = ConfigClient.class;
	}
	

	@Override
	public ConfigClient getObject() throws Exception {
		return (ConfigClient) new FactoryProxy().create(targetClass);
	}

	@Override
	public Class<?> getObjectType() {
		return targetClass;
	}

	@Override
	public boolean isSingleton() {
		return Boolean.TRUE;
	}


}
