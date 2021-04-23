package com.viegre.nas.pad.observer;

import android.os.FileObserver;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.viegre.nas.pad.entity.FtpFileObserverEntity;
import com.viegre.nas.pad.task.VoidTask;

import org.litepal.LitePal;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by レインマン on 2021/04/22 18:06 with Android Studio.
 */
public class FtpFileObserver extends FileObserver {

	private final SimpleDateFormat sdf;

	public FtpFileObserver(String path) {
		super(path, FileObserver.ALL_EVENTS);
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
	}

	@Override
	public void onEvent(int i, @Nullable String s) {
		switch (i) {
			case FileObserver.OPEN:
				ThreadUtils.executeByCached(new VoidTask() {
					@Override
					public Void doInBackground() {
						new FtpFileObserverEntity(s, TimeUtils.getNowString(sdf)).save();
						List<FtpFileObserverEntity> list = LitePal.order("time desc").limit(10).find(FtpFileObserverEntity.class);
						if (!list.isEmpty()) {
							String s = "";
							for (FtpFileObserverEntity f : list) {
								s += f.getPath() + ",";
							}
							LogUtils.iTag("FtpFileObserverList", s);
						}
						return null;
					}
				});
				break;

			default:
				break;
		}
	}
}
