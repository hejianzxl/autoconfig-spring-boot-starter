package com.autoConfig.client.thread;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import com.autoConfig.client.config.AutoConfig;
import com.autoConfig.client.config.ConfigAnnotationBeanPostProcessor;
import com.autoConfig.client.dto.CodeConfigDTO;
import com.autoConfig.client.propertyeditor.PropertyEditor;
import com.autoConfig.client.utils.AopTargetUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.JedisPubSub;

/**
 * Notifiy
 * 动态更新Annotation
 * @author hejian
 *
 */
public class Notifiy extends JedisPubSub {

	private Logger logger = LoggerFactory.getLogger(Notifiy.class);
	private ApplicationContext applicationContext;

	public Notifiy(ApplicationContext applicationContext) {
		super();
		this.applicationContext = applicationContext;
	}

	@Override
	public void onMessage(String channel, String message) {
		// 解析参数message
		try {
			CodeConfigDTO codeConfigDTO = new ObjectMapper().readValue(message, CodeConfigDTO.class);
			//动态注入属性
			Set<String> names = ConfigAnnotationBeanPostProcessor.GLOBALCACHE.getOrDefault(codeConfigDTO.getKey(), Collections.emptySet());
		    if(null == names) {
		    	logger.error("Notifiy not find groupId {} corresponding",codeConfigDTO.getGroupId());
		    	return;
		    }
		    
		    for(String name : names) {
		    	//动态获取spring容器bean
		    	if(applicationContext.isSingleton(name)) {
		    		//获取容器对象
		    		Object targetObject = applicationContext.getBean(name);
		    		Field[] fields = targetObject.getClass().getDeclaredFields();
		    		System.out.println(targetObject + "是否为代理对象 ：" + AopUtils.isAopProxy(targetObject));
		    		if(AopUtils.isAopProxy(targetObject)) {
		    			fields = AopUtils.getTargetClass(targetObject).getDeclaredFields();
		    			targetObject = AopTargetUtils.getTarget(targetObject);
		    		}
		    		
		    		for(Field field : fields) {
		    			AutoConfig targetCode = field.getDeclaredAnnotation(AutoConfig.class);
		    			if(null != field.getDeclaredAnnotation(AutoConfig.class)) {
		    				try {
		    					if(codeConfigDTO.getKey().equalsIgnoreCase(targetCode.key())) {
		    						//重新赋予实例
			    					ReflectionUtils.makeAccessible(field);
									field.set(targetObject, PropertyEditor.primitiveTypeConvert(field.getType(), codeConfigDTO.getValue()));
		    					}
		    				} catch (IllegalArgumentException e) {
		    				    logger.warn("field set is fail ", e);
							} catch (IllegalAccessException e) {
							    logger.warn("field set is fail ", e);
							}
		    			}
		    		}
		    	}
		    }
		} catch (Exception e) {
			logger.error("Notifiy is error .",e.getMessage());
		}
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {

	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {

	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {

	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {

	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {

	}

}
