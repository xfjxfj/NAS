package com.viegre.nas.pad.controller;

import android.os.Build;

import com.blankj.utilcode.util.FileUtils;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.util.JsonUtils;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.multipart.MultipartFile;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import androidx.annotation.RequiresApi;

/**
 * Created by レインマン on 2021/05/28 14:18 with Android Studio.
 */
@RestController
@RequestMapping(path = "/file")
@RequiresApi(api = Build.VERSION_CODES.N)
public class FileController {

	/**
	 * 查询根目录列表
	 *
	 * @return
	 */
	@PostMapping(path = "/rootList")
	String fileList() {
		return JsonUtils.succeedJson("查询成功",
		                             FileUtils.listFilesInDir(PathConfig.NAS).stream().map(File::getAbsolutePath).collect(Collectors.toList()));
	}

	@PostMapping(path = "/uploadFile", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	String uploadFile(@RequestParam(name = "path") String path, @RequestParam(name = "file") MultipartFile file) throws IOException {
		if (!FileUtils.isFileExists(path)) {
			FileUtils.createOrExistsFile(path);
		} else {
			String name, extension;
			int index = 1;
			while (true) {
				if (path.contains(".")) {
					if (path.startsWith(".") && path.split("\\.").length == 1) {
						name = FileUtils.getFileName(path);
						path = name + " (" + index + ")";
					} else {
						name = FileUtils.getFileNameNoExtension(path);
						extension = FileUtils.getFileExtension(path);
						path = name + " (" + index + ")." + extension;
					}
				} else {
					name = FileUtils.getFileName(path);
					path = name + " (" + index + ")";
				}
				if (!FileUtils.isFileExists(path)) {
					FileUtils.createOrExistsFile(path);
					break;
				}
				index++;
			}
		}
		file.transferTo(FileUtils.getFileByPath(path));
		return JsonUtils.succeedJson("上传成功", path);
	}
}
