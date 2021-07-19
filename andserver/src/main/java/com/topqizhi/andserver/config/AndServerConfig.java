package com.topqizhi.andserver.config;

import android.content.Context;

import com.yanzhenjie.andserver.annotation.Config;
import com.yanzhenjie.andserver.framework.config.Multipart;
import com.yanzhenjie.andserver.framework.config.WebConfig;

import java.io.File;

/**
 * Created by レインマン on 2021/07/16 14:41 with Android Studio.
 */
@Config
public class AndServerConfig implements WebConfig {
	@Override
	public void onConfig(Context context, Delegate delegate) {
		// 自定义配置表单请求和文件上传的条件
		delegate.setMultipart(Multipart.newBuilder().allFileMaxSize(1024 * 1024 * 20L)//单个请求上传文件总大小
		                               .fileMaxSize(1024 * 1024 * 5L)//单个文件的最大大小
		                               .maxInMemorySize(1024 * 10)//保存上传文件时buffer大小
		                               .uploadTempDir(new File("/storage/emulated/0/nas/.uploadCache/"))//文件保存目录
		                               .build());
	}
}
