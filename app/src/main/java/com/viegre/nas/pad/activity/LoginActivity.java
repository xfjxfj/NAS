package com.viegre.nas.pad.activity;

import android.graphics.Color;
import android.view.View;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivityLoginBinding;
import com.viegre.nas.pad.entity.LoginEntity;
import com.viegre.nas.pad.entity.LoginInfoEntity;
import com.viegre.nas.pad.manager.PopupManager;
import com.viegre.nas.pad.popup.LoginTimePopup;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.util.ImageStreamUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import org.litepal.LitePal;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

/**
 * Created by レインマン on 2021/01/06 10:39 with Android Studio.
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding> implements View.OnClickListener {

	@Override
	protected void initialize() {
		mViewBinding.acivLoginAccountCode.setTag(false);
		mViewBinding.acetLoginAccountPhone.setText("18715008554");
		mViewBinding.acetLoginAccountPassword.setText("abcd1234");
		initListener();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//取消获取图片验证码任务
		ThreadUtils.cancel(ThreadUtils.getSinglePool());
		//清除验证码sessionId缓存
		SPUtils.getInstance().remove(SPConfig.LOGIN_CODE_SESSION_ID);
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
				return ImageStreamUtils.getImageFromStream(UrlConfig.User.GET_IMAGE_CODE);
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
		RxHttp.postForm(UrlConfig.User.LOGIN)
		      .addHeader("Cookie", SPUtils.getInstance().getString(SPConfig.LOGIN_CODE_SESSION_ID))
		      .add("code", code)
		      .add("password", password)
		      .add("phoneNumber", phone)
		      .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
		      .asResponse(LoginEntity.class)
		      .subscribe(new Observer<LoginEntity>() {
			      @Override
			      public void onSubscribe(@NonNull Disposable d) {}

			      @Override
			      public void onNext(@NonNull LoginEntity loginEntity) {
				      SPUtils.getInstance().put(SPConfig.LOGIN_CODE_SESSION_ID, loginEntity.getToken());
				      LoginInfoEntity loginInfoEntity = new LoginInfoEntity(phone);

				      ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Void>() {
					      @Override
					      public Void doInBackground() {
						      LitePal.deleteAll(LoginInfoEntity.class);
						      loginInfoEntity.save();
						      return null;
					      }

					      @Override
					      public void onSuccess(Void result) {
						      Kalle.getConfig().getHeaders().set("token", SPUtils.getInstance().getString(SPConfig.LOGIN_CODE_SESSION_ID));
						      Kalle.setConfig(Kalle.getConfig());
						      setLoginTime();
					      }
				      });
			      }

			      @Override
			      public void onError(@NonNull Throwable e) {

			      }

			      @Override
			      public void onComplete() {

			      }
		      });
		Kalle.post(UrlConfig.User.LOGIN)
		     .addHeader("Cookie", SPUtils.getInstance().getString(SPConfig.LOGIN_CODE_SESSION_ID))
		     .param("code", code)
		     .param("password", password)
		     .param("phoneNumber", phone)
		     .param("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
		     .perform(new SimpleCallback<LoginEntity>() {
			     @Override
			     public void onResponse(SimpleResponse<LoginEntity, String> response) {
				     if (!response.isSucceed()) {
					     CommonUtils.showErrorToast(response.failed());
				     } else {

				     }
			     }

			     @Override
			     public void onEnd() {
				     super.onEnd();
				     SPUtils.getInstance().remove(SPConfig.LOGIN_CODE_SESSION_ID);
				     mViewBinding.actvLoginAccountBtn.setClickable(true);
			     }
		     });
	}

	/**
	 * 设置登录有效时间
	 */
	private void setLoginTime() {
		PopupManager.INSTANCE.showCustomXPopup(this, new LoginTimePopup(this));
	}
}
