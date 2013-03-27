package com.shendu.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.shendu.theme.LocalThemeActivity;
import com.shendu.theme.R;
import com.shendu.theme.ben.LocalPreview;
import com.shendu.theme.ben.ThemeDescription;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

public class PacketParser {
	private static final String TAG = "PacketParser";
	// 存放文件路径与文件名
	public static Map<String, String> mapPathAndName = new HashMap<String, String>();
	// 去重复
	private static Set<ThemeDescription> themeDescriptionSet = new HashSet<ThemeDescription>();

	/**
	 * @param context
	 *            应用程序上下文对象
	 * @param path
	 *            目录
	 * @param Suffix
	 *            查找主题包文件的后缀名
	 * @return ArratyList 包含所有包文件的絕對路徑
	 */
	public static Map<String, String> findPackege(Context context, String path,
			String suffix) {

		mapPathAndName.clear();

		Log.e("wjg", "::::::::查找主题包:::::::::");

		// 判断sdcar是否挂载
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			File themeFile = new File(path);
			Log.i("wjg", "主题包路径：:" + themeFile.getPath());
			if (themeFile.exists()) {
				File[] file = themeFile.listFiles();
				mapPathAndName.clear();
				for (int i = 0; i < file.length; i++) {
					if (file[i].isFile() && file[i].getName().endsWith(suffix)) {

						Log.i("wjg", "主题包名称：:" + file[i].getPath());

						mapPathAndName.put(file[i].getAbsolutePath(),
								file[i].getName());
					}
					Log.i("wjg", "扫描到" + mapPathAndName.size() + "主题包");
				}
				return mapPathAndName;
			}

		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			Log.d(TAG, "sdcard  only read");
		} else {
			Toast.makeText(context, "sdCard No Mount", Toast.LENGTH_SHORT)
					.show();
			Log.d(TAG, "sdcard NO Mount ");
			return null;
		}
		return null;
	}

	/**
	 * 查找主题包并去掉重复主题包
	 * 
	 * @param path
	 *            主题包所在的磁盘路径
	 * @param suffix
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> findPackege(String path, String suffix)
			throws Exception {
		Log.e("wjg", "查找主题包并去掉重复主题包");

		mapPathAndName.clear();

		File themeFile = new File(path);

		Log.i("wjg", "主题包所在路径：:" + themeFile.getPath());
		Log.i("wjg", "主题包所在路径是否存在？：:" + themeFile.exists());

		if (themeFile.exists()) {

			File[] files = themeFile.listFiles();

			for (File file : files) {

				if (file.isFile() && file.getName().endsWith(suffix)) {
					Log.i("wjg", "主题包名称：:" + file.getPath());
					// 1 获取主题描述文件对象
					String cruThemePath = file.getAbsolutePath();
					// 获取描述文件输入流
					InputStream ins = UpZip(cruThemePath,
							LocalThemeActivity.DESCRIPTIONFILE);
					// 解析描述文件 ThemeDescription 为主题包的描述信息
					ThemeDescription themedes = PullXmlParser(ins);

					Log.e("wjg", "---------xxxxxxxx------------"+(themedes==null));
					if (themedes == null) {
						continue;
					}
					// 去掉重复主题包
					if (themeDescriptionSet.add(themedes)) {
						// key : getAbsolutePath value : getName()
						mapPathAndName.put(cruThemePath, file.getName());
					} else {
						Log.e("wjg", "重复包名：：：" + file.getName());
						Log.e("wjg", "删除重复包：：：" + file.delete());
					}
				}
				Log.i("wjg", "扫描到" + mapPathAndName.size() + "主题包");
			}
			return mapPathAndName;
		}
		return null;
	}

	/**
	 * 清楚PacketParser对象中的static数据
	 */
	public static void clear() {
		themeDescriptionSet.clear();
		mapPathAndName.clear();
	}

	/**
	 * 解析主题包
	 * 
	 * @param packetPathAndName
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<LocalPreview> parserPacket(
			Map<String, String> packetPathAndName) throws Exception {

		if (packetPathAndName == null) {
			return null;
		}
		Log.e("wjg", "::::::::::::::::::::解析主题包中的资源::::::::::");
		ArrayList<LocalPreview> lpList = new ArrayList<LocalPreview>();
		int mapSize = packetPathAndName.size();

		Log.e("wjg", "mapSize" + mapSize);

		if (mapSize > 0) {
			Iterator<?> keyValuePairs1 = packetPathAndName.entrySet()
					.iterator();
			for (int i = 0; i < mapSize; i++) {
				Map.Entry entry = (Map.Entry) keyValuePairs1.next();
				// 文件绝对路径
				String key = (String) entry.getKey();
				// 获取描述文件输入流
				InputStream ins = UpZip(key, LocalThemeActivity.DESCRIPTIONFILE);
				// 解析描述文件 ThemeDescription 为主题包的描述信息

				ThemeDescription themedes = PullXmlParser(ins);
				if (themedes == null) {
					themedes = new ThemeDescription();
					String title = (String) entry.getValue();
					themedes.setTitle(title.substring(0, title.lastIndexOf(".")));// 默认给一个主题包文件名
					themedes.setDesigner("ShenDuOS");
					themedes.setAuthor("ShenDuOS");
					themedes.setUiVersion("1.0.0");
					themedes.setVersion("1.0.0");
				}

				// 获取主题包中preview目录中的一张预览图
				Bitmap image = getBitmap(key);
				LocalPreview lp = new LocalPreview();
				lp.setPreviewBitmap(image);
				lp.setThemedes(themedes);
				lp.setThemePath(key);
				lpList.add(lp);
			}
		}
		return lpList;
	}

	private static Bitmap parserMtzPacket(String zipFileString) {
		Log.w(TAG, "文件是=" + zipFileString);

		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFileString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (zipFile != null) {
			ZipEntry zipEntry = zipFile
					.getEntry("preview/preview_lockscreen_0.jpg");
			if (zipEntry == null) {
				Log.e(TAG, "null");
				return null;
			} else {

				Log.e(TAG, "文件名是=" + zipEntry.getName() + "-----:dir=:"
						+ zipEntry.isDirectory());
			}

			InputStream inputStream = null;
			try {
				inputStream = zipFile.getInputStream(zipEntry);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return createBitmap(inputStream);
		}

		return null;
	}

	/**
	 * 从 包中解析出预览图片
	 * 
	 * @param packetPathAndName
	 *            包的路径(key)与包名(value)
	 * @param num
	 *            图片的数目
	 * @return
	 * @throws IOException
	 */

	public static Map<String, ArrayList<Bitmap>> parserPacket(
			Map<String, String> packetPathAndName, int num) throws IOException {

		if (packetPathAndName == null) {
			return null;
		}
		Log.e("wjg", "::::::::::::::::::::parserPacket::::::::::");
		Map<String, ArrayList<Bitmap>> map = new HashMap<String, ArrayList<Bitmap>>();
		ArrayList<Bitmap> imageList = null;
		int mapSize = packetPathAndName.size();
		if (mapSize > 0) {
			Iterator<Entry<String, String>> keyValuePairs1 = packetPathAndName
					.entrySet().iterator();
			if (num > 0 && num < 15) {
				mapSize = num;
			}
			for (int i = 0; i < mapSize; i++) {
				Map.Entry entry = (Entry) keyValuePairs1.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				imageList = getBitmaps(key);
				map.put(value, imageList);
				map.put(key, imageList);
			}
		}
		return map;
	}

	/**
	 * 解析指定主题包中的所有图片资源
	 * 
	 * @param zipFileString
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<Bitmap> getBitmaps(String zipFileString)
			throws IOException {

		Log.w(TAG, "ZipFiles");

		if (zipFileString == null)
			return null;

		ArrayList<Bitmap> list = new ArrayList<Bitmap>();
		ZipFile file = new ZipFile(zipFileString);
		ZipEntry zipEntry;
		InputStream is;
		File fileName;
		String name;
		Enumeration<? extends ZipEntry> enumeration = file.entries();
		while (enumeration.hasMoreElements()) {
			zipEntry = enumeration.nextElement();
			Log.w(TAG, "zipEntry name:" + zipEntry.getName());
			String entryName = zipEntry.getName();
			fileName = new File(entryName);
			String praentPath = fileName.getParent();
			name = fileName.getName();
			if (praentPath != null && praentPath.equals("preview")) {
				if (name.endsWith(".jpg") | name.endsWith(".png")) {
					is = file.getInputStream(zipEntry);
					Bitmap bm = createBitmap(is);

					if (bm != null) {
						list.add(bm);
						Log.w(TAG, list.size() + "");
						break;
					}
				}
			}
		}
		return list;

	}

	/**
	 * 解析指定主题包中的一個图片资源
	 * 
	 * @param zipFileString
	 *            zip文件路径
	 * @return
	 * @throws IOException
	 */
	public static Bitmap getBitmap(String zipFileString) throws IOException {

		Log.w(TAG, "ZipFiles");

		if (zipFileString == null)
			return null;

		ArrayList<Bitmap> list = new ArrayList<Bitmap>();
		ZipFile file = new ZipFile(zipFileString);
		ZipEntry zipEntry;
		InputStream is;
		File fileName;
		String name;
		Bitmap bm = null;
		Enumeration<ZipEntry> enumeration = (Enumeration<ZipEntry>) file
				.entries();
		while (enumeration.hasMoreElements()) {
			zipEntry = enumeration.nextElement();
			Log.w(TAG, "zipEntry name:" + zipEntry.getName());
			String entryName = zipEntry.getName();
			fileName = new File(entryName);
			String praentPath = fileName.getParent();
			name = fileName.getName();
			if (praentPath != null && praentPath.equals("preview")) {
				if (name.endsWith(".jpg") | name.endsWith(".png")) {
					if (name.contains("launcher")) {
						Log.e("wjg", "预览图" + name);
						is = file.getInputStream(zipEntry);
						bm = createBitmap(is);
						if (is != null)
							is.close();
						if (bm != null) {
							return bm;
						}
					} else if (bm != null) {
						continue;
					} else {
						Log.e("wjg", "-------预览图" + name);
						is = file.getInputStream(zipEntry);
						bm = createBitmap(is);
						if (is != null)
							is.close();
					}

				}
			}
		}
		return bm;

	}

	/**
	 * 創建Bitmap
	 * 
	 * @param is
	 * @return
	 */
