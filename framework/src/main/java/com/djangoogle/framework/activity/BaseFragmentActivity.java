package com.djangoogle.framework.activity;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ReflectUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by レインマン on 2020/11/26 14:19 with Android Studio.
 */
public abstract class BaseFragmentActivity<VB extends ViewBinding> extends FragmentActivity {

	protected VB mViewBinding;
	protected Activity mActivity;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		BarUtils.setNavBarVisibility(this, false);
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

	protected abstract void initialize();

	@Override
	public void onBackPressed() {}
}
