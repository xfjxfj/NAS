package com.viegre.nas.pad.controller;

import android.os.Build;

import com.blankj.utilcode.util.FileUtils;
import com.viegre.nas.pad.config.PathConfig;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.RequiresApi;

/**
 * Created by レインマン on 2021/05/28 14:18 with Android Studio.
 */
@RestController
@RequestMapping(path = "/file")
@RequiresApi(api = Build.VERSION_CODES.N)
public class FileController {

	@GetMapping(path = "/rootList")
	List<String> fileList() {
		return FileUtils.listFilesInDir(PathConfig.NAS).stream().map(File::getAbsolutePath).collect(Collectors.toList());
	}
}
