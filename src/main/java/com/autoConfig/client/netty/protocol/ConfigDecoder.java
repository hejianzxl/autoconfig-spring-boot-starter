package com.autoConfig.client.netty.protocol;

import java.util.List;

import com.autoConfig.client.utils.SerializeUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 解码器
 * 
 * @author hejian
 *
 */
public class ConfigDecoder extends ByteToMessageDecoder {

	// 目标class
	private Class<?> genericClass;

	public ConfigDecoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		if (in.readableBytes() < 4) {
			return;
		}
		// 标记一下当前的readIndex的位置
		in.markReaderIndex();
		// 初始化数组大小
		int dataLength = in.readInt();

		// 数据未0，关闭链接
		if (dataLength < 0) {
			ctx.close();
			return;
		}

		// 读到的消息体长度如果小于我们传送过来的消息长度,把readIndex重置到mark的地方
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}

		byte[] data = new byte[dataLength];

		in.readBytes(data);
		// 序列化data 转化对象
		Object result = SerializeUtils.deSerialize(data, genericClass);
		out.add(result);
	}

}
