package com.autoConfig.client.config;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import com.autoConfig.client.commons.Constants;
import com.autoConfig.client.dto.CodeConfigDTO;
import com.autoConfig.client.name.factory.NameFactory;
import com.autoConfig.client.name.factory.ThreadNameFactory;
import com.autoConfig.client.propertyeditor.PropertyEditor;
import com.autoConfig.client.thread.NotifyThread;
import com.autoConfig.client.utils.IPHelp;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * 
 * bean初始化处理动态配置
 * 
 * @author hejian
 *
 */
public class ConfigAnnotationBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

	private JedisCluster jedisCluster;
	private Jedis jedis;

	private String appName;

	public static volatile ConcurrentMap<String, Set<String>> GLOBALCACHE = new ConcurrentHashMap<>();

	public static volatile ConcurrentMap<String, String> CACHE_LISTENTER = new ConcurrentHashMap<>();

	public static volatile ConcurrentMap<String, Method> METHOD_MAP = new ConcurrentHashMap<>();

	public static volatile ConcurrentMap<String, Object> METHOD_CACH = new ConcurrentHashMap<>();

	static final ExecutorService executor = Executors
			.newSingleThreadExecutor(ThreadNameFactory.createNameThreadFactory(Constants.DYNAMIC_CONFIG));

	private ApplicationContext applicationContext;

	public ConfigAnnotationBeanPostProcessor(JedisCluster jedisCluster) {
		this(jedisCluster, NameFactory.init().create());
	}

	public ConfigAnnotationBeanPostProcessor(Jedis jedis) {
		this(jedis, NameFactory.init().create());
	}

	public ConfigAnnotationBeanPostProcessor(JedisCluster jedisCluster, String appName) {
		this.jedisCluster = jedisCluster;
	}

	public ConfigAnnotationBeanPostProcessor(Jedis jedis, String appName) {
		this.jedis = jedis;
	}

	/**
	 * bean实例化后调用
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		try {
			// 初始化field
			this.initializeField(bean, beanName);
			this.methodHandler(bean, beanName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bean;
	}

	/**
	 * @param bean
	 * @param beanName
	 */
	private void methodHandler(Object bean, String beanName) {
		Method[] methods = bean.getClass().getDeclaredMethods();
		if (StringUtils.isEmpty(methods)) {
			return;
		}
		// Reflections reflections = new Reflections();
		for (Method method : methods) {
			Annotation[][] annotations = method.getParameterAnnotations();
			for (Annotation[] aArray : annotations) {
				for (Annotation a : aArray) {
					if (a instanceof AutoConfig) {
						AutoConfig codeConfig = (AutoConfig) a;
						if (null != codeConfig) {
							ReflectionUtils.makeAccessible(method);
							try {
								method.invoke(bean, jedis.get(codeConfig.key()));
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
							// 方法对象
							METHOD_MAP.putIfAbsent(codeConfig.groupId(), method);
						}
					}
				}
			}
		}

		if (!METHOD_MAP.isEmpty()) {
			METHOD_CACH.put(beanName, METHOD_MAP);
		}
	}

	private void initializeField(Object bean, String beanName) throws IllegalArgumentException, IllegalAccessException,
			JsonParseException, JsonMappingException, IOException {
		Field[] fields = bean.getClass().getDeclaredFields();
		if (StringUtils.isEmpty(fields)) {
			return;
		}

		for (Field field : fields) {
			AutoConfig codeConfig = field.getAnnotation(AutoConfig.class);

			if (null != codeConfig) {
				// REDIS初始化数据
				String groupId = codeConfig.groupId();
				String key = codeConfig.key();
				String value = jedis.get(key);
				if (!StringUtils.isEmpty(value) && !"null".equals(value)) {
					// 序列化对象
					CodeConfigDTO codeConfigDTO = new ObjectMapper().readValue(value, CodeConfigDTO.class);
					// IP过滤
					if (!StringUtils.isEmpty(codeConfigDTO.getIp()) && !codeConfigDTO.equals(IPHelp.getIp())) {
						return;
					}
					value = codeConfig.defaultValue();
				}

				ReflectionUtils.makeAccessible(field);
				try {
					field.set(bean, PropertyEditor.primitiveTypeConvert(field.getType(), value));
				} catch (Exception e) {
				}
				Set<String> beanNameSet = GLOBALCACHE.getOrDefault(key, Collections.emptySet());
				if (beanNameSet.isEmpty()) {
					beanNameSet = new HashSet<>();
				}

				beanNameSet.add(beanName);
				GLOBALCACHE.put(key, beanNameSet);
				// 注册监听
				if (!CACHE_LISTENTER.containsKey(groupId)) {
					executor.execute(new NotifyThread(groupId, jedis, applicationContext));
					CACHE_LISTENTER.put(groupId, "1");
				}
			}
		}
	}

	// 初始
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
