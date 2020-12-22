package com.viegre.nas.speaker.activity;

import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseFragmentActivity;
import com.viegre.nas.speaker.adapter.SettingsModuleAdapter;
import com.viegre.nas.speaker.config.NasConfig;
import com.viegre.nas.speaker.databinding.ActivitySettingsBinding;
import com.viegre.nas.speaker.entity.SettingsModuleEntity;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

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
		initModuleArray();
	}

	@Override
	protected void initData() {
		initFragment();
	}

	private void initModuleArray() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<SettingsModuleEntity>>() {
			@Override
			public List<SettingsModuleEntity> doInBackground() {
				int[] moduleIconArr = getResources().getIntArray(R.array.settings_module_icon);
				String[] moduleNameArr = getResources().getStringArray(R.array.settings_module_name);
				List<SettingsModuleEntity> list = new ArrayList<>();
				for (int i = 0; i < moduleIconArr.length; i++) {
					list.add(new SettingsModuleEntity(moduleIconArr[i], moduleNameArr[i]));
				}
				return list;
			}

			@Override
			public void onSuccess(List<SettingsModuleEntity> result) {
				result.get(0).setSelected(true);
				initModuleList(result);
			}
		});
	}

	private void initModuleList(List<SettingsModuleEntity> moduleList) {
		SettingsModuleAdapter settingsModuleAdapter = new SettingsModuleAdapter(moduleList);
		settingsModuleAdapter.setOnItemClickListener((adapter, view, position) -> {
			FragmentUtils.replace(getSupportFragmentManager(), mFragmentList.get(position), R.id.flSettingsFragment);
			if (!settingsModuleAdapter.getData().get(position).isSelected()) {
				settingsModuleAdapter.getData().get(position).setSelected(true);
				settingsModuleAdapter.notifyItemChanged(position);
			}
			for (int i = 0; i < settingsModuleAdapter.getData().size(); i++) {
				if (i != position && settingsModuleAdapter.getData().get(i).isSelected()) {
					settingsModuleAdapter.getData().get(i).setSelected(false);
					settingsModuleAdapter.notifyItemChanged(i);
					break;
				}
			}
		});
		mViewBinding.rvSettingsModule.setLayoutManager(new LinearLayoutManager(mActivity));
		mViewBinding.rvSettingsModule.setAdapter(settingsModuleAdapter);
		SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) mViewBinding.rvSettingsModule.getItemAnimator();
		if (null != simpleItemAnimator) {
			simpleItemAnimator.setSupportsChangeAnimations(false);
		}
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
		FragmentUtils.add(getSupportFragmentManager(), mFragmentList, R.id.flSettingsFragment, NasConfig.SETTINGS_MY_DEVICE_INDEX);
	}
}
