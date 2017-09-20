package cn.shyman.library.picture.picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import java.io.File;
import java.util.ArrayList;

import cn.shyman.library.picture.R;

public class SPPicker implements Parcelable {
	/** 查看参数 */
	static final String PICKER = "picker";
	
	int themeId;
	int count;
	boolean editable;
	int aspectX;
	int aspectY;
	int maxWidth;
	int maxHeight;
	int quality = 100;
	String cachePath;
	
	public static Builder picker() {
		return picker(R.style.ThemePicker_Dark);
	}
	
	public static Builder picker(int themeId) {
		Builder builder = new Builder();
		builder.themeId = themeId;
		return builder;
	}
	
	public static ArrayList<PictureInfo> pickPictureInfoList(Intent data) {
		return data.getParcelableArrayListExtra(PICKER);
	}
	
	public static ArrayList<Uri> pickPictureUriList(Intent data) {
		ArrayList<Uri> pictureUriList = new ArrayList<>();
		ArrayList<PictureInfo> pictureInfoList = data.getParcelableArrayListExtra(PICKER);
		for (PictureInfo pictureInfo : pictureInfoList) {
			pictureUriList.add(pictureInfo.pictureUri);
		}
		return pictureUriList;
	}
	
	public static Uri pickPictureUri(Intent data) {
		return pickPictureUri(data, 0);
	}
	
	private static Uri pickPictureUri(Intent data, int position) {
		ArrayList<PictureInfo> pictureInfoList = pickPictureInfoList(data);
		if (pictureInfoList == null || pictureInfoList.size() < position) {
			return null;
		}
		return pictureInfoList.get(position).pictureUri;
	}
	
	/**
	 * 创建相机照片信息
	 *
	 * @return 图片信息
	 */
	PictureInfo createPictureInfo() {
		PictureInfo pictureInfo = new PictureInfo();
		String picturePath = this.cachePath + "/img_" + System.currentTimeMillis() + ".jpg";
		pictureInfo.pictureUri = Uri.fromFile(new File(picturePath));
		return pictureInfo;
	}
	
	public static class Builder {
		private int themeId;
		private int count;
		private boolean editable;
		private int aspectX;
		private int aspectY;
		private int maxWidth;
		private int maxHeight;
		private int quality;
		private String cachePath;
		
		public Builder count(int count) {
			this.count = count;
			return this;
		}
		
		public Builder editable(boolean editable) {
			this.editable = editable;
			return this;
		}
		
		public Builder aspectRatio(int aspectX, int aspectY) {
			this.aspectX = aspectX;
			this.aspectY = aspectY;
			return this;
		}
		
		public Builder scale(int maxWidth, int maxHeight) {
			this.maxWidth = maxWidth;
			this.maxHeight = maxHeight;
			return this;
		}
		
		public Builder compress(int quality) {
			this.quality = quality;
			return this;
		}
		
		public void build(Fragment fragment) {
			Intent intent = new Intent(fragment.getContext(), SPPicturePickerActivity.class);
			intent.putExtra(PICKER, buildPicker(fragment.getContext()));
			fragment.startActivity(intent);
		}
		
		public void build(Fragment fragment, Bundle options) {
			Intent intent = new Intent(fragment.getContext(), SPPicturePickerActivity.class);
			intent.putExtra(PICKER, buildPicker(fragment.getContext()));
			fragment.startActivity(intent, options);
		}
		
		public void build(Fragment fragment, int requestCode) {
			Intent intent = new Intent(fragment.getContext(), SPPicturePickerActivity.class);
			intent.putExtra(PICKER, buildPicker(fragment.getContext()));
			fragment.startActivityForResult(intent, requestCode);
		}
		
		public void build(Fragment fragment, int requestCode, Bundle options) {
			Intent intent = new Intent(fragment.getContext(), SPPicturePickerActivity.class);
			intent.putExtra(PICKER, buildPicker(fragment.getContext()));
			fragment.startActivityForResult(intent, requestCode, options);
		}
		
		public void build(Activity activity) {
			Intent intent = new Intent(activity, SPPicturePickerActivity.class);
			intent.putExtra(PICKER, buildPicker(activity));
			activity.startActivity(intent);
		}
		
		public void build(Activity activity, Bundle options) {
			Intent intent = new Intent(activity, SPPicturePickerActivity.class);
			intent.putExtra(PICKER, buildPicker(activity));
			ActivityCompat.startActivity(activity, intent, options);
		}
		
		public void build(Activity activity, int requestCode) {
			Intent intent = new Intent(activity, SPPicturePickerActivity.class);
			intent.putExtra(PICKER, buildPicker(activity));
			activity.startActivityForResult(intent, requestCode);
		}
		
		private SPPicker buildPicker(Context context) {
			File cachePath = null;
			if (this.cachePath != null) {
				cachePath = new File(this.cachePath);
			}
			if (cachePath == null || !cachePath.canWrite()) {
				cachePath = context.getExternalCacheDir();
				if (cachePath == null || !cachePath.canWrite()) {
					cachePath = context.getCacheDir();
					if (cachePath == null || !cachePath.canWrite()) {
						cachePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
						if (cachePath == null || !cachePath.canWrite()) {
							throw new RuntimeException("can't write file!");
						}
					}
				}
				cachePath = new File(cachePath, "sp_image");
			}
			if (!cachePath.exists() && !cachePath.mkdirs()) {
				throw new RuntimeException("can't write file!");
			}
			this.cachePath = cachePath.getPath();
			System.out.println(this.cachePath);
			
			SPPicker spPicker = new SPPicker();
			spPicker.themeId = this.themeId;
			spPicker.count = this.count;
			spPicker.editable = this.editable;
			spPicker.aspectX = this.aspectX;
			spPicker.aspectY = this.aspectY;
			spPicker.maxWidth = this.maxWidth;
			spPicker.maxHeight = this.maxHeight;
			spPicker.quality = this.quality;
			spPicker.cachePath = this.cachePath;
			return spPicker;
		}
	}
	
	private SPPicker() {
	}
	
	private SPPicker(Parcel in) {
		themeId = in.readInt();
		count = in.readInt();
		editable = in.readByte() != 0;
		aspectX = in.readInt();
		aspectY = in.readInt();
		maxWidth = in.readInt();
		maxHeight = in.readInt();
		quality = in.readInt();
		cachePath = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(themeId);
		dest.writeInt(count);
		dest.writeByte((byte) (editable ? 1 : 0));
		dest.writeInt(aspectX);
		dest.writeInt(aspectY);
		dest.writeInt(maxWidth);
		dest.writeInt(maxHeight);
		dest.writeInt(quality);
		dest.writeString(cachePath);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<SPPicker> CREATOR = new Creator<SPPicker>() {
		@Override
		public SPPicker createFromParcel(Parcel in) {
			return new SPPicker(in);
		}
		
		@Override
		public SPPicker[] newArray(int size) {
			return new SPPicker[size];
		}
	};
}
