package cn.shyman.library.picture.picker;

import android.os.Parcel;
import android.os.Parcelable;

class AlbumInfo implements Parcelable {
	/** 相册ID */
	String albumId;
	/** 相册名称 */
	String albumName;
	/** 相册封面图片地址 */
	String albumCoverUrl;
	/** 相册图片数量 */
	int albumPictureCount;
	
	AlbumInfo(String albumId, String albumName, String albumCoverUrl, int albumPictureCount) {
		this.albumId = albumId;
		this.albumName = albumName;
		this.albumCoverUrl = albumCoverUrl;
		this.albumPictureCount = albumPictureCount;
	}
	
	private AlbumInfo(Parcel in) {
		albumId = in.readString();
		albumName = in.readString();
		albumCoverUrl = in.readString();
		albumPictureCount = in.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(albumId);
		dest.writeString(albumName);
		dest.writeString(albumCoverUrl);
		dest.writeInt(albumPictureCount);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<AlbumInfo> CREATOR = new Creator<AlbumInfo>() {
		@Override
		public AlbumInfo createFromParcel(Parcel in) {
			return new AlbumInfo(in);
		}
		
		@Override
		public AlbumInfo[] newArray(int size) {
			return new AlbumInfo[size];
		}
	};
	
	public boolean isAll() {
		return this.albumId == null;
	}
}
