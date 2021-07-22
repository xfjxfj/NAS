package com.viegre.nas.pad.manager;

import android.app.Application;
import com.viegre.nas.pad.BuildConfig;
import com.viegre.nas.pad.interceptor.TokenInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import rxhttp.RxHttpPlugins;

public class RxHttpManager {
	public static void init(Application context) {
		OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
		                                                .readTimeout(15, TimeUnit.SECONDS)
		                                                .writeTimeout(15, TimeUnit.SECONDS)
		                                                .retryOnConnectionFailure(true)
		                                                .addInterceptor(new TokenInterceptor())
		                                                .build();
		RxHttpPlugins.init(client).setDebug(BuildConfig.DEBUG);
	}
}