//	public static Bitmap createBitmap(InputStream is) {
//		if (is == null)
//			return null;
//
//		BitmapDrawable bmpDraw = new BitmapDrawable(is);
//		Bitmap bmp = bmpDraw.getBitmap();
//		return bmp;
//	}
	public static Bitmap createBitmap(InputStream is) {
		if (is == null)
			return null;
		Bitmap defBitmap = BitmapFactory.decodeStream(is);
		defBitmap = Bitmap.createScaledBitmap(defBitmap, 100, 150, true);
		return defBitmap;
	}

	/**
	 * 存储数据到Shared
	 */
	public static boolean WriteSharedPreferences(Context context,
			String strName, String strValue) {
		SharedPreferences user = context.getSharedPreferences("theme_info", 0);
		Editor editor = user.edit();
		editor.putString(strName, strValue);
		return editor.commit();
	}

	/**
	 * 存储数据到Shared 存储整个ThemeDescription对象的到shared中
	 * 
	 * @param context
	 *            上下文
	 * @param themeDes
	 *            主题包中的描述文件对象
	 * @return
	 */
	public static boolean WriteSharedPreferences(Context context,
			ThemeDescription themeDes) {
		SharedPreferences user = context.getSharedPreferences("theme_info", 0);
		Editor editor = user.edit();
		editor.putString("title", themeDes.getTitle());
		editor.putString("designer", themeDes.getDesigner());
		editor.putString("author", themeDes.getAuthor());
		editor.putString("version", themeDes.getVersion());
		editor.putString("uiVersion", themeDes.getUiVersion());
		return editor.commit();
	}

	/**
	 * 获取Shared数据 获取指定key的值
	 * 
	 * @param context
	 *            上下文
	 * @param strName
	 *            key
	 * @param strValue
	 *            value
	 * @return value
	 */
	public static String ReadSharedPreferences(Context context, String strName,
			String strValue) {
		String value = null;
		SharedPreferences user = context.getSharedPreferences("theme_info", 0);
		value = user.getString(strName, strValue);
		return value;
	}

	/**
	 * 从shared中获取所有ThemeDescription对象的属性值
	 * 
	 * @param context
	 * @param themeDes
	 *            ThemeDescription对象
	 * @return
	 */
	public static ThemeDescription ReadSharedPreferences(Context context,
			ThemeDescription themeDes) {
		if (themeDes == null)
			themeDes = new ThemeDescription();
		SharedPreferences user = context.getSharedPreferences("theme_info", 0);
		themeDes.setTitle(user.getString("title", null));
		themeDes.setTitle(user.getString("designer", null));
		themeDes.setTitle(user.getString("author", null));
		themeDes.setTitle(user.getString("version", null));
		themeDes.setTitle(user.getString("uiVersion", null));
		return themeDes;
	}

	// description.xml解析
	/**
	 * description.xml解析
	 * 
	 * @param ins
	 *            主题包中description.xml对象流
	 * @return ThemeDescription对象，包含主题包的描述信息
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static ThemeDescription PullXmlParser(InputStream ins)
			throws IOException, XmlPullParserException {

		if (ins == null) {
			return null;
		}
		ThemeDescription themedes = null;

		XmlPullParser parser = Xml.newPullParser(); // 由android.util.Xml创建一个XmlPullParser实例
		parser.setInput(ins, "UTF-8"); // 设置输入流 并指明编码方式

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				themedes = new ThemeDescription();
				break;
			case XmlPullParser.START_TAG:
				if (parser.getName().equals("title")) {
					eventType = parser.next();
					themedes.setTitle(parser.getText());
				} else if (parser.getName().equals("designer")) {
					eventType = parser.next();
					themedes.setDesigner(parser.getText());
				} else if (parser.getName().equals("author")) {
					eventType = parser.next();
					themedes.setAuthor(parser.getText());
				} else if (parser.getName().equals("version")) {
					eventType = parser.next();
					themedes.setVersion(parser.getText());
				} else if (parser.getName().equals("uiVersion")) {
					eventType = parser.next();
					themedes.setUiVersion(parser.getText());
				}
				break;
			}
			eventType = parser.next();
		}

		return themedes;

	}

	/**
	 * 获取描述文件，
	 * 
	 * @param zipFileString
	 *            zip文件路径
	 * @param fileString
	 *            zip文件名称
	 * @return 返回描述文件流
	 * @throws Exception
	 */
	public static InputStream UpZip(String zipFileString, String fileString)
			throws Exception {
		if (zipFileString == null)
			return null;
		if (!(new File(zipFileString).exists()))
			return null;
		ZipFile zipFile = new ZipFile(zipFileString);
		ZipEntry zipEntry = zipFile.getEntry(fileString);

		if (zipEntry == null)
			return null;

		return zipFile.getInputStream(zipEntry);

	}

	/**
	 * byte数组转换成16进制字符串
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

}
