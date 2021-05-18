package com.djangoogle.framework.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.ReflectUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by レインマン on 2020/11/26 14:22 with Android Studio.
 */
public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {

	protected VB mViewBinding;
	protected Activity mActivity;
	protected Context mContext;

	@Nullable
	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Type superClass = getClass().getGenericSuperclass();
		if (null == superClass) {
			throw new RuntimeException("BaseFragment泛型反射失败");
		} else {
			Class<?> viewBindingClass = (Class<?>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
			mViewBinding = ReflectUtils.reflect(viewBindingClass).method("inflate", getLayoutInflater(), container, false).get();
			return mViewBinding.getRoot();
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mContext = getContext();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = getActivity();
		initialize();
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EventBus.getDefault().unregister(this);
	}

	protected abstract void initialize();

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onBaseFragmentEvent(String event) {}
}
