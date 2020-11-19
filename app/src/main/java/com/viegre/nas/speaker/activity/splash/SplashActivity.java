package com.viegre.nas.speaker.activity.splash;

import android.os.Bundle;

import com.viegre.nas.speaker.activity.base.BaseActivity;
import com.viegre.nas.speaker.databinding.ActivitySplashBinding;

import androidx.annotation.Nullable;

/**
 * Created by Djangoogle on 2020/11/19 10:26 with Android Studio.
 */
public class SplashActivity extends BaseActivity {

	private ActivitySplashBinding mSplashBinding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSplashBinding = ActivitySplashBinding.inflate(getLayoutInflater());
		setContentView(mSplashBinding.getRoot());
	}
}
