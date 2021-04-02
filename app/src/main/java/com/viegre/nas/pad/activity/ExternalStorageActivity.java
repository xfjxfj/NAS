package com.viegre.nas.pad.activity;

import androidx.recyclerview.widget.GridLayoutManager;

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

/**
 * 外部存储
 * Created by レインマン on 2021/04/01 17:03 with Android Studio.
 */
public class ExternalStorageActivity extends BaseActivity<ActivityExternalStorageBinding> {

	private ExternalStorageListAdapter mExternalStorageListAdapter;
	private final CopyOnWriteArrayList<List<FileEntity>> mHistoryList = new CopyOnWriteArrayList<>();

	@Override
	protected void initialize() {
		initList();
	}

	private void initList() {
		mViewBinding.acivExternalStorageBack.setOnClickListener(view -> {
			if (mHistoryList.isEmpty() || mHistoryList.size() <= 1) {
				finish();
			} else {
				mHistoryList.remove(mHistoryList.size() - 1);
				List<FileEntity> list = new ArrayList<>(mHistoryList.get(mHistoryList.size() - 1));
				mExternalStorageListAdapter.setList(list);
			}
		});
		mExternalStorageListAdapter = new ExternalStorageListAdapter();
		mExternalStorageListAdapter.setOnItemClickListener((adapter, view, position) -> {
			FileEntity.Type clickFileType = mExternalStorageListAdapter.getData().get(position).getType();
			switch (clickFileType) {
				//打开文件夹
				case STORAGE:
				case DIR:
					ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<FileEntity>>() {
						@Override
						public List<FileEntity> doInBackground() {
							List<FileEntity> list = new ArrayList<>(mExternalStorageListAdapter.getData());
							List<File> subDirList = FileUtils.listFilesInDir(list.get(position).getPath());
							if (subDirList.isEmpty()) {
								return null;
							}
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
							mHistoryList.add(subFileList);
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
					});
					break;
			}
		});
		mViewBinding.rvExternalStorageList.setLayoutManager(new GridLayoutManager(this, 4));
		mViewBinding.rvExternalStorageList.addItemDecoration(new GridSpaceItemDecoration(4, 30, 30));
		mViewBinding.rvExternalStorageList.setAdapter(mExternalStorageListAdapter);
		List<FileEntity> list = new ArrayList<>();
		list.add(new FileEntity("外部磁盘", "/sdcard/", FileEntity.Type.STORAGE));
		list.add(new FileEntity("我的U盘", "/sdcard/U/", FileEntity.Type.STORAGE));
		mHistoryList.add(list);
		mExternalStorageListAdapter.setList(list);
	}
}
