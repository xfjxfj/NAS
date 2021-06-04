package com.viegre.nas.pad.config;

import android.content.Context;

import com.blankj.utilcode.util.FileUtils;
import com.yanzhenjie.andserver.annotation.Config;
import com.yanzhenjie.andserver.framework.config.Multipart;
import com.yanzhenjie.andserver.framework.config.WebConfig;

/**
 * Created by レインマン on 2021/05/28 11:11 with Android Studio.
 */
@Config
public class AndServerConfig implements WebConfig {

	@Override
	public void onConfig(Context context, Delegate delegate) {
//		delegate.addWebsite(new FileBrowser(PathConfig.NAS));
		delegate.setMultipart(Multipart.newBuilder()
		                               .allFileMaxSize(-1L)
		                               .fileMaxSize(-1L)
		                               .maxInMemorySize(1024 * 10)
		                               .uploadTempDir(FileUtils.getFileByPath(PathConfig.UPLOAD_CACHE))
		                               .build());
	}
}