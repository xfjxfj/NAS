package com.viegre.nas.pad.manager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.topqizhi.ai.entity.skill.SkillEntity;
import com.topqizhi.ai.entity.skill.SkillMusicProSemanticEntity;
import com.topqizhi.ai.entity.skill.SkillSemanticArrayEntity;
import com.topqizhi.ai.entity.skill.SkillSemanticObjectEntity;
import com.topqizhi.ai.manager.AIUIManager;
import com.viegre.nas.pad.api.Api;
import com.viegre.nas.pad.task.VoidTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import hdp.http.APIConstant;

/**
 * Created by レインマン on 2021/04/16 16:15 with Android Studio.
 */
public enum SkillManager {

	INSTANCE;

	private static final String TAG = SkillManager.class.getSimpleName();

	public void parseSkillMsg(String message) {
		Log.i(TAG, message);
		JSONObject json = JSON.parseObject(message);
		if (!json.containsKey("service")) {
			AIUIManager.INSTANCE.startListening();
			return;
		}
		json.getString("service");
		switch (json.getString("service")) {
			case SkillEntity.TVCHANNEL:
				SkillSemanticArrayEntity tvchannelEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
				parseTvchannel(tvchannelEntity.getSemantic().get(0).getIntent(), tvchannelEntity.getSemantic().get(0).getSlots().get(0).getValue());
				break;

			case SkillEntity.TV_SMART_HOME:
				SkillSemanticObjectEntity skillSemanticObjectEntity = JSON.parseObject(message, SkillSemanticObjectEntity.class);
				parseTvSmartHome(skillSemanticObjectEntity.getOperation(),
				                 skillSemanticObjectEntity.getSemantic().getSlots().getAttr(),
				                 skillSemanticObjectEntity.getSemantic().getSlots().getAttrValue());
				break;

			case SkillEntity.VIDEO:
				SkillSemanticArrayEntity videoEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
				parseVideo(videoEntity.getSemantic().get(0).getIntent(), videoEntity.getSemantic().get(0).getSlots().get(0).getValue());
				break;

			case SkillEntity.APP:
				SkillSemanticArrayEntity appEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
				parseApp(appEntity.getSemantic().get(0).getIntent(), appEntity.getSemantic().get(0).getSlots().get(0).getValue());
				break;

			case SkillEntity.MUSIC_PRO:
				SkillMusicProSemanticEntity musicProEntity = JSON.parseObject(message, SkillMusicProSemanticEntity.class);
				ThreadUtils.executeByCached(new VoidTask() {
					@Override
					public Void doInBackground() {
						Api.queryMusic(musicProEntity.getText(), JSONArray.parseArray(musicProEntity.getSemantic()).getJSONObject(0));
						return null;
					}
				});
//				parseMusicPro(musicProEntity.getSemantic().get(0).getIntent(), musicProEntity.getSemantic().get(0).getSlots().get(0).getValue());
				break;

			default:
				AIUIManager.INSTANCE.startListening();
				break;
		}
	}

