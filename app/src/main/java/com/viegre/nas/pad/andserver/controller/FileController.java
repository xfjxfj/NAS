package com.viegre.nas.pad.andserver.controller;

import android.annotation.SuppressLint;
import android.media.MediaScannerConnection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.viegre.nas.pad.entity.FileChunkEntity;
import com.viegre.nas.pad.util.MediaScanner;
import com.yanzhenjie.andserver.annotation.FormPart;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.RequestBody;
import com.yanzhenjie.andserver.http.multipart.MultipartFile;
import com.yanzhenjie.andserver.util.MediaType;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

	@PostMapping(path = "/checkAll", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	String checkAll(RequestBody body) throws IOException {
		String content = body.string();
		JSONArray fileArray = JSON.parseArray(content);
		for (int i = 0; i < fileArray.size(); i++) {
			JSONObject fileObj = fileArray.getJSONObject(i);
			String path = fileObj.getString("path");
			path = PathConfig.NAS + path;
			fileObj.put("exists",FileUtils.isFileExists(path));
			if(fileObj.containsKey("md5")) {
				String md5 = fileObj.getString("md5");
				fileObj.put("md5","new");
				List<File> files = FileUtils.listFilesInDir(FileUtils.getDirName(path));
				for (File file:files) {
					if(md5.equalsIgnoreCase(FileUtils.getFileMD5ToString(file))) {
						fileObj.put("md5","exist");
						break;
					}
				}
			}
		}
		return fileArray.toJSONString();
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

	@PostMapping(path = "/uploadChunk", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	boolean uploadChunk(@RequestParam(name = "file") MultipartFile chunk,@FormPart("chunk") FileChunkEntity fileChunk) {
		String path = PathConfig.NAS + fileChunk.getFullPath();
		if (!path.startsWith(PathConfig.PUBLIC) && !path.startsWith(PathConfig.PRIVATE)) {
			throw new PermissionDeniedException();
		}
		if (FileUtils.isFileExists(path)) {
			throw new FileAlreadyExistsException();
		}

		// 模块写入对应的位置
		try(RandomAccessFile rf = new RandomAccessFile(fileChunk.getFullPath(),
				"rw")) {
			rf.seek(fileChunk.getStart());
			rf.write(chunk.getBytes());
		} catch (Exception e) {
			LogUtils.e(e.getMessage(), e);
			return false;
		}

		// chunk 记录到数据库
		fileChunk.save();

		// 文件全部上传完成
		int chunkSize = LitePal.where("fileHash = ?",fileChunk.getFileHash()).count(FileChunkEntity.class);
		if (chunkSize == fileChunk.getChunkNum()) {//判断是否全部上传完成
			// 删除 chunk 记录
			LitePal.deleteAll(FileChunkEntity.class,"fileHash = ?",fileChunk.getFileHash());
			try {
				File localFile = new File(path);
				MediaScannerConnection.scanFile(Utils.getApp(),
						new String[]{path},
						null,
						(s, uri) -> LogUtils.iTag("FileController.upload", "path = " + s, "uri = " + uri));
				new MediaScanner().scanFile(localFile);
			} catch (Exception e) {
				throw new CustomException(e.getMessage());
			}
		}
		return true;
	}

	@PostMapping(path = "/isChunkUpload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	JSONObject isChunkUpload(@RequestParam("fullPath") String path, @RequestParam("fileHash") String fileHash) {
		path = PathConfig.NAS + path;
		JSONObject returnMap = new JSONObject();
		if (!path.startsWith(PathConfig.PUBLIC) && !path.startsWith(PathConfig.PRIVATE)) {
			throw new PermissionDeniedException();
		}
		if (FileUtils.isFileExists(path)) {
			returnMap.put("isUploaded",true);
			return returnMap;
		}

		List<File> files = FileUtils.listFilesInDir(FileUtils.getDirName(path));
		for (File file:files) {
			if(fileHash.equalsIgnoreCase(FileUtils.getFileMD5ToString(file))) {
				returnMap.put("isFileExist",true);
				return returnMap;
			}
		}

		List<FileChunkEntity> fileChunkList = LitePal.where("fileHash = ?",fileHash).find(FileChunkEntity.class);
		returnMap.put("uploadedChunkList", fileChunkList);

		return returnMap;
	}
}
