package com.shendu.theme.ben;

import android.util.Log;

/**
 * 主题包中的描述文件封装成了一个ThemeDescription对象
 * 
 * @author wjg
 * 
 */
public class ThemeDescription {
	private String title;
	private String designer;
	private String author;
	private String version;
	private String uiVersion;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesigner() {
		return designer;
	}

	public void setDesigner(String designer) {
		this.designer = designer;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUiVersion() {
		return uiVersion;
	}

	public void setUiVersion(String uiVersion) {
		this.uiVersion = uiVersion;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.title + "::" + this.version + "::" + this.designer + "::"
				+ this.uiVersion + "::" + this.author;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if (o == null)
			return false;

		if (!(o instanceof ThemeDescription)) {
			return false;
		}
		ThemeDescription themeDes = (ThemeDescription) o;
		return this.title.equals(themeDes.title)
				&& this.author.equals(themeDes.author)
				&& this.designer.equals(themeDes.designer)
				&& this.uiVersion.equals(themeDes.uiVersion)
				&& this.version.equals(themeDes.version);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub

		Log.e("wjg",
				":::::ThemeDescription:::::::hashCode:::"
						+ (this.title.hashCode() + this.designer.hashCode()
								+ this.author.hashCode()
								+ this.uiVersion.hashCode() + this.version
								.hashCode() * 19) + ":::name::"
						+ this.toString());
		return this.title.hashCode() + this.designer.hashCode()
				+ this.author.hashCode() + this.uiVersion.hashCode()
				+ this.version.hashCode() * 19;
	}

}
