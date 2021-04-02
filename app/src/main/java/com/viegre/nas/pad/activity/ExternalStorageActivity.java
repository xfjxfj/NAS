package com.viegre.nas.pad.activity;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ExternalStorageListAdapter;
import com.viegre.nas.pad.databinding.ActivityExternalStorageBinding;
import com.viegre.nas.pad.entity.FileEntity;
import com.viegre.nas.pad.widget.GridSpaceItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.recyclerview.widget.GridLayoutManager;

/**
 * 外部存储
 * Created by レインマン on 2021/04/01 17:03 with Android Studio.
 */
public class ExternalStorageActivity extends BaseActivity<ActivityExternalStorageBinding> {

	private ExternalStorageListAdapter mExternalStorageListAdapter;
	private final CopyOnWriteArrayList<FileEntity> mTmpList = new CopyOnWriteArrayList<>();

	@Override
	protected void initialize() {
		mViewBinding.acivExternalStorageBack.setOnClickListener(view -> finish());
		initList();
	}

	private void initList() {
		mExternalStorageListAdapter = new ExternalStorageListAdapter();
		mExternalStorageListAdapter.setOnItemClickListener((adapter, view, position) -> ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<FileEntity>>() {
			@Override
			public List<FileEntity> doInBackground() {
				List<File> subDirList = FileUtils.listFilesInDir(mTmpList.get(position).getPath());
				if (subDirList.isEmpty()) {
					return null;
				}
				mTmpList.clear();
				mTmpList.addAll(mExternalStorageListAdapter.getData());
				List<FileEntity> subFileList = new ArrayList<>();
				for (File file : subDirList) {
					FileEntity.Type type;
					if (FileUtils.isFile(file)) {
						type = FileEntity.Type.FILE;
					} else if (FileUtils.isDir(file)) {
						type = FileEntity.Type.DIR;
					} else {
						type = FileEntity.Type.UNKNOWN;
					}
					subFileList.add(new FileEntity(file.getName(), file.getAbsolutePath(), type));
				}
				return subFileList;
			}

			@Override
			public void onSuccess(List<FileEntity> result) {
				if (null == result || result.isEmpty()) {
					ToastUtils.showShort(R.string.external_storage_empty);
				} else {
					mExternalStorageListAdapter.setList(result);
				}
			}
		}));
		mViewBinding.rvExternalStorageList.setLayoutManager(new GridLayoutManager(this, 4));
		mViewBinding.rvExternalStorageList.addItemDecoration(new GridSpaceItemDecoration(4, 30, 30));
		mViewBinding.rvExternalStorageList.setAdapter(mExternalStorageListAdapter);
		List<FileEntity> list = new ArrayList<>();
		list.add(new FileEntity("外部磁盘", "/sdcard/", FileEntity.Type.STORAGE));
		list.add(new FileEntity("我的U盘", "/sdcard/U/", FileEntity.Type.STORAGE));
		mExternalStorageListAdapter.setList(list);
	}
}
