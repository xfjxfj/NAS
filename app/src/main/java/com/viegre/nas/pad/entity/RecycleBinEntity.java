package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/21 14:46 with Android Studio.
 */
public class RecycleBinEntity extends LitePalSupport implements Serializable {

	private String deleteTime;
	private String createTime;
	private String type;
	private String pathBeforeDelete;
	private String pathAfterDelete;

	public RecycleBinEntity(String deleteTime, String createTime, String type, String pathBeforeDelete, String pathAfterDelete) {
		this.deleteTime = deleteTime;
		this.createTime = createTime;
		this.type = type;
		this.pathBeforeDelete = pathBeforeDelete;
		this.pathAfterDelete = pathAfterDelete;
	}

	public String getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(String deleteTime) {
		this.deleteTime = deleteTime;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPathBeforeDelete() {
		return pathBeforeDelete;
	}

	public void setPathBeforeDelete(String pathBeforeDelete) {
		this.pathBeforeDelete = pathBeforeDelete;
	}

	public String getPathAfterDelete() {
		return pathAfterDelete;
	}

	public void setPathAfterDelete(String pathAfterDelete) {
		this.pathAfterDelete = pathAfterDelete;
	}
}
