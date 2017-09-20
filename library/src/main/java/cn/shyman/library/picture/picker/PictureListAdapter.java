package cn.shyman.library.picture.picker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.ArrayList;

import cn.shyman.library.picture.R;

public class PictureListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	/** 拍照 */
	private static final int CAMERA = 0;
	/** 图片 */
	private static final int PICTURE = 1;
	
	private LayoutInflater mInflater;
	
	private boolean mIsEditable;
	private OnPictureListener mOnPictureListener;
	
	private ArrayList<PictureInfo> mPictureInfoList;
	private ArrayList<PictureInfo> mSelectedPictureInfoList;
	
	PictureListAdapter(Context context,
	                   boolean editable,
	                   OnPictureListener onPictureListener) {
		mInflater = LayoutInflater.from(context);
		
		mIsEditable = editable;
		mOnPictureListener = onPictureListener;
		
		mSelectedPictureInfoList = new ArrayList<>();
	}
	
	ArrayList<PictureInfo> getSelectedPictureInfo() {
		return mSelectedPictureInfoList;
	}
	
	void setSelectedPictureInfo(ArrayList<PictureInfo> selectedPictureInfoList) {
		mSelectedPictureInfoList = selectedPictureInfoList;
	}
	
	void setPictureInfoList(ArrayList<PictureInfo> pictureInfoList) {
		mPictureInfoList = pictureInfoList;
		notifyDataSetChanged();
	}
	
	private PictureInfo getItem(int position) {
		return mPictureInfoList.get(position);
	}
	
	private void pickCamera() {
		mOnPictureListener.onCamera();
	}
	
	/**
	 * 选中图片
	 *
	 * @param adapterPosition 图片列表位置
	 */
	private void pickPicture(int adapterPosition) {
		PictureInfo pictureInfo = getItem(adapterPosition);
		
		int index = mSelectedPictureInfoList.indexOf(pictureInfo);
		if (index >= 0) {
			mSelectedPictureInfoList.remove(index);
			notifyItemChanged(adapterPosition);
			mOnPictureListener.onPick(null, mSelectedPictureInfoList.size());
		} else {
			if (mOnPictureListener.canPick(mSelectedPictureInfoList.size())) {
				mSelectedPictureInfoList.add(pictureInfo);
				notifyItemChanged(adapterPosition);
				mOnPictureListener.onPick(pictureInfo, mSelectedPictureInfoList.size());
			}
		}
	}
	
	private void pickEdit(int adapterPosition) {
		mOnPictureListener.onEdit(getItem(adapterPosition));
	}
	
	private void pickPreview(int adapterPosition) {
		mOnPictureListener.onPreview(getItem(adapterPosition), adapterPosition);
	}
	
	@Override
	public int getItemCount() {
		return mPictureInfoList != null ? mPictureInfoList.size() : 0;
	}
	
	@Override
	public int getItemViewType(int position) {
		PictureInfo pictureInfo = getItem(position);
		if (pictureInfo.isCamera()) {
			return CAMERA;
		}
		return PICTURE;
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == CAMERA) {
			return new CameraHolder(mInflater.inflate(R.layout.sp_item_camera_list, parent, false));
		}
		return new PictureHolder(mInflater.inflate(R.layout.sp_item_picture_list, parent, false));
	}
	
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		int viewType = holder.getItemViewType();
		if (viewType == PICTURE) {
			PictureHolder pictureHolder = (PictureHolder) holder;
			pictureHolder.setPictureInfo(getItem(position));
		}
	}
	
	private class CameraHolder extends RecyclerView.ViewHolder {
		
		CameraHolder(View itemView) {
			super(itemView);
			
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickCamera();
				}
			});
		}
	}
	
	private class PictureHolder extends RecyclerView.ViewHolder {
		private SimpleDraweeView spPictureView;
		private View spLayer;
		private ImageView spSelected;
		
		PictureHolder(View itemView) {
			super(itemView);
			
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mIsEditable) {
						pickEdit(getAdapterPosition());
					} else {
						pickPreview(getAdapterPosition());
					}
				}
			});
			this.spPictureView = (SimpleDraweeView) itemView.findViewById(R.id.spPictureView);
			this.spLayer = itemView.findViewById(R.id.spLayer);
			this.spSelected = (ImageView) itemView.findViewById(R.id.spSelected);
			this.spSelected.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickPicture(getAdapterPosition());
				}
			});
		}
		
		void setPictureInfo(PictureInfo pictureInfo) {
			boolean selected = mSelectedPictureInfoList.contains(pictureInfo);
			
			this.spLayer.setVisibility(mIsEditable ? View.GONE : View.VISIBLE);
			this.spLayer.setSelected(selected);
			this.spSelected.setVisibility(mIsEditable ? View.GONE : View.VISIBLE);
			this.spSelected.setSelected(selected);
			
			this.spPictureView.setController(
					Fresco.newDraweeControllerBuilder()
							.setOldController(this.spPictureView.getController())
							.setImageRequest(
									ImageRequestBuilder
											.newBuilderWithSource(pictureInfo.pictureUri)
											.setResizeOptions(new ResizeOptions(150, 150))
											.build()
							)
							.build()
			);
		}
	}
	
	/**
	 * 图片获取监听
	 */
	interface OnPictureListener {
		/**
		 * 拍照
		 */
		void onCamera();
		
		boolean canPick(int pickCount);
		
		/**
		 * 选择图片
		 */
		void onPick(PictureInfo pictureInfo, int pickCount);
		
		void onEdit(PictureInfo pictureInfo);
		
		/**
		 * 预览
		 */
		void onPreview(PictureInfo pictureInfo, int position);
	}
}
