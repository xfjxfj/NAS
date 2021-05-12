package com.viegre.nas.pad.util;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.CacheDoubleUtils;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;
import com.viegre.nas.pad.api.OkHttpHelper;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IotGateway {

	//	private static final String GATEWAY_BASE_URL = "http://" + NetWorkUtil.getHostIp() + ":8080/rest/";//"http://192.168.10.211:8080/rest/";
	private static final String GATEWAY_BASE_URL = "http://" + "192.168.10.211" + ":8080/rest/";//"http://192.168.10.211:8080/rest/";
	private static final String TAG = "IotGateway";
	private static final String GATEWAY_NAME = "Sub1g_Gateway";
	private static final String CACHE_KEY_MODELS = "iot_models";
	private static final String CACHE_KEY_DEVICES = "iot_devices";
	private static final String CACHE_KEY_AREAS = "iot_areas";

	public static boolean isOnline() {
		boolean isOnline = false;
		try {
			String url = GATEWAY_BASE_URL + "things";
			Request request = new Request.Builder().url(url).addHeader("Content-Type", "application/json; charset=utf-8").get().build();

			Response response = OkHttpHelper.getInstance().doRequest(request);
			if (response.isSuccessful()) {
				JSONArray thingsArray = JSON.parseArray(response.body().string());
				JSONArray deviceArray = new JSONArray();
				for (int i = 0; i < thingsArray.size(); i++) {
					JSONObject thing = thingsArray.getJSONObject(i);
					String thingType = thing.getString("thingTypeUID");
					if (thingType.equalsIgnoreCase("fanmis:bridge") && thing.getString("label").equals(GATEWAY_NAME)) {
						String status = thing.getJSONObject("statusInfo").getString("status");
						if (status.equalsIgnoreCase("ONLINE")) {
							isOnline = true;
						}
					}
					if (thingType.startsWith("fanmis:") && !thingType.startsWith("fanmis:gateway") && !thingType.startsWith("fanmis:bridge")) {
						deviceArray.add(thing);
					}
				}

				if (isOnline) { CacheDoubleUtils.getInstance().put(CACHE_KEY_DEVICES, deviceArray); }
			}
		} catch (Exception ex) {
			Log.e(TAG, "isOnline exception:" + ex.getMessage());
		}

		return isOnline;
	}

	public static JSONArray getAllDevice() {
		boolean isOnline = false;
		try {
			String url = GATEWAY_BASE_URL + "things";
			Request request = new Request.Builder().url(url).addHeader("Content-Type", "application/json; charset=utf-8").get().build();

			Response response = OkHttpHelper.getInstance().doRequest(request);
			if (response.isSuccessful()) {
				JSONArray thingsArray = JSON.parseArray(response.body().string());
				JSONArray deviceArray = new JSONArray();
				for (int i = 0; i < thingsArray.size(); i++) {
					JSONObject thing = thingsArray.getJSONObject(i);
					String thingType = thing.getString("thingTypeUID");
					if (thingType.equalsIgnoreCase("fanmis:bridge") && thing.getString("label").equals(GATEWAY_NAME)) {
						String status = thing.getJSONObject("statusInfo").getString("status");
						if (status.equalsIgnoreCase("ONLINE")) {
							isOnline = true;
						}
					}
					if (thingType.startsWith("fanmis:") && !thingType.startsWith("fanmis:gateway") && !thingType.startsWith("fanmis:bridge")) {
						deviceArray.add(thing);
					}
				}
				if (!deviceArray.isEmpty()) {
					CacheDoubleUtils.getInstance().put(CACHE_KEY_DEVICES, deviceArray);
				}

				if (isOnline) { return deviceArray; }
			}
		} catch (Exception ex) {
			Log.e(TAG, "isOnline exception:" + ex.getMessage());
		}

		return null;
	}

	//获取区域
	public static JSONArray getAllArea() {
		try {
			String url = GATEWAY_BASE_URL + "items?type=Group";
			Request request = new Request.Builder().url(url).addHeader("Content-Type", "application/json; charset=utf-8").get().build();

			Response response = OkHttpHelper.getInstance().doRequest(request);
			if (response.isSuccessful()) {
				JSONArray areaArray = JSON.parseArray(response.body().string());
				if (areaArray != null) {
					CacheDoubleUtils.getInstance().put(CACHE_KEY_AREAS, areaArray);
					return areaArray;
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "getAllArea exception:" + ex.getMessage());
		}

		return null;
	}

	//获取场景模式
	public static JSONArray getAllModel() {
		try {
			String url = GATEWAY_BASE_URL + "rules";
			Request request = new Request.Builder().url(url).addHeader("Content-Type", "application/json; charset=utf-8").get().build();

			Response response = OkHttpHelper.getInstance().doRequest(request);
			if (response.isSuccessful()) {
				JSONArray modelArray = JSON.parseArray(response.body().string());
				if (modelArray != null) {
					CacheDoubleUtils.getInstance().put(CACHE_KEY_MODELS, modelArray);
					return modelArray;
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "getAllModel exception:" + ex.getMessage());
		}

		return null;
	}

	public static boolean executeModel(String modelName) {
		if (TextUtils.isEmpty(modelName)) { return false; }
		//判断网关是否在线
		//查找模式是否存在，取UID
		String uid = null;
		Object modelArrObj = CacheDoubleUtils.getInstance().getSerializable(CACHE_KEY_MODELS);
		JSONArray modelArr = null;
		if (modelArrObj != null) {
			modelArr = (JSONArray) modelArrObj;
			//Cache中查找
			if (modelArr == null || modelArr.size() == 0) { return false; }
			for (int i = 0; i < modelArr.size(); i++) {
				JSONObject model = modelArr.getJSONObject(i);
				if (modelName.equalsIgnoreCase(model.getString("name"))) {
					uid = model.getString("uid");
					break;
				}
			}
		}
		//如果没找到，则请求最新的，重新查找一次
		if (uid == null) {
			modelArr = getAllModel();
			CacheDoubleUtils.getInstance().put(CACHE_KEY_MODELS, modelArr);

			if (modelArr == null || modelArr.size() == 0) { return false; }
			for (int i = 0; i < modelArr.size(); i++) {
				JSONObject model = modelArr.getJSONObject(i);
				if (modelName.equalsIgnoreCase(model.getString("name"))) {
					uid = model.getString("uid");
					break;
				}
			}
		}

		if (uid == null) { return false; }
		//执行模式
		try {
			String url = GATEWAY_BASE_URL + "rules/" + uid + "/runnow";
			RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), "");
			Request request = new Request.Builder().url(url).addHeader("Content-Type", "text/plain; charset=utf-8").post(requestBody).build();
			Response response = OkHttpHelper.getInstance().doRequest(request);
			if (response.isSuccessful()) {
				return true;
			}
		} catch (Exception ex) {
			Log.e(TAG, "executeModel exception:" + ex.getMessage());
		}
		return false;
	}

	public static boolean executeDevice(String deviceName, String area, String status) {
		if (TextUtils.isEmpty(deviceName)) { return false; }
		//判断area是否存在，找出area中的所有设备
		List<String> areaDeviceUids = null;
		if (!TextUtils.isEmpty(area)) {
			Object areaArrObj = CacheDoubleUtils.getInstance().getSerializable(CACHE_KEY_AREAS);
			JSONArray areaArr = null;
			if (areaArrObj != null) {
				areaArr = (JSONArray) areaArrObj;
				//Cache中查找
				if (areaArr != null && areaArr.size() > 0) {
					for (int i = 0; i < areaArr.size(); i++) {
						JSONObject areaObj = areaArr.getJSONObject(i);
						if (areaObj.getString("label").equalsIgnoreCase(area)) {
							JSONArray groupNames = areaObj.getJSONArray("groupNames");
							if (groupNames != null && groupNames.size() > 0) {
								areaDeviceUids = groupNames.toJavaList(String.class);
								break;
							}
						}
					}
				}
			}

			//如果没找到，则请求最新的，重新查找一次
			if (areaDeviceUids == null) {
				areaArr = getAllArea();
				CacheDoubleUtils.getInstance().put(CACHE_KEY_AREAS, areaArr);

				if (areaArr != null && areaArr.size() > 0) {
					for (int i = 0; i < areaArr.size(); i++) {
						JSONObject areaObj = areaArr.getJSONObject(i);
						if (areaObj.getString("label").equalsIgnoreCase(area)) {
							JSONArray groupNames = areaObj.getJSONArray("groupNames");
							if (groupNames != null && groupNames.size() > 0) {
								areaDeviceUids = groupNames.toJavaList(String.class);
								break;
							}
						}
					}
				}
			}

			if (areaDeviceUids == null) { return false; }
		}

		//查找设备是否存在，取itemName
		String itemName = null;
		Object modelArrObj = CacheDoubleUtils.getInstance().getSerializable(CACHE_KEY_DEVICES);
		JSONArray deviceArr = null;
		if (modelArrObj != null) {
			deviceArr = (JSONArray) modelArrObj;
			//Cache中查找
			if (deviceArr == null || deviceArr.size() == 0) { return false; }
			for (int i = 0; i < deviceArr.size(); i++) {
				JSONObject device = deviceArr.getJSONObject(i);
				if (deviceName.equalsIgnoreCase(device.getString("label")) && (areaDeviceUids == null || areaDeviceUids.contains(device.getString(
						"UID")))) {
					JSONArray channels = device.getJSONArray("channels");
					for (int j = 0; j < channels.size(); j++) {
						JSONObject channel = channels.getJSONObject(j);
						if (channel.getString("itemType").equalsIgnoreCase("Switch")) {
							itemName = channel.getJSONArray("linkedItems").getString(0);
							break;
						}
					}
					break;
				}
			}
		}

		if (itemName == null) {
			deviceArr = getAllDevice();
			CacheDoubleUtils.getInstance().put(CACHE_KEY_DEVICES, deviceArr);
			//如果没找到，则请求最新的，重新查找一次
			if (deviceArr != null && deviceArr.size() >= 0) {
				for (int i = 0; i < deviceArr.size(); i++) {
					JSONObject device = deviceArr.getJSONObject(i);
					if (deviceName.equalsIgnoreCase(device.getString("label")) && (areaDeviceUids == null || areaDeviceUids.contains(device.getString(
							"UID")))) {
						JSONArray channels = device.getJSONArray("channels");
						for (int j = 0; j < channels.size(); j++) {
							JSONObject channel = channels.getJSONObject(j);
							if (channel.getString("itemType").equalsIgnoreCase("Switch")) {
								itemName = channel.getJSONArray("linkedItems").getString(0);
								break;
							}
						}
						break;
					}
				}
			}
		}

		if (itemName == null) {
			return false;
		}

		//执行设备操作
		try {
			String url = GATEWAY_BASE_URL + "items/" + itemName;
			RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), status);
			Request request = new Request.Builder().url(url).addHeader("Content-Type", "text/plain; charset=utf-8").post(requestBody).build();
			Response response = OkHttpHelper.getInstance().doRequest(request);
			if (response.isSuccessful()) {
				return true;
			}
		} catch (Exception ex) {
			Log.e(TAG, "executeModel exception:" + ex.getMessage());
		}
		return false;
	}

	public static void uploadAreaEntity(AIUIAgent mAIUIAgent) throws UnsupportedEncodingException {
		Object areaArrObj = CacheDoubleUtils.getInstance().getSerializable(CACHE_KEY_AREAS);
		JSONArray areaArr = null;
		StringBuilder sb = new StringBuilder();
		if (areaArrObj != null) {
			areaArr = (JSONArray) areaArrObj;
			//Cache中查找
			if (areaArr != null && areaArr.size() > 0) {
				for (int i = 0; i < areaArr.size(); i++) {
					JSONObject areaObj = areaArr.getJSONObject(i);
					sb.append("{\"name\":\"" + areaObj.getString("label") + "\",\"alias\":\"\",\"extra\":\"\"}\r\n");
				}
			}
		}

		if (areaArr == null) { return; }

		JSONObject syncSchemaJson = new JSONObject();
		JSONObject paramJson = new JSONObject();

		paramJson.put("id_name", "uid");
		paramJson.put("res_name", "TOPQIZHI.region_user");

		syncSchemaJson.put("param", paramJson);
		syncSchemaJson.put("data", Base64.encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8), Base64.DEFAULT | Base64.NO_WRAP));

		byte[] syncData = syncSchemaJson.toString().getBytes(StandardCharsets.UTF_8);

		AIUIMessage syncAthenaMessage = new AIUIMessage(AIUIConstant.CMD_SYNC, AIUIConstant.SYNC_DATA_SCHEMA, 0, "", syncData);
		mAIUIAgent.sendMessage(syncAthenaMessage);
		Log.e(TAG, "上传区域名称实体:" + sb.toString());
	}

	public static void uploadDeviceEntity(AIUIAgent mAIUIAgent) throws UnsupportedEncodingException {
		//如果没找到，则请求最新的，重新查找一次
		Object deviceArrObj = CacheDoubleUtils.getInstance().getSerializable(CACHE_KEY_DEVICES);
		JSONArray deviceArr = null;
		StringBuilder sb = new StringBuilder();
		if (null != deviceArrObj) {
			deviceArr = (JSONArray) deviceArrObj;
		}
		if (null == deviceArr) {
			return;
		}

		for (int i = 0; i < deviceArr.size(); i++) {
			JSONObject device = deviceArr.getJSONObject(i);
			sb.append("{\"name\":\"" + device.getString("label") + "\",\"alias\":\"\",\"extra\":\"\"}\r\n");
		}

		JSONObject syncSchemaJson = new JSONObject();
		JSONObject paramJson = new JSONObject();

		paramJson.put("id_name", "uid");
		paramJson.put("res_name", "TOPQIZHI.device_user");

		syncSchemaJson.put("param", paramJson);
		syncSchemaJson.put("data", Base64.encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8), Base64.DEFAULT | Base64.NO_WRAP));

		byte[] syncData = syncSchemaJson.toString().getBytes(StandardCharsets.UTF_8);

		AIUIMessage syncAthenaMessage = new AIUIMessage(AIUIConstant.CMD_SYNC, AIUIConstant.SYNC_DATA_SCHEMA, 0, "", syncData);
		mAIUIAgent.sendMessage(syncAthenaMessage);
		Log.e(TAG, "上传设备名称实体:" + sb.toString());
	}
}
