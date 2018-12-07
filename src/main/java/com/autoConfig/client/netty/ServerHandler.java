package com.autoConfig.client.netty;

import com.autoConfig.client.netty.protocol.ConfigRequest;
import com.autoConfig.client.netty.protocol.ConfigResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@SuppressWarnings("rawtypes")
public class ServerHandler extends SimpleChannelInboundHandler {
	
	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("RemoteAddress : " + ctx.channel().remoteAddress() + " active !");
        //ctx.writeAndFlush("服务端：连接成功！"); 
    }
	
	/**
	 * 异常处理
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();// 捕捉异常信息
		ctx.close();// 出现异常时关闭channel
		System.out.println(">>>>>>>>>>>>>>>ServerHandler error");
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		ConfigRequest request = (ConfigRequest) msg;
		//System.out.println(">>>>>>>>>>>>>>>server received data :" + request);
		//ctx.writeAndFlush("{\"defauleValue\":\"test\",\"delay\":false,\"groupId\":\"testConfig\",\"key\":\"param1\",\"value\":\"netty_test\"}");
		ConfigResponse response = new ConfigResponse();
		response.setRequestId(request.getInvoikeId());
		response.setResult("server : is ok");
		ctx.writeAndFlush(response);
	}
}
