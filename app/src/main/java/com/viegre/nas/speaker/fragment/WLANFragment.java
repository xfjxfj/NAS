package com.viegre.nas.speaker.fragment;

import android.os.Bundle;

import com.viegre.nas.speaker.databinding.FragmentWlanBinding;
import com.viegre.nas.speaker.fragment.base.BaseFragment;

/**
 * 网络设置
 * Created by Djangoogle on 2020/11/26 11:35 with Android Studio.
 */
public class WLANFragment extends BaseFragment<FragmentWlanBinding> {

	public static final String IS_FIRST_RUN = "isFirstRun";

	@Override
	protected void initView() {

	}

	@Override
	protected void initData() {

	}

	public static WLANFragment newInstance(boolean isFirstRun) {
		WLANFragment wlanFragment = new WLANFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(IS_FIRST_RUN, isFirstRun);
		wlanFragment.setArguments(bundle);
		return wlanFragment;
	}
}
