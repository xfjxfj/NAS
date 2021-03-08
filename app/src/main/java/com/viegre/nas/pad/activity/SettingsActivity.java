package com.viegre.nas.pad.activity;

import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.activity.BaseFragmentActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.SettingsMenuListAdapter;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivitySettingsBinding;
import com.viegre.nas.pad.entity.LoginInfoEntity;
import com.viegre.nas.pad.fragment.settings.AboutViegreFragment;
import com.viegre.nas.pad.fragment.settings.AutoAnswerFragment;
import com.viegre.nas.pad.fragment.settings.InstructionsFragment;
import com.viegre.nas.pad.fragment.settings.IntelligentVoiceFragment;
import com.viegre.nas.pad.fragment.settings.MyDeviceFragment;
import com.viegre.nas.pad.fragment.settings.NetworkFragment;
import com.viegre.nas.pad.fragment.settings.ProtocolFragment;
import com.viegre.nas.pad.fragment.settings.ScreenFragment;
import com.viegre.nas.pad.fragment.settings.SoundFragment;
import com.viegre.nas.pad.fragment.settings.TimeFragment;
import com.viegre.nas.pad.impl.PopupClickListener;
import com.viegre.nas.pad.manager.PopupManager;
import com.viegre.nas.pad.popup.PromptPopup;
import com.viegre.nas.pad.util.CommonUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import org.litepal.LitePal;

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
	private LoginInfoEntity mLoginInfoEntity;

	@Override
	protected void initialize() {
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
	protected void onResume() {
		super.onResume();
		checkLoginStatus();
	}

	/**
	 * 检查登录状态
	 */
	private void checkLoginStatus() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<LoginInfoEntity>() {
			@Override
			public LoginInfoEntity doInBackground() {
				return LitePal.findFirst(LoginInfoEntity.class);
			}

			@Override
			public void onSuccess(LoginInfoEntity result) {
				mLoginInfoEntity = result;
				if (null == mLoginInfoEntity) {
					mViewBinding.acivSettingsLogout.setVisibility(View.GONE);
					mViewBinding.acivSettingsAvatar.setImageResource(R.mipmap.settings_unlogin);
					mViewBinding.actvSettingsUsername.setText(R.string.settings_view_after_login);
					mViewBinding.clSettingsLoginArea.setOnClickListener(view -> ActivityUtils.startActivity(LoginActivity.class));
				} else {
					mViewBinding.acivSettingsLogout.setVisibility(View.VISIBLE);
					mViewBinding.acivSettingsAvatar.setImageResource(R.mipmap.settings_unlogin);
					mViewBinding.actvSettingsUsername.setText(CommonUtils.getMarkedPhoneNumber(mLoginInfoEntity.getPhoneNumber()));
					mViewBinding.clSettingsLoginArea.setOnClickListener(null);
				}
			}
		});
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
		                                               new SettingsMenuListAdapter(this));
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
		Kalle.post(UrlConfig.User.LOGOUT)
		     .param("phoneNumber", mLoginInfoEntity.getPhoneNumber())
		     .param("sn", PhoneUtils.getSerial())
		     .perform(new SimpleCallback<String>() {
			     @Override
			     public void onResponse(SimpleResponse<String, String> response) {
				     if (!response.isSucceed()) {
					     CommonUtils.showErrorToast(response.failed());
				     } else {
					     ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Void>() {
						     @Override
						     public Void doInBackground() {
							     LitePal.deleteAll(LoginInfoEntity.class);
							     return null;
						     }

						     @Override
						     public void onSuccess(Void result) {
							     checkLoginStatus();
						     }
					     });
				     }
			     }
		     });
	}
}
