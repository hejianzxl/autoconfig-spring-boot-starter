package com.autoConfig.client.netty.protocol;

import java.io.Serializable;

public class ConfigResponse implements Serializable {
	/**
	 * @author hejian 
	 * Date:2018年12月6日上午9:56:11 
	 */
	private static final long serialVersionUID = -2820395142294668873L;

	private Long requestId;
	
	private String error;
	
	private Object result;

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "ConfigResponse [requestId=" + requestId + ", error=" + error + ", result=" + result + "]";
	}
}
