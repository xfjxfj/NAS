package com.viegre.nas.speaker.activity;

import android.graphics.Color;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseActivity;
import com.viegre.nas.speaker.config.SPConfig;
import com.viegre.nas.speaker.config.UrlConfig;
import com.viegre.nas.speaker.databinding.ActivityLoginBinding;
import com.viegre.nas.speaker.entity.LoginEntity;
import com.viegre.nas.speaker.util.CommonUtils;
import com.viegre.nas.speaker.util.ImageStreamUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> implements View.OnClickListener {

	@Override
	protected void initView() {
		mViewBinding.acivLoginAccountCode.setTag(false);
		mViewBinding.acetLoginAccountPhone.setText("18715008554");
		mViewBinding.acetLoginAccountPassword.setText("abcd1234");
		initListener();
	}

	@Override
	protected void initData() {}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//取消获取图片验证码任务
		ThreadUtils.cancel(ThreadUtils.getSinglePool());
		//清除验证码sessionId缓存
		SPUtils.getInstance().remove(SPConfig.SP_LOGIN_CODE_SESSION_ID);
	}

	private void initListener() {
		mViewBinding.actvLoginTabScan.setOnClickListener(this);
		mViewBinding.actvLoginTabAccount.setOnClickListener(this);
		mViewBinding.actvLoginTabPhoneCode.setOnClickListener(this);
		mViewBinding.acivLoginAccountCode.setOnClickListener(this);
		mViewBinding.actvLoginAccountBtn.setOnClickListener(this);
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
		} else if (R.id.actvLoginAccountBtn == view.getId()) {//账号密码登录
			loginbyAccount();
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
				mViewBinding.acetLoginAccountCode.setText("");
			}

			@Override
			public void onFail(Throwable t) {
				super.onFail(t);
				t.printStackTrace();
				mViewBinding.acivLoginAccountCode.setImageResource(0);
				mViewBinding.acivLoginAccountCode.setTag(false);
				mViewBinding.acivLoginAccountCode.setClickable(true);
				mViewBinding.acetLoginAccountCode.setText("");
			}
		});
	}

	/**
	 * 账号密码登录
	 */
	private void loginbyAccount() {
		mViewBinding.actvLoginAccountBtn.setClickable(false);
		String phone = String.valueOf(mViewBinding.acetLoginAccountPhone.getText()), password = String.valueOf(mViewBinding.acetLoginAccountPassword.getText()), code = String
				.valueOf(mViewBinding.acetLoginAccountCode.getText());
		if (StringUtils.isEmpty(phone)) {
			CommonUtils.showErrorToast(R.string.login_please_input_phone_number);
			mViewBinding.actvLoginAccountBtn.setClickable(true);
			return;
		}
		if (!RegexUtils.isMobileExact(phone)) {
			CommonUtils.showErrorToast(R.string.login_phone_error);
			mViewBinding.actvLoginAccountBtn.setClickable(true);
			return;
		}
		if (password.length() < 8 || !RegexUtils.isMatch(".*(?:[a-zA-z]+.*\\d+)|(?:\\d+.*[a-zA-z]+).*", password)) {
			CommonUtils.showErrorToast(R.string.login_please_input_password);
			mViewBinding.actvLoginAccountBtn.setClickable(true);
			return;
		}
		if (StringUtils.isEmpty(code)) {
			CommonUtils.showErrorToast(R.string.login_please_input_code);
			mViewBinding.actvLoginAccountBtn.setClickable(true);
			return;
		}
		if (code.length() < 4) {
			CommonUtils.showErrorToast(R.string.login_code_error);
			mViewBinding.actvLoginAccountBtn.setClickable(true);
			return;
		}
		Kalle.post(UrlConfig.SERVER_URL + UrlConfig.USER + UrlConfig.LOGIN)
		     .addHeader("Cookie", SPUtils.getInstance().getString(SPConfig.SP_LOGIN_CODE_SESSION_ID))
		     .param("code", code)
		     .param("password", password)
		     .param("phoneNumber", phone)
		     .param("sn", PhoneUtils.getSerial())
		     .perform(new SimpleCallback<LoginEntity>() {
			     @Override
			     public void onResponse(SimpleResponse<LoginEntity, String> response) {
				     if (!response.isSucceed()) {
					     CommonUtils.showErrorToast(response.failed());
				     } else {
					     String token = response.succeed().getToken();
					     SPUtils.getInstance().put(SPConfig.SP_TOKEN, token);
					     SPUtils.getInstance().put(SPConfig.SP_PHONE_NUMBER, phone);
					     Kalle.getConfig().getHeaders().set("token", token);
					     Kalle.setConfig(Kalle.getConfig());
					     ActivityUtils.startActivity(MainActivity.class);
					     finish();
				     }
			     }

			     @Override
			     public void onEnd() {
				     super.onEnd();
				     SPUtils.getInstance().remove(SPConfig.SP_LOGIN_CODE_SESSION_ID);
				     mViewBinding.actvLoginAccountBtn.setClickable(true);
			     }
		     });
	}
}
