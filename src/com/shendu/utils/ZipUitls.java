package com.shendu.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.shendu.theme.ben.ModuleSelecet;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ZipUitls {
	private static final String TAG = "ZipUtils";
	private static final String[] IMAGE_FORMAT = { "jpg", "png" };
	private static final String DIRECTORY = "preview";
	private static boolean isDebug = true;
	private static Pattern pattern = Pattern.compile(".*" + DIRECTORY + ".*\\."
			+ IMAGE_FORMAT[0] + "|" + IMAGE_FORMAT[1]);

	/**
	 * unZip Image form zip file with fileName
	 * 
	 * @throws IOException
	 */
	public static Bitmap getImageFromZip(String zipFile, String name)
			throws IOException {
		Bitmap bitmap = null;
		InputStream inputStream = null;
		ZipFile zip = new ZipFile(zipFile);
		ZipEntry zipEntry = zip.getEntry(name);
		inputStream = zip.getInputStream(zipEntry);
		bitmap = BitmapFactory.decodeStream(inputStream);
		return bitmap;
	}
	
	/**
	 * unZip Image form zip file with fileName
	 * 
	 * @throws IOException
	 */
	public static Bitmap getImageFromZip(String zipFile, String name,int wScaling,int hScaling)
			throws IOException {
		Bitmap bitmap = null;
		InputStream inputStream = null;
		ZipFile zip = new ZipFile(zipFile);
		ZipEntry zipEntry = zip.getEntry(name);
		inputStream = zip.getInputStream(zipEntry);
		bitmap = BitmapFactory.decodeStream(inputStream);
		bitmap = Bitmap.createScaledBitmap(bitmap,wScaling, hScaling,true);
		return bitmap;
	}

	/**
	 * 获取主题包中的模块
	 * 
	 * @param path
	 *            主题包
	 * @return ArrayList 主题包中的各个模块名称 key 模块原始名称 value 汉字名称
	 * @throws Exception
	 */
	public static ArrayList<ModuleSelecet> getModuleName(String path)
			throws Exception {
		if (path == null)
			return null;
		if (!(new File(path).exists()))
			return null;
		ArrayList<ModuleSelecet> list = new ArrayList<ModuleSelecet>();
		ZipInputStream inZip = new ZipInputStream(new FileInputStream(path));
		ZipEntry zipEntry;
		String szName = "";
		ModuleSelecet ms;
		StringBuilder other = new StringBuilder();
		while ((zipEntry = inZip.getNextEntry()) != null) {
			szName = zipEntry.getName();
			if (szName.contains("preview/")) {
				continue;
			} else if (szName.contains("wallpaper/")) {
				if (szName.equals("wallpaper/")) {
					ms = new ModuleSelecet("桌面壁纸", szName + "*");
					list.add(ms);
				}
				continue;
			} else if (szName.equals("com.android.settings")) {
				ms = new ModuleSelecet("设置", szName);
				list.add(ms);
				continue;
			} else if (szName.equals("framework-res")) {
				ms = new ModuleSelecet("系统界面", szName);
				list.add(ms);
				continue;
			} else if (szName.equals("com.android.systemui")) {
				ms = new ModuleSelecet("通知栏", szName);
				list.add(ms);
				continue;
			} else if (szName.equals("com.android.mms")) {
				ms = new ModuleSelecet("短信", szName);
				list.add(ms);
				continue;
			} else if (szName.contains("com.android.phone")) {
				ms = new ModuleSelecet("拨号与联系人", szName + " "
						+ "com.android.contacts");
				list.add(ms);
				continue;
			} else if (szName.equals("icons")) {
				ms = new ModuleSelecet("图标", szName);
				list.add(ms);
				continue;
			} else if (szName.equals("com.shendu.launcher")) {
				ms = new ModuleSelecet("桌面", szName);
				list.add(ms);
				continue;
			}
			if (szName.contains("com.android.contacts"))
				continue;
			other.append(szName + " ");
		}
		// 如果主题包中没有任何Entry 不做任何操作
		if (list.size() > 0) {
			ms = new ModuleSelecet("其他", other.toString());
			list.add(ms);
		}
		inZip.close();
		return list;

	}

	/**
	 * 获取指定路径下的主题包
	 * 
	 * @param path
	 *            返回主题包绝对路径
	 * @return
	 */
	public static List<String> getZIP(String path) {

		if (path == null)
			return null;
		ArrayList<String> fileName = new ArrayList<String>();
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles(new FileFilter() {

					@Override
					public boolean accept(File pathname) {
						// TODO Auto-generated method stub
						return pathname.getName().endsWith(".zip");
					}
				});
				if (files == null)
					return null;
				for (File f : files) {
					Log.e("wjg", "主题包路径：：：：：" + f.getAbsolutePath());
					fileName.add(f.getAbsolutePath());
				}
				return fileName;
			}
		}
		return null;
	}

	/**
	 * 获取指定主题包中的preview目录的预览图名称
	 * 
	 * @param Pattern
	 *            正则，过滤 主题包
	 * @return 题包中的preview目录的预览图名称
	 */
	public static ArrayList<String> parseZip(File zipFile, Pattern pat) {
		if (pat != null)
			pattern = pat;
		if (zipFile == null)
			return null;
		if (!zipFile.exists())
			return null;
		try {
			// 判断是否是zip文件
			byte[] head = new byte[4];
			FileInputStream is = new FileInputStream(zipFile);
			is.read(head, 0, head.length);
			String type = PacketParser.bytesToHexString(head).toUpperCase();
			if (!type.contains("504B0304")) {
				return null;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		ArrayList<String> arrayList = new ArrayList<String>();
		try {
			ZipFile zip = new ZipFile(zipFile);
			if (zip != null) {
				// 正则表达式 匹配 /preview/*.png
				Enumeration<?> numeration = zip.entries();
				while (numeration.hasMoreElements()) {
					ZipEntry zipEntry = (ZipEntry) numeration.nextElement();
					Matcher matcher = pattern.matcher(zipEntry.getName());
					if (matcher.find()) {

						String name = zipEntry.getName();

						if (name.contains("wallpaper/"))
							continue;
						if (name.contains("launcher")) {
							arrayList.add(0, name);
							continue;
						}
						arrayList.add(name);
					}
				}
				return arrayList;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<String> parseZip(File name) {
		ArrayList<String> arrayList = new ArrayList<String>();
		try {
			ZipFile zipFile = new ZipFile(name);
			if (zipFile != null) {
				// 正则表达式 匹配 /preview/*.png
				Enumeration<?> numeration = zipFile.entries();

				while (numeration.hasMoreElements()) {
					ZipEntry zipEntry = (ZipEntry) numeration.nextElement();
					Matcher matcher = pattern.matcher(zipEntry.getName());
					if (matcher.find()) {
						arrayList.add(zipEntry.getName());
					}
				}
				return arrayList;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<Bitmap> parseZip(String name) {
		ArrayList<Bitmap> arrayList = new ArrayList<Bitmap>();
		Bitmap bitmap;
		int i = 0;
		try {
			ZipFile zipFile = new ZipFile(name);
			if (zipFile != null) {
				// 正则表达式 匹配 /preview/*.png
				Enumeration<?> numeration = zipFile.entries();

				while (numeration.hasMoreElements()) {
					ZipEntry zipEntry = (ZipEntry) numeration.nextElement();
					Matcher matcher = pattern.matcher(zipEntry.getName());
					if (matcher.find()) {
						InputStream inputStream = null;
						try {
							inputStream = zipFile.getInputStream(zipEntry);
						} catch (IOException e) {
							e.printStackTrace();
						}

						bitmap = BitmapFactory.decodeStream(inputStream);
						if (bitmap != null)
							arrayList.add(bitmap);
					}
				}
				return arrayList;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static InputStream UpZip(String zipFileString, String fileString)
			throws Exception {
		ZipFile zipFile = new ZipFile(zipFileString);
		ZipEntry zipEntry = zipFile.getEntry(fileString);
		return zipFile.getInputStream(zipEntry);

	}

	/**
	 * 解压一个压缩文档 到指定位置
	 * 
	 * @param zipFileString
	 *            压缩包的名字
	 * @param outPathString
	 *            指定的路径
	 * @throws Exception
	 */
	@SuppressLint("NewApi")
	public static boolean UnZipFolder(String zipFileString, String outPathString) {
		Log.v("XZip", "UnZipFolder(String, String)");
		boolean isdelete = false;
		File themePath = new File(outPathString);

		if (themePath.exists() && themePath.isDirectory()) {

			isdelete = removeDir(themePath);

			Log.e(TAG, "删除目录：：：:" + isdelete);
		}

		ZipInputStream inZip = null;
		try {
			inZip = new ZipInputStream(new FileInputStream(zipFileString));
			ZipEntry zipEntry;
			String szName = "";

			while ((zipEntry = inZip.getNextEntry()) != null) {
				szName = zipEntry.getName();

				Log.e(TAG,
						"名称：：：：" + szName + "::::: 是目录吗？？？：:"
								+ zipEntry.isDirectory());

				if (zipEntry.isDirectory()) {
					szName = szName.substring(0, szName.length() - 1);
					File folder = new File(outPathString + File.separator
							+ szName);
					if (!folder.exists())
						folder.mkdirs();
					continue;
				}
				boolean isCreate = false;
				File file = new File(outPathString + File.separator + szName);
				File praentPath = new File(file.getParent());
				if (!praentPath.exists()) {
					isCreate = praentPath.mkdirs();
					Log.i("wjg",
							"创建目录：:" + isCreate + "：：：名称：："
									+ praentPath.getName());
				}

				boolean is = file.createNewFile();

				Log.i(TAG, "创建文件：:" + is + "：：：名称：：" + file.getName());
				// get the output stream of the file
				FileOutputStream out = new FileOutputStream(file);
				int len;
				byte[] buffer = new byte[1024];
				// read (len) bytes into buffer
				while ((len = inZip.read(buffer)) != -1) {
					// write (len) byte from buffer at the position 0
					out.write(buffer, 0, len);
					out.flush();
				}
				out.close();

			}// end of while
		} catch (Exception e) {

			Log.d("wjg", "异常产生：：：" + e.getMessage());
			// return false;
			// TODO: handle exception
		} finally {
			if (inZip != null) {
				try {
					inZip.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return isdelete;
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * 
	 * @param dir
	 *            将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	/*
	 * 删除指定路径下的文件及文件夹
	 */
	public boolean removerDir(String path) {
		File detFile = new File(path);
		if (!detFile.exists())
			return false;
		return removeDir(detFile);
	}

	public static boolean removeDir(File dir) {
		if (dir == null)
			return false;
		boolean isDet = false;
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();

			for (int x = 0; x < files.length; x++) {
				if (files[x].isDirectory())
					removeDir(files[x]);
				else
					Log.e("wjg",
							"删除文件：：" + files[x] + "::::::" + files[x].delete());
			}
		}

		Log.e("wjg", "删除目录：：" + dir + "::::::" + (isDet = dir.delete()));
		return isDet;
	}
}
