package com.topqizhi.ai.entity.skill;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/16 15:46 with Android Studio.
 */
public class SkillEntity implements Serializable {

	public static final String TVCHANNEL = "tvchannel";
	public static final String TV_SMART_HOME = "tv_smartHome";
	public static final String VIDEO = "video";
	public static final String APP = "app";
	public static final String MUSIC_PRO = "musicPro";
	public static final String JOKE = "joke";
	public static final String WEATHER = "weather";
	public static final String STORY = "story";

	private AnswerEntity answer;
	private String service;
	private String operation;
	private String text;
	private SkillDataEntity data;

	public SkillEntity() {}

	public AnswerEntity getAnswer() {
		return answer;
	}

	public void setAnswer(AnswerEntity answer) {
		this.answer = answer;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public SkillDataEntity getData() {
		return data;
	}

	public void setData(SkillDataEntity data) {
		this.data = data;
	}
}
