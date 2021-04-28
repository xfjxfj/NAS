package com.viegre.nas.pad.activity;

import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.activity.BaseFragmentActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.SettingsMenuListAdapter;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivitySettingsBinding;
import com.viegre.nas.pad.entity.LoginInfoEntity;
import com.viegre.nas.pad.fragment.settings.AboutViegreFragment;
import com.viegre.nas.pad.fragment.settings.AutoAnswerFragment;
import com.viegre.nas.pad.fragment.settings.InstructionsFragment;
import com.viegre.nas.pad.fragment.settings.IntelligentVoiceFragment;
import com.viegre.nas.pad.fragment.settings.MyDeviceFragment;
import com.viegre.nas.pad.fragment.settings.ProtocolFragment;
import com.viegre.nas.pad.fragment.settings.SoundFragment;
import com.viegre.nas.pad.fragment.settings.TimeFragment;
import com.viegre.nas.pad.fragment.settings.network.NetworkFragment;
import com.viegre.nas.pad.fragment.settings.screen.ScreenCustomImageFragment;
import com.viegre.nas.pad.fragment.settings.screen.ScreenFragment;
import com.viegre.nas.pad.impl.PopupClickListener;
import com.viegre.nas.pad.manager.PopupManager;
import com.viegre.nas.pad.popup.PromptPopup;
import com.viegre.nas.pad.util.CommonUtils;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

/**
 * 设置页
 * Created by レインマン on 2020/12/16 15:33 with Android Studio.
 */
public class SettingsActivity extends BaseFragmentActivity<ActivitySettingsBinding> {

	private MyDeviceFragment mMyDeviceFragment;
	private NetworkFragment mNetworkFragment;
	private AutoAnswerFragment mAutoAnswerFragment;
	private ScreenFragment mScreenFragment;
	private ScreenCustomImageFragment mScreenCustomImageFragment;
	private IntelligentVoiceFragment mIntelligentVoiceFragment;
	private TimeFragment mTimeFragment;
	private SoundFragment mSoundFragment;
	private ProtocolFragment mProtocolFragment;
	private InstructionsFragment mInstructionsFragment;
	private AboutViegreFragment mAboutViegreFragment;
	private final List<Fragment> mMenuFragmentList = new ArrayList<>();
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
		                                               mMenuFragmentList,
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
		mMenuFragmentList.add(mMyDeviceFragment);
		mMenuFragmentList.add(mNetworkFragment);
		mMenuFragmentList.add(mAutoAnswerFragment);
		mMenuFragmentList.add(mScreenFragment);
		mMenuFragmentList.add(mIntelligentVoiceFragment);
		mMenuFragmentList.add(mTimeFragment);
		mMenuFragmentList.add(mSoundFragment);
		mMenuFragmentList.add(mProtocolFragment);
		mMenuFragmentList.add(mInstructionsFragment);
		mMenuFragmentList.add(mAboutViegreFragment);

		mScreenCustomImageFragment = ScreenCustomImageFragment.newInstance();
	}

	/**
	 * 登出接口
	 */
	private void logout() {
		RxHttp.postForm(UrlConfig.User.LOGOUT)
		      .addHeader("token", SPUtils.getInstance().getString(SPConfig.LOGIN_CODE_SESSION_ID))
		      .add("phoneNumber", mLoginInfoEntity.getPhoneNumber())
		      .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
		      .asString()
		      .subscribe(new Observer<String>() {
			      @Override
			      public void onSubscribe(@NonNull Disposable d) {}

			      @Override
			      public void onNext(@NonNull String s) {
				      LitePal.deleteAll(LoginInfoEntity.class);
				      checkLoginStatus();
			      }

			      @Override
			      public void onError(@NonNull Throwable e) {
				      CommonUtils.showErrorToast(e.getMessage());
			      }

			      @Override
			      public void onComplete() {}
		      });
	}

	@BusUtils.Bus(tag = BusConfig.SCREEN_CUSTOM_SHOW, threadMode = BusUtils.ThreadMode.MAIN)
	public void screenCustomShow() {
		FragmentUtils.add(getSupportFragmentManager(), mScreenCustomImageFragment, R.id.flSettingsFragment);
		FragmentUtils.show(mScreenCustomImageFragment);
	}

	@BusUtils.Bus(tag = BusConfig.SCREEN_CUSTOM_HIDE, threadMode = BusUtils.ThreadMode.MAIN)
	public void screenCustomHide() {
		FragmentUtils.remove(mScreenCustomImageFragment);
	}
}
