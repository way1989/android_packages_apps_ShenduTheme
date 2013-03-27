package com.shendu.theme.ben;

import android.graphics.Bitmap;

/**
 * 解析主题包对象，得到的实例
 * @author wjg
 *
 */
public class LocalPreview {
	private Bitmap previewBitmap;
	private ThemeDescription themedes;
	private String themePath;
	public Bitmap getPreviewBitmap() {
		return previewBitmap;
	}
	public void setPreviewBitmap(Bitmap previewBitmap) {
		this.previewBitmap = previewBitmap;
	}
	public ThemeDescription getThemedes() {
		return themedes;
	}
	public void setThemedes(ThemeDescription themedes) {
		this.themedes = themedes;
	}
	public String getThemePath() {
		return themePath;
	}
	public void setThemePath(String themePath) {
		this.themePath = themePath;
	}
}
