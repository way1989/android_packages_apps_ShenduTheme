package com.shendu.broadcast;

import android.content.res.Configuration;
import android.content.res.CustomTheme;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParserException;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.shendu.theme.DetailThemeActivity;
import com.shendu.theme.LocalThemeActivity;
import com.shendu.theme.R;
import com.shendu.theme.ben.ThemeDescription;
import com.shendu.utils.PacketParser;
import com.shendu.utils.SuCommander;
import com.shendu.utils.ZipUitls;

public class LauncherBroadcast extends BroadcastReceiver {

	private final String RECEIVE_PREVIEW_BROADCAST = "com.shendu.theme.LauncherBroadcast_parser_perview_Action";
	private final String RECEIVE_SETTING_THEME_BROADCAST = "com.shendu.theme.LauncherBroadcast_setting_theme_Action";
	private final String PATH = "/data/com.shendu.theme/launcher";
	private String[] perview = { "preview_launcher_1.png",
			"preview_launcher_1.jpg", "preview_contact_1.png",
			"preview_contact_1.jpg", "preview_mms_1.png", "preview_mms_1.jpg" };
	private Context mContext;
	private String mCurrentThemePath;

	@Override
	public void onReceive(final Context arg0, final Intent arg1) {
		// TODO Auto-generated method stub
		this.mContext = arg0;

		if (arg1.getAction().equals(RECEIVE_PREVIEW_BROADCAST)) {
			/**
			 * 解析主题包中的预览图到指定目录 /data/data/Package name/file/ 1 起一个新线程 遍历主题包存放的目录
			 * sdcard/shendu/theme /system/media/theme/
			 * 解析主题包中的preivew目录下的一张预览图到/data/data/Package name/file目录下
			 * 命名方式：包名.jpg 上述完成后，发送广播通知完成操作
			 */

			Thread thread = new Thread() {
				public void run() {
					// 遍历sdcard
					String path = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + "/shendu/theme";
					ArrayList<String> sdcardList = (ArrayList<String>) ZipUitls
							.getZIP(path);

					// 遍历system
					ArrayList<String> list = (ArrayList<String>) ZipUitls
							.getZIP("/system/media/theme");
					if (sdcardList != null) {
						while (!sdcardList.isEmpty()) {
						    if(list!=null){
						        list.add(sdcardList.remove(0));
						    }
						}
						sdcardList = null;
					}
					parserPreviewPicture(list);
					list = null;
					try {
						SuCommander suComm = new SuCommander();
						suComm.exec("chmod -R 777  /data/data/com.shendu.theme/launcher");
						while (!suComm.isReady()) {
							SystemClock.sleep(50);
						}
						String output = suComm.getOutput();
						if (output != null) {
							output = output.trim().replace("\n", "");
							if (output.length() > 2) {
								Log.e("wjg", ":shell信息：:" + output);
							}
						}
						String errors = suComm.getErrors();
						if (errors != null) {
							Log.e("wjg", "shell Command line Error::" + errors);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					Intent intent = new Intent("com.shendu.launcher_receive");
					intent.putExtra("ischanged", true);
					intent.putExtra("path", PATH);
					mContext.sendBroadcast(intent);
					mContext = null;
				}
			};
			thread.start();
		} else if (arg1.getAction().equals(RECEIVE_SETTING_THEME_BROADCAST)) {
			mCurrentThemePath = arg1.getStringExtra("path");
			ChangeTheme changeTheme = new ChangeTheme();
			changeTheme.start();
		}
	}

	private void parserPreviewPicture(ArrayList<String> list) {

		// 预览图存放路径
		File launcherPath = null;
		FileOutputStream os = null;
		try {
			File systemThemePaht = Environment.getDataDirectory();
			launcherPath = new File(systemThemePaht.getPath() + PATH);
			if (launcherPath.exists()) {
				if (ZipUitls.removeDir(launcherPath)) {
					boolean isCreate = launcherPath.mkdirs();
				}
			} else {
				boolean isCreate = launcherPath.mkdirs();
			}

			// 系统主题---点击是恢复主题
			Resources resources = mContext.getResources();
			InputStream ins = resources
					.openRawResource(R.drawable.default_preview_launcher_1);
			if (ins != null) {
				int len = 0;
				byte[] buffer = new byte[1024];

				if (ins != null) {
					os = new FileOutputStream(launcherPath + File.separator
							+ "default_preview_launcher.jpg");
					while ((len = ins.read(buffer)) != -1) {
						os.write(buffer);
					}
					if (os != null) {
						os.close();
						os = null;
					}
					ins.close();
					ins = null;
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (list != null && list.size() > 0) {
			// 解析预览图
			InputStream is = null;
			int len = 0;
			byte[] buffer = new byte[1024];
			ZipEntry zipEntry = null;
			ZipFile zipFile = null;
			while (!list.isEmpty()) {
				try {
					String name = list.remove(0);
					int i = 0;
					zipFile = new ZipFile(name);
					while ((i < perview.length)) {
						zipEntry = zipFile.getEntry("preview/" + perview[i]);
						if (zipEntry != null) {
							String preview = perview[i];

							if (preview.contains(".jpg")) {
								preview = ".jpg";
							} else {
								preview = ".png";
							}
							is = zipFile.getInputStream(zipEntry);
							if (name.contains("/mnt/sdcard")) {
								name = "sdcard_"
										+ name.substring(
												name.lastIndexOf("/") + 1,
												name.indexOf(".")) + preview;
							} else if (name.contains("/system/media")) {
								name = "system_"
										+ name.substring(
												name.lastIndexOf("/") + 1,
												name.indexOf(".")) + preview;
							}
							if (is != null) {
								os = new FileOutputStream(launcherPath
										+ File.separator + name);
								while ((len = is.read(buffer)) != -1) {
									os.write(buffer, 0, len);
								}
								if (os != null) {
									os.close();
									os = null;
								}
								is.close();
								is = null;
							}
							zipFile.close();
							break;
						}
						i++;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			Log.e("wjg", ":::::::::::::::没有主题包::::::::::::::::");
		}
	}

	private class ChangeTheme extends Thread {
		public void run() {

			if (mCurrentThemePath == null)
				return;

			if (mCurrentThemePath.contains("sdcard_")) {
				mCurrentThemePath = mCurrentThemePath.substring(
						mCurrentThemePath.lastIndexOf("/") + 1,
						mCurrentThemePath.indexOf(".jpg"));
				mCurrentThemePath = Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ "/shendu/theme/"
						+ mCurrentThemePath.substring(mCurrentThemePath
								.indexOf("_") + 1) + ".zip";
			} else if (mCurrentThemePath.contains("system_")) {
				mCurrentThemePath = mCurrentThemePath.substring(
						mCurrentThemePath.lastIndexOf("/") + 1,
						mCurrentThemePath.indexOf(".jpg"));
				mCurrentThemePath = "/system/media/theme/"
						+ mCurrentThemePath.substring(mCurrentThemePath
								.indexOf("_") + 1) + ".zip";
			} else if (mCurrentThemePath.contains("default_preview_launcher")) {
				try {
					ArrayList<String> cmds_re = new ArrayList<String>();
					SuCommander suComm = new SuCommander();
					cmds_re.add("rm -rf /data/system/theme");
					boolean done = true;
					while (done) {
						if (!cmds_re.isEmpty()) {
							done = true;
							String c = cmds_re.remove(0);
							suComm.exec(c);
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
								Log.e("wjg", "shell Command line Error::"
										+ errors);
							}
						} else {
							done = false;
							recoveryWallpaper();
							mContext = null;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			try {
				ArrayList<String> cmds = new ArrayList<String>();
				SuCommander suComm = new SuCommander();
				cmds.add("rm -rf /data/system/theme");
				cmds.add("mkdir /data/system/theme");
				cmds.add("unzip -n " + mCurrentThemePath
						+ " -x preview/*  -d /data/system/theme");
				cmds.add("chmod -R 777 /data/system/theme");
				boolean done = true;
				while (done) {
					if (!cmds.isEmpty()) {
						done = true;
						String c = cmds.remove(0);
						suComm.exec(c);
						while (!suComm.isReady()) {
							SystemClock.sleep(100);
						}
						suComm.isSuccess();

						String output = suComm.getOutput();
						if (output != null) {
							output = output.trim().replace("\n", "");
							if (output.length() > 2) {
								Log.e("wjg", ":shell信息：:" + output);
							}
						}
						String errors = suComm.getErrors();
						if (errors != null) {
							Log.e("wjg", "shell Command line Error::" + errors);
							done = false;
						}
					} else {
						done = false;
						setCurrentTheme();
						mContext = null;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * 恢复主题与壁纸
		 */
		private void recoveryWallpaper() {
			// 获取当前主题名称 需要去解析xml文件 description.xml
			WallpaperManager wall = null;
			try {
				wall = (WallpaperManager) mContext
						.getSystemService(Context.WALLPAPER_SERVICE);
				wall.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ThemeDescription themedes = null;
			try {
				InputStream ins = PacketParser.UpZip(mCurrentThemePath,
						LocalThemeActivity.DESCRIPTIONFILE);
				themedes = PacketParser.PullXmlParser(ins);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			// 把主题包名称写到，SharedPrefereces中
			if (themedes == null) {
				themedes = new ThemeDescription();
				themedes.setTitle(mCurrentThemePath.substring(
						mCurrentThemePath.lastIndexOf("/") + 1,
						mCurrentThemePath.lastIndexOf('.')));// 默认给一个主题包文件名
				themedes.setDesigner("ShenDuOS");
				themedes.setAuthor("ShenDuOS");
				themedes.setUiVersion("1.0.0");
				themedes.setVersion("1.0.0");
				boolean isWrite = PacketParser.WriteSharedPreferences(mContext,
						themedes);
			} else {
				boolean isWrite = PacketParser.WriteSharedPreferences(mContext,
						themedes);
			}
			ActivityManager am = (ActivityManager) mContext
					.getSystemService(Context.ACTIVITY_SERVICE);
			Configuration config = am.getConfiguration();
			if (config.customTheme == null) {
				config.customTheme = new CustomTheme("1", "");
			} else {
				int id = Integer.parseInt(config.customTheme.getThemeId()) + 1;
				config.customTheme = new CustomTheme("" + id, "");
			}
			am.updateConfiguration(config);
			BackupManager.dataChanged("com.android.providers.settings");
		}

		/**
		 * 换主题与壁纸
		 */
		private void setCurrentTheme() {

			try {
				InputStream is = null;
				File wallpaper = new File(
						"/data/system/theme/wallpaper/default_wallpaper.jpg");
				WallpaperManager wall = null;
				if (wallpaper.exists()) {
					wall = (WallpaperManager) mContext
							.getSystemService(Context.WALLPAPER_SERVICE);
					InputStream in = new FileInputStream(wallpaper);
					wall.setStream(in);
					in.close();
				}
				// 获取当前主题名称 需要去解析xml文件 description.xml
				ThemeDescription themedes = null;
				try {
					InputStream ins = new FileInputStream("/data/system/theme"
							+ File.separator
							+ LocalThemeActivity.DESCRIPTIONFILE);
					themedes = PacketParser.PullXmlParser(ins);
				} catch (Exception e1) {
					InputStream ins = PacketParser.UpZip(mCurrentThemePath,
							LocalThemeActivity.DESCRIPTIONFILE);
					themedes = PacketParser.PullXmlParser(ins);
					e1.printStackTrace();
				}

				// 把主题包名称写到，SharedPrefereces中
				if (themedes == null) {

					themedes = new ThemeDescription();
					themedes.setTitle(mCurrentThemePath.substring(
							mCurrentThemePath.lastIndexOf("/") + 1,
							mCurrentThemePath.lastIndexOf('.')));// 默认给一个主题包文件名
					themedes.setDesigner("ShenDuOS");
					themedes.setAuthor("ShenDuOS");
					themedes.setUiVersion("1.0.0");
					themedes.setVersion("1.0.0");

					boolean isWrite = PacketParser.WriteSharedPreferences(
							mContext, themedes);
				} else {

					boolean isWrite = PacketParser.WriteSharedPreferences(
							mContext, themedes);
				}

			} catch (Exception e) {
				Log.e("wjg", "---------------------壁纸挂掉---------------------"
						+ e.getMessage());
			}
			ActivityManager am = (ActivityManager) mContext
					.getSystemService(Context.ACTIVITY_SERVICE);
			Configuration config = am.getConfiguration();
			Configuration mConfig = new Configuration(config);

			if (config.customTheme == null) {
				config.customTheme = new CustomTheme("1", "");
			} else {
				int id = Integer.parseInt(config.customTheme.getThemeId()) + 1;
				config.customTheme = new CustomTheme("" + id, "");
			}
			am.updateConfiguration(config);
			BackupManager.dataChanged("com.android.providers.settings");
		}
	}
}
