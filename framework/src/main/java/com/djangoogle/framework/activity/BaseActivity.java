package com.djangoogle.framework.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.dylanc.viewbinding.base.ViewBindingUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

/**
 * Created by レインマン on 2020/11/26 14:19 with Android Studio.
 */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

	protected VB mViewBinding;
	protected Activity mActivity;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
		mViewBinding = ViewBindingUtil.inflateWithGeneric(this, getLayoutInflater());
		setContentView(mViewBinding.getRoot());
		initialize();
	}

	@Override
	protected void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	protected abstract void initialize();

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onBaseActivityEvent(String event) {}

	@Override
	public void onBackPressed() {}
}
