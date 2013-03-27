package com.shendu.theme.ben;

import android.graphics.Bitmap;

public class Picture {

	private String title;
	private int imageId;
	private Bitmap image;

	public Picture() {
		super();
	}

	public Picture(String title, Bitmap image) {
		super();
		this.title = title;
		this.image = image;
	}

	public Picture(String title, int imageId) {
		super();
		this.title = title;
		this.imageId = imageId;
	}

	public Bitmap getBitmap() {
		return image;
	}

	public void setBitmap(Bitmap image) {
		this.image = image;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
}
