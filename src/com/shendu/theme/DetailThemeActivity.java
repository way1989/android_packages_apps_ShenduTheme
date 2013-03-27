package com.shendu.theme;

import android.content.res.CustomTheme;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shendu.theme.adapter.ImageAdapter;
import com.shendu.theme.adapter.ModuleAdapter;
import com.shendu.theme.ben.ModuleSelecet;
import com.shendu.theme.ben.ThemeDescription;
import com.shendu.utils.PacketParser;
import com.shendu.utils.SuCommander;
import com.shendu.utils.ZipUitls;
import com.shendu.view.MGallery;

@SuppressLint("NewApi")
public class DetailThemeActivity extends Activity implements OnClickListener {
	private boolean isDdbug = true;
	// dialog ...
	private static final int DIALOG1_KEY = 0;
	private static final int DIALOG2_KEY = 1;
	// 应用主题按钮
	private Button mButtonApp;
	// 恢复主题按钮
	// private Button mButtonRec;
	private ProgressDialog mProgressDialog = null;
	// 当前选择主题包路径
	private String mCurrentThemePath;
	// 当前选择主题包名称
	private String mCurrentThemeName;
	private boolean isCurrentThemePath = false;
	// 主题包中preview目录的预览图片路径
	private ArrayList<String> imagePath = null;
	// 缩放部分
	private DisplayMetrics dm;
	private boolean isScale = true;
	private int scrWidht;
	private int scrHeight;
	Matrix srcMatrix;
	private ImageView bigImageView, mFenYeView;
	// 滑动小点
	private LinearLayout mlinearLayoutDot;
	private MGallery mGallery;
	private ActionBar bar;

	private ImageButton imagebtn_info;
	private ImageButton imagebtn_del;

	private LayoutInflater mInflater;
	// 需要动态变化的布局 预览和模块部分
	private LinearLayout mParent_ModuleView, detailBottom;
	// 模块预览图
	private FrameLayout mModul_PreView;

	// 模块选择
	private LinearLayout moduleLayout;
	private List<String> selecetName = null;
	private List<ModuleSelecet> moduleName = null;
	// 判断是否是系统主题
	private boolean isSystemTheme = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获取屏幕宽高
		Display display = getWindowManager().getDefaultDisplay();
		scrWidht = display.getHeight();
		scrHeight = display.getWidth();

		// 获取数据（主题路径与title）
		Intent intent = getIntent();
		mCurrentThemePath = intent.getStringExtra(LocalThemeActivity.KEY);
		mCurrentThemeName = intent.getStringExtra(LocalThemeActivity.TITLE);
		if (isDdbug)
			Log.e("wjg", "选择主题包路径：" + mCurrentThemePath);
		setContentView(R.layout.detail);

