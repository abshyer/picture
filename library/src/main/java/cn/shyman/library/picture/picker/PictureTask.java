package cn.shyman.library.picture.picker;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

class PictureTask extends AsyncTask<Void, Void, ArrayList<PictureInfo>> {
	private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
	private static final String[] PROJECTION = {
			MediaStore.Files.FileColumns._ID,
			MediaStore.MediaColumns.DISPLAY_NAME,
			MediaStore.MediaColumns.MIME_TYPE,
			MediaStore.MediaColumns.DATA,
			MediaStore.MediaColumns.SIZE};
	private static final String SELECTION_ALL =
			MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
					+ " AND " + MediaStore.MediaColumns.SIZE + ">0";
	
	private static final String SELECTION_FOR_ALBUM =
			MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
					+ " AND "
					+ " bucket_id=?"
					+ " AND " + MediaStore.MediaColumns.SIZE + ">0";
	
	private static final String MEDIA_TYPE_IMAGE = String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
	
	private static final String ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC";
	
	private ContentResolver mContentResolver;
	
	private AlbumInfo mAlbumInfo;
	
	private OnPictureListener mOnPictureListener;
	
	PictureTask(ContentResolver contentResolver,
	            AlbumInfo albumInfo,
	            OnPictureListener onPictureListener) {
		mContentResolver = contentResolver;
		mAlbumInfo = albumInfo;
		mOnPictureListener = onPictureListener;
	}
	
	@Override
	protected ArrayList<PictureInfo> doInBackground(Void... params) {
		ArrayList<PictureInfo> pictureInfoList = new ArrayList<>();
		
		Cursor cursor;
		if (mAlbumInfo.isAll()) {
			cursor = mContentResolver.query(QUERY_URI, PROJECTION, SELECTION_ALL, new String[]{MEDIA_TYPE_IMAGE}, ORDER_BY);
		} else {
			cursor = mContentResolver.query(QUERY_URI, PROJECTION, SELECTION_FOR_ALBUM, new String[]{MEDIA_TYPE_IMAGE, mAlbumInfo.albumId}, ORDER_BY);
		}
		if (cursor != null) {
			while (cursor.moveToNext()) {
				if (isCancelled()) {
					return null;
				}
				PictureInfo pictureInfo = new PictureInfo(
						cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
						cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
						Uri.fromFile(new File(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)))),
						cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)));
				pictureInfoList.add(pictureInfo);
			}
			cursor.close();
		}
		
		if (mAlbumInfo.isAll()) {
			PictureInfo pictureInfo = new PictureInfo(0, "", Uri.EMPTY, 0);
			pictureInfoList.add(0, pictureInfo);
		}
		
		return pictureInfoList;
	}
	
	@Override
	protected void onPostExecute(ArrayList<PictureInfo> pictureInfoList) {
		mOnPictureListener.onPicture(pictureInfoList);
	}
	
	/**
	 * 相册读取监听
	 */
	interface OnPictureListener {
		/**
		 * 相册读取结果
		 *
		 * @param pictureInfoList 相册列表
		 */
		void onPicture(ArrayList<PictureInfo> pictureInfoList);
	}
}
