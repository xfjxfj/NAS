package com.topqizhi.ai.entity.skill;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/16 16:30 with Android Studio.
 */
public class SemanticObjectEntity implements Serializable {

	private SemanticObjectSlotsEntity slots;

	public SemanticObjectEntity() {}

	public SemanticObjectSlotsEntity getSlots() {
		return slots;
	}

	public void setSlots(SemanticObjectSlotsEntity slots) {
		this.slots = slots;
	}
}
