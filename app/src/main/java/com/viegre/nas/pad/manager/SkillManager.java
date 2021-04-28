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
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.lzx.starrysky.SongInfo;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.utils.CommExtKt;
import com.topqizhi.ai.entity.skill.SkillEntity;
import com.topqizhi.ai.entity.skill.SkillMusicProSemanticEntity;
import com.topqizhi.ai.entity.skill.SkillSemanticArrayEntity;
import com.topqizhi.ai.entity.skill.SkillSemanticObjectEntity;
import com.topqizhi.ai.manager.AIUIManager;
import com.viegre.nas.pad.task.VoidTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import hdp.http.HdpConstant;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

/**
 * Created by レインマン on 2021/04/16 16:15 with Android Studio.
 */
public enum SkillManager {

	INSTANCE;

	private static final String TAG = SkillManager.class.getSimpleName();

	public static final String MUSIC_SERVER = "http://119.23.253.236:3000/";
	public static final String MUSIC_PLAY_URL = "http://music.163.com/song/media/outer/url?id=";

	public void parseSkillMsg(String message) {
		ThreadUtils.executeByCached(new VoidTask() {
			@Override
			public Void doInBackground() {
				Log.i(TAG, message);
				JSONObject json = JSON.parseObject(message);
				if (!json.containsKey("service")) {
					AIUIManager.INSTANCE.startListening();
					return null;
				}
				switch (json.getString("service")) {
					case SkillEntity.TVCHANNEL:
						AIUIManager.INSTANCE.setPlayNewMusicList(true);
						SkillSemanticArrayEntity tvchannelEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
						parseTvchannel(tvchannelEntity.getSemantic().get(0).getIntent(),
						               tvchannelEntity.getSemantic().get(0).getSlots().get(0).getValue());
						break;

					case SkillEntity.TV_SMART_HOME:
						AIUIManager.INSTANCE.setPlayNewMusicList(true);
						SkillSemanticObjectEntity skillSemanticObjectEntity = JSON.parseObject(message, SkillSemanticObjectEntity.class);
						parseTvSmartHome(skillSemanticObjectEntity.getOperation(),
						                 skillSemanticObjectEntity.getSemantic().getSlots().getAttr(),
						                 skillSemanticObjectEntity.getSemantic().getSlots().getAttrValue());
						break;

					case SkillEntity.VIDEO:
						AIUIManager.INSTANCE.setPlayNewMusicList(true);
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
								parseMusicPro(musicProEntity.getText(), JSONArray.parseArray(musicProEntity.getSemantic()).getJSONObject(0));
								return null;
							}
						});
						break;

					default:
						AIUIManager.INSTANCE.startListening();
						break;
				}
				return null;
			}
		});
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
							liveIntent.putExtra(HdpConstant.HIDE_LOADING_DEFAULT, true);
							liveIntent.putExtra(HdpConstant.HIDE_EXIT_DIAG, true);
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
					playIntent.putExtra(HdpConstant.HIDE_LOADING_DEFAULT, true);
					playIntent.putExtra(HdpConstant.HIDE_EXIT_DIAG, true);
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

	private void parseMusicPro(String anwser, JSONObject sematicJSON) {
		JSONArray slots = sematicJSON.getJSONArray("slots");
		String intent = sematicJSON.getString("intent");

		if ("INSTRUCTION".equals(intent)) {
			AIUIManager.INSTANCE.setPlayNewMusicList(true);
			AIUIManager.INSTANCE.startListening();
			return;
		}

		StringBuilder keywords = new StringBuilder();
		if (slots == null || slots.size() == 0) {
			keywords = new StringBuilder(anwser);
			if ("RANDOM_SEARCH".equals(intent)) {
				keywords = new StringBuilder("中文歌曲");
			}
		} else {
			for (int i = 0; i < slots.size(); i++) {
				String value = slots.getJSONObject(i).getString("value");
				keywords.append(value);
			}
		}

		//查询歌曲
		RxHttp.get(MUSIC_SERVER + "search").add("limit", 5).add("keywords", keywords.toString()).asString().subscribe(new Observer<String>() {
			@Override
			public void onSubscribe(@NonNull Disposable d) {}

			@Override
			public void onNext(@NonNull String s) {
				LogUtils.iTag("parseMusicPro", "查询歌曲");
				JSONObject resultObj = JSON.parseObject(s);
				List<JSONObject> musicObjList = JSONObject.parseArray(resultObj.getJSONObject("result").getJSONArray("songs").toJSONString(),
				                                                      JSONObject.class);
				//乱序排列
				Collections.shuffle(musicObjList);
				List<SongInfo> playList = new ArrayList<>();
				//遍历检查歌曲是否能播放
				for (JSONObject musicObj : musicObjList) {
					String id = musicObj.getString("id");
					String name = musicObj.getString("name");
					//同步请求
					RxHttp.get(MUSIC_SERVER + "check/music").add("id", id).setSync().asString().subscribe(new Observer<String>() {
						@Override
						public void onSubscribe(@NonNull Disposable d) {}

						@Override
						public void onNext(@NonNull String s) {
							LogUtils.iTag("parseMusicPro", "遍历检查歌曲是否能播放");
							JSONObject checkJson = JSON.parseObject(s);
							if (checkJson.getBoolean("success")) {
								SongInfo songInfo = new SongInfo();
								String url = MUSIC_PLAY_URL + id + ".mp3";
								songInfo.setSongId(CommExtKt.md5(url));
								songInfo.setSongName(name);
								songInfo.setSongUrl(url);
								playList.add(songInfo);
							}
						}

						@Override
						public void onError(@NonNull Throwable e) {
							e.printStackTrace();
							LogUtils.iTag("parseMusicPro", "遍历歌曲报错", e);
						}

						@Override
						public void onComplete() {
							LogUtils.iTag("parseMusicPro", "遍历完毕");
						}
					});
				}

				if (!playList.isEmpty()) {
					AIUIManager.INSTANCE.setPlayNewMusicList(true);
					StarrySky.with().playMusic(playList, 0);
				} else {
					AIUIManager.INSTANCE.setPlayNewMusicList(false);
				}
			}

			@Override
			public void onError(@NonNull Throwable e) {
				e.printStackTrace();
				AIUIManager.INSTANCE.setPlayNewMusicList(true);
				LogUtils.iTag("parseMusicPro", "查询歌曲报错", e);
			}

			@Override
			public void onComplete() {
				LogUtils.iTag("parseMusicPro", "查询完毕");
				AIUIManager.INSTANCE.startListening();
			}
		});
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
