package com.viegre.nas.speaker.activity;

import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseFragmentActivity;
import com.viegre.nas.speaker.adapter.SettingsMenuAdapter;
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

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

/**
 * 设置页
 * Created by Djangoogle on 2020/12/16 15:33 with Android Studio.
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
	}

	@Override
	protected void initData() {}

	private void initMenuList() {
		//设置分割线
//		LinearLayout linearLayout = (LinearLayout) mViewBinding.vtlSettingsMenu.getChildAt(0);
//		linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
//		linearLayout.setDividerDrawable(ContextCompat.getDrawable(this, R.drawable.settings_menu_divider)); //设置分割线的样式
//		linearLayout.setDividerPadding(66); //设置分割线间隔
		mViewBinding.vtlSettingsMenu.setupWithFragment(getSupportFragmentManager(), R.id.flSettingsFragment, mFragmentList, new SettingsMenuAdapter(this));
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
}
