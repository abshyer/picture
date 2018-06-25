package cn.shyman.library.picture.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import cn.shyman.library.picture.R;

public class MediaLayout extends ViewGroup {
	private Stack<View> pictureScrapStack = new Stack<>();
	private Stack<View> editScrapStack = new Stack<>();
	
	private static final int FIXED = 0;
	private static final int WRAP = 1;
	private static final int START = 0;
	private static final int END = 1;
	private static final int INSIDE = 0;
	private static final int BORDER = 1;
	private static final int OUTSIDE = 2;
	
	private int horizontalPadding;
	private int verticalPadding;
	
	private int pictureLayoutId;
	private int editLayoutId;
	
	private int maxCount;
	private int rowCount;
	private float viewRatio;
	
	private int showMode = WRAP;
	private int wrapBorderlineCount = 0;
	
	private boolean supportInsert;
	private ImageView ivInsert;
	
	private int mediaViewWidth;
	private int mediaViewHeight;
	
	private Map<Object, MediaInfo> mediaInfoMap = new LinkedHashMap<>();
	private Map<Object, MediaView> mediaViewMap = new LinkedHashMap<>();
	
	private OnMediaInsertListener onMediaInsertListener;
	private OnMediaEditListener onMediaEditListener;
	private OnMediaSelectListener onMediaSelectListener;
	
