package com.viegre.nas.pad.observer;

import android.os.FileObserver;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.entity.FtpFileObserverEntity;
import com.viegre.nas.pad.task.VoidTask;

import org.litepal.LitePal;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.Nullable;

/**
 * Created by レインマン on 2021/04/22 18:06 with Android Studio.
 */
public class FtpFileObserver extends FileObserver {

	private static final String TAG = FtpFileObserver.class.getSimpleName();
	public static final int MULTI_DIRS_TYPE = 0;
	public static final int ONE_DIR_TYPE = 1;

	private final SimpleDateFormat sdf;
	private final CopyOnWriteArrayList<FtpFileObserver> mFtpFileObserverList = new CopyOnWriteArrayList<>();
	private final String mAbsolutePath;

	public FtpFileObserver(String path, int type) {
		super(path, FileObserver.ALL_EVENTS);
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		mAbsolutePath = path;
		if (MULTI_DIRS_TYPE == type) {
			setFileObserver(path);
		}
	}

	@Override
	public void onEvent(int i, @Nullable String s) {
		final int action = i & FileObserver.ALL_EVENTS;
		switch (action) {
			case FileObserver.OPEN:
				if (null == s || s.isEmpty()) {
					break;
				}
				ThreadUtils.executeByCached(new VoidTask() {
					@Override
					public Void doInBackground() {
						String path = mAbsolutePath + File.separator + s;
						if (PathConfig.GUIDE_RESOURCE.equals(path + File.separator)) {
							return null;
						}
						if (PathConfig.PRIVATE.equals(path + File.separator)) {
							return null;
						}
						if (PathConfig.PUBLIC.equals(path + File.separator)) {
							return null;
						}
						if (PathConfig.RECYCLE_BIN.equals(path + File.separator)) {
							return null;
						}
						new FtpFileObserverEntity(path, TimeUtils.getNowString(sdf)).save();
						List<FtpFileObserverEntity> list = LitePal.order("id desc").limit(10).find(FtpFileObserverEntity.class);
						if (!list.isEmpty()) {
							String test = "";
							for (FtpFileObserverEntity f : list) {
								test += "[" + f.getPath() + ", " + f.getTime() + "]\n";
							}
//							LogUtils.iTag(TAG, test);
						}
						return null;
					}
				});
				break;

			default:
				break;
		}
	}

	private List<File> setFileObserver(String dir) {
		ArrayList<File> fileList = new ArrayList<>();
		LinkedList<File> list = new LinkedList<>();
		File fileDir = new File(dir);
		File[] file = fileDir.listFiles();
		mFtpFileObserverList.add(this);
		for (File item : file) {
			if (item.isDirectory()) {
				list.add(item);
				FtpFileObserver ftpFileObserver = new FtpFileObserver(item.getAbsolutePath(), FtpFileObserver.ONE_DIR_TYPE);
				mFtpFileObserverList.add(ftpFileObserver);
			}
		}
		File tmp;
		while (!list.isEmpty()) {
			tmp = list.removeFirst();
			if (tmp.isDirectory()) {
				FtpFileObserver ftpFileObserver = new FtpFileObserver(tmp.getAbsolutePath(), FtpFileObserver.ONE_DIR_TYPE);
				mFtpFileObserverList.add(ftpFileObserver);
				file = tmp.listFiles();
				if (null == file) {
					continue;
				}
				for (File value : file) {
					if (value.isDirectory()) {
						list.add(value);
						FtpFileObserver fileObserver = new FtpFileObserver(value.getAbsolutePath(), FtpFileObserver.ONE_DIR_TYPE);
						mFtpFileObserverList.add(fileObserver);
					}
				}
			}
		}
		return fileList;
	}

	public void startDirWatch() {
		for (FtpFileObserver listener : mFtpFileObserverList) {
			listener.startWatching();
		}
	}

	public void stopDirWatch() {
		for (FtpFileObserver listener : mFtpFileObserverList) {
			listener.stopWatching();
		}
	}
}
