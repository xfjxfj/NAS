package com.viegre.nas.speaker.entity;

import android.net.wifi.ScanResult;

import java.io.Serializable;

/**
 * Created by レインマン on 2020/12/02 10:23 with Android Studio.
 */
public class WiFiEntity implements Serializable {

	private ScanResult scanResult;
	private String password;

	public WiFiEntity() {}

	public WiFiEntity(ScanResult scanResult, String password) {
		this.scanResult = scanResult;
		this.password = password;
	}

	public ScanResult getScanResult() {
		return scanResult;
	}

	public void setScanResult(ScanResult scanResult) {
		this.scanResult = scanResult;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
