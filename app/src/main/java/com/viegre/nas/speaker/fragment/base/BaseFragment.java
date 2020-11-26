package com.viegre.nas.speaker.fragment.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ReflectUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

/**
 * Created by Djangoogle on 2020/11/26 14:22 with Android Studio.
 */
public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {

	protected VB mViewBinding;
	protected Activity mActivity;
	protected Context mContext;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//		Type superclass = getClass().getGenericSuperclass();
//		Class<?> aClass = (Class<?>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
//		try {
//			Method method = aClass.getDeclaredMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
//			mViewBinding = (VB) method.invoke(null, getLayoutInflater(), container, false);
//		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//			e.printStackTrace();
//		}

		mViewBinding = ReflectUtils.reflect(mViewBinding).newInstance().method("inflate", getLayoutInflater(), container, false).get();
		return mViewBinding.getRoot();
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
		initView();
		initData();
	}

	@Override
	public void onStart() {
		super.onStart();
		BusUtils.register(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		BusUtils.unregister(this);
	}

	protected abstract void initView();

	protected abstract void initData();
}
