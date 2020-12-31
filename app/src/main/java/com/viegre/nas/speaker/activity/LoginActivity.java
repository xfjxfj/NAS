package com.viegre.nas.speaker.activity;

import android.graphics.Color;
import android.view.View;

import com.blankj.utilcode.util.ColorUtils;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseActivity;
import com.viegre.nas.speaker.databinding.ActivityLoginBinding;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> implements View.OnClickListener {

	@Override
	protected void initView() {
		initTab();
	}

	@Override
	protected void initData() {

	}

	private void initTab() {
		mViewBinding.actvLoginTabScan.setOnClickListener(this);
		mViewBinding.actvLoginTabAccount.setOnClickListener(this);
		mViewBinding.actvLoginTabPhoneCode.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (R.id.actvLoginTabScan == view.getId()) {
			mViewBinding.actvLoginTabScan.setTextColor(Color.WHITE);
			mViewBinding.actvLoginTabScan.setBackgroundResource(R.drawable.login_tab_bg);
			mViewBinding.clLoginScan.setVisibility(View.VISIBLE);
			mViewBinding.actvLoginTabAccount.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabAccount.setBackgroundResource(0);
			mViewBinding.clLoginAccount.setVisibility(View.GONE);
			mViewBinding.actvLoginTabPhoneCode.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabPhoneCode.setBackgroundResource(0);
			mViewBinding.clLoginPhoneCode.setVisibility(View.GONE);
		} else if (R.id.actvLoginTabAccount == view.getId()) {
			mViewBinding.actvLoginTabScan.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabScan.setBackgroundResource(0);
			mViewBinding.clLoginScan.setVisibility(View.GONE);
			mViewBinding.actvLoginTabAccount.setTextColor(Color.WHITE);
			mViewBinding.actvLoginTabAccount.setBackgroundResource(R.drawable.login_tab_bg);
			mViewBinding.clLoginAccount.setVisibility(View.VISIBLE);
			mViewBinding.actvLoginTabPhoneCode.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabPhoneCode.setBackgroundResource(0);
			mViewBinding.clLoginPhoneCode.setVisibility(View.GONE);
		} else if (R.id.actvLoginTabPhoneCode == view.getId()) {
			mViewBinding.actvLoginTabScan.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabScan.setBackgroundResource(0);
			mViewBinding.clLoginScan.setVisibility(View.GONE);
			mViewBinding.actvLoginTabAccount.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabAccount.setBackgroundResource(0);
			mViewBinding.clLoginAccount.setVisibility(View.GONE);
			mViewBinding.actvLoginTabPhoneCode.setTextColor(Color.WHITE);
			mViewBinding.actvLoginTabPhoneCode.setBackgroundResource(R.drawable.login_tab_bg);
			mViewBinding.clLoginPhoneCode.setVisibility(View.VISIBLE);
		} else if (R.id.acivLoginExit == view.getId()) {
			finish();
		}
	}
}