	private void parseTvchannel(String intent, String value) {
		Log.i(TAG, "intent = " + intent + ", " + "value = " + value);
		switch (intent) {
			case "INSTRUCTION":
				switch (value) {
					//打开电视
					case "live":
						ThreadUtils.runOnUiThread(() -> {
							Intent liveIntent = new Intent();
							liveIntent.putExtra(APIConstant.HIDE_LOADING_DEFAULT, true);
							liveIntent.putExtra(APIConstant.HIDE_EXIT_DIAG, true);
							liveIntent.setAction("com.hdpfans.live.start");
							liveIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							liveIntent.putExtra("ChannelNum", 1);
							ActivityUtils.startActivity(liveIntent);
							AIUIManager.INSTANCE.startListening();
						});
						break;

					default:
						AIUIManager.INSTANCE.startListening();
						break;
				}
				break;

			case "PLAY":
				String tvchannelPlayId = "1";
				if (value.contains("综合频道")) {
					tvchannelPlayId = "1";
				} else {
					String[] tvchannelPlayValueArr = value.split("`");
					for (String tvchannelPlayValue : tvchannelPlayValueArr) {
						try {
							String playStr = parseLiveList();
							int pos = playStr.indexOf("\"" + tvchannelPlayValue + "\"");
							playStr = playStr.substring(pos);
							pos = playStr.indexOf("频道号");
							playStr = playStr.substring(pos + 6);
							int pos1 = playStr.indexOf("\"");
							tvchannelPlayId = playStr.substring(0, pos1);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				String finalTvchannelPlayId = tvchannelPlayId;
				ThreadUtils.runOnUiThread(() -> {
					Intent playIntent = new Intent();
					playIntent.putExtra(APIConstant.HIDE_LOADING_DEFAULT, true);
					playIntent.putExtra(APIConstant.HIDE_EXIT_DIAG, true);
					playIntent.setAction("com.hdpfans.live.start");
					playIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					playIntent.putExtra("ChannelNum", Integer.parseInt(finalTvchannelPlayId));
					ActivityUtils.startActivity(playIntent);
					AIUIManager.INSTANCE.startListening();
				});
				break;

			default:
				AIUIManager.INSTANCE.startListening();
				break;
		}
	}

	private void parseTvSmartHome(String operation, String attr, String attrValue) {
		Log.i(TAG, "operation = " + operation + ", " + "attr = " + attr + ", " + "attrValue = " + attrValue);
		switch (operation) {
			case "SET":
				switch (attr) {
					case "开关":
						switch (attrValue) {
							case "关":
								ThreadUtils.runOnUiThread(() -> {
									//获取ActivityManager
									ActivityManager mAm = (ActivityManager) Utils.getApp().getSystemService(Context.ACTIVITY_SERVICE);
									// 获得当前运行的task
									List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(100);
									for (ActivityManager.RunningTaskInfo rti : taskList) {
										//找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台
										if (rti.topActivity.getPackageName().equals(Utils.getApp().getPackageName())) {
											//判断app进程是否存活
											Log.i("NotificationReceiver", "the app process is alive");
											try {
												Intent resultIntent = new Intent(Utils.getApp(), Class.forName(rti.topActivity.getClassName()));
												resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
												Utils.getApp().startActivity(resultIntent);
											} catch (ClassNotFoundException e) {
												e.printStackTrace();
											}
											AIUIManager.INSTANCE.startListening();
											return;
										}
									}
									//若没有找到运行的task，用户结束了task或被系统释放，则重新启动mainactivity
									ActivityUtils.startLauncherActivity(Utils.getApp().getPackageName());
									AIUIManager.INSTANCE.startListening();
								});
								break;

							default:
								AIUIManager.INSTANCE.startListening();
								break;
						}
						break;

					default:
						AIUIManager.INSTANCE.startListening();
						break;
				}
				break;

			default:
				AIUIManager.INSTANCE.startListening();
				break;
		}
	}

	private void parseVideo(String intent, String value) {
		Log.i(TAG, "intent = " + intent + ", " + "value = " + value);
		switch (intent) {
			case "QUERY":
				ThreadUtils.runOnUiThread(() -> {
					Intent searchIntent = new Intent("myvst.intent.action.SearchActivity");
					searchIntent.putExtra("search_word", value);
					searchIntent.putExtra("check_back_home", false);
					ActivityUtils.startActivity(searchIntent);
					AIUIManager.INSTANCE.startListening();
				});
				break;

			default:
				AIUIManager.INSTANCE.startListening();
				break;
		}
	}

	private void parseApp(String intent, String value) {
		Log.i(TAG, "intent = " + intent + ", " + "value = " + value);
		AIUIManager.INSTANCE.startListening();
	}

	private void parseMusicPro(String intent, String value) {
		Log.i(TAG, "intent = " + intent + ", " + "value = " + value);
		AIUIManager.INSTANCE.startListening();
	}

	private String parseLiveList() {
		AssetManager assetManager = Utils.getApp().getAssets();
		String jsonString = "";
		try {
			InputStream inputStream = assetManager.open("hdp_channel_list.xml");
			XmlToJson xmlToJson = new XmlToJson.Builder(inputStream, null).build();
			jsonString = xmlToJson.toString();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}
}
