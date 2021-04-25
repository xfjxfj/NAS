package com.topqizhi.ai.entity.skill;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/16 17:12 with Android Studio.
 */
public class SemanticObjectSlotsEntity implements Serializable {

	private String attr;
	private String attrType;
	private String attrValue;

	public SemanticObjectSlotsEntity() {}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public String getAttrType() {
		return attrType;
	}

	public void setAttrType(String attrType) {
		this.attrType = attrType;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}
}
