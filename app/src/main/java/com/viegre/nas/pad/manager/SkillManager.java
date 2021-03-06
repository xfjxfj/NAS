package com.viegre.nas.pad.manager;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.topqizhi.ai.entity.skill.SemanticArrayEntity;
import com.topqizhi.ai.entity.skill.SkillDataResultEntity;
import com.topqizhi.ai.entity.skill.SkillEntity;
import com.topqizhi.ai.entity.skill.SkillMusicProSemanticEntity;
import com.topqizhi.ai.entity.skill.SkillSemanticArrayEntity;
import com.topqizhi.ai.entity.skill.SkillSemanticObjectEntity;
import com.topqizhi.ai.manager.AIUIManager;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.video.VideoPlayerActivity;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.entity.AudioEntity;
import com.viegre.nas.pad.entity.TvChannelInfoEntity;
import com.viegre.nas.pad.entity.TvChannelInfoEntityClassEntity;
import com.viegre.nas.pad.entity.VideoEntity;
import com.viegre.nas.pad.task.VoidTask;
import com.viegre.nas.pad.util.IotGateway;
import com.ywl5320.libmusic.WlMusic;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import hdp.http.APIConstant;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

/**
 * Created by ??????????????? on 2021/04/16 16:15 with Android Studio.
 */
public enum SkillManager {

	INSTANCE;

	private static final String TAG = SkillManager.class.getSimpleName();

	public static final String MUSIC_SERVER = "http://119.23.253.236:3000/";
	public static final String MUSIC_PLAY_URL = "http://music.163.com/song/media/outer/url?id=";

	private final Set<TvChannelInfoEntity> mTvChannelInfoSet = new HashSet<>();
	private final List<AudioEntity> mPlayList = new CopyOnWriteArrayList<>();
	private boolean mIsPlayFirst;

	public void parseSkillMsg(String message) {
		ThreadUtils.executeByCached(new VoidTask() {
			@Override
			public Void doInBackground() {
				Log.i(TAG, message);
				JSONObject json = JSON.parseObject(message);
				if (!json.containsKey("service")) {
					AIUIManager.INSTANCE.startTTS("?????????????????????????????????????????????????????????", AIUIManager.INSTANCE::startListening);
					return null;
				}
				switch (json.getString("service")) {
					case SkillEntity.TVCHANNEL:
						parseTvchannel(JSON.parseObject(message, SkillSemanticArrayEntity.class));
						break;

					case SkillEntity.TV_SMART_HOME:
						parseTvSmartHome(JSON.parseObject(message, SkillSemanticObjectEntity.class));
						break;

					case SkillEntity.VIDEO:
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
					case SkillEntity.WEATHER:
					case SkillEntity.AIUI_BRAINTEASER:
					case SkillEntity.AIUI_FOREX:
					case SkillEntity.LEIQIAO_HISTORYTODAY:
					case SkillEntity.LEIQIAO_RELATIONSHIP:
					case SkillEntity.KLLI3_AREASCALER:
					case SkillEntity.KLLI3_VOLUMESCALER:
					case SkillEntity.KLLI3_NUMBERSCALER:
					case SkillEntity.KLLI3_POWERSCALER:
					case SkillEntity.KLLI3_WEIGHTSCALER:
					case SkillEntity.ZUOMX_QUERYCAPITAL:
					case SkillEntity.LEIQIAO_CITYOFPRO:
					case SkillEntity.LEIQIAO_LENGTH:
					case SkillEntity.LEIQIAO_TEMPERATURE:
					case SkillEntity.EGO_FOODSCALORIE:
					case SkillEntity.KLLI3_CAPTIALINFO:
					case SkillEntity.AIUI_IDIOMSDICT:
					case SkillEntity.AIUI_CALC:
					case SkillEntity.CALENDAR:
					case SkillEntity.STOCK:
					case SkillEntity.AIUI_GARBAGECLASSIFY:
					case SkillEntity.HOLIDAY:
					case SkillEntity.CONSTELLATION:
					case SkillEntity.DATETIMEX:
					case SkillEntity.CHINESEZODIAC:
					case SkillEntity.CARNUMBER:
					case SkillEntity.TRANSLATION:
					case SkillEntity.AIUI_VIRUSSEARCH:
					case SkillEntity.BAIKE:
					case SkillEntity.PETROLPRICE:
					case SkillEntity.DREAM:
						SkillSemanticArrayEntity answerTextEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
						parseAnswerText(answerTextEntity.getAnswer().getText());
						break;

					case SkillEntity.STORY:
						SkillSemanticArrayEntity playUrlEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
						parsePlayUrl(playUrlEntity.getAnswer().getText(), playUrlEntity.getData().getResult());
						break;

					case SkillEntity.ANIMALCRIES:
					case SkillEntity.CROSSTALK:
					case SkillEntity.DRAMA:
						SkillSemanticArrayEntity urlEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
						parseUrl(urlEntity.getAnswer().getText(), urlEntity.getData().getResult());
						break;

					case "TOPQIZHI.eyecare":
						SkillSemanticArrayEntity iotControlEntity = JSON.parseObject(message, SkillSemanticArrayEntity.class);
						AIUIManager.INSTANCE.startTTS(iotControlEntity.getAnswer().getText(), () -> parseIotControl(iotControlEntity));
						break;

					default:
						AIUIManager.INSTANCE.startTTS(JSON.parseObject(message, SkillEntity.class).getAnswer().getText(),
						                              AIUIManager.INSTANCE::startListening);
						break;
				}
				return null;
			}
		});
	}

