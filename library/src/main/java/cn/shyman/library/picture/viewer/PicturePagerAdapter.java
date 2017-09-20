package cn.shyman.library.picture.viewer;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import cn.shyman.library.picture.R;
import cn.shyman.library.picture.widget.SPLoadingDrawable;
import cn.shyman.library.picture.widget.ZoomableDraweeView;

class PicturePagerAdapter extends PagerAdapter {
	private LayoutInflater mInflater;
	private DisplayMetrics mDisplayMetrics;
	
	private SPViewer mSPViewer;
	
	/** 事件监听 */
	private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener;
	
	PicturePagerAdapter(Context context,
	                    SPViewer spViewer,
	                    GestureDetector.SimpleOnGestureListener simpleOnGestureListener) {
		mInflater = LayoutInflater.from(context);
		mDisplayMetrics = context.getResources().getDisplayMetrics();
		
		mSPViewer = spViewer;
		
		mSimpleOnGestureListener = simpleOnGestureListener;
	}
	
	@Override
	public int getCount() {
		return mSPViewer.pictureUriList.size();
	}
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
	
	@Override
	public View instantiateItem(ViewGroup container, int position) {
		View convertView = mInflater.inflate(R.layout.sp_item_picture_viewer_pager, container, false);
		ZoomableDraweeView pictureView = (ZoomableDraweeView) convertView.findViewById(R.id.spPictureView);
		
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		pictureView.setTapListener(new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {
				return mSimpleOnGestureListener.onSingleTapConfirmed(event);
			}
		});
		
		Uri pictureUri = mSPViewer.pictureUriList.get(position);
		// 加载中图片
		SPLoadingDrawable loadingDrawable = new SPLoadingDrawable(mInflater.getContext(), pictureView);
		loadingDrawable.updateSizes(SPLoadingDrawable.LARGE);
		loadingDrawable.setProgressRotation(0.8f);
		loadingDrawable.setStartEndTrim(0f, 0.5f);
		loadingDrawable.setAlpha(255);
		pictureView.getHierarchy().setPlaceholderImage(loadingDrawable);
		
		ImageRequest imageRequest =
				ImageRequestBuilder.newBuilderWithSource(pictureUri)
						.setResizeOptions(new ResizeOptions(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels))
						.build();
		
		DraweeController controller =
				Fresco.newDraweeControllerBuilder()
						.setControllerListener(new PictureControllerListener(loadingDrawable))
						.setOldController(pictureView.getController())
						.setImageRequest(imageRequest)
						.build();
		
		pictureView.setController(controller);
		
		container.addView(convertView);
		return convertView;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
	
	private static class PictureControllerListener extends BaseControllerListener<ImageInfo> {
		private SPLoadingDrawable mLoadingDrawable;
		
		PictureControllerListener(SPLoadingDrawable loadingDrawable) {
			mLoadingDrawable = loadingDrawable;
		}
		
		@Override
		public void onSubmit(String id, Object callerContext) {
			mLoadingDrawable.start();
		}
		
		@Override
		public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
			mLoadingDrawable.stop();
		}
		
		@Override
		public void onFailure(String id, Throwable throwable) {
			mLoadingDrawable.stop();
		}
	}
}
