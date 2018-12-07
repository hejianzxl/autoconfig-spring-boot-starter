package com.autoConfig.client.netty;

import com.autoConfig.client.netty.protocol.ConfigDecoder;
import com.autoConfig.client.netty.protocol.ConfigEncoder;
import com.autoConfig.client.netty.protocol.ConfigRequest;
import com.autoConfig.client.netty.protocol.ConfigResponse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyService {

	public void start() throws InterruptedException {
		ServerBootstrap b = new ServerBootstrap();
		// 通过nio方式来接收连接和处理连接
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			b.group(group);
			// 设置nio类型的channel
			b.channel(NioServerSocketChannel.class);
			// b.localAddress(new InetSocketAddress("127.0.0.1",9999));// 设置监听端口
			b.childHandler(new ChannelInitializer<SocketChannel>() {// 有连接到达时会创建一个channel
				protected void initChannel(SocketChannel ch) throws Exception {
					// pipeline管理channel中的Handler，在channel队列中添加一个handler来处理业务
					// 字符串解码和编码
					//ch.pipeline().addLast("decoder", new StringDecoder());
					//ch.pipeline().addLast("encoder", new StringEncoder());
					ch.pipeline().addLast(new ConfigEncoder(ConfigResponse.class));
					ch.pipeline().addLast(new ConfigDecoder(ConfigRequest.class));
					// 服务器的逻辑
					ch.pipeline().addLast("myHandler", new ServerHandler());
				}
			});
			ChannelFuture f = b.bind(9999).sync();// 配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
			System.out.println(NettyService.class.getName() + " started and listen on " + f.channel().localAddress());
			f.channel().closeFuture().sync();// 应用程序会一直等待，直到channel关闭
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully().sync();// 关闭EventLoopGroup，释放掉所有资源包括创建的线程
		}
	}

	public static void main(String[] args) {
		try {
			new NettyService().start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
