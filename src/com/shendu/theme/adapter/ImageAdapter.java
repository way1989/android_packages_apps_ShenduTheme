package com.shendu.theme.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import com.shendu.theme.R;
import com.shendu.utils.ZipUitls;

public class ImageAdapter extends BaseAdapter {

	private float mDensity;
	// 当前主题包绝对路径
	private static String zipPath = null;
	private Context mContext;
	// 记录图片名字
	private List<String> pathLists = null;
	// 解析出的图片
	public static Map<String, Bitmap> bmList = new HashMap<String, Bitmap>();
	// private static String zipPath=null;
//	private final int ITEM_WIDTH = 200;
//	private final int ITEM_HEIGHT = 333;
	
	    private int ITEM_WIDTH = 220;
	    private int ITEM_HEIGHT = 350;
	// 图片的边框
	private Bitmap previewBitmap = null;
	// 系统默认主题图
	public static int[] mSystemTheme = null;

	public ImageAdapter(Context context, List<String> path, String zipFilePath) {
		super();
		this.mContext = context;

		if (zipPath == null) {
			zipPath = zipFilePath;
		}
		// 清理上次缓存数据，避免内存开销
		if (!(zipPath.equals(zipFilePath))) {
			zipPath = zipFilePath;
			bmList.clear();
		}

		// 如果路径为空，表示默认选择为系统主题
		if (path == null) {
			mSystemTheme = new int[] { R.drawable.default_preview_launcher_1,
					R.drawable.default_preview_statusbar_1,
					R.drawable.default_preview_mms_1,
					R.drawable.default_preview_setting_1,
					R.drawable.default_preview_contact_1};
			// bmList = new HashMap<String, Bitmap>();
		} else {
			this.pathLists = path;
			// bmList = new HashMap<String, Bitmap>();
		}

		// TypedArray a = context.obtainStyledAttributes(R.styleable.Gallery1);
		// mGalleryItemBackground = a.getResourceId(
		// R.styleable.Gallery1_android_galleryItemBackground, 0);
		// a.recycle();
		mDensity = context.getResources().getDisplayMetrics().density;
		if(mDensity <= 1.5){
		    ITEM_WIDTH = 200;
		    ITEM_HEIGHT = 333;
		}
		//Toast.makeText(context, "density = " + mDensity, 0).show();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mSystemTheme != null) {
			return mSystemTheme.length;
		}
		if (pathLists != null)
			return pathLists.size();
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (mSystemTheme != null) {

			Resources r = mContext.getResources();

			if (r == null)
				return null;

			return BitmapFactory.decodeResource(r, mSystemTheme[position]);
		}
		return pathLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ImageView imageView = new ImageView(mContext);
		convertView = imageView;
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		imageView.setLayoutParams(new Gallery.LayoutParams((int) (ITEM_WIDTH
				* mDensity + 0.5f), (int) (ITEM_HEIGHT * mDensity + 0.5f)));
		// imageView.setBackgroundResource(mGalleryItemBackground);
		if (mSystemTheme != null) {
			imageView.setImageResource(mSystemTheme[position]);
			return imageView;
		}
		previewBitmap = bmList.get("" + position);
		if (previewBitmap != null) {
			imageView.setImageBitmap(previewBitmap);
		} else {
			new AsyncTask<String, Void, Bitmap>() {

				@Override
				protected void onPreExecute() {
					imageView.setImageResource(R.drawable.default_wallpaper);
					super.onPreExecute();
				}

				@Override
				protected void onPostExecute(Bitmap result) {
					if (result != null) {
						imageView.setImageBitmap(result);
					} else {
						imageView
								.setImageResource(R.drawable.default_wallpaper);
					}
					super.onPostExecute(result);
				}

				@Override
				protected void onProgressUpdate(Void... values) {
					super.onProgressUpdate(values);
				}

				@Override
				protected void onCancelled() {
					super.onCancelled();
				}

				@Override
				protected Bitmap doInBackground(String... params) {
					try {
						String path = params[0];
						Bitmap bitmap = ZipUitls.getImageFromZip(zipPath, path,
								(int) (ITEM_WIDTH * mDensity + 0.5f),
								(int) (ITEM_HEIGHT * mDensity + 0.5f));
						if (bitmap != null) {
							bmList.put("" + position, bitmap);
							Log.d("wjg",
									"-------getHeight-----"
											+ bitmap.getHeight());
							Log.d("wjg",
									"------getWidth------" + bitmap.getWidth());
						}
						return bitmap;
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}
			}.execute(pathLists.get(position));
		}
		return imageView;
	}
}
