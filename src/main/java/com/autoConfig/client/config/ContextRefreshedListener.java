/*package com.autoConfig.client.config;

import java.lang.reflect.Field;
import java.util.Map;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ReflectionUtils;

public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {  
	  
    @Override  
    public void onApplicationEvent(ContextRefreshedEvent event) {  
        // 根容器为Spring容器  
        if(event.getApplicationContext().getParent()==null){  
            // 获取所有bean name
        	String[] beans = event.getApplicationContext().getBeanDefinitionNames();
        	for(String beanName : beans) {
        		Object targetBean = event.getApplicationContext().getBean(beanName);
        		Field[] fields = targetBean.getClass().getDeclaredFields();
        		if(null != fields && fields.length > 0) {
        			for(Field f : fields) {
        				ReflectionUtils.makeAccessible(f);
        				AutoConfig autoConfig = f.getAnnotation(AutoConfig.class);
        				if(null != autoConfig) {
        					System.out.println("bean name " + beanName + " class name " + targetBean.getClass() + " field name " + f.getName());
        				}
        			}
        		}
        	}
        }  
    }  
}
*/