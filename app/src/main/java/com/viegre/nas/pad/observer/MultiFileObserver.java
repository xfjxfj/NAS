package com.viegre.nas.pad.observer;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by レインマン on 2021/04/26 14:16 with Android Studio.
 */
public class MultiFileObserver extends FileObserver {

	public static int CHANGES_ONLY = CREATE | MODIFY | DELETE | CLOSE_WRITE | DELETE_SELF | MOVE_SELF | MOVED_FROM | MOVED_TO;

	private List<SingleFileObserver> mObservers;
	private final String mPath;
	private int mMask = ALL_EVENTS;

	public MultiFileObserver(String path) {
		super(path, ALL_EVENTS);
		mPath = path;
	}

	public MultiFileObserver(String path, int mask) {
		super(path, mask);
		mPath = path;
		mMask = mask;
	}

	@Override
	public void startWatching() {
		if (mObservers != null) { return; }

		mObservers = new ArrayList<>();
		Stack<String> stack = new Stack<>();
		stack.push(mPath);

		while (!stack.isEmpty()) {
			String parent = stack.pop();
			mObservers.add(new SingleFileObserver(parent, mMask));
			File path = new File(parent);
			File[] files = path.listFiles();
			if (null == files) { continue; }
			for (File f : files) {
				if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
					stack.push(f.getPath());
				}
			}
		}

		for (int i = 0; i < mObservers.size(); i++) {
			SingleFileObserver sfo = mObservers.get(i);
			sfo.startWatching();
		}
	}

	@Override
	public void stopWatching() {
		if (mObservers == null) { return; }

		for (int i = 0; i < mObservers.size(); i++) {
			SingleFileObserver sfo = mObservers.get(i);
			sfo.stopWatching();
		}

		mObservers.clear();
		mObservers = null;
	}

	@Override
	public void onEvent(int event, String path) {
		switch (event) {
			case FileObserver.ACCESS:
				Log.i("MultiFileObserver", "ACCESS: " + path);
				break;
			case FileObserver.ATTRIB:
				Log.i("MultiFileObserver", "ATTRIB: " + path);
				break;
			case FileObserver.CLOSE_NOWRITE:
				Log.i("MultiFileObserver", "CLOSE_NOWRITE: " + path);
				break;
			case FileObserver.CLOSE_WRITE:
				Log.i("MultiFileObserver", "CLOSE_WRITE: " + path);
				break;
			case FileObserver.CREATE:
				Log.i("MultiFileObserver", "CREATE: " + path);
				break;
			case FileObserver.DELETE:
				Log.i("MultiFileObserver", "DELETE: " + path);
				break;
			case FileObserver.DELETE_SELF:
				Log.i("MultiFileObserver", "DELETE_SELF: " + path);
				break;
			case FileObserver.MODIFY:
				Log.i("MultiFileObserver", "MODIFY: " + path);
				break;
			case FileObserver.MOVE_SELF:
				Log.i("MultiFileObserver", "MOVE_SELF: " + path);
				break;
			case FileObserver.MOVED_FROM:
				Log.i("MultiFileObserver", "MOVED_FROM: " + path);
				break;
			case FileObserver.MOVED_TO:
				Log.i("MultiFileObserver", "MOVED_TO: " + path);
				break;
			case FileObserver.OPEN:
				Log.i("MultiFileObserver", "OPEN: " + path);
				break;
			default:
				Log.i("MultiFileObserver", "DEFAULT(" + event + " : " + path);
				break;
		}
	}

	/**
	 * Monitor single directory and dispatch all events to its parent, with full
	 * path.
	 */
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
			String newPath = mPath + "/" + path;
			MultiFileObserver.this.onEvent(event, newPath);
		}
	}
}
