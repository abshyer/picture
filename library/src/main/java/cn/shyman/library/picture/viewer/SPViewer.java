package cn.shyman.library.picture.viewer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import cn.shyman.library.picture.R;

public class SPViewer implements Parcelable {
	/** 查看参数 */
	static final String VIEWER = "viewer";
	/** 查看过度名称 */
	public static final String TRANSITION_NAME = "view";
	
	int themeId;
	ArrayList<Uri> pictureUriList;
	int position;
	boolean editable;
	
	public static Builder viewer() {
		return viewer(R.style.ThemeViewer_Dark);
	}
	
	public static Builder viewer(int themeId) {
		Builder builder = new Builder();
		builder.themeId = themeId;
		return builder;
	}
	
	public static Uri viewPictureUri(Intent data) {
		return viewPictureUri(data, 0);
	}
	
	public static Uri viewPictureUri(Intent data, int position) {
		ArrayList<Uri> pictureUriList = viewPictureUriList(data);
		if (pictureUriList == null || pictureUriList.size() < position) {
			return null;
		}
		return pictureUriList.get(position);
	}
	
	public static ArrayList<Uri> viewPictureUriList(Intent data) {
		return data.getParcelableArrayListExtra(VIEWER);
	}
	
	public static class Builder {
		private int themeId;
		private ArrayList<Uri> pictureUriList;
		private int position;
		private boolean editable;
		
		private Builder() {
		}
		
		public Builder urlList(ArrayList<String> pictureUrlList) {
			ArrayList<Uri> pictureUriList = new ArrayList<>();
			for (String pictureUrl : pictureUrlList) {
				if (pictureUrl == null) {
					pictureUriList.add(Uri.EMPTY);
				} else {
					pictureUriList.add(Uri.parse(pictureUrl));
				}
			}
			return uriList(pictureUriList);
		}
		
		public Builder uriList(ArrayList<Uri> pictureUriList) {
			this.pictureUriList = pictureUriList;
			return this;
		}
		
		public Builder position(int position) {
			this.position = position;
			return this;
		}
		
		public Builder editable(boolean editable) {
			this.editable = editable;
			return this;
		}
		
		public void build(Fragment fragment) {
			Intent intent = new Intent(fragment.getContext(), SPPictureViewerActivity.class);
			intent.putExtra(VIEWER, buildViewer());
			fragment.startActivity(intent);
		}
		
		public void build(Fragment fragment, Bundle options) {
			Intent intent = new Intent(fragment.getContext(), SPPictureViewerActivity.class);
			intent.putExtra(VIEWER, buildViewer());
			fragment.startActivity(intent, options);
		}
		
		public void build(Fragment fragment, int requestCode) {
			Intent intent = new Intent(fragment.getContext(), SPPictureViewerActivity.class);
			intent.putExtra(VIEWER, buildViewer());
			fragment.startActivityForResult(intent, requestCode);
		}
		
		public void build(Fragment fragment, int requestCode, Bundle options) {
			Intent intent = new Intent(fragment.getContext(), SPPictureViewerActivity.class);
			intent.putExtra(VIEWER, buildViewer());
			fragment.startActivityForResult(intent, requestCode, options);
		}
		
		public void build(Activity activity) {
			Intent intent = new Intent(activity, SPPictureViewerActivity.class);
			intent.putExtra(VIEWER, buildViewer());
			activity.startActivity(intent);
		}
		
		public void build(Activity activity, Bundle options) {
			Intent intent = new Intent(activity, SPPictureViewerActivity.class);
			intent.putExtra(VIEWER, buildViewer());
			ActivityCompat.startActivity(activity, intent, options);
		}
		
		public void build(Activity activity, int requestCode) {
			Intent intent = new Intent(activity, SPPictureViewerActivity.class);
			intent.putExtra(VIEWER, buildViewer());
			activity.startActivityForResult(intent, requestCode);
		}
		
		private SPViewer buildViewer() {
			SPViewer spViewer = new SPViewer();
			spViewer.pictureUriList = this.pictureUriList;
			spViewer.position = this.position;
			spViewer.editable = this.editable;
			return spViewer;
		}
	}
	
	private SPViewer() {
	}
	
	private SPViewer(Parcel in) {
		pictureUriList = in.createTypedArrayList(Uri.CREATOR);
		position = in.readInt();
		editable = in.readByte() != 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(pictureUriList);
		dest.writeInt(position);
		dest.writeByte((byte) (editable ? 1 : 0));
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<SPViewer> CREATOR = new Creator<SPViewer>() {
		@Override
		public SPViewer createFromParcel(Parcel in) {
			return new SPViewer(in);
		}
		
		@Override
		public SPViewer[] newArray(int size) {
			return new SPViewer[size];
		}
	};
}
