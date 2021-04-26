package com.topqizhi.ai.entity.skill;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/16 17:08 with Android Studio.
 */
public class SkillMusicProSemanticEntity extends SkillEntity implements Serializable {

	private String semantic;

	public SkillMusicProSemanticEntity() {}

	public String getSemantic() {
		return semantic;
	}

	public void setSemantic(String semantic) {
		this.semantic = semantic;
	}
}