	private void parseIotControl(SkillSemanticArrayEntity semanticArr) {
		WlMusic.getInstance().stop();
		SemanticArrayEntity semantic = semanticArr.getSemantic().get(0);
		switch (semantic.getIntent()) {
			case "iot_model":
				String modelName = semantic.findSlotValue("name");
				String modelSwitch = semantic.findSlotValue("switch");
				iotSwitchModel(modelName, modelSwitch);
				break;

			case "iot_model_backhome":
				iotSwitchModel("??????", "on");
				break;

			case "iot_model_leavehome":
				iotSwitchModel("??????", "on");
				break;
			case "iot_model_reading":
				iotSwitchModel("??????", "on");
				break;

			case "iot_model_sleep":
				iotSwitchModel("??????", "on");
				break;

			case "iot_mode_rise":
				iotSwitchModel("??????", "on");
				break;

			case "iot_device":
				String deviceName = semantic.findSlotValue("name");
				String deviceSwitch = semantic.findSlotValue("switch");
				String deviceRegion = semantic.findSlotValue("region");
				String deviceClPercent = semantic.findSlotValue("cl_percent");
				if (null != deviceClPercent) {
					deviceName = "??????";
				}
				iotSwitchDevice(deviceName, deviceSwitch, deviceRegion, deviceClPercent);
				break;

			default:
				AIUIManager.INSTANCE.startTTS("?????????????????????????????????????????????????????????", AIUIManager.INSTANCE::startListening);
				break;
		}
	}

	private void iotSwitchModel(String modelName, String modelSwitch) {
		ThreadUtils.executeByCached(new VoidTask() {
			@Override
			public Void doInBackground() {
				boolean isSuccess = IotGateway.executeModel(modelName);
				if (isSuccess) {
					AIUIManager.INSTANCE.startTTS("????????????", AIUIManager.INSTANCE::startListening);
				} else {
					AIUIManager.INSTANCE.startTTS("????????????", AIUIManager.INSTANCE::startListening);
				}
				return null;
			}
		});
		LogUtils.iTag(TAG, "iot model switch, " + modelName + " " + modelSwitch);
	}

