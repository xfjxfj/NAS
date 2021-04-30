package com.djangoogle.framework.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ReflectUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
		Type superClass = getClass().getGenericSuperclass();
		if (null == superClass) {
			throw new RuntimeException("BaseActivity泛型反射失败");
		} else {
			Class<?> viewBindingClass = (Class<?>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
			mViewBinding = ReflectUtils.reflect(viewBindingClass).method("inflate", getLayoutInflater()).get();
			setContentView(mViewBinding.getRoot());
			initialize();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		BusUtils.register(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		BusUtils.unregister(this);
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

	@Override
	public void onBackPressed() {}
}
