package com.viegre.nas.pad.config;

import java.io.File;

/**
 * Created by レインマン on 2021/01/19 09:37 with Android Studio.
 */
public class PathConfig {

	//	public static final String NAS = PathUtils.getExternalStoragePath() + File.separator + "nas" + File.separator;
	public static final String NAS = "/storage/52207D41207D2CDB/nas/";

	//引导资源目录
	public static final String GUIDE_RESOURCE = NAS + ".guideResource" + File.separator;

	//私有空间目录
	public static final String PRIVATE = NAS + "private" + File.separator;

	//公共空间目录
	public static final String PUBLIC = NAS + "public" + File.separator;

	//回收站目录
	public static final String RECYCLE_BIN = NAS + ".recycleBin" + File.separator;

	//上传文件缓存目录
	public static final String UPLOAD_CACHE = NAS + ".uploadCache" + File.separator;

	public static final String CONTACTS_RECOMDING = "/Recomding.txt";

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
