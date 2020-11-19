package com.viegre.nas.speaker.application;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.viegre.nas.voice_wake_up.util.WakeRecognition;

/**
 * Created by Djangoogle on 2020/09/10 10:21 with Android Studio.
 */
public class NasApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.init(this);
		WakeRecognition.INSTANCE.initialize(this);
	}
}
