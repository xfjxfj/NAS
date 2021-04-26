package com.viegre.nas.pad.api;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.LogUtils;
import com.lzx.starrysky.StarrySky;
import com.topqizhi.ai.manager.AIUIManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

import okhttp3.Request;
import okhttp3.Response;

public class Api {

	public static final String MUSIC_SERVER = "http://119.23.253.236:3000/";

	public static final String MUSIC_PLAY_URL = "http://music.163.com/song/media/outer/url?id=";

	public static void queryMusic(String anwser, JSONObject sematicJSON) {
		try {
			JSONArray slots = sematicJSON.getJSONArray("slots");
			String intent = sematicJSON.getString("intent");

			if ("INSTRUCTION".equals(intent)) {
				AIUIManager.INSTANCE.startListening();
				return;
			}

			String keywords = "";
			if (slots == null || slots.size() == 0) {
				keywords = anwser;

				if ("RANDOM_SEARCH".equals(intent)) {
					keywords = "中文歌曲";
				}
			} else {
				for (int i = 0; i < slots.size(); i++) {
					String value = slots.getJSONObject(i).getString("value");
					keywords += value;
				}
			}

			String url;
			try {
				url = MUSIC_SERVER + "search?limit=5&keywords=" + URLEncoder.encode(keywords, "utf-8");//固定5首待选
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				AIUIManager.INSTANCE.startListening();
				return;
			}

			Request request = new Request.Builder().url(url).addHeader("Content-Type", "application/json; charset=utf-8").get().build();
			Response response = OkHttpHelper.getInstance().doRequest(request);
			if (!response.isSuccessful()) {
				Log.e("Api", "未能获取到音乐资源");
				response.close();
				AIUIManager.INSTANCE.startListening();
			} else {
				String result = response.body().string();
				response.close();
				JSONObject resultObj = JSON.parseObject(result);
				JSONArray songs = resultObj.getJSONObject("result").getJSONArray("songs");
				JSONArray resultArray = new JSONArray();
				if ("RANDOM_SEARCH".equals(intent)) {
					Collections.shuffle(songs);//打乱一下
				}

				for (int i = 0; i < songs.size(); i++) {
					JSONObject song = songs.getJSONObject(i);
					String id = song.getString("id");
					String name = song.getString("name");

					try {
						//检查歌曲是否能直接播放
						Request checkRequest = new Request.Builder().url(MUSIC_SERVER + "check/music?id=" + id)
						                                            .addHeader("Content-Type", "application/json; charset=utf-8")
						                                            .get()
						                                            .build();
						Response checkResponse = OkHttpHelper.getInstance().doRequest(checkRequest);
						if (!checkResponse.isSuccessful()) {
							Log.e("Api", "未能获取到音乐资源");
							checkResponse.close();
							AIUIManager.INSTANCE.startListening();
						} else {
							JSONObject checkObj = JSON.parseObject(checkResponse.body().string());
							checkResponse.close();
							boolean isAvailable = checkObj.getBoolean("success");
							if (isAvailable) {
								JSONObject songInfo = new JSONObject();
								songInfo.put("songname", name);
								String songUrl = MUSIC_PLAY_URL + id + ".mp3";
								songInfo.put("audiopath", songUrl);
								resultArray.add(songInfo);

								JSONObject musicObj = new JSONObject();
								musicObj.put("result", resultArray);
								musicObj.put("answer", "即将为您播放" + name);
								LogUtils.iTag("musicObj", musicObj.toJSONString());
								StarrySky.with().playMusicByUrl(musicObj.getJSONArray("result").getJSONObject(0).getString("audiopath"));
								AIUIManager.INSTANCE.startListening();
							}
						}
					} catch (Exception ex) {
						Log.e("Api", "检查歌曲是否可用异常", ex);
						AIUIManager.INSTANCE.startListening();
						return;
					}
				}
			}
		} catch (Exception ex) {
			Log.e("Api", "获取音乐资源失败", ex);
			AIUIManager.INSTANCE.startListening();
		}
	}
}
