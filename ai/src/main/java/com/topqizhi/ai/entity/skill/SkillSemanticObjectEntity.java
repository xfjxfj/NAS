package com.topqizhi.ai.entity.skill;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/16 17:08 with Android Studio.
 */
public class SkillSemanticObjectEntity extends SkillEntity implements Serializable {

	private SemanticObjectEntity semantic;

	public SkillSemanticObjectEntity() {}

	public SemanticObjectEntity getSemantic() {
		return semantic;
	}

	public void setSemantic(SemanticObjectEntity semantic) {
		this.semantic = semantic;
	}
}
