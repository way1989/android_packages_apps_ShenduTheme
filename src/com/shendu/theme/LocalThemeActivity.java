package com.shendu.theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.shendu.theme.R;
import com.shendu.theme.adapter.PictureAdapter;
import com.shendu.theme.ben.LocalPreview;
import com.shendu.theme.ben.Picture;
import com.shendu.utils.PacketParser;

public class LocalThemeActivity extends Activity implements OnItemClickListener {
	private final int REQUESTCODE_ADAPTER_ONITEMCLICK = 0x08;
	private final int ONOPTIONSITEMSELECTED = 0x09;
	private ProgressDialog mProgressDialog = null;
	private GridView gridView;
	// 用于传递主题包绝对路径
	public static final String KEY = "PATH";
	public static final String TITLE = "NAME";
	private static final String mDefaultThemePath = "/系统主题.zip";
	public static String defaultThemeName = "系统主题";

	public static final String DESCRIPTIONFILE = "description.xml";
	// Sdcard theme root directory
	private static String mSDcardThemeParentPath = File.separator + "mnt"
			+ File.separator + "sdcard" + File.separator + "shendu"
			+ File.separator + "theme";
	// System theme root directory
	private static final String mSystemThemeParentPath = File.separator
			+ "system" + File.separator + "media" + File.separator + "theme";
	// 存放所有主题的描述信息
	private ArrayList<LocalPreview> systemThemeList;
	// 存放所有主题的描述信息
	private ArrayList<LocalPreview> sdcardThemeList;
	// 主题包中的预览图片
	private ArrayList<Bitmap> image = new ArrayList<Bitmap>();
	// 主题包名称
	private List<String> mThemeFileName = new ArrayList<String>();
	// 主题包路径
	public List<String> mThemeFilePath = new ArrayList<String>();

	private PictureAdapter adapter;

	private List<Picture> pictures;

