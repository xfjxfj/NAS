package com.viegre.nas.speaker.entity;

import java.io.Serializable;

/**
 * Created by Djangoogle on 2021/01/04 17:14 with Android Studio.
 */
public class HttpEntity<T> implements Serializable {

	private int code;
	private String data;
	private String msg;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
