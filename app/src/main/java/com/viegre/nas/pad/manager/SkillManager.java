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
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.lzx.starrysky.SongInfo;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.utils.CommExtKt;
import com.topqizhi.ai.entity.skill.SkillDataResultEntity;
import com.topqizhi.ai.entity.skill.SkillEntity;
import com.topqizhi.ai.entity.skill.SkillMusicProSemanticEntity;
import com.topqizhi.ai.entity.skill.SkillSemanticArrayEntity;
import com.topqizhi.ai.entity.skill.SkillSemanticObjectEntity;
import com.topqizhi.ai.manager.AIUIManager;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.TvChannelInfoEntity;
import com.viegre.nas.pad.entity.TvChannelInfoEntityClassEntity;
import com.viegre.nas.pad.task.VoidTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import hdp.http.APIConstant;
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

	private final Set<TvChannelInfoEntity> mTvChannelInfoSet = new HashSet<>();
	private volatile boolean mIsQueryMusicTtsPlayEnd = true;

	public void parseSkillMsg(String message) {
		ThreadUtils.executeByCached(new VoidTask() {
			@Override
			public Void doInBackground() {
				Log.i(TAG, message);
				JSONObject json = JSON.parseObject(message);
				if (!json.containsKey("service")) {
					AIUIManager.INSTANCE.startTTS("对不起，我没明白你的意思，请再说一遍。", AIUIManager.INSTANCE::startListening);
					return null;
				}
				switch (json.getString("service")) {
					case SkillEntity.TVCHANNEL:
						AIUIManager.INSTANCE.setPlayNewMusicList(true);
						parseTvchannel(JSON.parseObject(message, SkillSemanticArrayEntity.class));
						break;

					case SkillEntity.TV_SMART_HOME:
						AIUIManager.INSTANCE.setPlayNewMusicList(true);
						parseTvSmartHome(JSON.parseObject(message, SkillSemanticObjectEntity.class));
						break;

					case SkillEntity.VIDEO:
						AIUIManager.INSTANCE.setPlayNewMusicList(true);
						parseVideo(JSON.parseObject(message, SkillSemanticArrayEntity.class));
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
								parseMusicPro(musicProEntity);
								return null;
							}
						});
						break;

					case SkillEntity.JOKE:
						AIUIManager.INSTANCE.setPlayNewMusicList(true);
						SkillSemanticArrayEntity jokeEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
						parseJoke(jokeEntity.getAnswer().getText());
						break;

					case SkillEntity.WEATHER:
						AIUIManager.INSTANCE.setPlayNewMusicList(true);
						SkillSemanticArrayEntity weatherEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
						parseWeather(weatherEntity.getAnswer().getText());
						break;

					case SkillEntity.STORY:
						AIUIManager.INSTANCE.setPlayNewMusicList(true);
						SkillSemanticArrayEntity storyEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
						parseStory(storyEntity.getAnswer().getText(), storyEntity.getData().getResult());
						break;

					default:
						AIUIManager.INSTANCE.startTTS("对不起，我没明白你的意思，请再说一遍。", AIUIManager.INSTANCE::startListening);
						break;
				}
				return null;
			}
		});
	}

	private void parseTvchannel(SkillSemanticArrayEntity tvchannelEntity) {
		switch (tvchannelEntity.getSemantic().get(0).getIntent()) {
			case "INSTRUCTION":
				switch (tvchannelEntity.getSemantic().get(0).getSlots().get(0).getValue()) {
					//打开电视
					case "live":
						AIUIManager.INSTANCE.startTTS(StringUtils.getString(R.string.initial_response), () -> ThreadUtils.runOnUiThread(() -> {
							Intent liveIntent = new Intent();
							liveIntent.putExtra(APIConstant.HIDE_LOADING_DEFAULT, true);
							liveIntent.putExtra(APIConstant.HIDE_EXIT_DIAG, true);
							liveIntent.setAction("com.hdpfans.live.start");
							liveIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							liveIntent.putExtra("ChannelNum", 1);
							ActivityUtils.startActivity(liveIntent);
							AIUIManager.INSTANCE.startListening();
						}));
						break;

					default:
						AIUIManager.INSTANCE.startTTS(tvchannelEntity.getText(), AIUIManager.INSTANCE::startListening);
						break;
				}
				break;

			case "PLAY":
				String tvchannelPlayId = "1";
				if (tvchannelEntity.getSemantic().get(0).getSlots().get(0).getValue().contains("综合频道")) {
					tvchannelPlayId = "1";
				} else {
					if (mTvChannelInfoSet.isEmpty()) {
						parseChannelList();
					}
					String[] tvchannelPlayValueArr = tvchannelEntity.getSemantic().get(0).getSlots().get(0).getValue().split("`");
					TvchannelPlayValue:
					for (String tvchannelPlayValue : tvchannelPlayValueArr) {
						for (TvChannelInfoEntity tvChannelInfoEntity : mTvChannelInfoSet) {
							if (tvchannelPlayValue.equals(tvChannelInfoEntity.getChannelName())) {
								tvchannelPlayId = tvChannelInfoEntity.getChannelId();
								break TvchannelPlayValue;
							}
						}
					}
				}
				String finalTvchannelPlayId = tvchannelPlayId;
				AIUIManager.INSTANCE.startTTS(StringUtils.getString(R.string.initial_response), () -> ThreadUtils.runOnUiThread(() -> {
					Intent playIntent = new Intent();
					playIntent.putExtra(APIConstant.HIDE_LOADING_DEFAULT, true);
					playIntent.putExtra(APIConstant.HIDE_EXIT_DIAG, true);
					playIntent.setAction("com.hdpfans.live.start");
					playIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					playIntent.putExtra("ChannelNum", Integer.parseInt(finalTvchannelPlayId));
					ActivityUtils.startActivity(playIntent);
					AIUIManager.INSTANCE.startListening();
				}));
				break;

			default:
				AIUIManager.INSTANCE.startTTS(tvchannelEntity.getText(), AIUIManager.INSTANCE::startListening);
				break;
		}
	}

	private void parseTvSmartHome(SkillSemanticObjectEntity skillSemanticObjectEntity) {
		switch (skillSemanticObjectEntity.getOperation()) {
			case "SET":
				switch (skillSemanticObjectEntity.getSemantic().getSlots().getAttr()) {
					case "开关":
						switch (skillSemanticObjectEntity.getSemantic().getSlots().getAttrValue()) {
							case "关":
								AIUIManager.INSTANCE.startTTS(StringUtils.getString(R.string.initial_response),
								                              () -> ThreadUtils.runOnUiThread(() -> {
									                              //获取ActivityManager
									                              ActivityManager mAm = (ActivityManager) Utils.getApp()
									                                                                           .getSystemService(Context.ACTIVITY_SERVICE);
									                              // 获得当前运行的task
									                              List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(100);
									                              for (ActivityManager.RunningTaskInfo rti : taskList) {
										                              //找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台
										                              if (rti.topActivity.getPackageName().equals(Utils.getApp().getPackageName())) {
											                              //判断app进程是否存活
											                              Log.i("NotificationReceiver", "the app process is alive");
											                              try {
												                              Intent resultIntent = new Intent(Utils.getApp(),
												                                                               Class.forName(rti.topActivity.getClassName()));
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
								                              }));
								break;

							default:
								AIUIManager.INSTANCE.startTTS(skillSemanticObjectEntity.getText(), AIUIManager.INSTANCE::startListening);
								break;
						}
						break;

					default:
						AIUIManager.INSTANCE.startTTS(skillSemanticObjectEntity.getText(), AIUIManager.INSTANCE::startListening);
						break;
				}
				break;

			default:
				AIUIManager.INSTANCE.startTTS(skillSemanticObjectEntity.getText(), AIUIManager.INSTANCE::startListening);
				break;
		}
	}

	private void parseVideo(SkillSemanticArrayEntity videoEntity) {
		switch (videoEntity.getSemantic().get(0).getIntent()) {
			case "QUERY":
				AIUIManager.INSTANCE.startTTS("以下是" + videoEntity.getText() + "的搜索结果。", () -> ThreadUtils.runOnUiThread(() -> {
					Intent searchIntent = new Intent("myvst.intent.action.SearchActivity");
					searchIntent.putExtra("search_word", videoEntity.getSemantic().get(0).getSlots().get(0).getValue());
					searchIntent.putExtra("check_back_home", false);
					ActivityUtils.startActivity(searchIntent);
					AIUIManager.INSTANCE.startListening();
				}));
				break;

			default:
				AIUIManager.INSTANCE.startTTS(videoEntity.getText(), AIUIManager.INSTANCE::startListening);
				break;
		}
	}

	private void parseApp(String intent, String value) {
		Log.i(TAG, "intent = " + intent + ", " + "value = " + value);
		AIUIManager.INSTANCE.startListening();
	}

	private void parseMusicPro(SkillMusicProSemanticEntity musicProEntity) {
		JSONObject sematicJSON = JSONArray.parseArray(musicProEntity.getSemantic()).getJSONObject(0);
		JSONArray slots = sematicJSON.getJSONArray("slots");
		String intent = sematicJSON.getString("intent");

		if ("INSTRUCTION".equals(intent)) {
			AIUIManager.INSTANCE.setPlayNewMusicList(true);
			AIUIManager.INSTANCE.startTTS(musicProEntity.getText(), AIUIManager.INSTANCE::startListening);
			return;
		}

		StringBuilder keywords = new StringBuilder();
		if (slots == null || slots.size() == 0) {
			keywords = new StringBuilder(musicProEntity.getText());
			if ("RANDOM_SEARCH".equals(intent)) {
				keywords = new StringBuilder("中文歌曲");
			}
		} else {
			for (int i = 0; i < slots.size(); i++) {
				String value = slots.getJSONObject(i).getString("value");
				keywords.append(value);
			}
		}

		mIsQueryMusicTtsPlayEnd = false;
		AIUIManager.INSTANCE.startTTS("正在查询，请稍候。", () -> mIsQueryMusicTtsPlayEnd = true);
		//查询歌曲
		RxHttp.get(MUSIC_SERVER + "search")
		      .setAssemblyEnabled(false)
		      .add("limit", 5)
		      .add("keywords", keywords.toString())
		      .asString()
		      .subscribe(new Observer<String>() {
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
					      RxHttp.get(MUSIC_SERVER + "check/music")
					            .setAssemblyEnabled(false)
					            .add("id", id)
					            .setSync()
					            .asString()
					            .subscribe(new Observer<String>() {
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
					      if (mIsQueryMusicTtsPlayEnd) {
						      AIUIManager.INSTANCE.startTTS("即将为您播放" + playList.get(0).getSongName(),
						                                    () -> StarrySky.with().playMusic(playList, 0),
						                                    200L);
					      } else {
						      while (true) {
							      if (mIsQueryMusicTtsPlayEnd) {
								      AIUIManager.INSTANCE.startTTS("即将为您播放" + playList.get(0).getSongName(),
								                                    () -> StarrySky.with().playMusic(playList, 0),
								                                    200L);
								      break;
							      }
						      }
					      }
				      } else {
					      AIUIManager.INSTANCE.setPlayNewMusicList(false);
					      if (mIsQueryMusicTtsPlayEnd) {
						      AIUIManager.INSTANCE.startTTS("对不起，没有查询到歌曲，请再说一遍。", null, 200L);
					      } else {
						      while (true) {
							      if (mIsQueryMusicTtsPlayEnd) {
								      AIUIManager.INSTANCE.startTTS("对不起，没有查询到歌曲，请再说一遍。", null, 200L);
								      break;
							      }
						      }
					      }
				      }
			      }

			      @Override
			      public void onError(@NonNull Throwable e) {
				      e.printStackTrace();
				      AIUIManager.INSTANCE.setPlayNewMusicList(true);
				      LogUtils.iTag("parseMusicPro", "查询歌曲报错", e);
				      if (mIsQueryMusicTtsPlayEnd) {
					      AIUIManager.INSTANCE.startTTS("对不起，没有查询到歌曲，请再说一遍。", null, 200L);
				      } else {
					      while (true) {
						      if (mIsQueryMusicTtsPlayEnd) {
							      AIUIManager.INSTANCE.startTTS("对不起，没有查询到歌曲，请再说一遍。", null, 200L);
							      break;
						      }
					      }
				      }
			      }

			      @Override
			      public void onComplete() {
				      LogUtils.iTag("parseMusicPro", "查询完毕");
				      AIUIManager.INSTANCE.startListening();
			      }
		      });
	}

	private void parseJoke(String text) {
		AIUIManager.INSTANCE.startTTS(text, null);
		AIUIManager.INSTANCE.startListening();
	}

	private void parseWeather(String text) {
		AIUIManager.INSTANCE.startTTS(text, null);
		AIUIManager.INSTANCE.startListening();
	}

	private void parseStory(String text, List<SkillDataResultEntity> result) {
		if (result.isEmpty()) {
			AIUIManager.INSTANCE.startTTS(text, AIUIManager.INSTANCE::startListening);
		} else {
			AIUIManager.INSTANCE.startTTS(text, () -> {
				StarrySky.with().playMusicByUrl(result.get(0).getPlayUrl());
				AIUIManager.INSTANCE.startListening();
			});
		}
	}

	private void parseChannelList() {
		String xmlString;
		try {
			AssetManager assetManager = Utils.getApp().getAssets();
			InputStream inputStream = assetManager.open("hdp_channel_list.xml");
			XmlToJson xmlToJson = new XmlToJson.Builder(inputStream, null).build();
			xmlString = xmlToJson.toString();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (!xmlString.isEmpty()) {
			JSONObject jsonObject = JSON.parseObject(xmlString);
			JSONObject channel_list = jsonObject.getJSONObject("channel_list");
			JSONArray classArr = channel_list.getJSONArray("class");
			List<TvChannelInfoEntityClassEntity> tvInfoClassList = JSON.parseArray(classArr.toJSONString(), TvChannelInfoEntityClassEntity.class);
			for (TvChannelInfoEntityClassEntity tvInfoClass : tvInfoClassList) {
				mTvChannelInfoSet.addAll(tvInfoClass.getChannel());
			}
		}
	}
}
