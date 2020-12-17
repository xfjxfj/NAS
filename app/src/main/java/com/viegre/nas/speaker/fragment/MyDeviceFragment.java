package com.viegre.nas.speaker.fragment;

import android.os.Bundle;

import com.viegre.nas.speaker.databinding.FragmentMyDeviceBinding;
import com.viegre.nas.speaker.fragment.base.BaseFragment;

/**
 * 我的设备
 * Created by Djangoogle on 2020/12/17 11:21 with Android Studio.
 */
public class MyDeviceFragment extends BaseFragment<FragmentMyDeviceBinding> {

	public static final String IS_LOGIN = "isLogin";

	@Override
	protected void initView() {

	}

	@Override
	protected void initData() {

	}

	public static MyDeviceFragment newInstance(boolean isLogin) {
		MyDeviceFragment myDeviceFragment = new MyDeviceFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(IS_LOGIN, isLogin);
		myDeviceFragment.setArguments(bundle);
		return myDeviceFragment;
	}
}
