package com.autoConfig.client.dto;

import java.io.Serializable;

/**
 * 配置信息DTO
 * @author hejian
 * {"groupId":"testConfig","key":"param1","value":"哈哈"}
 */
public class CodeConfigDTO implements Serializable {
private static final long serialVersionUID = 1713397459678191437L;
	// groupid
	private String groupId;
	// 属性key
	private String key;
	// 默认值
	private String defauleValue;
	// 最新值
	private String value;
	// 执行IP
	private String ip;
	// 是否延迟执行
	private boolean delay;
	
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDefauleValue() {
		return defauleValue;
	}

	public void setDefauleValue(String defauleValue) {
		this.defauleValue = defauleValue;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    
    public boolean isDelay() {
        return delay;
    }

    
    public void setDelay(boolean delay) {
        this.delay = delay;
    }

    @Override
    public String toString() {
        return "CodeConfigDTO [groupId=" + groupId + ", key=" + key + ", defauleValue=" + defauleValue + ", value=" + value + ", ip=" + ip + ", delay=" + delay + "]";
    }
}
