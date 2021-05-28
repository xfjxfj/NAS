package com.viegre.nas.pad.config;

import android.content.Context;

import com.yanzhenjie.andserver.annotation.Config;
import com.yanzhenjie.andserver.framework.config.WebConfig;

/**
 * Created by レインマン on 2021/05/28 11:11 with Android Studio.
 */
@Config
public class AndServerConfig implements WebConfig {

	@Override
	public void onConfig(Context context, Delegate delegate) {
//		delegate.addWebsite(new FileBrowser("/storage/52207D41207D2CDB/nas/"));
	}
}
