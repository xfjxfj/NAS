package com.viegre.nas.pad.andserver.exception;

import com.blankj.utilcode.util.StringUtils;
import com.viegre.nas.pad.R;
import com.yanzhenjie.andserver.error.HttpException;
import com.yanzhenjie.andserver.http.StatusCode;

/**
 * Created by レインマン on 2021/07/16 17:43 with Android Studio.
 */
public class FileNotExistException extends HttpException {

	public FileNotExistException() {
		super(StatusCode.SC_FORBIDDEN, StringUtils.getString(R.string.andserver_file_does_not_exist));
	}

	public FileNotExistException(Throwable cause) {
		super(StatusCode.SC_FORBIDDEN, StringUtils.getString(R.string.andserver_file_does_not_exist), cause);
	}
}