		// 初始化父布局
		ininParenLayout();
		// 初始化父布局中的按钮布局及事件
		ininParentClickView();
		// 获取父布局中的ActionBar
		mParentActionBar();
		// 初始化预览模块布局
		initModulePreview();
		registerForContextMenu(mGallery);
		mGetModuleNameTheme();
	}

	/**
	 * 初始化父布局元素
	 */
	private void ininParenLayout() {
		mParent_ModuleView = (LinearLayout) findViewById(R.id.parentLinearLayout);
		// 这个对象用于查找其他布局资源
		mInflater = LayoutInflater.from(this);
	}

	/**
	 * 获取父布局中的ActionBar
	 */
	private void mParentActionBar() {
		// TODO Auto-generated method stub
		// 回退按钮
		bar = getActionBar();
		bar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_TITLE);
		bar.setTitle(mCurrentThemeName);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// 当Action Bar的图标被单击时执行下面的Intent
			DetailThemeActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 初始化父布局中的按钮布局及事件
	 */
	private void ininParentClickView() {

		detailBottom = (LinearLayout) findViewById(R.id.detail_bottom);
		if (new File(mCurrentThemePath).exists()) {

			// 模块按钮
			imagebtn_info = (ImageButton) findViewById(R.id.img_info);
			imagebtn_info.setVisibility(View.VISIBLE);
			// 删除按钮
			imagebtn_del = (ImageButton) findViewById(R.id.img_del);
			imagebtn_del.setVisibility(View.VISIBLE);

			imagebtn_info.setBackgroundResource(R.drawable.information);
			imagebtn_del.setBackgroundResource(R.drawable.del);

			imagebtn_info.setOnClickListener(new ImageButtonListener());
			imagebtn_del.setOnClickListener(new ImageButtonListener());
		}
		// 应用--恢复--按钮
		mButtonApp = (Button) findViewById(R.id.buttonApp);
		mButtonApp.setOnClickListener(this);

		bigImageView = (ImageView) findViewById(R.id.imageView1);
		bigImageView.setBackgroundColor(0xFF848284);
	}

	/**
	 * 初始化预览模块布局
	 */
	private void initModulePreview() {
		mModul_PreView = (FrameLayout) mInflater.inflate(R.layout.modu_preview,
				null).findViewById(R.id.preview);

		mGallery = (MGallery) mModul_PreView.findViewById(R.id.gallery1);
		mlinearLayoutDot = (LinearLayout) mModul_PreView
				.findViewById(R.id.dot1);
		File file = new File(mCurrentThemePath);
		imagePath = ZipUitls.parseZip(file, null);
		mGallery.setAdapter(new ImageAdapter(this, imagePath, mCurrentThemePath));

		// 添加试图到父布局中
		mParent_ModuleView.removeAllViews();
		mParent_ModuleView.addView(mModul_PreView);

		if (imagePath == null) {
			initDotlayout((int) mGallery.getSelectedItemId(),
					ImageAdapter.mSystemTheme.length);
			isCurrentThemePath = true;
		} else {
			initDotlayout((int) mGallery.getSelectedItemId(), imagePath.size());
			isCurrentThemePath = false;
		}
		// 小点的移动
		mGallery.setOnItemSelectedListener(new GalleryOnItemSelectedListener(
				mGallery.getSelectedItemPosition()));
		// 图片放大
		mGallery.setOnItemClickListener(new GalleryOnItemClickListener());
	}

	// 小点移动操作部分
	private class GalleryOnItemSelectedListener implements
			OnItemSelectedListener {
		private ImageView dotImage;
		private int last;

		public GalleryOnItemSelectedListener(int last) {
			this.last = last;
		}

		private void setDotlayout(int id) {
			dotImage = (ImageView) mlinearLayoutDot.getChildAt(id);
			dotImage.setBackgroundResource(R.drawable.ic_viewpager_on);
			last = id;
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			dotImage = (ImageView) mlinearLayoutDot.getChildAt(last);
			dotImage.setBackgroundResource(R.drawable.ic_viewpager_off);
			setDotlayout(position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}

	}

	// 图片放大部分
	private class GalleryOnItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			SystemClock.sleep(100);
			bar.hide();
			Bitmap bm = null;
			if (imagePath == null) {
				bm = BitmapFactory.decodeResource(getResources(),
						ImageAdapter.mSystemTheme[position]);
			} else {
				bm = ImageAdapter.bmList.get("" + position);
			}
			if (bm != null) {
				WindowManager.LayoutParams params = getWindow().getAttributes();
				params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				getWindow().setAttributes(params);
				getWindow().addFlags(
						WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

				srcMatrix = bigImageView.getMatrix();
				Matrix matrix = new Matrix();
				int width = bm.getWidth();
				int height = bm.getHeight();
				float scaleWidth = ((float) scrWidht) / width;
				float scaleHeight = ((float) scrHeight) / height;
				matrix.postScale(scaleWidth, scaleHeight);

				bigImageView.setImageMatrix(matrix);
				bigImageView.setScaleType(ImageView.ScaleType.FIT_XY);
				bigImageView.setImageBitmap(bm);
				bigImageView.setVisibility(View.VISIBLE);
				bigImageView.bringToFront();
				bigImageView.setClickable(true);
				v.setDrawingCacheEnabled(false);
				detailBottom.setVisibility(View.GONE);
				bigImageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						bar.show();
						WindowManager.LayoutParams params = getWindow()
								.getAttributes();
						params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
						getWindow().setAttributes(params);
						getWindow()
								.clearFlags(
										WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

						bigImageView.setVisibility(View.GONE);
						v.setDrawingCacheEnabled(false);
						detailBottom.setVisibility(View.VISIBLE);
					}
				});
			}
		}
	}

	// gallery 下面的小点布局
	private void initDotlayout(int id, int length) {
		mlinearLayoutDot.removeAllViews();
		for (int i = 0; i < length; i++) {
			mFenYeView = new ImageView(DetailThemeActivity.this);
			mFenYeView.setBackgroundResource(R.drawable.ic_viewpager_off);
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(17,
					17);
			param.setMargins(5, 0, 5, 0);
			mFenYeView.setLayoutParams(param);
			mFenYeView.setId(i);
			if (mFenYeView.getId() == id) {
				mFenYeView.setBackgroundResource(R.drawable.ic_viewpager_on);
			}
			mlinearLayoutDot.addView(mFenYeView);
		}
	}

	Handler mHandlerDialog = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				mProgressDialog = new ProgressDialog(DetailThemeActivity.this);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mProgressDialog
						.setTitle(getString(R.string.progressDialog_title_app));
				mProgressDialog
						.setMessage(getString(R.string.progressDialog_message));
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setCancelable(false);
				mProgressDialog.show();
				break;
			case 2:
				mProgressDialog = new ProgressDialog(DetailThemeActivity.this);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mProgressDialog
						.setTitle(getString(R.string.progressDialog_title_app));
				mProgressDialog
						.setMessage(getString(R.string.progressDialog_message));
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setCancelable(false);
				mProgressDialog.show();
				break;
			case 3:
				if (mProgressDialog != null) {
					if (mProgressDialog.isShowing()) {
						mProgressDialog.cancel();
					}
				}
				break;
			case 4:
				dialog("请选择", "提示", "确定", null, null);
				break;
			}
		}
	};

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.buttonApp:
			if (isCurrentThemePath) {
				Thread rec = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							mHandlerDialog.sendEmptyMessage(2);
							SystemClock.sleep(500);
							boolean done = true;
							SuCommander suComm = null;
							try {
								suComm = new SuCommander();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							// 主题---------shell命令操作部分
							ArrayList<String> cmds_re = new ArrayList<String>();
							cmds_re.clear();
							cmds_re.add("rm -rf /data/system/theme");
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
										output = output.trim()
												.replace("\n", "");
										if (output.length() > 2) {
										}
									}
									String errors = suComm.getErrors();
									if (errors != null) {
										Log.e("wjg",
												"shell Command line Error::"
														+ errors);
									}
								} else {
									done = false;
									recoveryWallpaper();
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
							mHandlerDialog.sendEmptyMessage(3);
						}
					}
				});
				rec.start();
			} else {
				Theme thread = new Theme();
				thread.start();
			}
		}
	}

	private class Theme extends Thread {
		@Override
		public void run() {
			try {
				ArrayList<String> cmds = new ArrayList<String>();
				if (selecetName != null && selecetName.size() > 0
						&& moduleName != null) {
					if (moduleName.size() == selecetName.size()) {
						mHandlerDialog.sendEmptyMessage(4);
						return;
					}
					mHandlerDialog.sendEmptyMessage(1);
					String name;
					StringBuilder exclude = new StringBuilder();
					exclude.append(" -x preview/*");

					for (int i = 0; i < moduleName.size(); i++) {
						if (selecetName.contains(moduleName.get(i).getTitle())) {
							exclude.append(" ");
							name = moduleName.get(i).getName();
							exclude.append(name);
						}
					}
					cmds.add("rm -rf /data/system/theme");
					cmds.add("mkdir /data/system/theme");
					cmds.add("unzip -n " + mCurrentThemePath
							+ exclude.toString() + " -d /data/system/theme");
					cmds.add("chmod -R 777 /data/system/theme");
				} else {
					mHandlerDialog.sendEmptyMessage(1);
					cmds.add("rm -rf /data/system/theme");
					cmds.add("mkdir /data/system/theme");
					cmds.add("unzip -n " + mCurrentThemePath
							+ " -x preview/*  -d /data/system/theme");
					cmds.add("chmod -R 777 /data/system/theme");
				}
				boolean done = true;
				SuCommander suComm = null;
				try {
					suComm = new SuCommander();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				while (done) {
					if (!cmds.isEmpty()) {
						done = true;
						String c = cmds.remove(0);
						suComm.exec(c);
						while (!suComm.isReady()) {
							SystemClock.sleep(200);
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
						}
					} else {
						done = false;
						setCurrentTheme();
					}
				}
			} catch (IOException e) {
				Log.e("wjg", "-------------------------33333--");
				e.printStackTrace();
				mHandlerDialog.sendEmptyMessage(3);
			}

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
					wall = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
					wall.setStream(new FileInputStream(wallpaper));
				}
				// 获取当前主题名称 需要去解析xml文件 description.xml
				ThemeDescription themedes = null;
				try {
					InputStream ins = PacketParser.UpZip(mCurrentThemePath,
							LocalThemeActivity.DESCRIPTIONFILE);
					themedes = PacketParser.PullXmlParser(ins);
				} catch (Exception e1) {
					mHandlerDialog.sendEmptyMessage(3);
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
							DetailThemeActivity.this, themedes);
				} else {

					boolean isWrite = PacketParser.WriteSharedPreferences(
							DetailThemeActivity.this, themedes);
				}

			} catch (Exception e) {
			}

			ActivityManager am = (ActivityManager) DetailThemeActivity.this
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

	/**
	 * 换壁纸
	 */
	private void recoveryWallpaper() {
		// 获取当前主题名称 需要去解析xml文件 description.xml
		ThemeDescription themedes = null;
		try {
			InputStream ins = PacketParser.UpZip(mCurrentThemePath,
					LocalThemeActivity.DESCRIPTIONFILE);
			themedes = PacketParser.PullXmlParser(ins);
		} catch (Exception e1) {
			mHandlerDialog.sendEmptyMessage(3);
			e1.printStackTrace();
		}

		// 把主题包名称写到，SharedPrefereces中
		if (themedes == null) {
			themedes = new ThemeDescription();
			themedes.setTitle(getString(R.string.system_Internal_Theme));// 默认给一个主题包文件名
			themedes.setDesigner("ShenDuOS");
			themedes.setAuthor("ShenDuOS");
			themedes.setUiVersion("1.0.0");
			themedes.setVersion("1.0.0");
			boolean isWrite = PacketParser.WriteSharedPreferences(
					DetailThemeActivity.this, themedes);
		} else {
			boolean isWrite = PacketParser.WriteSharedPreferences(
					DetailThemeActivity.this, themedes);
		}
		WallpaperManager wall = null;
		try {
			wall = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
			wall.clear();
		} catch (IOException e) {
			mHandlerDialog.sendEmptyMessage(3);
			e.printStackTrace();
		}
		ActivityManager am = (ActivityManager) DetailThemeActivity.this
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		// 清除原生主题图片资源，在ImageAdapter中，避免占用过多的内存
		ImageAdapter.mSystemTheme = null;
	}

	private ModuleAdapter moduleAdapter = null;

	// 处理模块按钮事件
	private class ImageButtonListener implements View.OnClickListener {
		boolean info = true, del = true;

		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.img_info:
				if (info) {
					imagebtn_info
							.setBackgroundResource(R.drawable.information_1);
					mParent_ModuleView.removeAllViews();
					// 切换主题模块选择布局
					ininModuleGrid();
					info = false;
				} else {
					imagebtn_info.setBackgroundResource(R.drawable.information);
					mParent_ModuleView.removeAllViews();
					initModulePreview();
					info = true;
				}
				break;
			case R.id.img_del:

				if (mCurrentThemePath.startsWith("/system/media")) {
					dialog(getString(R.string.dialog_system_message),
							getString(R.string.dialog_system_title),
							getString(R.string.dialog_system_negativeButton),
							null, null);
					break;
				}else{
				    dialog(getString(R.string.dialog_sdcard_message),
                            getString(R.string.dialog_system_title),
                            getString(R.string.dialog_system_neutralButton),
                            getString(R.string.dialog_system_negativeButton),
                            mCurrentThemePath);
				}
				/*if (mCurrentThemePath.startsWith("/mnt/sdcard")) {
					dialog(getString(R.string.dialog_sdcard_message),
							getString(R.string.dialog_system_title),
							getString(R.string.dialog_system_neutralButton),
							getString(R.string.dialog_system_negativeButton),
							mCurrentThemePath);
				}*/
				break;
			}
		}

		/**
		 * create Module layout
		 */
		private void ininModuleGrid() {

			while (true) {
				if (moduleName != null)
					break;
				SystemClock.sleep(200);
			}
			selecetName = new ArrayList<String>();
			moduleLayout = (LinearLayout) mInflater.inflate(R.layout.modu_grid,
					null);
			GridView mModul_GridView = (GridView) moduleLayout
					.findViewById(R.id.myGrid);
			final String title = getString(R.string.selected);
			final TextView mTextViewSelected = (TextView) moduleLayout
					.findViewById(R.id.modul_description);
			mTextViewSelected.setText(title + "(" + moduleName.size() + ")");
			moduleAdapter = new ModuleAdapter(DetailThemeActivity.this,
					moduleName);
			mModul_GridView.setAdapter(moduleAdapter);
			mParent_ModuleView.addView(moduleLayout);

			mModul_GridView
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						TextView textView;

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							// TODO Auto-generated method stub
							textView = (TextView) view;

							if (selecetName.contains(textView.getText())) {
								textView.setBackgroundResource(R.drawable.selected);
								selecetName.remove((String) textView.getText());
							} else {
								textView.setBackgroundResource(R.drawable.unselected);
								selecetName.add((String) textView.getText());
							}
							mTextViewSelected.setText(title + "("
									+ (moduleName.size() - selecetName.size())
									+ ")");
						}
					});
		}
	}

	/**
	 * init Module name
	 */
	private void mGetModuleNameTheme() {
		/**
		 * 获取主题包中的各个模块的名称
		 */
		Thread mGetModuleNameThread = new Thread() {
			public void run() {
				try {
					moduleName = ZipUitls.getModuleName(mCurrentThemePath);
					if (moduleAdapter != null) {
						moduleAdapter.notifyDataSetChanged();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		mGetModuleNameThread.start();
	}

	/**
	 * create a AlertDialog
	 * 
	 * @param message
	 * @param title
	 * @param negativeButton
	 * @param neutralButton
	 * @param deleteFilePath
	 */
	private void dialog(String message, String title, String negativeButton,
			String neutralButton, final String deleteFilePath) {
		imagebtn_del.setBackgroundResource(R.drawable.del_1);
		AlertDialog.Builder builder = new Builder(DetailThemeActivity.this);
		builder.setMessage(message);

		builder.setTitle(title);
		if (neutralButton != null && neutralButton.length() > 0
				&& neutralButton.equals("确定") && deleteFilePath != null) {

			builder.setNeutralButton(neutralButton,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							File deleFile = new File(deleteFilePath);
							if (deleFile.exists() && deleFile.isFile()) {
								if (deleFile.delete()) {
									DetailThemeActivity.this.setResult(
											RESULT_OK, null);
									notifyLauncherChanged();
									DetailThemeActivity.this.finish();
								}
							}
						}
					});
		}

		builder.setNegativeButton(negativeButton,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						imagebtn_del.setBackgroundResource(R.drawable.del);
					}
				});
		builder.setCancelable(false);
		builder.create().show();
	}

	/**
	 * Data change notification Launcher
	 */
	private void notifyLauncherChanged() {
		Intent intent = new Intent(
				"com.shendu.theme.LauncherBroadcast_parser_perview_Action");
		sendBroadcast(intent);
	}
}
