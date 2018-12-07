package com.autoConfig.client.netty.protocol;

import java.io.Serializable;
import java.util.Arrays;

public class ConfigRequest implements Serializable {
	/**
	 * @author hejian 
	 * Date:2018年12月6日上午9:55:15 
	 */
	private static final long serialVersionUID = -4544314337827373750L;
	// 请求ID
	private Long invoikeId;
	// 标识请求类型  默认 0普通json,1 RPC请求 
	private int reqType;
	// 方法名称
	private String method;
	// 接口名称
	private String interfaceName;
	// 参数类型
	private Class<?>[] parameterTypes;
	// 参数
	private Object[] parameters;
	
	private String body;

	public Long getInvoikeId() {
		return invoikeId;
	}

	public void setInvoikeId(Long invoikeId) {
		this.invoikeId = invoikeId;
	}

	public int getReqType() {
		return reqType;
	}

	public void setReqType(int reqType) {
		this.reqType = reqType;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "ConfigRequest [invoikeId=" + invoikeId + ", reqType=" + reqType + ", method=" + method
				+ ", interfaceName=" + interfaceName + ", parameterTypes=" + Arrays.toString(parameterTypes)
				+ ", parameters=" + Arrays.toString(parameters) + ", body=" + body + "]";
	}
}
