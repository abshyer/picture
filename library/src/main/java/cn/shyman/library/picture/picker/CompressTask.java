package cn.shyman.library.picture.picker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

class CompressTask extends AsyncTask<Void, Void, ArrayList<PictureInfo>> {
	/** jpg图片属性 */
	private final static String IMAGE_JPEG = "image/jpeg";
	/** png图片属性 */
	private final static String IMAGE_PNG = "image/png";
	
	private SPPicker mSPPicker;
	private ArrayList<PictureInfo> mPictureInfoList;
	
	private OnCompressListener mOnCompressListener;
	
	CompressTask(SPPicker spPicker,
	             ArrayList<PictureInfo> pictureInfoList,
	             OnCompressListener onCompressListener) {
		mSPPicker = spPicker;
		mPictureInfoList = pictureInfoList;
		
		mOnCompressListener = onCompressListener;
	}
	
	@Override
	protected ArrayList<PictureInfo> doInBackground(Void... params) {
		ArrayList<PictureInfo> pictureInfoList = new ArrayList<>();
		
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		for (PictureInfo pictureInfo : mPictureInfoList) {
			if (isCancelled()) {
				break;
			}
			pictureInfo.obtainPictureSize();
			
			boolean success = false;
			PictureInfo outputPictureInfo = mSPPicker.createPictureInfo();
			try {
				inputStream = new FileInputStream(pictureInfo.pictureUri.getPath());
				outputStream = new FileOutputStream(outputPictureInfo.pictureUri.getPath());
				
				if (mSPPicker.maxWidth > 0 || mSPPicker.maxHeight > 0 || mSPPicker.quality < 100) {
					BitmapFactory.Options options = calculateBitmapOptions(
							pictureInfo.pictureUri.getPath(),
							mSPPicker.maxWidth, mSPPicker.maxHeight
					);
					Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
					if (IMAGE_PNG.equalsIgnoreCase(options.outMimeType)) {
						success = bitmap.compress(Bitmap.CompressFormat.PNG, mSPPicker.quality, outputStream);
					} else {
						success = bitmap.compress(Bitmap.CompressFormat.JPEG, mSPPicker.quality, outputStream);
					}
				}
				if (success) {
					outputPictureInfo.obtainPictureSize();
					pictureInfoList.add(outputPictureInfo);
				} else {
					outputPictureInfo.delete();
					pictureInfoList.add(pictureInfo);
				}
			} catch (Exception error) {
				outputPictureInfo.delete();
				pictureInfoList.add(pictureInfo);
			} catch (OutOfMemoryError error) {
				System.gc();
				outputPictureInfo.delete();
				pictureInfoList.add(pictureInfo);
			} finally {
				closeSilently(inputStream);
				closeSilently(outputStream);
			}
		}
		return pictureInfoList;
	}
	
	@Override
	protected void onPostExecute(ArrayList<PictureInfo> pictureInfoList) {
		mOnCompressListener.onCompress(pictureInfoList);
	}
	
	/**
	 * 计算图片信息
	 *
	 * @param picturePath   图片路径
	 * @param pickMaxWidth  图片最大宽度
	 * @param pickMaxHeight 图片最大高度
	 * @return 图片信息
	 * @throws IOException 图片读取错误信息
	 */
	private BitmapFactory.Options calculateBitmapOptions(String picturePath,
	                                                     int pickMaxWidth,
	                                                     int pickMaxHeight) throws IOException {
		InputStream input = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try {
			input = new FileInputStream(picturePath);
			BitmapFactory.decodeStream(input, null, options);
		} finally {
			closeSilently(input);
		}
		options.inJustDecodeBounds = false;
		
		if ((pickMaxWidth > 0 && pickMaxWidth >= options.outWidth)
				|| (pickMaxHeight > 0 && pickMaxHeight >= options.outHeight)) {
			return options;
		}
		
		int sampleSize = 1;
		while ((pickMaxWidth > 0 && options.outWidth / sampleSize > pickMaxWidth)
				|| (pickMaxHeight > 0 && options.outHeight / sampleSize > pickMaxHeight)) {
			sampleSize = sampleSize << 1;
		}
		options.inSampleSize = sampleSize;
		
		return options;
	}
	
	private void closeSilently(Closeable close) {
		try {
			if (close != null) {
				close.close();
			}
		} catch (IOException ignored) {
		}
	}
	
	/**
	 * 图片压缩监听
	 */
	interface OnCompressListener {
		
		/**
		 * 图片压缩结果
		 *
		 * @param pictureInfoList 图片列表
		 */
		void onCompress(ArrayList<PictureInfo> pictureInfoList);
	}
}
