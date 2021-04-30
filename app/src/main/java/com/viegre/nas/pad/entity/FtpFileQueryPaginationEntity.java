package com.viegre.nas.pad.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/04/30 14:52 with Android Studio.
 */
public class FtpFileQueryPaginationEntity implements Serializable {

	private int total;
	private int page;
	private int size;
	private final List<FtpFileQueryEntity> queryList = new ArrayList<>();

	public FtpFileQueryPaginationEntity() {}

	public FtpFileQueryPaginationEntity(int total, int page, int size) {
		this.total = total;
		this.page = page;
		this.size = size;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public List<FtpFileQueryEntity> getQueryList() {
		return queryList;
	}
}
