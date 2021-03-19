package com.viegre.nas.pad.config;

import com.blankj.utilcode.util.PathUtils;

import java.io.File;

/**
 * Created by レインマン on 2021/01/19 09:37 with Android Studio.
 */
public class PathConfig {

	public static final String NAS = PathUtils.getExternalStoragePath() + File.separator + "nas" + File.separator;

	//引导资源目录
	public static final String GUIDE_RESOURCE = NAS + "guideResource" + File.separator;

	//私有空间目录
	public static final String PRIVATE = NAS + "private" + File.separator;

	//公共空间目录
	public static final String PUBLIC = NAS + "public" + File.separator;

//	//图片目录
//	public static class IMAGE {
//		private static final String IMAGE = "image";
//		public static final String PRI = PRIVATE + IMAGE;
//		public static final String PUB = PUBLIC + IMAGE;
//	}
//
//	//音频目录
//	public static class AUDIO {
//		private static final String AUDIO = "audio";
//		public static final String PRI = PRIVATE + AUDIO;
//		public static final String PUB = PUBLIC + AUDIO;
//	}
//
//	//视频目录
//	public static class VIDEO {
//		private static final String VIDEO = "video";
//		public static final String PRI = PRIVATE + VIDEO;
//		public static final String PUB = PUBLIC + VIDEO;
//	}
}
