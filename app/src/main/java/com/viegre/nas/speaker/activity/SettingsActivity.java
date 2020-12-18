package com.viegre.nas.speaker.activity;

import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseFragmentActivity;
import com.viegre.nas.speaker.adapter.SettingsModuleAdapter;
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
				String[] moduleTagArr = getResources().getStringArray(R.array.settings_module_tag);
				List<SettingsModuleEntity> list = new ArrayList<>();
				for (int i = 0; i < moduleIconArr.length; i++) {
					list.add(new SettingsModuleEntity(moduleIconArr[i], moduleNameArr[i], moduleTagArr[i]));
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
		settingsModuleAdapter.setOnItemClickListener((adapter, view, position) -> FragmentUtils.show(FragmentUtils.findFragment(getSupportFragmentManager(),
		                                                                                                                        moduleList.get(position).getTag())));
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
		FragmentUtils.add(getSupportFragmentManager(), mMyDeviceFragment, R.id.flSettingsFragment, StringUtils.getString(R.string.settings_my_device_tag));
		FragmentUtils.add(getSupportFragmentManager(), mNetworkFragment, R.id.flSettingsFragment, StringUtils.getString(R.string.settings_network_tag), true);
		FragmentUtils.add(getSupportFragmentManager(), mAutoAnswerFragment, R.id.flSettingsFragment, StringUtils.getString(R.string.settings_auto_answer_tag), true);
		FragmentUtils.add(getSupportFragmentManager(), mScreenFragment, R.id.flSettingsFragment, StringUtils.getString(R.string.settings_screen_tag), true);
		FragmentUtils.add(getSupportFragmentManager(),
		                  mIntelligentVoiceFragment,
		                  R.id.flSettingsFragment,
		                  StringUtils.getString(R.string.settings_intelligent_voice_tag),
		                  true);
		FragmentUtils.add(getSupportFragmentManager(), mTimeFragment, R.id.flSettingsFragment, StringUtils.getString(R.string.settings_time_tag), true);
		FragmentUtils.add(getSupportFragmentManager(), mSoundFragment, R.id.flSettingsFragment, StringUtils.getString(R.string.settings_sound_tag), true);
		FragmentUtils.add(getSupportFragmentManager(), mProtocolFragment, R.id.flSettingsFragment, StringUtils.getString(R.string.settings_protocol_tag), true);
		FragmentUtils.add(getSupportFragmentManager(), mInstructionsFragment, R.id.flSettingsFragment, StringUtils.getString(R.string.settings_instructions_tag), true);
		FragmentUtils.add(getSupportFragmentManager(), mAboutViegreFragment, R.id.flSettingsFragment, StringUtils.getString(R.string.settings_about_viegre_tag), true);
		FragmentUtils.show(mMyDeviceFragment);
	}
}
