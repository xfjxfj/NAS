package com.viegre.nas.pad.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;

/**
 * Created by レインマン on 2021/03/19 17:27 with Android Studio.
 */
public class MediaScanner {

	private static final String TAG = MediaScanner.class.getSimpleName();

	private MediaScannerConnection mConn = null;
	private ScannerClient mClient;
	private File mFile = null;
	private AddOnScanCompleted mAddOnScanCompleted;

	public MediaScanner(Context context, AddOnScanCompleted addOnScanCompleted) {
		if (null == mClient) {
			mClient = new ScannerClient();
		}
		if (null == mConn) {
			mConn = new MediaScannerConnection(context, mClient);
		}
		if (null == mAddOnScanCompleted) {
			mAddOnScanCompleted = addOnScanCompleted;
		}
	}

	private class ScannerClient implements MediaScannerConnection.MediaScannerConnectionClient {
		public void onMediaScannerConnected() {
			if (null == mFile) {
				return;
			}
			scan(mFile);
		}

		public void onScanCompleted(String path, Uri uri) {
			mConn.disconnect();
			mAddOnScanCompleted.onCompleted();
		}

		/**
		 * 递归实现扫描文件夹或文件
		 *
		 * @param file 文件夹或文件
		 */
		private void scan(File file) {
			LogUtils.iTag(TAG, "scan " + file.getAbsolutePath());
			if (file.isFile()) {//为文件时，直接取绝对路径
				mConn.scanFile(file.getAbsolutePath(), null);
				return;
			}
			File[] files = file.listFiles();
			if (files == null) {
				return;
			}
			for (File f : file.listFiles()) {
				scan(f);
			}
		}
	}

	public void scanFile(File file) {
		mFile = file;
		mConn.connect();
	}

	public interface AddOnScanCompleted {
		void onCompleted();
	}
}
