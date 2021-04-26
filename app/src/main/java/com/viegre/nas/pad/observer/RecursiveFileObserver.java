package com.viegre.nas.pad.observer;

import android.os.FileObserver;
import android.util.ArrayMap;
import android.util.Log;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Created by レインマン on 2021/04/26 14:32 with Android Studio.
 */
public class RecursiveFileObserver extends FileObserver {

	Map<String, SingleFileObserver> mObservers;
	String mPath;
	int mMask;

	public RecursiveFileObserver(String path) {
		this(path, ALL_EVENTS);
	}

	public RecursiveFileObserver(String path, int mask) {
		super(path, mask);
		mPath = path;
		mMask = mask;
	}

	@Override
	public void startWatching() {
		if (null != mObservers) {
			return;
		}
		mObservers = new ArrayMap<>();
		Stack stack = new Stack();
		stack.push(mPath);

		while (!stack.isEmpty()) {
			String temp = (String) stack.pop();
			mObservers.put(temp, new SingleFileObserver(temp, mMask));
			File path = new File(temp);
			File[] files = path.listFiles();
			if (null == files) { continue; }
			for (File f : files) {
				// 递归监听目录
				if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
					stack.push(f.getAbsolutePath());
				}
			}
		}
		Iterator<String> iterator = mObservers.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			mObservers.get(key).startWatching();
		}
	}

	@Override
	public void stopWatching() {
		if (null == mObservers) {
			return;
		}

		Iterator<String> iterator = mObservers.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			mObservers.get(key).stopWatching();
		}
		mObservers.clear();
		mObservers = null;
	}

	@Override
	public void onEvent(int event, String path) {
		int el = event & FileObserver.ALL_EVENTS;
		switch (el) {
			case FileObserver.ATTRIB:
				Log.i("RecursiveFileObserver", "ATTRIB: " + path);
				break;
			case FileObserver.CREATE:
				File file = new File(path);
				if (file.isDirectory()) {
					Stack stack = new Stack();
					stack.push(path);
					while (!stack.isEmpty()) {
						String temp = (String) stack.pop();
						if (mObservers.containsKey(temp)) {
							continue;
						} else {
							SingleFileObserver sfo = new SingleFileObserver(temp, mMask);
							sfo.startWatching();
							mObservers.put(temp, sfo);
						}
						File tempPath = new File(temp);
						File[] files = tempPath.listFiles();
						if (null == files) { continue; }
						for (File f : files) {
							// 递归监听目录
							if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
								stack.push(f.getAbsolutePath());
							}
						}
					}
				}
				Log.i("RecursiveFileObserver", "CREATE: " + path);
				break;
			case FileObserver.DELETE:
				Log.i("RecursiveFileObserver", "DELETE: " + path);
				break;
			case FileObserver.DELETE_SELF:
				Log.i("RecursiveFileObserver", "DELETE_SELF: " + path);
				break;
			case FileObserver.MODIFY:
				Log.i("RecursiveFileObserver", "MODIFY: " + path);
				break;
			case FileObserver.MOVE_SELF:
				Log.i("RecursiveFileObserver", "MOVE_SELF: " + path);
				break;
			case FileObserver.MOVED_FROM:
				Log.i("RecursiveFileObserver", "MOVED_FROM: " + path);
				break;
			case FileObserver.MOVED_TO:
				Log.i("RecursiveFileObserver", "MOVED_TO: " + path);
				break;
		}
	}

	class SingleFileObserver extends FileObserver {
		String mPath;

		public SingleFileObserver(String path) {
			this(path, ALL_EVENTS);
			mPath = path;
		}

		public SingleFileObserver(String path, int mask) {
			super(path, mask);
			mPath = path;
		}

		@Override
		public void onEvent(int event, String path) {
			if (path != null) {
				String newPath = mPath + "/" + path;
				RecursiveFileObserver.this.onEvent(event, newPath);
			}
		}
	}
}