	private int currentPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.localtheme);
		defaultThemeName = getString(R.string.system_Internal_Theme);
		pictures = new ArrayList<Picture>();
		 try {
		 getThemeResources();
		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		 initAdapte();
//		List list = ThemeHelp.getSystemTheme(mSystemThemeParentPath, ".zip");
//		Log.e("wjg", "主题缓存路径：："+ThemeHelp.getCurrentCachePath());
	}

	static {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can read and write the media

			mSDcardThemeParentPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ "shendu"
					+ File.separator + "theme";
		} else {
			// Determine whether there is a built-in sdcard
			if (SystemProperties.get("ro.vold.switchablepair", "").equals("")) {
				// No Internal sdcard
			} else {
				mSDcardThemeParentPath = getDirectory("INTERNAL_STORAGE",
						"/storage/sdcard1").getAbsoluteFile()
						+ File.separator + "shendu" + File.separator + "theme";
			}
		}

	}

	static File getDirectory(String variableName, String defaultPath) {
		String path = System.getenv(variableName);
		return path == null ? new File(defaultPath) : new File(path);
	}

	private void initAdapte() {
		// TODO Auto-generated method stub
		gridView = (GridView) findViewById(R.id.local_gridview);
		// adapter = new PictureAdapter(mThemeFileName, image, this);
		adapter = new PictureAdapter(pictures, this);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);
	}

	/**
	 * 获取主题
	 * 
	 * @throws Exception
	 */
	private void getThemeResources() throws Exception {

		mThemeFilePath.clear();
		mThemeFileName.clear();
		pictures.clear();
		image.clear();
		// Scaling Bitmap
		BitmapFactory.Options option = new BitmapFactory.Options();
		// option.inSampleSize = 5;
		Bitmap defBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.default_preview_launcher_1, option);
		int width = (int)getResources().getDimension(R.dimen.prv_width);
		int height = (int) getResources().getDimension(R.dimen.prv_height);
		defBitmap = Bitmap.createScaledBitmap(defBitmap, width, height, true);
		if (defBitmap != null) {
			mThemeFileName.add(defaultThemeName);
			mThemeFilePath.add(mDefaultThemePath);
			image.add(defBitmap);
		}
		// 1 找包
		/**
		 * 一 先遍历所有指定目录的主题包+排除重复包 1 定义一个集合用于存放 所有主题包，，，，排除重复
		 */
		// 2 拍除重复包
		// 扫描系统目录
		File systemThemePaht = new File(mSystemThemeParentPath);
		if (systemThemePaht.exists()) {
			Map<String, String> systemShenDuMap = PacketParser.findPackege(
					mSystemThemeParentPath, ".zip");

			try {
				systemThemeList = PacketParser.parserPacket(systemShenDuMap);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (systemThemeList.size() > 0) {
				for (int i = 0; i < systemThemeList.size(); i++) {
					LocalPreview lp = systemThemeList.get(i);
					mThemeFileName.add(lp.getThemedes().getTitle());
					mThemeFilePath.add(lp.getThemePath());
					image.add(lp.getPreviewBitmap());
				}

				systemThemeList.clear();
			} else {
				Toast.makeText(this, "系统目录没有主题资源", 0).show();
			}
		} else {
			boolean isMakdir = systemThemePaht.mkdirs();
			if (isMakdir) {
				Log.e("wjg", "创建" + mSystemThemeParentPath + "目录成功...");
			} else {
				Log.e("wjg", "创建" + mSystemThemeParentPath + "目录失败...");
			}
		}

		// SDcard 扫描部分
		// 1查找指定路径的主题包
		Map<String, String> shenDuMap = PacketParser.findPackege(
				mSDcardThemeParentPath, ".zip");
		try {
			sdcardThemeList = PacketParser.parserPacket(shenDuMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (sdcardThemeList != null && sdcardThemeList.size() > 0) {
			int mapSize = sdcardThemeList.size();
			if (mapSize > 0) {

				for (int i = 0; i < sdcardThemeList.size(); i++) {
					LocalPreview lp = sdcardThemeList.get(i);
					mThemeFileName.add(lp.getThemedes().getTitle());
					mThemeFilePath.add(lp.getThemePath());
					image.add(lp.getPreviewBitmap());
				}
			}
		} else {
			Log.e("wjg", "::::::::::::::::Sdcard 下沒有主題資源::::::::::::::::");
		}
		for (int i = 0; i < image.size(); i++) {
			Picture picture = new Picture(mThemeFileName.get(i), image.get(i));
			pictures.add(picture);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_shen_du__main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		currentPosition = position;
		// 跳到详细Theme Activity页面
		// 携带数据，数据是选择主题包的绝对路径
		Intent intent = new Intent(LocalThemeActivity.this,
				DetailThemeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(KEY, mThemeFilePath.get(position)); // file path
		intent.putExtra(TITLE, mThemeFileName.get(position));
		startActivityForResult(intent, REQUESTCODE_ADAPTER_ONITEMCLICK);
	}

	// 导入主题包
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		/*
		 * if (item.getItemId() == R.id.menu_settings) { Log.e("wjg",
		 * ":::::::::::onOptionsItemSelected:::::::");
		 * 
		 * Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		 * intent.setType("application/zip"); startActivityForResult(intent, 1);
		 * 
		 * }
		 */
		return true;
	}

	// 导入主题包
	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		switch (requestCode) {
		case REQUESTCODE_ADAPTER_ONITEMCLICK:
			// Log.e("wjg", "_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+");
			if (resultCode == RESULT_OK)
				notifyDataSetChanged();
			break;
		case ONOPTIONSITEMSELECTED:
			if (data != null) {
				final Uri uri = data.getData();
				Log.e("wjg", "导入主题包");
				final String path = uri.getPath();
				final File srcFile = new File(path);

				if (srcFile.exists() && srcFile.isFile()) {
					// 判断是否是zip文件
					byte[] head = new byte[4];
					try {
						FileInputStream is = new FileInputStream(srcFile);
						is.read(head, 0, head.length);
						String type = PacketParser.bytesToHexString(head)
								.toUpperCase();
						if (type.contains("504B0304")) {
							// 判断是否是深度主题包
							InputStream ins = PacketParser.UpZip(path,
									DESCRIPTIONFILE);
							if (ins == null) {
								Toast.makeText(this, "错误选择", 0).show();
								Log.e("wjg", "不是深度主题");
								return;
							}
							Log.e("wjg", "以选择主题包：：" + path);

							new Thread(new Runnable() {

								@Override
								public void run() {
									Log.e("wjg",
											":::::::::::::::::::::开始导入主题包::::::::::::::::");
									mHandlerDialog.sendEmptyMessage(1);
									File desFile = new File(
											"/mnt/sdcard/shendu/theme"
													+ uri.getPath()
														.substring(uri.getPath().lastIndexOf("/")));

									if (!desFile.exists()) {
										try {
											desFile.createNewFile();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									try {
										FileInputStream is = new FileInputStream(
												srcFile);
										FileOutputStream os = new FileOutputStream(
												desFile);
										byte[] buffer = new byte[1024 * 20];
										int len = 0;
										try {
											while ((len = is.read(buffer)) != -1) {
												os.write(buffer);
											}
											if (is != null)
												is.close();
											if (os != null)
												os.close();
											Log.e("wjg", "导入完毕");
											mHandlerDialog.sendEmptyMessage(3);
											// srcFile.delete();
											mHandlerDialog.sendEmptyMessage(2);
										} catch (IOException e) {
											e.printStackTrace();
										}
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}
								}
							}).start();
							if (ins != null)
								ins.close();
						} else {
							Log.e("wjg", "不是ZIP文件");
							return;
						}

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			break;
		default:
			break;
		}
	}

	Handler mHandlerDialog = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO
			switch (msg.what) {
			case 1:
				mProgressDialog = new ProgressDialog(LocalThemeActivity.this);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mProgressDialog.setTitle("导入中...");
				mProgressDialog.setMessage("请稍等...");
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setCancelable(false);
				mProgressDialog.show();
				break;
			case 2:
				if (mProgressDialog != null) {
					if (mProgressDialog.isShowing()) {
						mProgressDialog.cancel();
						mProgressDialog = null;
					}
				}
				break;
			case 3:
				adapter.notifyDataSetChanged();
				Log.d("wjg", "数据更改了");
				break;
			}
		}
	};

	/**
	 * The theme preview map number change notice change, UI
	 */
	public void notifyDataSetChanged() {
		if (pictures != null && pictures.size() > 0) {
			pictures.remove(currentPosition);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		PacketParser.clear();
		super.onDestroy();

	}

}
