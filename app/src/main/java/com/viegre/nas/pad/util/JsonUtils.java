package com.viegre.nas.pad.util;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by レインマン on 2021/05/28 16:24 with Android Studio.
 */
public class JsonUtils {

	public static String succeedJson(String msg, Object data) {
		Map<String, Object> map = new HashMap<>();
		map.put("code", 0);
		map.put("msg", msg);
		map.put("data", data);
		return new JSONObject(map).toJSONString();
	}

	public static String failedJson(int code, String errMsg) {
		Map<String, Object> map = new HashMap<>();
		map.put("code", code);
		map.put("msg", errMsg);
		map.put("data", null);
		return new JSONObject(map).toJSONString();
	}
}
