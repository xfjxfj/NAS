package com.topqizhi.ai.entity.skill;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/04/16 17:08 with Android Studio.
 */
public class SkillSemanticArrayEntity extends SkillEntity implements Serializable {

	private final List<SemanticArrayEntity> semantic = new ArrayList<>();

	public SkillSemanticArrayEntity() {}

	public List<SemanticArrayEntity> getSemantic() {
		return semantic;
	}
}
