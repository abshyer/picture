package cn.shyman.library.picture.picker;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.ArrayList;

class AlbumTask extends AsyncTask<Void, Void, ArrayList<AlbumInfo>> {
	private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
	private static final String COLUMN_BUCKET_ID = "bucket_id";
	private static final String COLUMN_BUCKET_NAME = "bucket_display_name";
	private static final String COLUMN_COUNT = "count";
	private static final String[] PROJECTION = {
			COLUMN_BUCKET_ID,
			COLUMN_BUCKET_NAME,
			MediaStore.MediaColumns.DATA,
			"COUNT(*) AS " + COLUMN_COUNT};
	
	private static final String[] SELECTION_ARGS = {
			String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
	};
	private static final String SELECTION =
			MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
					+ " AND " + MediaStore.MediaColumns.SIZE + ">0"
					+ ") GROUP BY (bucket_id";
	
	private static final String BUCKET_ORDER_BY = "datetaken DESC";
	
	/** 内容解析器 */
	private ContentResolver mContentResolver;
	
	private OnAlbumListener mOnAlbumListener;
	
	AlbumTask(ContentResolver contentResolver, OnAlbumListener onAlbumListener) {
		mContentResolver = contentResolver;
		mOnAlbumListener = onAlbumListener;
	}
	
	@Override
	protected ArrayList<AlbumInfo> doInBackground(Void... params) {
		ArrayList<AlbumInfo> albumInfoList = new ArrayList<>();
		
		String albumCoverUrl = "";
		int albumTotalCount = 0;
		Cursor cursor = mContentResolver.query(QUERY_URI, PROJECTION, SELECTION, SELECTION_ARGS, BUCKET_ORDER_BY);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				AlbumInfo albumInfo = new AlbumInfo(
						cursor.getString(cursor.getColumnIndex(COLUMN_BUCKET_ID)),
						cursor.getString(cursor.getColumnIndex(COLUMN_BUCKET_NAME)),
						cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)),
						cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT))
				);
				albumInfoList.add(albumInfo);
				
				albumTotalCount += albumInfo.albumPictureCount;
			}
			if (cursor.moveToFirst()) {
				albumCoverUrl = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
			}
			cursor.close();
		}
		
		// TODO
		AlbumInfo albumInfo = new AlbumInfo(null, "全部", albumCoverUrl, albumTotalCount);
		albumInfoList.add(0, albumInfo);
		
		return albumInfoList;
	}
	
	@Override
	protected void onPostExecute(ArrayList<AlbumInfo> albumInfoList) {
		mOnAlbumListener.onAlbum(albumInfoList);
	}
	
	/**
	 * 相册读取监听
	 */
	interface OnAlbumListener {
		/**
		 * 相册读取结果
		 *
		 * @param albumInfoList 相册列表
		 */
		void onAlbum(ArrayList<AlbumInfo> albumInfoList);
	}
}
