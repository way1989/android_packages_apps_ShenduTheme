package com.shendu.theme.adapter;

import java.util.ArrayList;
import java.util.List;

import com.shendu.theme.LocalThemeActivity;
import com.shendu.theme.ben.Picture;
import com.shendu.utils.PacketParser;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.shendu.theme.R;

public class PictureAdapter extends BaseAdapter {
	private String system_Theme_Name;
	private LayoutInflater inflater;
	private List<Picture> pictures;
	private Context context;

	public PictureAdapter(List<String> titles, ArrayList<Bitmap> images,
			Context context) {
		super();
		this.context = context;
		pictures = new ArrayList<Picture>();
		inflater = LayoutInflater.from(context);

		if (titles == null && images == null) {
			return;
		}
		for (int i = 0; i < images.size(); i++) {
			Picture picture = new Picture(titles.get(i), images.get(i));
			pictures.add(picture);
		}
		system_Theme_Name = context.getString(R.string.system_Internal_Theme);
	}

	public PictureAdapter(List<Picture> pictures, Context context) {
		super();
		this.context = context;
		inflater = LayoutInflater.from(context);
		this.pictures = pictures;
		system_Theme_Name = context.getString(R.string.system_Internal_Theme);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		Log.d("wjg","====="+pictures.size());
			return pictures.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return pictures.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {

			// 从SharedPreferences中获取当前应用哪套主题，如果没有数据，默认返回 “系统主题”
			String theme_name = PacketParser.ReadSharedPreferences(context,
					"title", context.getString(R.string.system_Internal_Theme));

			// 判断是否是当前使用主题
			if (!(pictures.get(position).getTitle().equals(theme_name))) {
				convertView = inflater.inflate(R.layout.picture_item, null);
				viewHolder = new ViewHolder();
				viewHolder.title = (TextView) convertView
						.findViewById(R.id.title);
				viewHolder.big_image = (ImageView) convertView
						.findViewById(R.id.big_image);
				convertView.setTag(viewHolder);
			} else {
				convertView = inflater.inflate(R.layout.picture_item, null);
				viewHolder = new ViewHolder();
				viewHolder.title = (TextView) convertView
						.findViewById(R.id.title);
				viewHolder.big_image = (ImageView) convertView
						.findViewById(R.id.big_image);
				viewHolder.small_image = (ImageView) convertView
						.findViewById(R.id.small_image);
				convertView.setTag(viewHolder);
			}
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.title.setText(pictures.get(position).getTitle());
		viewHolder.big_image.setImageBitmap(pictures.get(position).getBitmap());
		if (viewHolder.small_image != null) {
			viewHolder.small_image.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	public void removeIntem(int position) {
		if (pictures != null && pictures.size() > 0) {
			pictures.remove(position);
		}
	}

	class ViewHolder {
		public TextView title;
		public ImageView big_image;
		public ImageView small_image;
	}
}
