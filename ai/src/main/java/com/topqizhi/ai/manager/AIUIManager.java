package com.topqizhi.ai.manager;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.ThreadUtils;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.topqizhi.ai.impl.AIUIResultListener;
import com.ywl5320.libmusic.WlMusic;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by レインマン on 2021/04/13 18:00 with Android Studio.
 */
public enum AIUIManager {

	INSTANCE;

	public static final boolean IS_HARD_WAKEUP = true;

	private static final String TAG = AIUIManager.class.getSimpleName();

	private AIUIAgent mAIUIAgent;
	private int mAIUIState = AIUIConstant.STATE_IDLE;
	private String mSyncSid = "";
	//TTS结束回调
	private Runnable mTTSCallback;
	private AIUIResultListener mAIUIResultListener;
	private boolean hasResult = false;
	private boolean isManualStopVoiceNlp = false;
	private final List<String> mInitialResponseList = new ArrayList<>();
	private volatile boolean mIsPauseMusicManually;

	public void initialize(Context applicationContext) {
		mAIUIAgent = AIUIAgent.createAgent(applicationContext, getAIUIParams(applicationContext), mAIUIListener);
		mInitialResponseList.add("我在。");
		mInitialResponseList.add("你说。");
		mInitialResponseList.add("请说。");
		mInitialResponseList.add("在呢。");
		mInitialResponseList.add("怎么了。");
	}

	private String getAIUIParams(Context context) {
		String params = "";
		AssetManager assetManager = context.getResources().getAssets();
		try {
			InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
			byte[] buffer = new byte[ins.available()];

			ins.read(buffer);
			ins.close();

			params = new String(buffer);

			JSONObject paramsJson = new JSONObject(params);

			JSONObject vadParams = paramsJson.optJSONObject("vad");
			if (vadParams != null) {
				vadParams.put("vad_eos", "1000");
			}

			params = paramsJson.toString();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}

		return params;
	}

	public void addAIUIResultListener(AIUIResultListener aiuiResultListener) {
		mAIUIResultListener = aiuiResultListener;
	}

