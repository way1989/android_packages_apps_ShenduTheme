package com.shendu.theme;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TabHost;

public class ShenDu_MainActivity extends TabActivity {
	private TabHost tabHost;
	TabHost.TabSpec spec;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	/**
	 * 初始化Tab
	 */
	private void init() {
		// TODO Auto-generated method stub
		tabHost = getTabHost();

		Intent localIntent = new Intent(this, LocalThemeActivity.class);
		spec = tabHost.newTabSpec("local")
				.setIndicator(getString(R.string.local), null)
				.setContent(localIntent);
		tabHost.addTab(spec);

		Intent netwhorkIntent = new Intent(this, NetWorkTheme.class);
		spec = tabHost.newTabSpec("net")
				.setIndicator(getString(R.string.net), null)
				.setContent(netwhorkIntent);
		tabHost.addTab(spec);
		tabHost.setCurrentTab(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_shen_du__main, menu);
		return true;
	}
}