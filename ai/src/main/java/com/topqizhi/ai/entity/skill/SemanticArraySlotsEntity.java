package com.topqizhi.ai.entity.skill;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/16 17:24 with Android Studio.
 */
public class SemanticArraySlotsEntity implements Serializable {

	private String name;
	private String value;
	private String normValue;

	public SemanticArraySlotsEntity() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getNormValue() {
		return normValue;
	}

	public void setNormValue(String normValue) {
		this.normValue = normValue;
	}
}
