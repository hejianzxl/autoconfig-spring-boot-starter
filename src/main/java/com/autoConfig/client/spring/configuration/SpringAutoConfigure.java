package com.autoConfig.client.spring.configuration;

import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import com.autoConfig.client.spring.configuration.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.autoConfig.client.config.ConfigAnnotationBeanPostProcessor;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

@Configuration//开启配置
@EnableConfigurationProperties(RedisProperties.class)//开启使用映射实体对象
public class SpringAutoConfigure {

    @Autowired
   private com.autoConfig.client.spring.configuration.RedisProperties redisProperties;
    
    /*@Bean
    public JedisCluster create() {
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(100);
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxWaitMillis(3000);
        //jedisClusterNodes.add(new HostAndPort("192.168.10.128", 7001));// 集群ip自动探测
        jedisClusterNodes.add(new HostAndPort(redisProperties.getHost(), redisProperties.getPort()));
        JedisCluster jedisCluster = new JedisCluster(jedisClusterNodes, jedisPoolConfig);
        return jedisCluster;
    }*/
    
    @Bean
    public Jedis createJedis() {
        Jedis jedis = new Jedis(redisProperties.getHost(), redisProperties.getPort());
        return jedis;
    }
    
    @Bean//创建实体bean
    public BeanPostProcessor configAnnotationBeanPostProcessor() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        ConfigAnnotationBeanPostProcessor configAnnotationBeanPostProcessor = new ConfigAnnotationBeanPostProcessor(createJedis(),"test");
        System.out.println(">>>>>>>>>>>>>>>>>>>>> init over");
        return configAnnotationBeanPostProcessor;
    }
}
