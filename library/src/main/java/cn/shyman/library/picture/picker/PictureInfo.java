package cn.shyman.library.picture.picker;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

import cn.shyman.library.picture.widget.PictureProvider;

public class PictureInfo implements Parcelable {
	/** 图片ID */
	long pictureId;
	/** 图片编码格式 */
	String mimeType;
	/** 图片地址 */
	Uri pictureUri;
	/** 图片文件大小 */
	long fileSize;
	/** 图片宽度 */
	private int pictureWidth;
	/** 图片高度 */
	private int pictureHeight;
	
	PictureInfo() {
	}
	
	PictureInfo(long pictureId, String mimeType, Uri pictureUri, long fileSize) {
		this.pictureId = pictureId;
		this.mimeType = mimeType;
		this.pictureUri = pictureUri;
		this.fileSize = fileSize;
	}
	
	public Uri getPictureUri() {
		return pictureUri;
	}
	
	public void setPictureUri(Uri pictureUri) {
		this.pictureUri = pictureUri;
	}
	
	public int getPictureWidth() {
		return pictureWidth;
	}
	
	public void setPictureWidth(int pictureWidth) {
		this.pictureWidth = pictureWidth;
	}
	
	public int getPictureHeight() {
		return pictureHeight;
	}
	
	public void setPictureHeight(int pictureHeight) {
		this.pictureHeight = pictureHeight;
	}
	
	boolean isCamera() {
		return this.pictureUri == Uri.EMPTY;
	}
	
	/**
	 * 转换成调用第三方应用时使用的地址
	 *
	 * @param context 设备上下文环境
	 * @return 图片地址
	 */
	Uri getPictureContentUri(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return PictureProvider.getUriForFile(context, new File(this.pictureUri.getPath()));
		}
		return pictureUri;
	}
	
	void obtainPictureSize() {
		if (this.pictureWidth > 0 && this.pictureHeight > 0) {
			return;
		}
		
		File pictureFile = new File(this.pictureUri.getPath());
		if (!pictureFile.exists() || !pictureFile.canRead()) {
			return;
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(this.pictureUri.getPath(), options);
		this.pictureWidth = options.outWidth;
		this.pictureHeight = options.outHeight;
	}
	
	void delete() {
		// noinspection ResultOfMethodCallIgnored
		new File(pictureUri.getPath()).delete();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PictureInfo) {
			PictureInfo other = (PictureInfo) obj;
			return this.pictureUri.equals(other.pictureUri);
		}
		return super.equals(obj);
	}
	
	private PictureInfo(Parcel in) {
		pictureId = in.readLong();
		mimeType = in.readString();
		pictureUri = in.readParcelable(Uri.class.getClassLoader());
		fileSize = in.readLong();
		pictureWidth = in.readInt();
		pictureHeight = in.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(pictureId);
		dest.writeString(mimeType);
		dest.writeParcelable(pictureUri, flags);
		dest.writeLong(fileSize);
		dest.writeInt(pictureWidth);
		dest.writeInt(pictureHeight);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<PictureInfo> CREATOR = new Creator<PictureInfo>() {
		@Override
		public PictureInfo createFromParcel(Parcel in) {
			return new PictureInfo(in);
		}
		
		@Override
		public PictureInfo[] newArray(int size) {
			return new PictureInfo[size];
		}
	};
}
