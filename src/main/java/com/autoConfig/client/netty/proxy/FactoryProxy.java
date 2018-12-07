package com.autoConfig.client.netty.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.ClassUtils;
import com.autoConfig.client.netty.NettyCache;
import com.autoConfig.client.netty.NettyClient;
import com.autoConfig.client.netty.protocol.ConfigRequest;
import com.autoConfig.client.netty.protocol.ConfigResponse;
import io.netty.channel.Channel;

/**
 * 代理工厂
 * 
 * @author hejian
 *
 */
public class FactoryProxy implements Proxy<Object>, InvocationHandler {
	
	public static final Map<Long, Invoke> INVOKE_MAP = new ConcurrentHashMap<Long, Invoke>(32);
	// 代理目标接口
	private Class<?> targetInterface;
	
	public static final java.util.concurrent.atomic.AtomicLong atomicInteger = new AtomicLong(0);

	@Override
	public Object create(Class<?> targetClass) {
		this.targetInterface = targetClass;
		Class<?>[] proxiedInterfaces = { targetInterface };
		return java.lang.reflect.Proxy.newProxyInstance(org.springframework.util.ClassUtils.getDefaultClassLoader(),
				proxiedInterfaces, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		String name = method.getName();
		if(Object.class == method.getDeclaringClass()) {
			if ("equals".equals(name)) {
				return proxy == args[0];
			} else if ("hashCode".equals(name)) {
				return System.identityHashCode(proxy);
			} else if ("toString".equals(name)) {
				return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy))
						+ ", with InvocationHandler " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}
		// 远程调用请求
		// 获取client
		Channel channel = NettyCache.SERVER_CHANNEL.get(1);
		ConfigRequest configRequest = new ConfigRequest();
		configRequest.setInvoikeId(atomicInteger.getAndIncrement());
		configRequest.setInterfaceName(targetInterface.getName());
		configRequest.setMethod(name);
		configRequest.setParameters(args);
		channel.writeAndFlush(configRequest);
		Invoke invoke = new Invoke();
		INVOKE_MAP.put(configRequest.getInvoikeId(), invoke);
		return invoke.get();
	}
	
	
	/**
	 * 接收返回值
	 * @param channel
	 * @param response
	 */
	public static void received(ConfigResponse response) {
		if(null == response.getRequestId() || 0 == response.getRequestId()) {
			return;
		}
		System.out.println("received " + response.getRequestId());
		 // 根据invokeId 找到对应的invoke
		Invoke invoke = INVOKE_MAP.get(response.getRequestId());
		if(null != invoke) {
			invoke.doReceived(response);
		}
	}
}

/**
 * invoke内部类
 * 
 * @author hejian
 *
 */
class Invoke {
	private final Lock lock = new ReentrantLock();
	private final Condition done = lock.newCondition();
	private volatile ConfigResponse response;

	public Object get() {
		try {
		   if (!isDone()) {
			   long start = System.currentTimeMillis();
			   lock.lock();
				while(!isDone()) {
					  done.await(1, TimeUnit.MILLISECONDS);
					  //超时退出
					  long end = System.currentTimeMillis() - start;
					  if (isDone() || end > 5000) {
						  System.out.println("等待" + end/1000 +  "秒");
	                      return null;
	                  }
				}
		   }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return null;
	}

	public boolean isDone() {
		return response != null;
	}
	
	/**
	 * 唤醒等待线程，返回response
	 * @param res
	 */
	public void doReceived(ConfigResponse res) {
        lock.lock();
        try {
            response = res;
            if (done != null) {
                done.signal();
                // 此次请求结束
                FactoryProxy.INVOKE_MAP.remove(res.getRequestId());
                System.out.println("唤醒等待" + res.getRequestId());
            }
        } finally {
            lock.unlock();
        }
    }
}
