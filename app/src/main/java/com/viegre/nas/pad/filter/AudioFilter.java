package com.viegre.nas.pad.filter;

import com.blankj.utilcode.util.FileUtils;
import com.viegre.nas.pad.config.PathConfig;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by レインマン on 2021/01/19 17:36 with Android Studio.
 */
public class AudioFilter implements FileFilter {

	private final boolean mIsPublic;

	public AudioFilter(boolean isPublic) {
		mIsPublic = isPublic;
	}

	@Override
	public boolean accept(File file) {
		//过滤掉目录
		if (FileUtils.isDir(file)) {
			return false;
		}
		//查看公共空间时过滤私有空间
		if (mIsPublic && PathConfig.AUDIO.equals(file.getParent())) {
			return false;
		}
		//查看私有空间时过滤私有空间
		if (!mIsPublic && !PathConfig.AUDIO.equals(file.getParent())) {
			return false;
		}
		String fileName = file.getName();
		//通过后缀名过滤
		return fileName.endsWith(".mp3") || fileName.endsWith(".wma") || fileName.endsWith(".flac") || fileName.endsWith(".ape") || fileName.endsWith(
				".m4a");
	}
}
