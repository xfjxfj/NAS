package custom.fileobserver;

import android.util.Log;

import java.util.Hashtable;

/**
 * @author Dai Dongsheng
 * Email: doyee@163.com
 * FileWatcher support subdirectory(recursively)
 */
public class FileWatcher extends FileObserver {

	FileListener mFileListener;
	Hashtable<Integer, String> mRenameCookies = new Hashtable<>();

	public FileWatcher(String path) {
		this(path, ALL_EVENTS);
	}

	public FileWatcher(String path, int mask) {
		this(path, false, mask);
	}

	public FileWatcher(String path, boolean watchsubdir, int mask) {
		super(path, watchsubdir, mask);
	}

	public void setFileListener(FileListener fl) {
		mFileListener = fl;
	}

	@Override
	public void onEvent(int event, int cookie, String path) {
		switch (event) {
//			case ATTRIB:
//				Log.i("FileWatcher", "ATTRIB: " + path);
//				if (null != mFileListener) {
//					mFileListener.onFileModified(path);
//				}
//				break;

			case CLOSE_WRITE:
				Log.i("FileWatcher", "CLOSE_WRITE: " + path);
				if (null != mFileListener) {
					mFileListener.onFileModified(path);
				}
				break;

			case CREATE:
				Log.i("FileWatcher", "CREATE: " + path);
				if (null != mFileListener) {
					mFileListener.onFileCreated(path);
				}
				break;

			case DELETE:
				Log.i("FileWatcher", "DELETE: " + path);
				if (null != mFileListener) {
					mFileListener.onFileDeleted(path);
				}
				break;

			case DELETE_SELF:
				Log.i("FileWatcher", "DELETE_SELF: " + path);
				if (null != mFileListener) {
					mFileListener.onFileDeleted(path);
				}
				break;

			case MODIFY:
				Log.i("FileWatcher", "MODIFY: " + path);
				if (null != mFileListener) {
					mFileListener.onFileModified(path);
				}
				break;

			case MOVE_SELF:
				Log.i("FileWatcher", "MOVE_SELF: " + path);
				break;

			case MOVED_FROM:
				Log.i("FileWatcher", "MOVED_FROM: " + path);
				mRenameCookies.put(cookie, path);
				break;

			case MOVED_TO:
				Log.i("FileWatcher", "MOVED_TO: " + path);
				if (null != mFileListener) {
					String oldName = mRenameCookies.remove(cookie);
					mFileListener.onFileRenamed(oldName, path);
				}
				break;

			case OPEN:
				Log.i("FileWatcher", "OPEN: " + path);
				if (null != mFileListener) {
					mFileListener.onFileOpen(path);
				}
				break;

			default:
				switch (event - ISDIR) {
//					case ATTRIB:
//						Log.i("FileWatcher", "ATTRIB: " + path);
//						if (null != mFileListener) {
//							mFileListener.onFileModified(path);
//						}
//						break;

					case CLOSE_WRITE:
						Log.i("FileWatcher", "CLOSE_WRITE: " + path);
						if (null != mFileListener) {
							mFileListener.onFileModified(path);
						}
						break;

					case CREATE:
						Log.i("FileWatcher", "CREATE: " + path);
						if (null != mFileListener) {
							mFileListener.onFileCreated(path);
						}
						break;

					case DELETE:
						Log.i("FileWatcher", "DELETE: " + path);
						if (null != mFileListener) {
							mFileListener.onFileDeleted(path);
						}
						break;

					case DELETE_SELF:
						Log.i("FileWatcher", "DELETE_SELF: " + path);
						if (null != mFileListener) {
							mFileListener.onFileDeleted(path);
						}
						break;

					case MODIFY:
						Log.i("FileWatcher", "MODIFY: " + path);
						if (null != mFileListener) {
							mFileListener.onFileModified(path);
						}
						break;

					case MOVE_SELF:
						Log.i("FileWatcher", "MOVE_SELF: " + path);
						break;

					case MOVED_FROM:
						Log.i("FileWatcher", "MOVED_FROM: " + path);
						mRenameCookies.put(cookie, path);
						break;

					case MOVED_TO:
						Log.i("FileWatcher", "MOVED_TO: " + path);
						if (null != mFileListener) {
							String oldName = mRenameCookies.remove(cookie);
							mFileListener.onFileRenamed(oldName, path);
						}
						break;

					case OPEN:
						Log.i("FileWatcher", "OPEN: " + path);
						if (null != mFileListener) {
							mFileListener.onFileOpen(path);
						}
						break;

					default:
						break;
				}
				break;
		}
	}
}
