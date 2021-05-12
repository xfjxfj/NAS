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
	public static final String AIUI_BRAINTEASER = "AIUI.brainTeaser";
	public static final String AIUI_FOREX = "AIUI.forex";
	public static final String LEIQIAO_HISTORYTODAY = "LEIQIAO.historyToday";
	public static final String LEIQIAO_RELATIONSHIP = "LEIQIAO.relationShip";
	public static final String KLLI3_AREASCALER = "KLLI3.areaScaler";
	public static final String KLLI3_VOLUMESCALER = "KLLI3.volumeScaler";
	public static final String KLLI3_NUMBERSCALER = "KLLI3.numberScaler";
	public static final String KLLI3_POWERSCALER = "KLLI3.powerScaler";
	public static final String KLLI3_WEIGHTSCALER = "KLLI3.weightScaler";
	public static final String ZUOMX_QUERYCAPITAL = "ZUOMX.queryCapital";
	public static final String LEIQIAO_CITYOFPRO = "LEIQIAO.cityOfPro";
	public static final String LEIQIAO_LENGTH = "LEIQIAO.length";
	public static final String LEIQIAO_TEMPERATURE = "LEIQIAO.temperature";
	public static final String EGO_FOODSCALORIE = "EGO.foodsCalorie";
	public static final String KLLI3_CAPTIALINFO = "KLLI3.captialInfo";
	public static final String AIUI_IDIOMSDICT = "AIUI.idiomsDict";
	public static final String AIUI_CALC = "AIUI.calc";
	public static final String CALENDAR = "calendar";
	public static final String STOCK = "stock";
	public static final String AIUI_GARBAGECLASSIFY = "AIUI.garbageClassify";
	public static final String ANIMALCRIES = "animalCries";
	public static final String HOLIDAY = "holiday";
	public static final String CONSTELLATION = "constellation";
	public static final String CROSSTALK = "crossTalk";
	public static final String DRAMA = "drama";
	public static final String DATETIMEX = "datetimeX";
	public static final String CHINESEZODIAC = "chineseZodiac";
	public static final String CARNUMBER = "carNumber";
	public static final String TRANSLATION = "translation";
	public static final String AIUI_VIRUSSEARCH = "AIUI.virusSearch";
	public static final String BAIKE = "baike";
	public static final String PETROLPRICE = "petrolPrice";
	public static final String DREAM = "dream";

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
