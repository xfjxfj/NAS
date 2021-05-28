package com.viegre.nas.pad.manager;

import com.blankj.utilcode.util.Utils;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.util.concurrent.TimeUnit;

/**
 * Created by レインマン on 2021/05/28 10:55 with Android Studio.
 */
public enum ServerManager {

	INSTANCE;

	private Server mServer;

	public void initialize() {
		mServer = AndServer.webServer(Utils.getApp()).port(8080).timeout(15, TimeUnit.SECONDS).build();
	}

	public void startServer() {
		if (null != mServer && !mServer.isRunning()) {
			mServer.startup();
		}
	}

	public void stopServer() {
		if (null != mServer && mServer.isRunning()) {
			mServer.shutdown();
		}
	}
}
