package com.viegre.nas.pad.andserver.controller;

import android.annotation.SuppressLint;
import android.media.MediaScannerConnection;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.Utils;
import com.topqizhi.andserver.model.HttpFile;
import com.viegre.nas.pad.andserver.exception.CustomException;
import com.viegre.nas.pad.andserver.exception.DirAlreadyExistsException;
import com.viegre.nas.pad.andserver.exception.DirNotExistException;
import com.viegre.nas.pad.andserver.exception.FileAlreadyExistsException;
import com.viegre.nas.pad.andserver.exception.IncorrectPathException;
import com.viegre.nas.pad.andserver.exception.PermissionDeniedException;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.util.MediaScanner;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.multipart.MultipartFile;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by レインマン on 2021/07/16 10:33 with Android Studio.
 */
@SuppressLint("NewApi")
@RestController
@RequestMapping(path = "/file")
class FileController {
	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	List<HttpFile> list(@RequestParam("path") String path) {
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
			if (File.separator.equals(path)) {
				path = PathConfig.NAS;
				return FileUtils.listFilesInDir(path)
				                .stream()
				                .filter(file -> (file.getAbsolutePath().equals(PathConfig.PUBLIC.substring(0, PathConfig.PUBLIC.length() - 1)) || file
						                .getAbsolutePath()
						                .equals(PathConfig.PRIVATE.substring(0, PathConfig.PRIVATE.length() - 1))) && file.isDirectory())
				                .map(file -> new HttpFile(file.getName(),
				                                          file.getAbsolutePath().replaceFirst(PathConfig.NAS, ""),
				                                          FileUtils.isDir(file),
				                                          FileUtils.getLength(file),
				                                          TimeUtils.millis2String(FileUtils.getFileLastModified(file), simpleDateFormat)))
				                .collect(Collectors.toList());
			}
			path = PathConfig.NAS + path;
			if (!path.endsWith(File.separator)) {
				path = path + File.separator;
			}
			if (!FileUtils.isFileExists(path)) {
				throw new DirNotExistException();
			}
			if (!FileUtils.isDir(path)) {
				throw new IncorrectPathException();
			}
			if (!path.startsWith(PathConfig.PUBLIC) && !path.startsWith(PathConfig.PRIVATE)) {
				throw new PermissionDeniedException();
			}
			return FileUtils.listFilesInDir(path)
			                .stream()
			                .map(file -> new HttpFile(file.getName(),
			                                          file.getAbsolutePath().replaceFirst(PathConfig.NAS, ""),
			                                          FileUtils.isDir(file),
			                                          FileUtils.getLength(file),
			                                          TimeUtils.millis2String(FileUtils.getFileLastModified(file), simpleDateFormat)))
			                .collect(Collectors.toList());
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}

	@PostMapping(path = "/mkdir", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	void mkdir(@RequestParam("path") String path) {
		path = PathConfig.NAS + path;
		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}
		if (FileUtils.isFileExists(path)) {
			throw new DirAlreadyExistsException();
		}
		if (!path.startsWith(PathConfig.PUBLIC) && !path.startsWith(PathConfig.PRIVATE)) {
			throw new PermissionDeniedException();
		}
		FileUtils.createOrExistsDir(path);
		MediaScannerConnection.scanFile(Utils.getApp(),
		                                new String[]{path},
		                                null,
		                                (s, uri) -> LogUtils.iTag("FileController.mkdir", "path = " + s, "uri = " + uri));
	}

	@PostMapping(path = "/check", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	boolean check(@RequestParam("path") String path) {
		return FileUtils.isFileExists(path);
	}

	@PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	void upload(@RequestParam("path") String path, @RequestParam(name = "file") MultipartFile file) {
		path = PathConfig.NAS + path;
		if (!FileUtils.isFileExists(path)) {
			throw new DirNotExistException();
		}
		path = path + File.separator + file.getFilename();
		if (!path.startsWith(PathConfig.PUBLIC) && !path.startsWith(PathConfig.PRIVATE)) {
			throw new PermissionDeniedException();
		}
		if (FileUtils.isFileExists(path)) {
			throw new FileAlreadyExistsException();
		}
		try {
			File localFile = new File(path);
			file.transferTo(localFile);
			MediaScannerConnection.scanFile(Utils.getApp(),
			                                new String[]{path},
			                                null,
			                                (s, uri) -> LogUtils.iTag("FileController.upload", "path = " + s, "uri = " + uri));
			new MediaScanner().scanFile(localFile);
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}
}
