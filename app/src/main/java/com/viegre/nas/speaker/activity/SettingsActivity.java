package com.viegre.nas.speaker.activity;

import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseFragmentActivity;
import com.viegre.nas.speaker.adapter.SettingsMenuAdapter;
import com.viegre.nas.speaker.config.SPConfig;
import com.viegre.nas.speaker.config.UrlConfig;
import com.viegre.nas.speaker.databinding.ActivitySettingsBinding;
import com.viegre.nas.speaker.fragment.settings.AboutViegreFragment;
import com.viegre.nas.speaker.fragment.settings.AutoAnswerFragment;
import com.viegre.nas.speaker.fragment.settings.InstructionsFragment;
import com.viegre.nas.speaker.fragment.settings.IntelligentVoiceFragment;
import com.viegre.nas.speaker.fragment.settings.MyDeviceFragment;
import com.viegre.nas.speaker.fragment.settings.NetworkFragment;
import com.viegre.nas.speaker.fragment.settings.ProtocolFragment;
import com.viegre.nas.speaker.fragment.settings.ScreenFragment;
import com.viegre.nas.speaker.fragment.settings.SoundFragment;
import com.viegre.nas.speaker.fragment.settings.TimeFragment;
import com.viegre.nas.speaker.impl.PopupClickListener;
import com.viegre.nas.speaker.manager.PopupManager;
import com.viegre.nas.speaker.popup.PromptPopup;
import com.viegre.nas.speaker.util.CommonUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

/**
 * 设置页
 * Created by レインマン on 2020/12/16 15:33 with Android Studio.
 */
public class SettingsActivity extends BaseFragmentActivity<ActivitySettingsBinding> {

	private MyDeviceFragment mMyDeviceFragment;
	private NetworkFragment mNetworkFragment;
	private AutoAnswerFragment mAutoAnswerFragment;
	private ScreenFragment mScreenFragment;
	private IntelligentVoiceFragment mIntelligentVoiceFragment;
	private TimeFragment mTimeFragment;
	private SoundFragment mSoundFragment;
	private ProtocolFragment mProtocolFragment;
	private InstructionsFragment mInstructionsFragment;
	private AboutViegreFragment mAboutViegreFragment;
	private final List<Fragment> mFragmentList = new ArrayList<>();

	@Override
	protected void initView() {
		initFragment();
		initMenuList();
		mViewBinding.acivSettingsHome.setOnClickListener(view -> finish());
		mViewBinding.acivSettingsLogout.setOnClickListener(view -> PopupManager.INSTANCE.showCustomXPopup(this,
		                                                                                                  new PromptPopup(this,
		                                                                                                                  R.string.settings_logout_confirmation,
		                                                                                                                  R.string.settings_logout_confirmation_content,
		                                                                                                                  new PopupClickListener() {
			                                                                                                                  @Override
			                                                                                                                  public void onConfirm() {
				                                                                                                                  logout();
			                                                                                                                  }
		                                                                                                                  })));
	}

	@Override
	protected void initData() {}

	@Override
	protected void onResume() {
		super.onResume();
		checkLoginStatus();
	}

	/**
	 * 检查登录状态
	 */
	private void checkLoginStatus() {
		if (StringUtils.isEmpty(SPUtils.getInstance().getString(SPConfig.TOKEN, ""))) {
			mViewBinding.acivSettingsLogout.setVisibility(View.GONE);
			mViewBinding.acivSettingsAvatar.setImageResource(R.mipmap.settings_unlogin);
			mViewBinding.actvSettingsUsername.setText(R.string.settings_view_after_login);
			mViewBinding.clSettingsLoginArea.setOnClickListener(view -> ActivityUtils.startActivity(LoginActivity.class));
		} else {
			mViewBinding.acivSettingsLogout.setVisibility(View.VISIBLE);
			mViewBinding.acivSettingsAvatar.setImageResource(R.mipmap.settings_unlogin);
			mViewBinding.actvSettingsUsername.setText(CommonUtils.getMarkedPhoneNumber(SPUtils.getInstance().getString(SPConfig.PHONE_NUMBER)));
			mViewBinding.clSettingsLoginArea.setOnClickListener(null);
		}
	}

	private void initMenuList() {
		//设置分割线
//		LinearLayout linearLayout = (LinearLayout) mViewBinding.vtlSettingsMenu.getChildAt(0);
//		linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
//		linearLayout.setDividerDrawable(ContextCompat.getDrawable(this, R.drawable.settings_menu_divider)); //设置分割线的样式
//		linearLayout.setDividerPadding(66); //设置分割线间隔
		mViewBinding.vtlSettingsMenu.setupWithFragment(getSupportFragmentManager(),
		                                               R.id.flSettingsFragment,
		                                               mFragmentList,
		                                               new SettingsMenuAdapter(this));
	}

	private void initFragment() {
		mMyDeviceFragment = MyDeviceFragment.newInstance(false);
		mNetworkFragment = NetworkFragment.newInstance(false);
		mAutoAnswerFragment = AutoAnswerFragment.newInstance();
		mScreenFragment = ScreenFragment.newInstance();
		mIntelligentVoiceFragment = IntelligentVoiceFragment.newInstance();
		mTimeFragment = TimeFragment.newInstance();
		mSoundFragment = SoundFragment.newInstance();
		mProtocolFragment = ProtocolFragment.newInstance();
		mInstructionsFragment = InstructionsFragment.newInstance();
		mAboutViegreFragment = AboutViegreFragment.newInstance();
		mFragmentList.add(mMyDeviceFragment);
		mFragmentList.add(mNetworkFragment);
		mFragmentList.add(mAutoAnswerFragment);
		mFragmentList.add(mScreenFragment);
		mFragmentList.add(mIntelligentVoiceFragment);
		mFragmentList.add(mTimeFragment);
		mFragmentList.add(mSoundFragment);
		mFragmentList.add(mProtocolFragment);
		mFragmentList.add(mInstructionsFragment);
		mFragmentList.add(mAboutViegreFragment);
	}

	/**
	 * 登出接口
	 */
	private void logout() {
		Kalle.post(UrlConfig.UserConfig.LOGOUT).param("phoneNumber", SPUtils.getInstance().getString(SPConfig.PHONE_NUMBER))
		     .param("sn", PhoneUtils.getSerial())
		     .perform(new SimpleCallback<String>() {
			     @Override
			     public void onResponse(SimpleResponse<String, String> response) {
				     if (!response.isSucceed()) {
					     CommonUtils.showErrorToast(response.failed());
				     } else {
					     SPUtils.getInstance().remove(SPConfig.TOKEN);
					     SPUtils.getInstance().remove(SPConfig.PHONE_NUMBER);
					     checkLoginStatus();
				     }
			     }
		     });
	}
}
