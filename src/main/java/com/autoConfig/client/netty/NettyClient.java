package com.autoConfig.client.netty;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.autoConfig.client.netty.protocol.ConfigDecoder;
import com.autoConfig.client.netty.protocol.ConfigEncoder;
import com.autoConfig.client.netty.protocol.ConfigRequest;
import com.autoConfig.client.netty.protocol.ConfigResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

/**
 * 链接配置服务端口
 * 
 * @author hejian
 *
 */
public class NettyClient implements ApplicationContextAware {
	// host地址
	private final String host;
	// 端口
	private final int port;
	// 全局netty channel状态
	public static volatile int stat = 0;
	private static final int DEALYTIME = 10;
	private final CountDownLatch countDownLatch = new CountDownLatch(1);
	public static final java.util.concurrent.ConcurrentHashMap<String, Channel> clientCache = new ConcurrentHashMap<>(16);
	private ApplicationContext applicationContext;
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	
	/**
	 * 初始化默认值
	 */
	public NettyClient() {
		this("127.0.0.1", 9999);
	}

	public NettyClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@PostConstruct
	public void start() {
		scheduledExecutorService.execute(()->{
			EventLoopGroup group = new NioEventLoopGroup();
			try {
				Bootstrap b = new Bootstrap();
				b.group(group).
				channel(NioSocketChannel.class)
				//.remoteAddress(new InetSocketAddress(host, port))
				.option(ChannelOption.SO_KEEPALIVE, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						 //字符串解码和编码
						//ch.pipeline().addLast("decoder", new StringDecoder());
						//ch.pipeline().addLast("encoder", new StringEncoder());
						ch.pipeline().addLast(new ConfigEncoder(ConfigRequest.class));
						ch.pipeline().addLast(new ConfigDecoder(ConfigResponse.class));
				        //客户端的逻辑
						ch.pipeline().addLast(new ClientHandler(applicationContext));
					}
				});
				ChannelFuture f = b.connect(host,port).sync();
				f.addListener(new ChannelFutureListener() {
					
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						 if(future.isSuccess()){  
							    stat = 1;
							    countDownLatch.countDown();
		                        System.out.println("==============client is connected");  
		                        clientCache.put("disConfig" + InetAddress.getLocalHost().getHostAddress(), f.channel());
		                        NettyCache.SERVER_CHANNEL.put(1, f.channel());
		                    }else{  
		                        System.out.println("============== server start failed");  
		                        future.cause().printStackTrace();  
		                    }  
					}
				});
				f.channel().write(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
				f.channel().closeFuture().sync();
				
				// 启动定时线程池处理重新链接通道
				 Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
					try {
						countDownLatch.await();
					} catch (InterruptedException e1) {
					}
					 if(stat == 0) {
						 try {
							this.start();
						 } catch (Exception e) {
							e.printStackTrace();
						 }
					 }
				 }, DEALYTIME, DEALYTIME, TimeUnit.SECONDS);
				
			}catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					group.shutdownGracefully().sync();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			Runtime.getRuntime().addShutdownHook(new Thread(()-> {
				try {
					group.shutdownGracefully().sync();
				} catch (InterruptedException e) {
				}
			}));
		});
	}

	public static void main(String[] args) {
		try {
			new NettyClient().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
