package com.topqizhi.ai.entity.skill;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/05/07 15:45 with Android Studio.
 */
public class SkillDataEntity {

	private final List<SkillDataResultEntity> result = new ArrayList<>();

	public SkillDataEntity() {}

	public List<SkillDataResultEntity> getResult() {
		return result;
	}
}
