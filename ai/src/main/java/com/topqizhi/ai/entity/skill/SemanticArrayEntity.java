package com.topqizhi.ai.entity.skill;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/04/16 16:56 with Android Studio.
 */
public class SemanticArrayEntity implements Serializable {

	private String intent;
	private final List<SemanticArraySlotsEntity> slots = new ArrayList<>();

	public SemanticArrayEntity() {}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public List<SemanticArraySlotsEntity> getSlots() {
		return slots;
	}
}
