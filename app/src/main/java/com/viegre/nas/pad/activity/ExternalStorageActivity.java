package com.viegre.nas.pad.activity;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ExternalStorageListAdapter;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.databinding.ActivityExternalStorageBinding;
import com.viegre.nas.pad.entity.FileEntity;
import com.viegre.nas.pad.widget.GridSpaceItemDecoration;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
	private final CopyOnWriteArrayList<List<FileEntity>> mHistoryList = new CopyOnWriteArrayList<>();
	//	private UsbMassStorageDevice[] mUsbMassStorageDevices;
	private volatile String mVolumeLabel;

	@Override
	protected void initialize() {
//		mUsbMassStorageDevices = UsbMassStorageDevice.getMassStorageDevices(this);
		List<File> list = FileUtils.listFilesInDir("/storage/3C3E71843E71384A/Android/");
		for (File file : list) {
			LogUtils.iTag("ExternalStorageActivity", file.getAbsolutePath());
		}
		initList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ThreadUtils.cancel(ThreadUtils.getSinglePool());
	}

	private void initList() {
		mViewBinding.llcExternalStorageTitle.setOnClickListener(view -> {
			if (mHistoryList.isEmpty() || mHistoryList.size() <= 1) {
				finish();
			} else {
				mHistoryList.remove(mHistoryList.size() - 1);
				List<FileEntity> list = new ArrayList<>(mHistoryList.get(mHistoryList.size() - 1));
				mExternalStorageListAdapter.setList(list);
				if (mHistoryList.size() == 1) {
					mViewBinding.actvExternalStorageTitle.setText(StringUtils.getString(R.string.external_storage, ""));
				} else if (mHistoryList.size() == 2) {
					mViewBinding.actvExternalStorageTitle.setText(StringUtils.getString(R.string.external_storage,
					                                                                    ": " + mVolumeLabel));
				} else {
					mViewBinding.actvExternalStorageTitle.setText(StringUtils.getString(R.string.external_storage,
					                                                                    ": " + FileUtils.getFileName(FileUtils.getFileByPath(
							                                                                    list.get(0).getPath())
					                                                                                                          .getParent())));
				}
			}
		});
		mExternalStorageListAdapter = new ExternalStorageListAdapter();
		mExternalStorageListAdapter.setOnItemClickListener((adapter, view, position) -> {
			FileEntity.Type clickFileType = mExternalStorageListAdapter.getData().get(position).getType();
			switch (clickFileType) {
				//打开文件夹
				case STORAGE:
				case DIR:
					ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<List<FileEntity>>() {
						@Override
						public List<FileEntity> doInBackground() {
							List<FileEntity> list = new ArrayList<>(mExternalStorageListAdapter.getData());
							List<File> subDirList = FileUtils.listFilesInDir(list.get(position).getPath());
							if (subDirList.isEmpty()) {
								return null;
							}
							if (FileEntity.Type.STORAGE == list.get(position).getType()) {
								mVolumeLabel = list.get(position).getName();
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
								if (mHistoryList.size() == 2) {
									mViewBinding.actvExternalStorageTitle.setText(StringUtils.getString(R.string.external_storage,
									                                                                    ": " + mVolumeLabel));
								} else {
									mViewBinding.actvExternalStorageTitle.setText(StringUtils.getString(R.string.external_storage,
									                                                                    ": " + FileUtils.getFileName(
											                                                                    FileUtils.getFileByPath(
													                                                                    result.get(
															                                                                    0)
													                                                                          .getPath())
											                                                                             .getParent())));
								}
							}
						}
					});
					break;
			}
		});
		mViewBinding.rvExternalStorageList.setLayoutManager(new GridLayoutManager(this, 4));
		mViewBinding.rvExternalStorageList.addItemDecoration(new GridSpaceItemDecoration(4, 30, 30));
		mViewBinding.rvExternalStorageList.setAdapter(mExternalStorageListAdapter);
		ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<List<FileEntity>>() {
			@Override
			public List<FileEntity> doInBackground() throws Throwable {
				List<FileEntity> list = new ArrayList<>();
//				for (UsbMassStorageDevice device : mUsbMassStorageDevices) {
//					device.init();
//					FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
//					list.add(new FileEntity(currentFs.getVolumeLabel(),
//					                        currentFs.getRootDirectory().getAbsolutePath(),
//					                        FileEntity.Type.STORAGE));
//				}
				return list;
			}

			@Override
			public void onSuccess(List<FileEntity> result) {
				if (!result.isEmpty()) {
					mHistoryList.add(result);
					mExternalStorageListAdapter.setList(result);
					mViewBinding.actvExternalStorageTitle.setText(StringUtils.getString(R.string.external_storage, ""));
				}
			}
		});
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void usbDeviceAttached(String event) {
		if (!BusConfig.USB_DEVICE_ATTACHED.equals(event)) {
			return;
		}
		ThreadUtils.cancel(ThreadUtils.getSinglePool());
		initList();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void usbDeviceDetached(String event) {
		if (!BusConfig.USB_DEVICE_DETACHED.equals(event)) {
			return;
		}
		ThreadUtils.cancel(ThreadUtils.getSinglePool());
		initList();
	}
}
