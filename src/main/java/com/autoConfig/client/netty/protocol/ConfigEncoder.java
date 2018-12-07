package com.autoConfig.client.netty.protocol;

import com.autoConfig.client.utils.SerializeUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器
 * 
 * @author hejian
 *
 */
public class ConfigEncoder extends MessageToByteEncoder<Object> {

	// 目标对象
	private Class<?> genericClass;

	public ConfigEncoder(Class<?> genericClass) {
			this.genericClass = genericClass;
		}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object object, ByteBuf out) throws Exception {
		if(genericClass.isInstance(object)) {
			byte[] body = SerializeUtils.serialize(object);
			out.writeInt(body.length); // 先将消息长度写入，也就是消息头
			out.writeBytes(body); // 消息体中包含我们要发送的数据
		}
	}

}
