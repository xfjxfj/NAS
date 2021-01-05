package com.viegre.nas.speaker.kalle.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.viegre.nas.speaker.entity.HttpEntity;
import com.yanzhenjie.kalle.Response;
import com.yanzhenjie.kalle.simple.Converter;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import java.lang.reflect.Type;

/**
 * Created by Djangoogle on 2021/01/04 17:38 with Android Studio.
 */
public class JsonConverter implements Converter {
	@Override
	public <S, F> SimpleResponse<S, F> convert(Type succeed, Type failed, Response response, boolean fromCache) throws Exception {
		S succeedData = null;//业务成功的数据。
		F failedData = null;//业务失败的数据。

		int code = response.code();
		String serverJson = response.body().string();
		if (code >= 200 && code < 300) {//Http请求成功。
			HttpEntity httpEntity;
			try {
				httpEntity = JSON.parseObject(serverJson, HttpEntity.class);
			} catch (Exception e) {
				httpEntity = new HttpEntity();
				httpEntity.setCode(0);
				httpEntity.setMsg("服务器数据格式异常");
			}

			if (httpEntity.getCode() == 1) {//服务端业务成功。
				try {
					if (succeed == Integer.class) {
						Integer succeedInt = Integer.parseInt(httpEntity.getData());
						succeedData = (S) succeedInt;
					} else if (succeed == Long.class) {
						Long succeedLong = Long.parseLong(httpEntity.getData());
						succeedData = (S) succeedLong;
					} else if (succeed == String.class) {
						succeedData = (S) httpEntity.getData();
					} else if (succeed == Boolean.class) {
						Boolean succeedBoolean = Boolean.parseBoolean(httpEntity.getData());
						succeedData = (S) succeedBoolean;
					} else if (succeed == JSONObject.class) {
						JSONObject object = JSONObject.parseObject(httpEntity.getData());
						succeedData = (S) object;
					} else {
						succeedData = JSON.parseObject(httpEntity.getData(), succeed);
					}
				} catch (Exception e) {
					failedData = (F) "服务器数据格式异常";
				}
			} else {
				//业务失败，获取服务端提示信息。
				failedData = (F) httpEntity.getMsg();
			}
		} else if (code >= 400 && code < 500) {//客户端请求不符合服务端要求。
			failedData = (F) "发生未知异常";
		} else if (code >= 500) { // 服务端发生异常。
			failedData = (F) "服务器开小差啦";
		}

		//包装成SimpleResponse返回。
		return SimpleResponse.<S, F>newBuilder().code(response.code())
		                                        .headers(response.headers())
		                                        .fromCache(fromCache)
		                                        .succeed(succeedData)
		                                        .failed(failedData)
		                                        .build();
	}
}
