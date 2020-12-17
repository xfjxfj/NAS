package com.viegre.nas.speaker.activity;

import com.blankj.utilcode.util.ThreadUtils;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseFragmentActivity;
import com.viegre.nas.speaker.adapter.SettingsModuleAdapter;
import com.viegre.nas.speaker.databinding.ActivitySettingsBinding;
import com.viegre.nas.speaker.entity.SettingsModuleEntity;
import com.viegre.nas.speaker.fragment.MyDeviceFragment;

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

	@Override
	protected void initView() {
		initModuleArray();
	}

	@Override
	protected void initData() {

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
		mViewBinding.rvSettingsModule.setLayoutManager(new LinearLayoutManager(mActivity));
		mViewBinding.rvSettingsModule.setAdapter(settingsModuleAdapter);
		SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) mViewBinding.rvSettingsModule.getItemAnimator();
		if (null != simpleItemAnimator) {
			simpleItemAnimator.setSupportsChangeAnimations(false);
		}
	}

	private void initFragment() {
		mMyDeviceFragment = MyDeviceFragment.newInstance(false);
	}
}
