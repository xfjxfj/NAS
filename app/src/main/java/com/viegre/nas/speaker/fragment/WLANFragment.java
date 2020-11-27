package com.viegre.nas.speaker.fragment;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.github.iielse.switchbutton.SwitchView;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.adapter.WLANListAdapter;
import com.viegre.nas.speaker.databinding.FragmentWlanBinding;
import com.viegre.nas.speaker.fragment.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 网络设置
 * Created by Djangoogle on 2020/11/26 11:35 with Android Studio.
 */
public class WLANFragment extends BaseFragment<FragmentWlanBinding> {

	public static final String IS_FIRST_RUN = "isFirstRun";

	private WLANListAdapter mWLANListAdapter;
	private GetWifiScanResultTask mGetWifiScanResultTask;

	@Override
	protected void initView() {
		initAdapter();
		setWLANSwitch();
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

	/**
	 * 初始化适配器
	 */
	private void initAdapter() {
		mWLANListAdapter = new WLANListAdapter(R.layout.item_wlan_list);
		mViewBinding.rvWLANList.setLayoutManager(new LinearLayoutManager(mActivity));
		mViewBinding.rvWLANList.setAdapter(mWLANListAdapter);
	}

	/**
	 * 设置WLAN开关
	 */
	private void setWLANSwitch() {
		mGetWifiScanResultTask = new GetWifiScanResultTask();
		mViewBinding.svWLANSwitch.setOpened(NetworkUtils.getWifiEnabled());
		mViewBinding.svWLANSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(SwitchView view) {
				ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
					@Override
					public Boolean doInBackground() {
						NetworkUtils.setWifiEnabled(true);
						while (true) {
							if (NetworkUtils.getWifiEnabled()) {
								break;
							}
						}
						return true;
					}

					@Override
					public void onSuccess(Boolean result) {
						view.toggleSwitch(result);
						getWLANStatus();
					}
				});
			}

			@Override
			public void toggleToOff(SwitchView view) {
				ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
					@Override
					public Boolean doInBackground() {
						NetworkUtils.setWifiEnabled(false);
						while (true) {
							if (!NetworkUtils.getWifiEnabled()) {
								break;
							}
						}
						return false;
					}

					@Override
					public void onSuccess(Boolean result) {
						view.toggleSwitch(result);
						getWLANStatus();
					}
				});
			}
		});
		getWLANStatus();
	}

	/**
	 * 获取WLAN状态
	 */
	private void getWLANStatus() {
		if (NetworkUtils.getWifiEnabled()) {
			mViewBinding.actvWLANNearbyNetwork.setVisibility(View.VISIBLE);
			mViewBinding.rvWLANList.setVisibility(View.VISIBLE);
			initWLANList();
		} else {
			mWLANListAdapter.getData().clear();
			mWLANListAdapter.notifyDataSetChanged();
			ThreadUtils.cancel(mGetWifiScanResultTask);
			mViewBinding.vWLANLine.setVisibility(View.GONE);
			mViewBinding.ilWLANSelected.getRoot().setVisibility(View.GONE);
			mViewBinding.actvWLANNearbyNetwork.setVisibility(View.GONE);
			mViewBinding.rvWLANList.setVisibility(View.GONE);
		}
	}

	/**
	 * 初始化WLAN列表
	 */
	private void initWLANList() {
		ThreadUtils.executeByCachedAtFixRate(mGetWifiScanResultTask, 20L, TimeUnit.SECONDS);
	}

	private class GetWifiScanResultTask extends ThreadUtils.SimpleTask<List<ScanResult>> {
		@Override
		public List<ScanResult> doInBackground() {
			return NetworkUtils.getWifiScanResult().getFilterResults();
		}

		@Override
		public void onSuccess(List<ScanResult> result) {
			if (null != result && !result.isEmpty()) {
				List<ScanResult> list = new ArrayList<>(result);
				mWLANListAdapter.setList(list);
			}
		}
	}
}
