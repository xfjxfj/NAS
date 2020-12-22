package com.viegre.nas.speaker.fragment.settings;

import android.os.Bundle;

import com.blankj.utilcode.constant.MemoryConstants;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.SDCardUtils;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.databinding.FragmentMyDeviceBinding;
import com.viegre.nas.speaker.fragment.base.BaseFragment;

import androidx.core.content.ContextCompat;

/**
 * 我的设备
 * Created by Djangoogle on 2020/12/17 11:21 with Android Studio.
 */
public class MyDeviceFragment extends BaseFragment<FragmentMyDeviceBinding> {

	public static final String IS_LOGIN = "isLogin";

	@Override
	protected void initView() {
		initCurrentlyConnectedDevice();
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

	private void initCurrentlyConnectedDevice() {
		long totalSizeL = SDCardUtils.getInternalTotalSize();
		long availableSizeL = SDCardUtils.getInternalAvailableSize();
		long usedSizeL = totalSizeL - availableSizeL;
		String totalSizeStr = ConvertUtils.byte2FitMemorySize(totalSizeL, 2);
		String usedSizeS = ConvertUtils.byte2FitMemorySize(usedSizeL, 2);
		double totalSizeD = ConvertUtils.byte2MemorySize(totalSizeL, MemoryConstants.MB);
		double availableSizeD = ConvertUtils.byte2MemorySize(availableSizeL, MemoryConstants.MB);
		double usedSize = totalSizeD - availableSizeD;
		mViewBinding.actvMyDeviceCurrentlyUsed.setText(String.format(getResources().getString(R.string.my_device_currently_used),
		                                                             usedSizeS,
		                                                             totalSizeStr));
		mViewBinding.mpbMyDeviceProgress.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.my_device_progress_bar));
		mViewBinding.mpbMyDeviceProgress.setMax((int) totalSizeD);
		mViewBinding.mpbMyDeviceProgress.setProgress((int) usedSize);
	}
}
