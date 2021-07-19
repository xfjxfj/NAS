package com.viegre.nas.pad.andserver.exception;

import com.blankj.utilcode.util.StringUtils;
import com.viegre.nas.pad.R;
import com.yanzhenjie.andserver.error.HttpException;
import com.yanzhenjie.andserver.http.StatusCode;

/**
 * Created by レインマン on 2021/07/16 17:43 with Android Studio.
 */
public class FileAlreadyExistsException extends HttpException {

	public FileAlreadyExistsException() {
		super(StatusCode.SC_FORBIDDEN, StringUtils.getString(R.string.andserver_file_already_exists));
	}

	public FileAlreadyExistsException(Throwable cause) {
		super(StatusCode.SC_FORBIDDEN, StringUtils.getString(R.string.andserver_file_already_exists), cause);
	}
}
