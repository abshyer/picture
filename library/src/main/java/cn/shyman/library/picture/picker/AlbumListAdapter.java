package cn.shyman.library.picture.picker;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.util.ArrayList;

import cn.shyman.library.picture.R;

class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.AlbumHolder> {
	private LayoutInflater mInflater;
	
	private OnAlbumListener mOnAlbumListener;
	
	private ArrayList<AlbumInfo> mAlbumInfoList;
	private int mSelectedPosition = 0;
	
	AlbumListAdapter(Context context, OnAlbumListener onAlbumListener) {
		mInflater = LayoutInflater.from(context);
		mOnAlbumListener = onAlbumListener;
	}
	
	void setAlbumInfoList(ArrayList<AlbumInfo> albumInfoList) {
		mAlbumInfoList = albumInfoList;
		notifyDataSetChanged();
	}
	
	private AlbumInfo getItem(int position) {
		return mAlbumInfoList != null ? mAlbumInfoList.get(position) : null;
	}
	
	AlbumInfo getSelectedAlbumInfo() {
		return getItem(mSelectedPosition);
	}
	
	int getSelectedPosition() {
		return mSelectedPosition;
	}
	
	void setSelectedPosition(int selectedPosition) {
		int oldSelectedPosition = mSelectedPosition;
		mSelectedPosition = selectedPosition;
		notifyItemChanged(oldSelectedPosition);
		notifyItemChanged(selectedPosition);
		
		if (mAlbumInfoList != null) {
			mOnAlbumListener.onAlbum(getItem(selectedPosition));
		}
	}
	
	@Override
	public int getItemCount() {
		return mAlbumInfoList != null ? mAlbumInfoList.size() : 0;
	}
	
	@Override
	public AlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new AlbumHolder(mInflater.inflate(R.layout.sp_item_album_list, parent, false));
	}
	
	@Override
	public void onBindViewHolder(AlbumHolder holder, int position) {
		holder.setAlbumInfo(getItem(position));
	}
	
	class AlbumHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private SimpleDraweeView spPictureView;
		private TextView spTitle;
		private TextView spContent;
		private ImageView spSelected;
		
		AlbumHolder(View itemView) {
			super(itemView);
			
			itemView.setOnClickListener(this);
			this.spPictureView = (SimpleDraweeView) itemView.findViewById(R.id.spPictureView);
			this.spTitle = (TextView) itemView.findViewById(R.id.spTitle);
			this.spContent = (TextView) itemView.findViewById(R.id.spContent);
			this.spSelected = (ImageView) itemView.findViewById(R.id.spSelected);
		}
		
		/**
		 * 设置相册信息
		 *
		 * @param albumInfo 相册信息
		 */
		void setAlbumInfo(AlbumInfo albumInfo) {
			this.spTitle.setText(albumInfo.albumName);
			this.spContent.setText(String.valueOf(albumInfo.albumPictureCount));
			// this.kpContent.setText(inflater.getContext().getString(
			// 		R.string.kp_format_pieces_of_picture, albumInfo.pictureInfoList.size()));
			this.spSelected.setSelected(mSelectedPosition == getAdapterPosition());
			
			Uri pictureUri = Uri.fromFile(new File(albumInfo.albumCoverUrl));
			this.spPictureView.setController(
					Fresco.newDraweeControllerBuilder()
							.setOldController(this.spPictureView.getController())
							.setImageRequest(
									ImageRequestBuilder.newBuilderWithSource(pictureUri)
											.setResizeOptions(new ResizeOptions(300, 300))
											.build()
							)
							.build()
			);
		}
		
		@Override
		public void onClick(View v) {
			setSelectedPosition(getAdapterPosition());
		}
	}
	
	interface OnAlbumListener {
		
		void onAlbum(AlbumInfo albumInfo);
	}
}
