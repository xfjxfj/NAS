package com.djangoogle.framework.applicaiton;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.djangoogle.framework.manager.OkHttpManager;

/**
 * Created by レインマン on 2021/01/18 14:30 with Android Studio.
 */
public class BaseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.init(this);
		OkHttpManager.INSTANCE.initialize();
	}
}