	public MediaLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MediaLayout);
		
		this.horizontalPadding = (int) a.getDimension(R.styleable.MediaLayout_media_horizontalPadding, 0);
		this.verticalPadding = (int) a.getDimension(R.styleable.MediaLayout_media_verticalPadding, 0);
		
		this.pictureLayoutId = a.getResourceId(R.styleable.MediaLayout_media_pictureLayoutId, 0);
		this.editLayoutId = a.getResourceId(R.styleable.MediaLayout_media_editLayoutId, 0);
		
		this.maxCount = a.getInt(R.styleable.MediaLayout_media_maxCount, 4);
		this.rowCount = a.getInt(R.styleable.MediaLayout_media_rowCount, 4);
		this.viewRatio = a.getFloat(R.styleable.MediaLayout_media_viewRatio, 1F);
		
		this.showMode = a.getInt(R.styleable.MediaLayout_media_showMode, WRAP);
		this.wrapBorderlineCount = a.getInt(R.styleable.MediaLayout_media_wrapBorderlineCount, 0);
		
		// 	int editDrawableId = a.getResourceId(R.styleable.MediaLayout_media_editDrawable, -1);
		// 	if (editDrawableId != -1) {
		// 		this.editDrawable = ContextCompat.getDrawable(context, editDrawableId);
		// 	}
		// 	this.editPosition = a.getInt(R.styleable.MediaLayout_media_editPosition, INSIDE);
		//
		this.ivInsert = createInsertView();
		this.supportInsert = a.getBoolean(R.styleable.MediaLayout_media_supportInsert, false);
		int insertDrawableId = a.getResourceId(R.styleable.MediaLayout_media_insertDrawable, -1);
		if (insertDrawableId != -1) {
			this.ivInsert.setImageResource(insertDrawableId);
		}
		int insertBackgroundId = a.getResourceId(R.styleable.MediaLayout_media_insertBackground, -1);
		if (insertBackgroundId != -1) {
			this.ivInsert.setBackgroundResource(insertBackgroundId);
		}
		// 	this.insertGravity = a.getInt(R.styleable.MediaLayout_media_insertGravity, END);
		this.ivInsert.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onMediaInsertListener != null) {
					onMediaInsertListener.onInsert();
				}
			}
		});
		if (this.supportInsert) {
			resetInsertView();
		}
		a.recycle();
	}
	
	public void setOnMediaInsertListener(OnMediaInsertListener onMediaInsertListener) {
		this.onMediaInsertListener = onMediaInsertListener;
	}
	
	public void setOnMediaEditListener(OnMediaEditListener onMediaEditListener) {
		this.onMediaEditListener = onMediaEditListener;
	}
	
	public void setOnMediaSelectListener(OnMediaSelectListener onMediaSelectListener) {
		this.onMediaSelectListener = onMediaSelectListener;
		int mediaCount = getMediaCount();
		for (int index = 0; index < mediaCount; index++) {
			View view = getChildAt(index);
			view.setClickable(this.onMediaSelectListener != null);
		}
	}
	
	public Object addMediaInfo(Uri mediaUri) {
		return addMediaInfo(mediaUri, mediaUri);
	}
	
	public Object addMediaInfo(Uri mediaUri, Uri mediaPictureUri) {
		return addMediaInfo(mediaUri.hashCode() + new Random().nextInt(999999),
				mediaUri, mediaPictureUri);
	}
	
	public Object addMediaInfo(Object mediaTag, Uri mediaUri, Uri mediaPictureUri) {
		if (mediaTag == null || mediaUri == null || mediaPictureUri == null) {
			return null;
		}
		if (this.mediaInfoMap.size() >= this.maxCount) {
			return null;
		}
		
		addMediaInfo(mediaTag, mediaUri, mediaPictureUri);
		addMediaView(mediaTag);
		resetInsertView();
		
		return mediaTag;
	}
	
	public void setMediaInfo(Object mediaTag, @IntRange(from = 0, to = 100) int progress) {
		MediaView mediaView = this.mediaViewMap.get(mediaTag);
		if (mediaView == null) {
			return;
		}
		mediaView.setProgress(progress);
	}
	
	public void setMediaInfo(Object mediaTag, boolean showProgress) {
		MediaView mediaView = this.mediaViewMap.get(mediaTag);
		if (mediaView == null) {
			return;
		}
		mediaView.setShowProgress(showProgress);
	}
	
	public void removeMediaUri(Object mediaTag) {
		if (mediaTag == null) {
			return;
		}
		
		removeMediaInfo(mediaTag);
		removeMediaView(mediaTag);
		resetInsertView();
	}
	
	private void addMediaInfoInner(Object mediaTag, Uri mediaUri, Uri mediaPictureUri) {
		MediaInfo mediaInfo = new MediaInfo();
		mediaInfo.mediaTag = mediaTag;
		mediaInfo.mediaUri = mediaUri;
		mediaInfo.mediaPictureUri = mediaPictureUri;
		this.mediaInfoMap.put(mediaTag, mediaInfo);
	}
	
	private void removeMediaInfo(Object mediaTag) {
		this.mediaInfoMap.remove(mediaTag);
	}
	
	private void addMediaView(Object mediaTag) {
		int mediaCount = getMediaCount();
		
		LayoutInflater inflater = LayoutInflater.from(getContext());
		
		MediaView mediaView = new MediaView(getContext());
		mediaView.setTag(mediaTag);
		mediaView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onMediaSelectListener != null) {
					int viewIndex = indexOfChild(v);
					MediaInfo mediaInfo = mediaInfoMap.get(v.getTag());
					onMediaSelectListener.onSelected(viewIndex, mediaInfo.mediaTag, mediaInfo.mediaUri, mediaInfo.mediaPictureUri);
				}
			}
		});
		mediaView.setClickable(this.onMediaSelectListener != null);
		addView(mediaView, mediaCount);
		
		View editView = inflater.inflate(editLayoutId, this, false);
		editView.setTag(mediaTag);
		editView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onMediaEditListener != null) {
					int viewIndex = indexOfChild(v);
					viewIndex = viewIndex - getMediaCount();
					MediaInfo mediaInfo = mediaInfoMap.get(v.getTag());
					onMediaEditListener.onEdit(viewIndex, mediaInfo.mediaTag, mediaInfo.mediaUri, mediaInfo.mediaPictureUri);
				}
			}
		});
		addView(editView, mediaCount * 2 + 1);
		
		this.mediaViewMap.put(mediaTag, mediaView);
	}
	
	private void removeMediaView(Object mediaTag) {
		MediaView mediaView = this.mediaViewMap.remove(mediaTag);
		if (mediaView == null) {
			return;
		}
		int mediaCount = getMediaCount();
		
		int index = indexOfChild(mediaView);
		removeViewAt(index);
		removeViewAt(mediaCount + index - 1);
		
		this.mediaViewMap.remove(mediaTag);
	}
	
	private void resetInsertView() {
		if (!this.supportInsert && this.ivInsert.getParent() != null) {
			removeView(this.ivInsert);
		} else if (this.ivInsert.getParent() != null && this.mediaViewMap.size() >= this.maxCount) {
			removeView(this.ivInsert);
		} else if (this.supportInsert && this.ivInsert.getParent() == null) {
			addView(this.ivInsert);
		}
	}
	
	private ImageView createInsertView() {
		ImageView ivInsert = new ImageView(getContext());
		MarginLayoutParams layoutParams = new MarginLayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		ivInsert.setLayoutParams(layoutParams);
		ivInsert.setScaleType(ImageView.ScaleType.CENTER);
		return ivInsert;
	}
	
	private int getMediaCount() {
		int childCount = getChildCount();
		if (this.ivInsert.getParent() == this) {
			childCount--;
		}
		return childCount / 2;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidthSize = MeasureSpec.getSize(widthMeasureSpec);
		int parentWidthMode = MeasureSpec.getMode(widthMeasureSpec);
		
		this.mediaViewWidth = remeasureMediaSize(parentWidthSize, parentWidthMode);
		this.mediaViewHeight = (int) (this.mediaViewWidth * this.viewRatio);
		
		int mediaWidthMeasureSpec = MeasureSpec.makeMeasureSpec(this.mediaViewWidth, MeasureSpec.EXACTLY);
		int mediaHeightMeasureSpec = MeasureSpec.makeMeasureSpec(this.mediaViewHeight, MeasureSpec.EXACTLY);
		
		int mediaCount = getMediaCount();
		int rowBorderlineCount = (this.showMode == WRAP && this.wrapBorderlineCount != 0 && mediaCount > this.rowCount)
				? this.wrapBorderlineCount : this.rowCount;
		
		for (int mediaIndex = 0; mediaIndex < mediaCount; mediaIndex++) {
			View mediaView = getChildAt(mediaIndex);
			mediaView.measure(mediaWidthMeasureSpec, mediaHeightMeasureSpec);
		}
		for (int mediaIndex = 0; mediaIndex < mediaCount; mediaIndex++) {
			View editView = getChildAt(mediaCount + mediaIndex);
			measureChild(editView, mediaWidthMeasureSpec, mediaHeightMeasureSpec);
		}
		if (this.ivInsert.getParent() == this) {
			this.ivInsert.measure(mediaWidthMeasureSpec, mediaHeightMeasureSpec);
		}
		
		int viewWidthSize = parentWidthSize;
		if (parentWidthMode == MeasureSpec.AT_MOST && mediaCount < rowBorderlineCount) {
			viewWidthSize = getPaddingLeft() + getPaddingRight()
					+ this.mediaViewWidth * mediaCount
					+ this.horizontalPadding * (mediaCount + 1);
		}
		
		int viewHeightSize = getPaddingTop() + getPaddingBottom();
		mediaCount += this.ivInsert.getParent() == this ? 1 : 0;
		if (mediaCount > 0) {
			int colCount = mediaCount / rowBorderlineCount + (mediaCount % rowBorderlineCount == 0 ? 0 : 1);
			viewHeightSize += this.mediaViewHeight * colCount;
			viewHeightSize += this.verticalPadding * (colCount + 1);
		}
		setMeasuredDimension(viewWidthSize, viewHeightSize);
	}
	
	private int remeasureMediaSize(int parentWidthSize, int parentWidthMode) {
		int mediaCount = getMediaCount();
		int rowCount = this.rowCount;
		int padding = this.horizontalPadding;
		
		if (getChildCount() == 0) {
			return 0;
		}
		
		if (this.showMode == FIXED) {
			int netParentWidthSize = parentWidthSize - getPaddingLeft() - getPaddingRight() - padding * 2;
			return (netParentWidthSize - (rowCount - 1) * padding) / rowCount;
		}
		
		for (int currentCount = rowCount; currentCount > mediaCount; currentCount--) {
			int netParentWidthSize = parentWidthSize - getPaddingLeft() - getPaddingRight() - padding * 2;
			int childWidthSize = (netParentWidthSize - (currentCount - 1) * padding) / currentCount;
			parentWidthSize -= childWidthSize / 2;
		}
		if (rowCount > mediaCount) {
			rowCount = mediaCount;
		}
		
		int netParentWidthSize = parentWidthSize - getPaddingLeft() - getPaddingRight();
		return (netParentWidthSize - (rowCount - 1) * padding) / rowCount;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int mediaCount = getMediaCount();
		
		int rowBorderlineCount = (this.showMode == WRAP && this.wrapBorderlineCount != 0 && mediaCount > this.rowCount)
				? this.wrapBorderlineCount : this.rowCount;
		
		int top = getPaddingTop();
		int left = getPaddingLeft();
		int topOffsetSize = this.verticalPadding;
		int leftOffsetSize = this.horizontalPadding;
		
		left += leftOffsetSize;
		top += topOffsetSize;
		for (int mediaIndex = 0; mediaIndex < mediaCount; mediaIndex++) {
			MediaView mediaView = (MediaView) getChildAt(mediaIndex);
			mediaView.layout(left, top, left + this.mediaViewWidth, top + this.mediaViewHeight);
			loadPicture(mediaView);
			
			if (mediaIndex % rowBorderlineCount == rowBorderlineCount - 1) {
				top += this.mediaViewHeight;
				if (mediaIndex != 0) {
					top += topOffsetSize;
				}
				left = getPaddingLeft() + leftOffsetSize;
			} else {
				left += this.mediaViewWidth + leftOffsetSize;
			}
		}
		
		top = getPaddingTop();
		left = getPaddingLeft();
		
		left += leftOffsetSize;
		top += topOffsetSize;
		int editTopOffset = topOffsetSize / 2;
		int editLeftOffset = leftOffsetSize / 2;
		for (int mediaIndex = 0; mediaIndex < mediaCount; mediaIndex++) {
			View editView = getChildAt(mediaCount + mediaIndex);
			int measureWidth = editView.getMeasuredWidth();
			int measureHeight = editView.getMeasuredHeight();
			editView.layout(left + this.mediaViewWidth + editLeftOffset - measureWidth,
					top - editTopOffset,
					left + this.mediaViewWidth + editLeftOffset,
					top - editTopOffset + measureHeight);
			
			if (mediaIndex % rowBorderlineCount == rowBorderlineCount - 1) {
				top += this.mediaViewHeight;
				if (mediaIndex != 0) {
					top += topOffsetSize;
				}
				left = getPaddingLeft() + leftOffsetSize;
			} else {
				left += this.mediaViewWidth + leftOffsetSize;
			}
		}
		
		if (this.ivInsert.getParent() == this) {
			this.ivInsert.layout(left, top, left + this.mediaViewWidth, top + this.mediaViewHeight);
		}
	}
	
	private void loadPicture(MediaView mediaView) {
		SimpleDraweeView pictureView = mediaView.simpleDraweeView;
		pictureView.setController(
				Fresco.newDraweeControllerBuilder()
						.setOldController(pictureView.getController())
						.setImageRequest(
								ImageRequestBuilder.newBuilderWithSource(
										this.mediaInfoMap.get(mediaView.getTag()).mediaPictureUri)
										.setResizeOptions(new ResizeOptions(pictureView.getMeasuredWidth(),
												pictureView.getMeasuredHeight()))
										.build())
						.build());
	}
	
	private static class MediaView extends FrameLayout {
		private SimpleDraweeView simpleDraweeView;
		private TextView progressView;
		
		public MediaView(@NonNull Context context) {
			super(context);
			
			this.simpleDraweeView = new SimpleDraweeView(context);
			addView(this.simpleDraweeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			
			this.progressView = new TextView(context);
			this.progressView.setBackgroundColor(0x88000000);
			this.progressView.setTextColor(Color.WHITE);
			this.progressView.setGravity(Gravity.CENTER);
			this.progressView.setVisibility(GONE);
			addView(this.progressView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		}
		
		void setShowProgress(boolean showProgress) {
			this.progressView.setVisibility(showProgress ? View.VISIBLE : View.GONE);
		}
		
		void setProgress(int progress) {
			this.progressView.setText(String.valueOf(progress));
		}
	}
	
	final class MediaInfo {
		Object mediaTag;
		Uri mediaPictureUri;
		Uri mediaUri;
	}
	
	public interface OnMediaInsertListener {
		
		void onInsert();
	}
	
	public interface OnMediaEditListener {
		
		void onEdit(int viewIndex,
		            Object mediaTag,
		            Uri mediaUri,
		            Uri mediaPictureUri);
	}
	
	public interface OnMediaSelectListener {
		
		void onSelected(int viewIndex,
		                Object mediaTag,
		                Uri mediaUri,
		                Uri mediaPictureUri);
	}
	
	// /** 视图重用栈 */
	// private Stack<MediaView> scrapStack = new Stack<>();
	//
	// private static final int FIXED = 0;
	// private static final int WRAP = 1;
	// private static final int START = 0;
	// private static final int END = 1;
	// private static final int INSIDE = 0;
	// private static final int BORDER = 1;
	// private static final int OUTSIDE = 2;
	//
	// private int maxCount;
	// private int rowCount;
	// private int horizontalPadding;
	// private int verticalPadding;
	// private float viewRatio;
	//
	// private int showMode = WRAP;
	// private int wrapBorderlineCount = 0;
	//
	// private Drawable editDrawable;
	// private int editPosition;
	//
	// private boolean supportInsert;
	// private ImageView ivInsert;
	// private int insertGravity = END;
	//
	
	//
	// private ArrayList<Uri> mediaList = new ArrayList<>();
	//
	// public MediaLayout(Context context, AttributeSet attrs) {
	// 	super(context, attrs);
	//
	// 	TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MediaLayout);
	// 	this.maxCount = a.getInt(R.styleable.MediaLayout_media_maxCount, 4);
	// 	this.rowCount = a.getInt(R.styleable.MediaLayout_media_rowCount, 4);
	// 	this.horizontalPadding = (int) a.getDimension(R.styleable.MediaLayout_media_horizontalPadding, 0);
	// 	this.verticalPadding = (int) a.getDimension(R.styleable.MediaLayout_media_verticalPadding, 0);
	// 	this.viewRatio = a.getFloat(R.styleable.MediaLayout_media_viewRatio, 1F);
	//
	// 	this.showMode = a.getInt(R.styleable.MediaLayout_media_showMode, WRAP);
	// 	this.wrapBorderlineCount = a.getInt(R.styleable.MediaLayout_media_wrapBorderlineCount, 0);
	//
	// 	int editDrawableId = a.getResourceId(R.styleable.MediaLayout_media_editDrawable, -1);
	// 	if (editDrawableId != -1) {
	// 		this.editDrawable = ContextCompat.getDrawable(context, editDrawableId);
	// 	}
	// 	this.editPosition = a.getInt(R.styleable.MediaLayout_media_editPosition, INSIDE);
	//
	// 	this.ivInsert = createInsertView();
	// 	this.supportInsert = a.getBoolean(R.styleable.MediaLayout_media_supportInsert, false);
	// 	int insertDrawableId = a.getResourceId(R.styleable.MediaLayout_media_insertDrawable, -1);
	// 	if (insertDrawableId != -1) {
	// 		this.ivInsert.setImageResource(insertDrawableId);
	// 	}
	// 	int insertBackgroundId = a.getResourceId(R.styleable.MediaLayout_media_insertBackground, -1);
	// 	if (insertBackgroundId != -1) {
	// 		this.ivInsert.setBackgroundResource(insertBackgroundId);
	// 	}
	// 	this.insertGravity = a.getInt(R.styleable.MediaLayout_media_insertGravity, END);
	// 	if (this.supportInsert) {
	// 		resetInsertView();
	// 	}
	// 	a.recycle();
	// }
	//
	// public void addMediaUri(Uri mediaUri) {
	// 	addMediaUri(mediaUri, 100);
	// }
	//
	// public void addMediaUri(Uri mediaUri, int progress) {
	// 	if (mediaUri == null) {
	// 		return;
	// 	}
	// 	if (this.mediaList.size() >= this.maxCount) {
	// 		return;
	// 	}
	//
	// 	addMediaView(mediaUri);
	// 	resetInsertView();
	// }
	//
	// public void removeMediaUri(int index) {
	// 	if (index < 0 || index >= this.mediaList.size()) {
	// 		return;
	// 	}
	//
	// 	removeMediaView(index);
	// 	resetInsertView();
	//
	// 	// if (this.selectedPosition >= position) {
	// 	// 	if (this.selectedPosition > 0) {
	// 	// 		setSelectedPosition(this.selectedPosition - 1);
	// 	// 	} else if (this.pictureList.size() > 0) {
	// 	// 		setSelectedPosition(0);
	// 	// 	} else {
	// 	// 		setSelectedPosition(-1);
	// 	// 	}
	// 	// }
	// 	// resetContentView(position);
	// }
	//
	// public void updateMediaProgress(int index, int progress) {
	//
	// }
	//
	// private void addMediaView(Uri mediaUri) {
	// 	this.mediaList.add(mediaUri);
	// 	MediaView mediaView;
	// 	if (scrapStack.isEmpty()) {
	// 		mediaView = new MediaView(getContext());
	// 	} else {
	// 		mediaView = scrapStack.pop();
	// 	}
	// 	mediaView.setEditDrawable(this.editDrawable, this.editPosition);
	// 	if (this.insertGravity == START) {
	// 		addView(mediaView, this.mediaList.size());
	// 	} else {
	// 		addView(mediaView, this.mediaList.size() - 1);
	// 	}
	// }
	//
	// /**
	//  * 删除图片展示控件
	//  *
	//  * @param position 图片列表位置
	//  */
	// private void removeMediaView(int position) {
	// 	this.mediaList.remove(position);
	// 	if (this.insertGravity == START) {
	// 		position++;
	// 	}
	// 	MediaView mediaView = (MediaView) getChildAt(position);
	// 	removeViewAt(position);
	// 	scrapStack.push(mediaView);
	// }
	//
	// /**
	//  * 重置插入控件
	//  */
	// private void resetInsertView() {
	// 	if (!this.supportInsert && this.ivInsert.getParent() != null) {
	// 		removeView(this.ivInsert);
	// 	} else if (this.ivInsert.getParent() != null && this.mediaList.size() >= this.maxCount) {
	// 		removeView(this.ivInsert);
	// 	} else if (this.supportInsert && this.ivInsert.getParent() == null) {
	// 		if (this.insertGravity == START) {
	// 			addView(this.ivInsert, 0);
	// 		} else {
	// 			addView(this.ivInsert);
	// 		}
	// 	}
	// }
	//
	// /**
	//  * 创建插入控件
	//  *
	//  * @return 插入控件
	//  */
	// private ImageView createInsertView() {
	// 	ImageView ivInsert = new ImageView(getContext());
	// 	MarginLayoutParams layoutParams = new MarginLayoutParams(
	// 			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	// 	int margin = 0;
	// 	if (this.editDrawable != null) {
	// 		if (editPosition == INSIDE) {
	// 			margin = 0;
	// 		} else if (editPosition == BORDER) {
	// 			margin = editDrawable.getIntrinsicWidth() / 2;
	// 		} else {
	// 			margin = editDrawable.getIntrinsicWidth();
	// 		}
	// 	}
	// 	layoutParams.topMargin = margin;
	// 	layoutParams.rightMargin = margin;
	// 	ivInsert.setLayoutParams(layoutParams);
	// 	ivInsert.setScaleType(ImageView.ScaleType.CENTER);
	// 	return ivInsert;
	// }
	//
	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// 	int parentWidthSize = MeasureSpec.getSize(widthMeasureSpec);
	// 	int parentWidthMode = MeasureSpec.getMode(widthMeasureSpec);
	//
	// 	int childWidth = remeasureChild(parentWidthSize, parentWidthMode);
	// 	int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
	// 	int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childWidth * this.viewRatio), MeasureSpec.EXACTLY);
	//
	// 	int childCount = getChildCount();
	// 	int rowBorderlineCount = (this.showMode == WRAP && this.wrapBorderlineCount != 0 && childCount > this.rowCount)
	// 			? this.wrapBorderlineCount : this.rowCount;
	//
	// 	int viewWidthSize = parentWidthSize;
	// 	if (parentWidthMode == MeasureSpec.AT_MOST && childCount < rowBorderlineCount) {
	// 		viewWidthSize = getPaddingLeft() + getPaddingRight() + childWidth * childCount + this.horizontalPadding * (childCount - 1);
	// 	}
	//
	// 	int viewHeightSize = getPaddingTop() + getPaddingBottom();
	// 	for (int index = 0; index < childCount; index++) {
	// 		View child = getChildAt(index);
	// 		child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
	//
	// 		if (index % rowBorderlineCount == 0) {
	// 			viewHeightSize += child.getMeasuredHeight();
	// 			if (index != 0) {
	// 				viewHeightSize += this.verticalPadding;
	// 			}
	// 		}
	// 	}
	//
	// 	setMeasuredDimension(viewWidthSize, viewHeightSize);
	// }
	//
	// private int remeasureChild(int parentWidthSize, int parentWidthMode) {
	// 	int childCount = getChildCount();
	// 	int rowCount = this.rowCount;
	// 	int padding = this.horizontalPadding;
	//
	// 	if (childCount == 0) {
	// 		return 0;
	// 	}
	//
	// 	if (this.showMode == FIXED) {
	// 		int netParentWidthSize = parentWidthSize - getPaddingLeft() - getPaddingRight();
	// 		return (netParentWidthSize - (rowCount - 1) * padding) / rowCount;
	// 	}
	//
	// 	for (int currentCount = rowCount; currentCount > childCount; currentCount--) {
	// 		int netParentWidthSize = parentWidthSize - getPaddingLeft() - getPaddingRight();
	// 		int childWidthSize = (netParentWidthSize - (currentCount - 1) * padding) / currentCount;
	// 		parentWidthSize -= childWidthSize / 2;
	// 	}
	// 	if (rowCount > childCount) {
	// 		rowCount = childCount;
	// 	}
	//
	// 	int netParentWidthSize = parentWidthSize - getPaddingLeft() - getPaddingRight();
	// 	return (netParentWidthSize - (rowCount - 1) * padding) / rowCount;
	// }
	//
	// @Override
	// protected void onLayout(boolean changed, int l, int t, int r, int b) {
	// 	int childCount = getChildCount();
	//
	// 	int rowBorderlineCount = (this.showMode == WRAP && this.wrapBorderlineCount != 0 && childCount > this.rowCount)
	// 			? this.wrapBorderlineCount : this.rowCount;
	//
	// 	int top = getPaddingTop();
	// 	int left = getPaddingLeft();
	// 	int topOffsetSize = this.verticalPadding;
	// 	int leftOffsetSize = this.horizontalPadding;
	// 	for (int index = 0; index < childCount; index++) {
	// 		View child = getChildAt(index);
	//
	// 		int width = child.getMeasuredWidth();
	// 		int height = child.getMeasuredHeight();
	//
	// 		MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
	// 		child.layout(left + layoutParams.leftMargin,
	// 				top + layoutParams.topMargin,
	// 				left + width - layoutParams.rightMargin,
	// 				top + height - layoutParams.bottomMargin);
	// 		if (index % rowBorderlineCount == rowBorderlineCount - 1) {
	// 			top += height;
	// 			if (index != 0) {
	// 				top += topOffsetSize;
	// 			}
	// 			left = getPaddingLeft();
	// 		} else {
	// 			left += width + leftOffsetSize;
	// 		}
	// 	}
	// }
	//
	// @Override
	// protected MarginLayoutParams generateLayoutParams(LayoutParams p) {
	// 	if (p instanceof MarginLayoutParams) {
	// 		return (MarginLayoutParams) p;
	// 	}
	// 	return new MarginLayoutParams(p);
	// }
	//
	// @Override
	// protected MarginLayoutParams generateDefaultLayoutParams() {
	// 	return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	// }
	//
	// @Override
	// public MarginLayoutParams generateLayoutParams(AttributeSet attrs) {
	// 	return new MarginLayoutParams(getContext(), attrs);
	// }
	//
	// public static class MediaView extends RelativeLayout {
	// 	private SimpleDraweeView mediaPicture;
	// 	private ImageView mediaEdit;
	//
	// 	public MediaView(Context context) {
	// 		super(context);
	// 		initContent(context);
	// 	}
	//
	// 	protected void initContent(Context context) {
	// 		this.mediaPicture = new SimpleDraweeView(context);
	// 		this.mediaPicture.setBackgroundColor(Color.BLACK);
	// 		RelativeLayout.LayoutParams pictureLayoutParams = new RelativeLayout.LayoutParams(
	// 				RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
	// 		);
	// 		this.mediaPicture.setLayoutParams(pictureLayoutParams);
	// 		addView(this.mediaPicture);
	//
	// 		this.mediaEdit = new ImageView(context);
	// 		this.mediaEdit.setScaleType(ImageView.ScaleType.CENTER);
	// 		RelativeLayout.LayoutParams editLayoutParams = new RelativeLayout.LayoutParams(
	// 				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
	// 		);
	// 		editLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	// 		editLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	// 		this.mediaEdit.setLayoutParams(editLayoutParams);
	// 		addView(this.mediaEdit);
	// 	}
	//
	// 	void setEditDrawable(Drawable editDrawable, int editPosition) {
	// 		this.mediaEdit.setImageDrawable(editDrawable);
	// 		int margin = 0;
	// 		if (editDrawable != null) {
	// 			if (editPosition == INSIDE) {
	// 				margin = 0;
	// 			} else if (editPosition == BORDER) {
	// 				margin = editDrawable.getIntrinsicWidth() / 2;
	// 			} else {
	// 				margin = editDrawable.getIntrinsicWidth();
	// 			}
	// 		}
	// 		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mediaPicture.getLayoutParams();
	// 		layoutParams.topMargin = margin;
	// 		layoutParams.rightMargin = margin;
	// 		this.mediaPicture.setLayoutParams(layoutParams);
	// 	}
	// }
	//
	
}
