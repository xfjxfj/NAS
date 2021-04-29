package com.djangoogle.framework.manager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by レインマン on 2021/04/28 11:18 with Android Studio.
 */
public enum OkHttpManager {

	INSTANCE;

	private OkHttpClient mOkHttpClient;

	public void initialize() {
		mOkHttpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
		                                          .readTimeout(15, TimeUnit.SECONDS)
		                                          .writeTimeout(15, TimeUnit.SECONDS)
		                                          .retryOnConnectionFailure(true)
		                                          .build();
	}

	public OkHttpClient getOkHttpClient() {
		return mOkHttpClient;
	}
}
