package com.viegre.nas.pad.activity;

import android.graphics.Color;

import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.adapter.ExternalStorageAdapter;
import com.viegre.nas.pad.databinding.ActivityExternalStorageBinding;
import com.viegre.nas.pad.entity.ExternalStorageEntity;
import com.yqritc.recyclerviewflexibledivider.VerticalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 外部存储
 * Created by レインマン on 2021/04/01 17:03 with Android Studio.
 */
public class ExternalStorageActivity extends BaseActivity<ActivityExternalStorageBinding> {

	private ExternalStorageAdapter mExternalStorageAdapter;

	@Override
	protected void initialize() {
		mViewBinding.acivExternalStorageBack.setOnClickListener(view -> finish());
		initList();
	}

	private void initList() {
		mExternalStorageAdapter = new ExternalStorageAdapter();
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mViewBinding.rvExternalStorageList.setLayoutManager(linearLayoutManager);
		mViewBinding.rvExternalStorageList.addItemDecoration(new VerticalDividerItemDecoration.Builder(mActivity).color(Color.TRANSPARENT)
		                                                                                                         .size(30)
		                                                                                                         .build());
		mViewBinding.rvExternalStorageList.setAdapter(mExternalStorageAdapter);
		List<ExternalStorageEntity> list = new ArrayList<>();
		list.add(new ExternalStorageEntity("外部磁盘", ""));
		list.add(new ExternalStorageEntity("雨人的U盘", ""));
		mExternalStorageAdapter.setList(list);
	}
}
