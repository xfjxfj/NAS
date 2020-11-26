package com.viegre.nas.speaker.activity.base;

import android.app.Activity;
import android.os.Bundle;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ReflectUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

/**
 * Created by Djangoogle on 2020/11/26 14:19 with Android Studio.
 */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

	protected VB mViewBinding;
	protected Activity mActivity;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
//		Type superclass = getClass().getGenericSuperclass();
//		Class<?> aClass = (Class<?>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
//		try {
//			Method method = aClass.getDeclaredMethod("inflate", LayoutInflater.class);
//			mViewBinding = (VB) method.invoke(null, getLayoutInflater());
//			setContentView(mViewBinding.getRoot());
//		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//			e.printStackTrace();
//		}

		mViewBinding = ReflectUtils.reflect(mViewBinding).newInstance().method("inflate", getLayoutInflater()).get();
		setContentView(mViewBinding.getRoot());
		initView();
		initData();
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

	protected abstract void initView();

	protected abstract void initData();
}
