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
		String fileName = file.getName();
		return !FileUtils.isDir(file)//不能为目录
				&& ((mIsPublic && !PathConfig.AUDIO.equals(file.getParent())) || (!mIsPublic && PathConfig.AUDIO.equals(file.getParent())))//过滤公共/私有目录
				&& (fileName.endsWith(".mp3") || fileName.endsWith(".wma") || fileName.endsWith(".flac") || fileName.endsWith(".ape") || fileName.endsWith(
				".m4a"));//通过后缀名过滤
	}
}
