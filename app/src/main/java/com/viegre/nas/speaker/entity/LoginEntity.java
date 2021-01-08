package com.viegre.nas.speaker.entity;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/01/05 18:14 with Android Studio.
 */
public class LoginEntity implements Serializable {

	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
