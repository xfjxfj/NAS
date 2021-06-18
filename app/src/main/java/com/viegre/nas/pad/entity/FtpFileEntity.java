package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.TreeSet;

import androidx.annotation.StringDef;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by レインマン on 2021/06/16 10:23 with Android Studio.
 */
public class FtpFileEntity extends LitePalSupport implements Serializable {

	public static final String CP = "cp";
	public static final String MV = "mv";
	public static final String RE = "re";

	@Retention(SOURCE)
	@Target({PARAMETER})
	@StringDef(value = {Type.PUBLIC, Type.PRIVATE, Type.UNKNOWN})
	public @interface Type {
		String PUBLIC = "public";
		String PRIVATE = "private";
		String UNKNOWN = "unknown";
	}

	@Retention(SOURCE)
	@Target({PARAMETER})
	@StringDef(value = {State.NORMAL, State.RECYCLED})
	public @interface State {
		String NORMAL = "normal";
		String RECYCLED = "recycled";
	}

	private String path;
	private String recycledPath;
	private String createTime;
	private String deleteTime;
	private String deletePhoneNum;
	private String size;
	private String type;
	private String state;
	private String ext;
	private final Set<String> pickSet = new TreeSet<>();
	private final Set<String> banSet = new TreeSet<>();

	public FtpFileEntity() {}

	public FtpFileEntity(String path, String recycledPath, String createTime, String deleteTime, String deletePhoneNum,
	                     @Type String type, @State String state) {
		this.path = path;
		this.recycledPath = recycledPath;
		this.createTime = createTime;
		this.deleteTime = deleteTime;
		this.deletePhoneNum = deletePhoneNum;
		this.type = type;
		this.state = state;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRecycledPath() {
		return recycledPath;
	}

	public void setRecycledPath(String recycledPath) {
		this.recycledPath = recycledPath;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(String deleteTime) {
		this.deleteTime = deleteTime;
	}

	public String getDeletePhoneNum() {
		return deletePhoneNum;
	}

	public void setDeletePhoneNum(String deletePhoneNum) {
		this.deletePhoneNum = deletePhoneNum;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(@Type String type) {
		this.type = type;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public Set<String> getPickSet() {
		return pickSet;
	}

	public Set<String> getBanSet() {
		return banSet;
	}
}
