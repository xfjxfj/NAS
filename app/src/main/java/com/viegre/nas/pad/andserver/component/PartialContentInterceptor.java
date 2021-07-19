package com.viegre.nas.pad.andserver.component;

import android.text.TextUtils;

import com.topqizhi.andserver.model.RandomFileBody;
import com.viegre.nas.pad.config.PathConfig;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.framework.HandlerInterceptor;
import com.yanzhenjie.andserver.framework.handler.RequestHandler;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.util.MediaType;
import com.yanzhenjie.andserver.util.StatusCode;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import androidx.annotation.NonNull;

@Interceptor
public class PartialContentInterceptor implements HandlerInterceptor {
	@Override
	public boolean onIntercept(@NonNull HttpRequest request, @NonNull HttpResponse response, @NonNull RequestHandler handler) throws IOException {
		String range = request.getHeader("Range");
		if (!TextUtils.isEmpty(range)) {
			File file = new File(PathConfig.NAS + request.getPath());

			//开始下载位置
			long startByte = 0;
			//结束下载位置
			long endByte = file.length() - 1;

			//有range的话
			if (range != null && range.contains("bytes=") && range.contains("-")) {
				range = range.substring(range.lastIndexOf("=") + 1).trim();
				String[] ranges = range.split("-");
				try {
					//判断range的类型
					if (ranges.length == 1) {
						//类型一：bytes=-2343
						if (range.startsWith("-")) {
							endByte = Long.parseLong(ranges[0]);
						}
						//类型二：bytes=2343-
						else if (range.endsWith("-")) {
							startByte = Long.parseLong(ranges[0]);
						}
					}
					//类型三：bytes=22-2343
					else if (ranges.length == 2) {
						startByte = Long.parseLong(ranges[0]);
						endByte = Long.parseLong(ranges[1]);
					}
				} catch (NumberFormatException e) {
					startByte = 0;
					endByte = file.length() - 1;
				}
			}

			//要下载的长度（为啥要加一问小学数学老师去）
			long contentLength = endByte - startByte + 1;
			//文件名
			String fileName = file.getName();
			//文件类型
			MediaType contentType = MediaType.getFileMediaType(file.getName());

			//各种响应头设置
			//参考资料：https://www.ibm.com/developerworks/cn/java/joy-down/index.html
			//坑爹地方一：看代码
			response.setHeader("Accept-Ranges", "bytes");
			//坑爹地方二：http状态码要为206
			response.setStatus(StatusCode.SC_PARTIAL_CONTENT);
//			response.setHeader("Content-Length", String.valueOf(contentLength));
			//坑爹地方三：Content-Range，格式为
			//[要下载的开始位置]-[结束位置]/[文件总大小]
			response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			response.setBody(new RandomFileBody(startByte, endByte, raf, contentLength, contentType));
			return true;
		} else {
			return false;
		}
	}
}
