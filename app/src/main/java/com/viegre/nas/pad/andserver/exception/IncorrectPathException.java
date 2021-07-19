package com.viegre.nas.pad.andserver.exception;

import com.blankj.utilcode.util.StringUtils;
import com.viegre.nas.pad.R;
import com.yanzhenjie.andserver.error.HttpException;
import com.yanzhenjie.andserver.http.StatusCode;

/**
 * Created by レインマン on 2021/07/16 17:43 with Android Studio.
 */
public class IncorrectPathException extends HttpException {

	public IncorrectPathException() {
		super(StatusCode.SC_FORBIDDEN, StringUtils.getString(R.string.andserver_incorrect_path));
	}

	public IncorrectPathException(Throwable cause) {
		super(StatusCode.SC_FORBIDDEN, StringUtils.getString(R.string.andserver_incorrect_path), cause);
	}
}
