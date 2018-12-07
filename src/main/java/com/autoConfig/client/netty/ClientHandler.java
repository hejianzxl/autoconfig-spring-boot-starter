package com.autoConfig.client.netty;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import com.autoConfig.client.config.AutoConfig;
import com.autoConfig.client.config.ConfigAnnotationBeanPostProcessor;
import com.autoConfig.client.dto.CodeConfigDTO;
import com.autoConfig.client.netty.protocol.ConfigRequest;
import com.autoConfig.client.netty.protocol.ConfigResponse;
import com.autoConfig.client.netty.proxy.FactoryProxy;
import com.autoConfig.client.propertyeditor.PropertyEditor;
import com.autoConfig.client.utils.AopTargetUtils;
import com.autoConfig.client.utils.IpHelp;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 客户端业务处理Handler
 * 
 * @author hejian
 *
 */
public class ClientHandler extends SimpleChannelInboundHandler<Object> {

	private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	private static final int DEALYTIME = 10;
	private ApplicationContext applicationContext;
	
	public ClientHandler(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * 读取管道数据流
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("client channelRead0 " + msg);
		ConfigResponse response = (ConfigResponse) msg;
		FactoryProxy.received(response);
		/*String message = (String) msg;
		if(!message.startsWith("{")) {
			return;
		}
		scheduledExecutorService.execute(()->{
			this.handler(message);
		});*/
	}

	private void handler(String message) {
		// 解析参数message
		try {
			CodeConfigDTO codeConfigDTO = new ObjectMapper().readValue(message, CodeConfigDTO.class);
			// 动态注入属性
			Set<String> names = ConfigAnnotationBeanPostProcessor.GLOBALCACHE.getOrDefault(codeConfigDTO.getKey(),
					Collections.emptySet());
			if (null == names) {
				logger.error("Notifiy not find groupId {} corresponding", codeConfigDTO.getGroupId());
				return;
			}

			// IP过滤
			if (StringUtils.isNotEmpty(codeConfigDTO.getIp()) && !codeConfigDTO.equals(IpHelp.getIp())) {
				return;
			}
			
			this.invoke(names, codeConfigDTO);
		} catch (Exception e) {
			logger.error("Notifiy is error .", e.getMessage());
		}
	}

	/**
	 * 此方法会在连接到服务器后被调用
	 */
	public void channelActive(ChannelHandlerContext ctx) {

		try {
			System.out.println("===============链接服务端成功");
			ConfigRequest request = new ConfigRequest();
			request.setInvoikeId(System.currentTimeMillis());
			request.setBody("客户端：我是客户端 " + request.getInvoikeId());
			ctx.writeAndFlush(request);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 捕捉到异常
	 */
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		System.out.println("===============发生异常，关闭channel" + cause.getMessage());
		ctx.close(); //异常关闭链接通道
		// 设计全局通道链接状态，当通道断开后，重新建立TCP通道
		NettyClient.stat = 0;
		try {
			NettyClient.clientCache.remove("disConfig" + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void invoke(Set<String> names, CodeConfigDTO codeConfigDTO) throws Exception {
		for (String name : names) {
			// 动态获取spring容器bean
			if (applicationContext.isSingleton(name)) {
				// 获取容器对象
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
								// 重新赋予实例
								ReflectionUtils.makeAccessible(field);
								field.set(targetObject,PropertyEditor.primitiveTypeConvert(field.getType(), codeConfigDTO.getValue()));
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
}
