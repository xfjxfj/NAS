package org.primftpd.filepicker.nononsenseapps;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;

import androidx.annotation.Nullable;

@SuppressLint("Registered")
public class FilePickerActivity extends AbstractFilePickerActivity<File> {

	@Override
	protected AbstractFilePickerFragment<File> getFragment(@Nullable
	                                                       final String startPath, final int mode, final boolean allowMultiple, final boolean allowCreateDir, final boolean allowExistingFile, final boolean singleClick) {
		AbstractFilePickerFragment<File> fragment = new FilePickerFragment();
		// startPath is allowed to be null. In that case, default folder should be SD-card and not "/"
		fragment.setArgs(startPath != null ? startPath : Environment.getExternalStorageDirectory().getPath(),
		                 mode,
		                 allowMultiple,
		                 allowCreateDir,
		                 allowExistingFile,
		                 singleClick);
		return fragment;
	}
}
