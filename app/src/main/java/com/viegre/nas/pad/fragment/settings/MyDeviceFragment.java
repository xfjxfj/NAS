package com.viegre.nas.pad.fragment.settings;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.constant.MemoryConstants;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.FragmentMyDeviceBinding;
import com.viegre.nas.pad.util.CommonUtils;

import androidx.core.content.ContextCompat;

/**
 * 我的设备
 * Created by レインマン on 2020/12/17 11:21 with Android Studio.
 */
public class MyDeviceFragment extends BaseFragment<FragmentMyDeviceBinding> {

	private ClipboardManager mClipboardManager;

	@Override
	protected void initialize() {
		mClipboardManager = (ClipboardManager) Utils.getApp().getSystemService(Context.CLIPBOARD_SERVICE);
	}

	public static MyDeviceFragment newInstance() {
		return new MyDeviceFragment();
	}

	@Override
	public void onResume() {
		super.onResume();
		initCurrentlyConnectedDevice();
		if (SPUtils.getInstance().contains(SPConfig.PHONE)) {
			afterLogin();
		} else {
			mViewBinding.llcMyDeviceLogin.setVisibility(View.GONE);
		}
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
		mViewBinding.actvMyDeviceCurrentlyUsed.setText(StringUtils.getString(R.string.my_device_currently_used, usedSizeS, totalSizeStr));
		mViewBinding.mpbMyDeviceProgress.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.my_device_progress_bar));
		mViewBinding.mpbMyDeviceProgress.setMax((int) totalSizeD);
		mViewBinding.mpbMyDeviceProgress.setProgress((int) usedSize);
	}

	private void afterLogin() {
		mViewBinding.llcMyDeviceLogin.setVisibility(View.VISIBLE);
		//固件升级暂未开发
		mViewBinding.llcMyDeviceFirmware.setVisibility(View.GONE);

		//基础信息
		//产品型号
		mViewBinding.actvMyDeviceBasicInformationProductModel.setText("GAS NAS 2020");
		//序列号(SN)
		mViewBinding.actvMyDeviceBasicInformationSN.setText(SPUtils.getInstance().getString(SPConfig.ANDROID_ID));
		mViewBinding.actvMyDeviceBasicInformationSnCopy.setOnClickListener(view -> {
			ClipData mClipData = ClipData.newPlainText("Label", SPUtils.getInstance().getString(SPConfig.ANDROID_ID));
			mClipboardManager.setPrimaryClip(mClipData);
			ToastUtils.showShort(R.string.my_device_basic_information_sn_copy_success);
		});
		//MAC码
		mViewBinding.actvMyDeviceBasicInformationMac.setText(DeviceUtils.getMacAddress());
		//CPU
		mViewBinding.actvMyDeviceBasicInformationCpu.setText("Cortex-A53 8核 1.5GHz");
		//IP地址
		mViewBinding.actvMyDeviceBasicInformationIpAddress.setText(NetworkUtils.getIPAddress(true));
		//内存
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		mViewBinding.actvMyDeviceBasicInformationRam.setText(ConvertUtils.byte2FitMemorySize(memoryInfo.totalMem));
		//设备状态
		mViewBinding.actvMyDeviceBasicInformationDeviceState.setText("正常");
		//运行时间
		mViewBinding.actvMyDeviceBasicInformationOperationHours.setText(ConvertUtils.millis2FitTimeSpan(SystemClock.elapsedRealtime(), 2));
		//恢复出厂
		mViewBinding.actvMyDeviceBasicInformationReset.setOnClickListener(view -> {
			//暂未处理
			ToastUtils.showShort("敬请期待");
		});

		//硬盘信息
		//序列号(SN)
		mViewBinding.actvMyDeviceHardDiskInformationSN.setText("000000000033");
		mViewBinding.actvMyDeviceHardDiskInformationSnCopy.setOnClickListener(view -> {
			ClipData mClipData = ClipData.newPlainText("Label", "000000000033");
			mClipboardManager.setPrimaryClip(mClipData);
			ToastUtils.showShort(R.string.my_device_basic_information_sn_copy_success);
		});
		//硬盘型号
		mViewBinding.actvMyDeviceHardDiskInformationModel.setText("1816");
		//硬盘状态
		mViewBinding.actvMyDeviceHardDiskInformationState.setText("正常");
		//硬盘容量
		try {
			mViewBinding.actvMyDeviceHardDiskInformationCapacity.setText(ConvertUtils.byte2FitMemorySize(FileUtils.getFsTotalSize(PathConfig.NAS)));
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(CommonUtils.getFileName()+CommonUtils.getLineNumber(),e.toString());
		}
	}
}
