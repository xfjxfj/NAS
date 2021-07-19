package com.viegre.nas.pad.andserver.exception;

import com.yanzhenjie.andserver.error.HttpException;
import com.yanzhenjie.andserver.http.StatusCode;

/**
 * Created by レインマン on 2021/07/16 17:43 with Android Studio.
 */
public class CustomException extends HttpException {

	public CustomException(String message) {
		super(StatusCode.SC_FORBIDDEN, message);
	}

	public CustomException(String message, Throwable cause) {
		super(StatusCode.SC_FORBIDDEN, message, cause);
	}
}