	private void iotSwitchDevice(String deviceName, String deviceSwitch, String deviceRegion, String deviceClPercent) {
		ThreadUtils.executeByCached(new VoidTask() {
			@Override
			public Void doInBackground() throws UnsupportedEncodingException {
				boolean isSuccess = IotGateway.executeDevice(deviceName, deviceRegion, deviceSwitch.toUpperCase());
				if (isSuccess) {
					AIUIManager.INSTANCE.startTTS("????????????", AIUIManager.INSTANCE::startListening);
				} else {
					AIUIManager.INSTANCE.startTTS("????????????", AIUIManager.INSTANCE::startListening);
					IotGateway.uploadAreaEntity(AIUIManager.INSTANCE.getAIUIAgent());
					IotGateway.uploadDeviceEntity(AIUIManager.INSTANCE.getAIUIAgent());
				}
				return null;
			}
		});
		LogUtils.iTag(TAG, "iot device switch, " + deviceRegion + " " + deviceName + " " + deviceSwitch);
	}

	private void parseTvchannel(SkillSemanticArrayEntity tvchannelEntity) {
		switch (tvchannelEntity.getSemantic().get(0).getIntent()) {
			case "INSTRUCTION":
				switch (tvchannelEntity.getSemantic().get(0).getSlots().get(0).getValue()) {
					//????????????
					case "live":
						WlMusic.getInstance().stop();
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
				if (tvchannelEntity.getSemantic().get(0).getSlots().get(0).getValue().contains("????????????")) {
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
					case "??????":
						switch (skillSemanticObjectEntity.getSemantic().getSlots().getAttrValue()) {
							case "???":
								AIUIManager.INSTANCE.startTTS(StringUtils.getString(R.string.initial_response),
								                              () -> ThreadUtils.runOnUiThread(() -> {
									                              //??????ActivityManager
									                              ActivityManager mAm = (ActivityManager) Utils.getApp()
									                                                                           .getSystemService(Context.ACTIVITY_SERVICE);
									                              // ?????????????????????task
									                              List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(100);
									                              for (ActivityManager.RunningTaskInfo rti : taskList) {
										                              //?????????????????????task????????????task?????????activity??????????????????????????????
										                              if (rti.topActivity.getPackageName().equals(Utils.getApp().getPackageName())) {
											                              //??????app??????????????????
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
									                              //????????????????????????task??????????????????task????????????????????????????????????mainactivity
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
				WlMusic.getInstance().stop();
				queryVideo(videoEntity);
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
			if (slots == null || slots.size() == 0) {
				AIUIManager.INSTANCE.startTTS("?????????????????????????????????????????????????????????", AIUIManager.INSTANCE::startListening);
			} else {
				switch (slots.getJSONObject(0).getString("value")) {
					case "pause":
						AIUIManager.INSTANCE.setPauseMusicManually(true);
						AIUIManager.INSTANCE.startTTS("?????????", AIUIManager.INSTANCE::startListening);
						break;

					case "replay":
						AIUIManager.INSTANCE.setPauseMusicManually(false);
						AIUIManager.INSTANCE.startTTS("?????????", AIUIManager.INSTANCE::startListening);
						break;

					default:
						AIUIManager.INSTANCE.startTTS("?????????????????????????????????????????????????????????", AIUIManager.INSTANCE::startListening);
						break;
				}
			}
			return;
		}

		StringBuffer keywords = new StringBuffer();
		if (slots == null || slots.size() == 0) {
			keywords = new StringBuffer(musicProEntity.getText());
			if ("RANDOM_SEARCH".equals(intent)) {
				keywords = new StringBuffer("????????????");
			}
		} else {
			for (int i = 0; i < slots.size(); i++) {
				//??????????????????
				if ("song".equals(slots.getJSONObject(i).getString("name"))) {
					String value = slots.getJSONObject(i).getString("value");
					Cursor cursor = Utils.getApp()
					                     .getContentResolver()
					                     .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					                            new String[]{MediaStore.Audio.Media.DATA},
					                            MediaStore.Audio.Media.DATA + " like ?",
					                            new String[]{PathConfig.NAS + "%" + value + "%"},
					                            null);
					if (null != cursor) {
						List<AudioEntity> audioList = new ArrayList<>();
						while (cursor.moveToNext()) {
							String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
							if (null == path) {
								continue;
							}
							if (!path.startsWith(PathConfig.PUBLIC) && !path.startsWith(PathConfig.PRIVATE)) {
								continue;
							}
							audioList.add(new AudioEntity(FileUtils.getFileName(path), path));
						}
						cursor.close();
						if (!audioList.isEmpty()) {
							AIUIManager.INSTANCE.startTTS("??????????????????" + audioList.get(0).getName());
							playMusic(audioList);
							return;
						}
					}
					break;
				}
			}

			for (int i = 0; i < slots.size(); i++) {
				String value = slots.getJSONObject(i).getString("value");
				keywords.append(value);
			}
		}

		//??????????????????
		RxHttp.get(MUSIC_SERVER + "search")
		      .setAssemblyEnabled(false)
		      .add("limit", 10)
		      .add("keywords", keywords.toString())
		      .asString()
		      .subscribe(new Observer<String>() {
			      @Override
			      public void onSubscribe(@NonNull Disposable d) {}

			      @Override
			      public void onNext(@NonNull String s) {
				      LogUtils.iTag("parseMusicPro", "????????????");
				      JSONObject resultObj = JSON.parseObject(s);
				      List<JSONObject> musicObjList = JSONObject.parseArray(resultObj.getJSONObject("result").getJSONArray("songs").toJSONString(),
				                                                            JSONObject.class);
				      if (null == musicObjList || musicObjList.isEmpty()) {
					      AIUIManager.INSTANCE.startTTS("??????????????????????????????????????????????????????");
					      return;
				      }

				      List<String[]> playList = new ArrayList<>();
				      for (JSONObject jsonObject : musicObjList) {
					      playList.add(new String[]{jsonObject.getString("name"), jsonObject.getString("id")});
				      }
				      ThreadUtils.cancel(ThreadUtils.getCachedPool());
				      ThreadUtils.executeByCached(new VoidTask() {
					      @Override
					      public Void doInBackground() {
						      queryFirstMusic(playList);
						      return null;
					      }
				      });
			      }

			      @Override
			      public void onError(@NonNull Throwable e) {
				      e.printStackTrace();
				      WlMusic.getInstance().stop();
				      LogUtils.iTag("parseMusicPro", "??????????????????", e);
				      AIUIManager.INSTANCE.startTTS("??????????????????????????????????????????????????????");
			      }

			      @Override
			      public void onComplete() {
				      LogUtils.iTag("parseMusicPro", "????????????");
				      AIUIManager.INSTANCE.startListening();
			      }
		      });
	}

	private void parseAnswerText(String answer) {
		WlMusic.getInstance().stop();
		AIUIManager.INSTANCE.startTTS(answer);
		AIUIManager.INSTANCE.startListening();
	}

	private void parsePlayUrl(String text, List<SkillDataResultEntity> result) {
		if (result.isEmpty()) {
			AIUIManager.INSTANCE.startTTS(text, AIUIManager.INSTANCE::startListening);
		} else {
			WlMusic.getInstance().stop();
			AIUIManager.INSTANCE.startTTS(text, () -> {
				playMusic(result.get(0).getPlayUrl());
				AIUIManager.INSTANCE.startListening();
			});
		}
	}

	private void parseUrl(String text, List<SkillDataResultEntity> result) {
		if (result.isEmpty()) {
			AIUIManager.INSTANCE.startTTS(text, AIUIManager.INSTANCE::startListening);
		} else {
			WlMusic.getInstance().stop();
			AIUIManager.INSTANCE.startTTS(text, () -> {
				playMusic(result.get(0).getUrl());
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

	private void queryVideo(SkillSemanticArrayEntity videoEntity) {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<VideoEntity>>() {
			@Override
			public List<VideoEntity> doInBackground() {
				List<VideoEntity> videoList = new ArrayList<>();
				Cursor cursor = Utils.getApp()
				                     .getContentResolver()
				                     .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				                            new String[]{MediaStore.Video.VideoColumns.DATA},
				                            MediaStore.Video.Media.DATA + " like ?",
				                            new String[]{PathConfig.NAS + "%" + videoEntity.getSemantic().get(0).getSlots().get(0).getValue() + "%"},
				                            null);

				if (null != cursor) {
					while (cursor.moveToNext()) {
						String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
						if (null == path) {
							continue;
						}
						if (!path.startsWith(PathConfig.PUBLIC) && !path.startsWith(PathConfig.PRIVATE)) {
							continue;
						}
						String name;
						String suffix;
						String displayName = FileUtils.getFileName(path);
						name = FileUtils.getFileNameNoExtension(displayName);
						suffix = FileUtils.getFileExtension(displayName);
						videoList.add(new VideoEntity(name, suffix, path));
					}
					cursor.close();
				}
				return videoList;
			}

			@Override
			public void onSuccess(List<VideoEntity> result) {
				if (result.isEmpty()) {
					Intent searchIntent = new Intent("com.ktcp.voice.SEARCH");
					searchIntent.putExtra("_vr_context", videoEntity.getSemantic().get(0).getSlots().get(0).getValue());
					Utils.getApp().sendBroadcast(searchIntent);
					AIUIManager.INSTANCE.startListening();
					AIUIManager.INSTANCE.startTTS("?????????" + videoEntity.getSemantic().get(0).getSlots().get(0).getValue() + "??????????????????");
				} else {
					Intent intent = new Intent(Utils.getApp(), VideoPlayerActivity.class);
					List<VideoEntity> videoList = new ArrayList<>();
					videoList.add(result.get(0));
					intent.putExtra("videoListJson", JSON.toJSONString(videoList));
					intent.putExtra("index", 0);
					ActivityUtils.startActivity(intent);
					AIUIManager.INSTANCE.startListening();
				}
			}
		});
	}

	private void playMusic(List<AudioEntity> audioList) {
		WlMusic.getInstance().stop();
		final int[] index = {0};
		WlMusic.getInstance().setSource(audioList.get(index[0]).getPath());
		if (audioList.size() > 1) {
			WlMusic.getInstance().setOnCompleteListener(() -> {
				if (index[0] == audioList.size() - 1) {
					index[0] = 0;
				} else {
					index[0] = index[0] + 1;
				}
				WlMusic.getInstance().playNext(audioList.get(index[0]).getPath());
			});
			WlMusic.getInstance().setOnErrorListener((code, msg) -> {
				if (index[0] == audioList.size() - 1) {
					index[0] = 0;
				} else {
					index[0] = index[0] + 1;
				}
				WlMusic.getInstance().playNext(audioList.get(index[0]).getPath());
			});
		}
		WlMusic.getInstance().setOnPreparedListener(() -> WlMusic.getInstance().start());
		WlMusic.getInstance().prePared();
		AIUIManager.INSTANCE.startListening();
	}

	private void playMusic(String path) {
		WlMusic.getInstance().stop();
		WlMusic.getInstance().setSource(path);
		WlMusic.getInstance().setOnPreparedListener(() -> WlMusic.getInstance().start());
		WlMusic.getInstance().prePared();
		AIUIManager.INSTANCE.startListening();
	}

	@SuppressLint("NewApi")
	private void queryFirstMusic(List<String[]> playList) {
		mIsPlayFirst = false;
		mPlayList.clear();
		playList.parallelStream().forEach(strings -> {
			String result = "";
			try {
				result = RxHttp.get(MUSIC_SERVER + "check/music").setAssemblyEnabled(false).add("id", strings[1]).executeString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!TextUtils.isEmpty(result)) {
				JSONObject jsonObject = JSON.parseObject(result);
				if (jsonObject.containsKey("success") && jsonObject.getBoolean("success")) {
					ThreadUtils.runOnUiThread(() -> {
						if (!mIsPlayFirst) {
							mIsPlayFirst = true;
							mPlayList.add(new AudioEntity(strings[0], MUSIC_PLAY_URL + strings[1] + ".mp3"));
							AIUIManager.INSTANCE.startTTS("??????????????????" + mPlayList.get(0).getName());
							playMusic(mPlayList);
						} else {
							mPlayList.add(new AudioEntity(strings[0], MUSIC_PLAY_URL + strings[1] + ".mp3"));
						}
					});
				}
			}
		});
	}
}
