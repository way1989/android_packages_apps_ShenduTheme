package com.shendu.theme.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shendu.theme.R;
import com.shendu.theme.ben.ModuleSelecet;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class ModuleAdapter extends BaseAdapter {
	private Context context;
	private List<ModuleSelecet> list;
	// private LayoutInflater mInflater;
	private String mCurrentThemePath;

	public ModuleAdapter(Context context, List<ModuleSelecet> list) {
		this.context = context;
		this.list = list;
		// mInflater = LayoutInflater.from(context);
	}

	public ModuleAdapter(Context context, String mCurrentThemePath) {
		this.context = context;
		this.mCurrentThemePath = mCurrentThemePath;
		this.list = new ArrayList<ModuleSelecet>();
		// mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (list != null && (list.size() > 0))
			return list.size();
		return 10;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (list != null)
			return list.get(position);
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
		if (convertView == null) {
			textView = new TextView(context);
			GridView.LayoutParams param = new GridView.LayoutParams(
					GridView.LayoutParams.WRAP_CONTENT,
					GridView.LayoutParams.WRAP_CONTENT);
			textView.setLayoutParams(param);
			textView.setGravity(Gravity.CENTER);
			if (list.size() > 0) {
				textView.setBackgroundResource(R.drawable.selected);
				textView.setText(list.get(position).getTitle());
			} else {
				textView.setBackgroundResource(R.drawable.unselected);
			}
		} else {
			textView = (TextView) convertView;
		}
		return textView;
	}

	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// TextView textView;
	// if (convertView == null) {
	// textView = new TextView(context);
	// GridView.LayoutParams param = new GridView.LayoutParams(
	// GridView.LayoutParams.WRAP_CONTENT,
	// GridView.LayoutParams.WRAP_CONTENT);
	// textView.setLayoutParams(param);
	// textView.setGravity(Gravity.CENTER);
	// textView.setText(list.get(position).getTitle());
	// textView.setBackgroundResource(R.drawable.selected);
	// } else {
	// textView = (TextView) convertView;
	// }
	// return textView;
	// }

}
