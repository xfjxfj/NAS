package com.topqizhi.andserver.model;

import android.util.Log;

import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RandomFileBody implements ResponseBody {

	private final Long start;
	private final Long end;
	private final RandomAccessFile file;
	private final long mLength;
	private final MediaType mMediaType;

	public RandomFileBody(Long start, Long end, RandomAccessFile file, Long length, MediaType mediaType) {
		this.start = start;
		this.end = end;
		this.file = file;
		this.mLength = length;
		this.mMediaType = mediaType;
	}

	@Override
	public boolean isRepeatable() {
		return false;
	}

	@Override
	public long contentLength() {
		return mLength;
	}

	@Nullable
	@Override
	public MediaType contentType() {
		return mMediaType;
	}

	@Override
	public void writeTo(@NonNull OutputStream output) {
		long transmitted = 0;
		try {
			byte[] buff = new byte[10240];
			int len = 0;
			file.seek(start);
			//坑爹地方四：判断是否到了最后不足4096（buff的length）个byte这个逻辑（(transmitted + len) <= contentLength）要放前面！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
			//不然会会先读取randomAccessFile，造成后面读取位置出错，找了一天才发现问题所在
			while ((transmitted + len) <= mLength && (len = file.read(buff)) != -1) {
				output.write(buff, 0, len);
				output.flush();
				transmitted += len;
			}
			//处理不足buff.length部分
			if (transmitted < mLength) {
				len = file.read(buff, 0, (int) (mLength - transmitted));
				output.write(buff, 0, len);
				output.flush();
				transmitted += len;
			}

			file.close();
		} catch (IOException e) {
			Log.i("RandomFileBody", "用户停止下载：" + start + "-" + end + "：" + transmitted);
		} finally {
			try {
				if (file != null) {
					file.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
