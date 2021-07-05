package com.viegre.nas.pad.entity;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/01/05 18:14 with Android Studio.
 */
public class LoginUserPwdEntity implements Serializable {


	private int code;
	private String msg;
	private DataBeanX data;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public DataBeanX getData() {
		return data;
	}

	public void setData(DataBeanX data) {
		this.data = data;
	}
}
