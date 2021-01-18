package com.viegre.nas.pad.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/01/13 16:00 with Android Studio.
 */
public class DeviceResourceRootEntity implements Serializable {

	private final List<DeviceResourceEntity> resourceList = new ArrayList<>();

	public List<DeviceResourceEntity> getResourceList() {
		return resourceList;
	}
}
