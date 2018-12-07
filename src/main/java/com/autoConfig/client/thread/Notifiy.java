package com.autoConfig.client.thread;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
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
import com.autoConfig.client.utils.IpHelp;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.JedisPubSub;

/**
 * Notifiy
 * 动态更新Annotation
 * 
 * @author hejian
 */
public class Notifiy extends JedisPubSub {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static final int               DEALYTIME = 10;
    private static final Logger            logger    = LoggerFactory.getLogger(Notifiy.class);
    private ApplicationContext             applicationContext;

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
            if (null == names) {
                logger.error("Notifiy not find groupId {} corresponding", codeConfigDTO.getGroupId());
                return;
            }

            // IP过滤
            if (StringUtils.isNotEmpty(codeConfigDTO.getIp()) && !codeConfigDTO.equals(IpHelp.getIp())) {
                return;
            }
            // 延迟
            if (codeConfigDTO.isDelay()) {
                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            invoke(names, codeConfigDTO);
                        } catch (Exception e) {
                           //ignore
                        }
                    }
                }, DEALYTIME, DEALYTIME, TimeUnit.SECONDS);
            } else {
                this.invoke(names, codeConfigDTO);
            }

        } catch (Exception e) {
            logger.error("Notifiy is error .", e.getMessage());
        }
    }

    private void invoke(Set<String> names, CodeConfigDTO codeConfigDTO) throws Exception {
        for (String name : names) {
            //动态获取spring容器bean
            if (applicationContext.isSingleton(name)) {
                //获取容器对象
                Object targetObject = applicationContext.getBean(name);
                Field[] fields = targetObject.getClass().getDeclaredFields();
                System.out.println(targetObject + "是否为代理对象 ：" + AopUtils.isAopProxy(targetObject));
                if (AopUtils.isAopProxy(targetObject)) {
                    fields = AopUtils.getTargetClass(targetObject).getDeclaredFields();
                    targetObject = AopTargetUtils.getTarget(targetObject);
                }

                for (Field field : fields) {
                    AutoConfig targetCode = field.getDeclaredAnnotation(AutoConfig.class);
                    if (null != field.getDeclaredAnnotation(AutoConfig.class)) {
                        try {
                            if (codeConfigDTO.getKey().equalsIgnoreCase(targetCode.key())) {
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
