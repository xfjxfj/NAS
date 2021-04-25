package com.topqizhi.ai.entity.skill;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/16 16:10 with Android Studio.
 */
public class AnswerEntity implements Serializable {

	private String text;

	public AnswerEntity() {}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
