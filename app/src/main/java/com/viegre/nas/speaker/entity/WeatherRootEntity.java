package com.viegre.nas.speaker.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Djangoogle on 2021/01/12 15:13 with Android Studio.
 */
public class WeatherRootEntity implements Serializable {

	private final List<WeatherEntity> weather = new ArrayList<>();

	public List<WeatherEntity> getWeather() {
		return weather;
	}
}
