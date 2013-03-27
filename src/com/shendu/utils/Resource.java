package com.shendu.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Environment;
import android.util.Log;

import com.shendu.theme.ben.ThemeDescription;

public abstract class Resource {
	static boolean mExternalStorageAvailable = false;
	/**
	 * OS theme Directory
	 */
	public final static String SYSTEM_THEME_PATH = "/system/media/theme";
	public final static String SYSTEM_THEME_CACHE_PATH = SYSTEM_THEME_PATH
			+ "/.cache";
	public final static String SYSTEM_THEME_CACHE_PREVIEW_PATH = SYSTEM_THEME_CACHE_PATH
			+ "/preview";
	/**
	 * External Sdcard Directory theme
	 */
	public final static String EXTERNAL_SDCARD_THEME_PATH;
	/**
	 * external sdcard cache diretory
	 */
	public final static String EXTERNAL_SDCARD_CACHE_PATH;
	public final static String EXTERNAL_SDCARD_CACHE_PREVIEW_PATH;
	/**
	 * internal sdcard cache diretory
	 */
	public final static String INTERNAL_SDCARD_CACHE_PATH;
	/**
	 * Internal Sdcard Directory theme
	 */
	public final static String INTERNAL_SDCARD_THEME_PATH;
	public final static String INTERNAL_SDCARD_CACHE_PREVIEW_PATH;
	/**
	 * Checking media availability
	 */
	static {

		SuCommander suComm = null;
		try {
			suComm = new SuCommander();
			suComm.exec("chmod -R 777 /system/media/theme");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		while (!suComm.isReady()) {
			SystemClock.sleep(200);
		}
		suComm.isSuccess();
		String output = suComm.getOutput();
		if (output != null) {
			output = output.trim().replace("\n", "");
			if (output.length() > 2) {
			}
		}
		String errors = suComm.getErrors();
		if (errors != null) {
			Log.e("ThemeHelp_wjg", "shell Command line Error::" + errors);
		}

		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = true;
			EXTERNAL_SDCARD_THEME_PATH = Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ File.separator + "shendu" + File.separator + "theme";
			EXTERNAL_SDCARD_CACHE_PATH = EXTERNAL_SDCARD_THEME_PATH + "/.cache";
			EXTERNAL_SDCARD_CACHE_PREVIEW_PATH = EXTERNAL_SDCARD_CACHE_PATH
					+ "/preivew";
			// 创建每个主题包的预览图父目录，是隐藏目录
			File cache = new File(EXTERNAL_SDCARD_CACHE_PATH);
			if (!cache.exists())
				cache.mkdirs();

		} else {
			EXTERNAL_SDCARD_THEME_PATH = null;
			EXTERNAL_SDCARD_CACHE_PATH = EXTERNAL_SDCARD_THEME_PATH;
			EXTERNAL_SDCARD_CACHE_PREVIEW_PATH = EXTERNAL_SDCARD_THEME_PATH;
			mExternalStorageAvailable = false;
		}
		// Determine whether there is a built-in sdcard
		if (SystemProperties.get("ro.vold.switchablepair", "").equals("")) {
			// No Internal sdcard
			INTERNAL_SDCARD_THEME_PATH = null;
			INTERNAL_SDCARD_CACHE_PATH = INTERNAL_SDCARD_THEME_PATH;
			INTERNAL_SDCARD_CACHE_PREVIEW_PATH = INTERNAL_SDCARD_THEME_PATH;
		} else {
			INTERNAL_SDCARD_THEME_PATH = getDirectory("INTERNAL_STORAGE",
					"/storage/sdcard1").getAbsoluteFile()
					+ File.separator + "shendu" + File.separator + "theme";
			INTERNAL_SDCARD_CACHE_PATH = INTERNAL_SDCARD_THEME_PATH + "/.cache";
			INTERNAL_SDCARD_CACHE_PREVIEW_PATH = INTERNAL_SDCARD_CACHE_PATH
					+ "/preview";
			File cache = new File(INTERNAL_SDCARD_CACHE_PATH);
			if (!cache.exists()) {
				cache.mkdirs();
			}
		}
	}

	/**
	 * get Internal sdcard path
	 * 
	 * @param variableName
	 * @param defaultPath
	 * @return
	 */
	private static File getDirectory(String variableName, String defaultPath) {
		String path = System.getenv(variableName);
		return path == null ? new File(defaultPath) : new File(path);
	}

}
