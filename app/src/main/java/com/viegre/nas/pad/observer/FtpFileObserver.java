package com.viegre.nas.pad.observer;

import android.annotation.SuppressLint;
import android.os.FileObserver;

import com.blankj.utilcode.util.TimeUtils;
import com.viegre.nas.pad.entity.FtpFileObserverEntity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by レインマン on 2021/04/22 18:06 with Android Studio.
 */
public class FtpFileObserver extends FileObserver {

	private final SimpleDateFormat sdf;

	@SuppressLint("NewApi")
	public FtpFileObserver(@NonNull List<File> files) {
		super(files);
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
	}

	@Override
	public void onEvent(int i, @Nullable String s) {
		switch (i) {
			case FileObserver.OPEN:
				new FtpFileObserverEntity(s, TimeUtils.getNowString(sdf)).save();
				break;

			default:
				break;
		}
	}
}
