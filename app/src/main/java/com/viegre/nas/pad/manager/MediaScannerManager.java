package com.viegre.nas.pad.manager;

import android.content.Intent;
import android.media.MediaScannerConnection;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.impl.MediaScannerCompletedListener;
import com.viegre.nas.pad.task.VoidTask;

/**
 * Created by レインマン on 2021/05/21 17:11 with Android Studio.
 */
public enum MediaScannerManager {

	INSTANCE;

//	public void scan(MediaScannerCompletedListener mediaScannerCompletedListener, String... paths) {
//		final int[] count = {0};
//		ThreadUtils.executeBySingle(new VoidTask() {
//			@Override
//			public Void doInBackground() {
//				List<String> pathList = new ArrayList<>();
//				for (String path : paths) {
//					pathList.add(path);
//					List<File> fileList = FileUtils.listFilesInDirWithFilter(path, FileUtils::isDir, true);
//					for (File file : fileList) {
//						pathList.add(file.getAbsolutePath());
//					}
//				}
//				String[] paths = pathList.toArray(new String[0]);
//				MediaScannerConnection.scanFile(Utils.getApp(), paths, null, (s, uri) -> {
//					count[0]++;
//					if (count[0] == paths.length) {
//						mediaScannerCompletedListener.onScanCompleted();
//					}
//				});
//				return null;
//			}
//		});
//	}

	public void scan(MediaScannerCompletedListener mediaScannerCompletedListener, String... paths) {
		final int[] count = {0};
		ThreadUtils.executeBySingle(new VoidTask() {
			@Override
			public Void doInBackground() {
//				List<String> pathList = new ArrayList<>();
//				for (String path : paths) {
//					pathList.add(path);
//					List<File> fileList = FileUtils.listFilesInDirWithFilter(path, FileUtils::isDir, true);
//					for (File file : fileList) {
//						pathList.add(file.getAbsolutePath());
//					}
//				}
//				String[] paths = pathList.toArray(new String[0]);
//				MediaScannerConnection.scanFile(Utils.getApp(), paths, null, (s, uri) -> {
//					count[0]++;
//					if (count[0] == paths.length) {
//						mediaScannerCompletedListener.onScanCompleted();
//					}
//				});
				MediaScannerConnection.scanFile(Utils.getApp(), new String[]{PathConfig.NAS}, null, (s, uri) -> {
					Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					intent.setData(uri);
					Utils.getApp().sendBroadcast(intent);
					if (null != mediaScannerCompletedListener) {
						mediaScannerCompletedListener.onScanCompleted();
					}
				});
				return null;
			}
		});
	}

//	public void scan() {
//		scan(null);
//	}
//
//	public void scan(MediaScannerCompletedListener mediaScannerCompletedListener) {
//		ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<Void>() {
//			@Override
//			public Void doInBackground() {
////				ShellUtils.CommandResult commandResult = ShellUtils.execCmd(
////						"am broadcast -a android.intent.action.MEDIA_MOUNTED -d file:///mnt/sdcard/nas/",
////						true);
//				ShellUtils.execCmd("am broadcast -a android.intent.action.MEDIA_MOUNTED", true);
//				return null;
//			}
//
//			@Override
//			public void onSuccess(Void result) {
//				if (null != mediaScannerCompletedListener) {
//					mediaScannerCompletedListener.onScanCompleted();
//				}
//			}
//		});
//	}
}