	public void startListening() {
		MscManager.INSTANCE.setListenHardWakeup(true);
		if (!mIsPauseMusicManually && !WlMusic.getInstance().isPlaying()) {
			WlMusic.getInstance().resume();
		}
		//唤醒结束后恢复音量
		VolumeManager.INSTANCE.getAudioManager().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
		if (null == mAIUIAgent) {
			return;
		}
		hasResult = false;
		if (IS_HARD_WAKEUP) {
			AudioRecordManager.INSTANCE.startRecord();
			return;
		}
		MscManager.INSTANCE.startListening(wakeuperResultEntity -> {
			Log.i("WakeuperResultListener", wakeuperResultEntity.getRaw());
			//唤醒时静音
			VolumeManager.INSTANCE.getAudioManager().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
			if (WlMusic.getInstance().isPlaying()) {
				WlMusic.getInstance().pause();
			}
			Random random = new Random();
			int index = random.nextInt(mInitialResponseList.size());
			startTTS(mInitialResponseList.get(index), () -> {
				//先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
				//默认为oneshot模式，即一次唤醒后就进入休眠。可以修改aiui_phone.cfg中speech参数的interact_mode为continuous以支持持续交互
				AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
				mAIUIAgent.sendMessage(wakeupMsg);

				//打开AIUI内部录音机，开始录音。若要使用上传的个性化资源增强识别效果，则在参数中添加pers_param设置
				//个性化资源使用方法可参见http://doc.xfyun.cn/aiui_mobile/的用户个性化章节
				//在输入参数中设置tag，则对应结果中也将携带该tag，可用于关联输入输出
				String params = "sample_rate=16000,data_type=audio,pers_param={\"uid\":\"\"},tag=audio-tag";
				AIUIMessage startRecord = new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, params, null);
				mAIUIAgent.sendMessage(startRecord);
			});
		});
	}

	public void startHardListening() {
		//唤醒时静音
		VolumeManager.INSTANCE.getAudioManager().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
		if (WlMusic.getInstance().isPlaying()) {
			WlMusic.getInstance().pause();
		}
		Random random = new Random();
		int index = random.nextInt(mInitialResponseList.size());
		startTTS(mInitialResponseList.get(index), () -> {
			//先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
			//默认为oneshot模式，即一次唤醒后就进入休眠。可以修改aiui_phone.cfg中speech参数的interact_mode为continuous以支持持续交互
			AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
			mAIUIAgent.sendMessage(wakeupMsg);

			//打开AIUI内部录音机，开始录音。若要使用上传的个性化资源增强识别效果，则在参数中添加pers_param设置
			//个性化资源使用方法可参见http://doc.xfyun.cn/aiui_mobile/的用户个性化章节
			//在输入参数中设置tag，则对应结果中也将携带该tag，可用于关联输入输出
			String params = "sample_rate=16000,data_type=audio,pers_param={\"uid\":\"\"},tag=audio-tag";
			AIUIMessage startRecord = new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, params, null);
			mAIUIAgent.sendMessage(startRecord);
		});
	}

	public void stopVoiceNlp() {
		if (null == mAIUIAgent) {
			return;
		}
		isManualStopVoiceNlp = true;
		//停止录音
		String params = "sample_rate=16000,data_type=audio";
		AIUIMessage stopRecord = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, params, null);
		mAIUIAgent.sendMessage(stopRecord);
	}

	public void startTTS(String text, Runnable onComplete) {
		if (null == mAIUIAgent) {
			return;
		}
		mAIUIAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.PAUSE, 0, null, null));
		mTTSCallback = onComplete;
		String tag = "@" + System.currentTimeMillis();
		StringBuffer params = new StringBuffer();//构建合成参数
		params.append("vcn=x2_xiaojuan");//合成发音人
		params.append(",speed=50");//合成速度
		params.append(",pitch=50");//合成音调
		params.append(",volume=50");//合成音量
		params.append(",ent=x_tts");//合成音量
		params.append(",tag=" + tag);//合成tag，方便追踪合成结束，暂未实现
		AIUIMessage startTts = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0, params.toString(), text.getBytes());
		mAIUIAgent.sendMessage(startTts);
	}

	public void startTTS(String text, Runnable onComplete, long delay) {
		ThreadUtils.executeByCachedWithDelay(new ThreadUtils.SimpleTask<Void>() {
			@Override
			public Void doInBackground() {
				startTTS(text, onComplete);
				return null;
			}

			@Override
			public void onSuccess(Void result) {}
		}, delay, TimeUnit.MILLISECONDS);
	}

	private final AIUIListener mAIUIListener = new AIUIListener() {
		@Override
		public void onEvent(AIUIEvent aiuiEvent) {
//			Log.i(TAG, "on event: " + aiuiEvent.eventType);
			switch (aiuiEvent.eventType) {
				case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
					Log.i(TAG, "已连接服务器");
//					EventBus.getDefault().postSticky("AIUI_CONNECTED_TO_SERVER");
					break;

				case AIUIConstant.EVENT_SERVER_DISCONNECTED:
					Log.i(TAG, "与服务器断连");
					break;

				case AIUIConstant.EVENT_WAKEUP:
					Log.i(TAG, "进入识别状态");
					break;

				case AIUIConstant.EVENT_RESULT: {
					try {
						JSONObject bizParamJson = new JSONObject(aiuiEvent.info);
						JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
						JSONObject params = data.getJSONObject("params");
						JSONObject content = data.getJSONArray("content").getJSONObject(0);
						String sub = params.optString("sub");

						if (content.has("cnt_id") && !"tts".equals(sub)) {
							String cnt_id = content.getString("cnt_id");
							String cntStr = new String(aiuiEvent.data.getByteArray(cnt_id), StandardCharsets.UTF_8);

							//获取该路会话的id，将其提供给支持人员，有助于问题排查
							//也可以从Json结果中看到
							String sid = aiuiEvent.data.getString("sid");
							String tag = aiuiEvent.data.getString("tag");

							Log.i(TAG, "tag = " + tag);

							//获取从数据发送完到获取结果的耗时，单位：ms
							//也可以通过键名"bos_rslt"获取从开始发送数据到获取结果的耗时
							long eosRsltTime = aiuiEvent.data.getLong("eos_rslt", -1);
							Log.i(TAG, "eosRsltTime = " + eosRsltTime + "ms");

							if (TextUtils.isEmpty(cntStr)) {
								return;
							}

							JSONObject cntJson = new JSONObject(cntStr);

							Log.i(TAG, cntJson.toString());

							if ("nlp".equals(sub)) {
								//解析得到语义结果
								String resultStr = cntJson.optString("intent");
								if (null != mAIUIResultListener) {
									mAIUIResultListener.result(resultStr);
								}
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
						startTTS("对不起，我没明白你的意思，请再说一遍。", () -> startListening());
					}
				}
				break;

				case AIUIConstant.EVENT_ERROR: {
					Log.e(TAG, "错误: " + aiuiEvent.arg1 + "\n" + aiuiEvent.info);
				}
				break;

				case AIUIConstant.EVENT_VAD: {
					if (AIUIConstant.VAD_BOS == aiuiEvent.arg1) {
						Log.i(TAG, "找到vad_bos");
						hasResult = true;
					} else if (AIUIConstant.VAD_EOS == aiuiEvent.arg1) {
						Log.i(TAG, "找到vad_eos");
					} else {
						Log.i(TAG, String.valueOf(aiuiEvent.arg2));
					}
				}
				break;

				case AIUIConstant.EVENT_START_RECORD: {
					Log.i(TAG, "已开始录音");
				}
				break;

				case AIUIConstant.EVENT_STOP_RECORD: {
					Log.i(TAG, "已停止录音");
					if (isManualStopVoiceNlp) {
						isManualStopVoiceNlp = false;
						startListening();
					}
				}
				break;

				//状态事件
				case AIUIConstant.EVENT_STATE: {
					mAIUIState = aiuiEvent.arg1;

					if (AIUIConstant.STATE_IDLE == mAIUIState) {
						//闲置状态，AIUI未开启
						Log.i(TAG, "STATE_IDLE");
					} else if (AIUIConstant.STATE_READY == mAIUIState) {
						//AIUI已就绪，等待唤醒
						Log.i(TAG, "STATE_READY");
					} else if (AIUIConstant.STATE_WORKING == mAIUIState) {
						//AIUI工作中，可进行交互
						Log.i(TAG, "STATE_WORKING");
					}
				}
				break;

				case AIUIConstant.EVENT_CMD_RETURN: {
					if (AIUIConstant.CMD_SYNC == aiuiEvent.arg1) {//数据同步的返回
						int dtype = aiuiEvent.data.getInt("sync_dtype", -1);
						int retCode = aiuiEvent.arg2;

						switch (dtype) {
							case AIUIConstant.SYNC_DATA_SCHEMA: {
								if (AIUIConstant.SUCCESS == retCode) {
									//上传成功，记录上传会话的sid，以用于查询数据打包状态
									//注：上传成功并不表示数据打包成功，打包成功与否应以同步状态查询结果为准，数据只有打包成功后才能正常使用
									mSyncSid = aiuiEvent.data.getString("sid");

									//获取上传调用时设置的自定义tag
									String tag = aiuiEvent.data.getString("tag");

									//获取上传调用耗时，单位：ms
									long timeSpent = aiuiEvent.data.getLong("time_spent", -1);
									if (-1 != timeSpent) {
										Log.i(TAG, "上传调用耗时 = " + timeSpent + "ms");
									}

									Log.i(TAG, "上传成功，sid=" + mSyncSid + "，tag=" + tag + "，你可以试着说“打电话给刘德华”");
								} else {
									mSyncSid = "";
									Log.i(TAG, "上传失败，错误码：" + retCode);
								}
							}
							break;
						}
					} else if (AIUIConstant.CMD_QUERY_SYNC_STATUS == aiuiEvent.arg1) {    //数据同步状态查询的返回
						//获取同步类型
						int syncType = aiuiEvent.data.getInt("sync_dtype", -1);
						if (AIUIConstant.SYNC_DATA_QUERY == syncType) {
							//若是同步数据查询，则获取查询结果，结果中error字段为0则表示上传数据打包成功，否则为错误码
							String result = aiuiEvent.data.getString("result");
							Log.i(TAG, result);
						}
					}
				}
				break;

				case AIUIConstant.EVENT_SLEEP: {
					if (!hasResult && AIUIConstant.TYPE_AUTO == aiuiEvent.arg1) {
						Log.i(TAG, "交互超时");
						startTTS("对不起，我没有听清楚，请再说一遍。", () -> stopVoiceNlp());
					}
					break;
				}

				case AIUIConstant.EVENT_TTS: {
					if (AIUIConstant.TTS_SPEAK_COMPLETED == aiuiEvent.arg1) {
						if (null != mTTSCallback) {
							mTTSCallback.run();
							mTTSCallback = null;
						}
					}
					break;
				}

				default:
					break;
			}
		}
	};

	public void release() {
		if (null != mAIUIAgent) {
			mAIUIAgent.destroy();
			mAIUIAgent = null;
		}
	}

	public void setPauseMusicManually(boolean pauseMusicManually) {
		mIsPauseMusicManually = pauseMusicManually;
	}

	public AIUIAgent getAIUIAgent() {
		return mAIUIAgent;
	}
}
