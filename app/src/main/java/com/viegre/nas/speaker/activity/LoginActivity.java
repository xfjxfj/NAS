package com.viegre.nas.speaker.activity;

import android.graphics.Color;
import android.view.View;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseActivity;
import com.viegre.nas.speaker.config.UrlConfig;
import com.viegre.nas.speaker.databinding.ActivityLoginBinding;
import com.viegre.nas.speaker.util.ImageStreamUtils;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> implements View.OnClickListener {

	@Override
	protected void initView() {
		mViewBinding.acivLoginAccountCode.setTag(false);
		initListener();
	}

	@Override
	protected void initData() {}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//取消获取图片验证码任务
		ThreadUtils.cancel(ThreadUtils.getSinglePool());
	}

	private void initListener() {
		mViewBinding.actvLoginTabScan.setOnClickListener(this);
		mViewBinding.actvLoginTabAccount.setOnClickListener(this);
		mViewBinding.actvLoginTabPhoneCode.setOnClickListener(this);
		mViewBinding.acivLoginAccountCode.setOnClickListener(this);
		mViewBinding.acivLoginExit.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (R.id.actvLoginTabScan == view.getId()) {//点击扫码登录标签
			mViewBinding.actvLoginTabScan.setTextColor(Color.WHITE);
			mViewBinding.actvLoginTabScan.setBackgroundResource(R.drawable.login_tab_bg);
			mViewBinding.clLoginScan.setVisibility(View.VISIBLE);
			mViewBinding.actvLoginTabAccount.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabAccount.setBackgroundResource(0);
			mViewBinding.clLoginAccount.setVisibility(View.GONE);
			mViewBinding.actvLoginTabPhoneCode.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabPhoneCode.setBackgroundResource(0);
			mViewBinding.clLoginPhoneCode.setVisibility(View.GONE);
		} else if (R.id.actvLoginTabAccount == view.getId()) {//点击账号密码登录标签
			//如果没有加载验证码则请求获取
			if (!(Boolean) mViewBinding.acivLoginAccountCode.getTag()) {
				getCodeImage();
			}
			mViewBinding.actvLoginTabScan.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabScan.setBackgroundResource(0);
			mViewBinding.clLoginScan.setVisibility(View.GONE);
			mViewBinding.actvLoginTabAccount.setTextColor(Color.WHITE);
			mViewBinding.actvLoginTabAccount.setBackgroundResource(R.drawable.login_tab_bg);
			mViewBinding.clLoginAccount.setVisibility(View.VISIBLE);
			mViewBinding.actvLoginTabPhoneCode.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabPhoneCode.setBackgroundResource(0);
			mViewBinding.clLoginPhoneCode.setVisibility(View.GONE);
		} else if (R.id.actvLoginTabPhoneCode == view.getId()) {//点击手机验证码登录标签
			mViewBinding.actvLoginTabScan.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabScan.setBackgroundResource(0);
			mViewBinding.clLoginScan.setVisibility(View.GONE);
			mViewBinding.actvLoginTabAccount.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
			mViewBinding.actvLoginTabAccount.setBackgroundResource(0);
			mViewBinding.clLoginAccount.setVisibility(View.GONE);
			mViewBinding.actvLoginTabPhoneCode.setTextColor(Color.WHITE);
			mViewBinding.actvLoginTabPhoneCode.setBackgroundResource(R.drawable.login_tab_bg);
			mViewBinding.clLoginPhoneCode.setVisibility(View.VISIBLE);
		} else if (R.id.acivLoginAccountCode == view.getId()) {//点击验证码
			getCodeImage();
		} else if (R.id.acivLoginExit == view.getId()) {//点击退出
			finish();
		}
	}

	/**
	 * 获取图片验证码
	 */
	private void getCodeImage() {
		mViewBinding.acivLoginAccountCode.setClickable(false);
		ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<byte[]>() {
			@Override
			public byte[] doInBackground() throws Throwable {
				return ImageStreamUtils.getImageFromStream(UrlConfig.SERVER_URL + UrlConfig.USER + UrlConfig.GET_IMAGE_CODE);
			}

			@Override
			public void onSuccess(byte[] result) {
				if (null == result) {
					mViewBinding.acivLoginAccountCode.setImageResource(0);
					mViewBinding.acivLoginAccountCode.setTag(false);
				} else {
					Glide.with(mActivity).load(result).into(mViewBinding.acivLoginAccountCode);
					mViewBinding.acivLoginAccountCode.setTag(true);
				}
				mViewBinding.acivLoginAccountCode.setClickable(true);
			}

			@Override
			public void onFail(Throwable t) {
				super.onFail(t);
				t.printStackTrace();
				mViewBinding.acivLoginAccountCode.setImageResource(0);
				mViewBinding.acivLoginAccountCode.setTag(false);
				mViewBinding.acivLoginAccountCode.setClickable(true);
			}
		});
	}
}
