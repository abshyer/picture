package cn.shyman.library.picture.picker;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.yalantis.ucrop.UCrop;

import java.util.ArrayList;
import java.util.List;

import cn.shyman.library.picture.R;
import cn.shyman.library.picture.widget.SPLoadingDialog;

public class SPPicturePickerActivity extends AppCompatActivity {
	/** 权限 */
	private static final int PERMISSION = 1;
	
	private static final int CAPTURE = 1;
	private static final int CROP = 2;
	
	private static final String SELECTED_ALBUM = "selectedAlbum";
	private static final String SELECTED_PICTURE = "selectedPicture";
	private static final String CACHE_PICTURE = "cachePicture";
	
	private SPLoadingDialog mLoadingDialog;
	
	private SPPicker mSPPicker;
	
	private TextView mTVDone;
	private View mActionbar;
	private TextView mTVAlbumName;
	private TextView mTVPreview;
	
	private View mAlbumLayer;
	private Animation mLayerShowAnimation;
	private Animation mLayerHideAnimation;
	
	private AlbumTask mAlbumTask;
	private RecyclerView mRecyclerViewAlbum;
	private AlbumListAdapter mAlbumListAdapter;
	private Animation mAlbumShowAnimation;
	private Animation mAlbumHideAnimation;
	
	private PictureTask mPictureTask;
	private RecyclerView mRecyclerViewPicture;
	private PictureListAdapter mPictureListAdapter;
	
	private CompressTask mCompressTask;
	
	private PictureInfo mCachePictureInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			finish();
			return;
		}
		mSPPicker = bundle.getParcelable(SPPicker.PICKER);
		if (mSPPicker == null) {
			finish();
			return;
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
		setTheme(mSPPicker.themeId);
		setContentView(R.layout.sp_activity_picture_picker);
		
		mLoadingDialog = new SPLoadingDialog(this);
		
		observeToolbar();
		observeAlbumList();
		observePictureList();
		
		if (savedInstanceState != null) {
			mAlbumListAdapter.setSelectedPosition(savedInstanceState.getInt(SELECTED_ALBUM));
			mPictureListAdapter.setSelectedPictureInfo(savedInstanceState.<PictureInfo>getParcelableArrayList(SELECTED_PICTURE));
			mCachePictureInfo = savedInstanceState.getParcelable(CACHE_PICTURE);
			alterPickCount(mPictureListAdapter.getSelectedPictureInfo().size());
		} else {
			alterPickCount(0);
		}
		requestAlbumList();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_ALBUM, mAlbumListAdapter.getSelectedPosition());
		outState.putParcelableArrayList(SELECTED_PICTURE, mPictureListAdapter.getSelectedPictureInfo());
		outState.putParcelable(CACHE_PICTURE, mCachePictureInfo);
	}
	
	private void observeToolbar() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}
		
		View kpBack = findViewById(R.id.spBack);
		kpBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		mTVDone = (TextView) findViewById(R.id.spDone);
		mTVDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestCompress(mPictureListAdapter.getSelectedPictureInfo());
			}
		});
		mTVDone.setVisibility(mSPPicker.editable ? View.GONE : View.VISIBLE);
		
		mActionbar = findViewById(R.id.spActionbar);
		findViewById(R.id.spAlbum).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleAlbumAndLayer();
			}
		});
		mTVAlbumName = (TextView) findViewById(R.id.spAlbumName);
		
		mTVPreview = (TextView) findViewById(R.id.spPreview);
		mTVPreview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Intent intent = new Intent(KPPicturePickerActivity.this, KPPicturePreviewActivity.class);
				// intent.putExtra(KPPicker.PICKER, KPPicturePickerActivity.this.kpPicker);
				// intent.putExtra(KPPicturePreviewActivity.PREVIEW_PICTURE_POSITION, 0);
				// startActivityForResult(intent, PREVIEW);
			}
		});
		// mTVPreview.setVisibility(mSPPicker.editable ? View.GONE : View.VISIBLE);
		mTVPreview.setVisibility(View.GONE);
		
		mAlbumLayer = findViewById(R.id.spAlbumLayer);
		mAlbumLayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleAlbumAndLayer();
			}
		});
		mLayerShowAnimation = new AlphaAnimation(0F, 1F);
		mLayerShowAnimation.setDuration(400);
		mLayerHideAnimation = new AlphaAnimation(1F, 0F);
		mLayerHideAnimation.setDuration(400);
		mLayerHideAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mAlbumLayer.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
	}
	
	private void observeAlbumList() {
		mAlbumShowAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_PARENT, 1F,
				Animation.RELATIVE_TO_PARENT, 0F);
		mAlbumShowAnimation.setDuration(400);
		mAlbumHideAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_PARENT, 0F,
				Animation.RELATIVE_TO_PARENT, 1F);
		mAlbumHideAnimation.setDuration(400);
		mAlbumHideAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mRecyclerViewAlbum.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		
		mRecyclerViewAlbum = (RecyclerView) findViewById(R.id.spRecyclerViewAlbum);
		mRecyclerViewAlbum.setLayoutManager(new LinearLayoutManager(this));
		mAlbumListAdapter = new AlbumListAdapter(this, new AlbumListAdapter.OnAlbumListener() {
			@Override
			public void onAlbum(AlbumInfo albumInfo) {
				if (mAlbumLayer.getVisibility() == View.VISIBLE) {
					toggleAlbumAndLayer();
				}
				requestPictureList();
			}
		});
		mRecyclerViewAlbum.setAdapter(mAlbumListAdapter);
	}
	
	private void toggleAlbumAndLayer() {
		if ((mLayerShowAnimation.hasStarted() && !mLayerShowAnimation.hasEnded())
				|| (mLayerHideAnimation.hasStarted() && !mLayerHideAnimation.hasEnded())) {
			return;
		}
		if (mAlbumLayer.getVisibility() == View.VISIBLE) {
			mAlbumLayer.startAnimation(mLayerHideAnimation);
			mRecyclerViewAlbum.startAnimation(mAlbumHideAnimation);
		} else {
			mAlbumLayer.startAnimation(mLayerShowAnimation);
			mAlbumLayer.setVisibility(View.VISIBLE);
			mRecyclerViewAlbum.startAnimation(mAlbumShowAnimation);
			mRecyclerViewAlbum.setVisibility(View.VISIBLE);
		}
	}
	
	private void observePictureList() {
		mRecyclerViewPicture = (RecyclerView) findViewById(R.id.spRecyclerViewPicture);
		mRecyclerViewPicture.setLayoutManager(new GridLayoutManager(this, 3));
		mRecyclerViewPicture.addItemDecoration(new PictureItemDecoration(this));
		
		mRecyclerViewPicture.getItemAnimator().setChangeDuration(0);
		mRecyclerViewPicture.getItemAnimator().setAddDuration(0);
		mRecyclerViewPicture.getItemAnimator().setMoveDuration(0);
		mRecyclerViewPicture.getItemAnimator().setRemoveDuration(0);
		
		mPictureListAdapter = new PictureListAdapter(this,
				mSPPicker.editable,
				new PictureListAdapter.OnPictureListener() {
					
					@Override
					public void onCamera() {
						requestCapture();
					}
					
					@Override
					public boolean canPick(int pickCount) {
						return pickCount < mSPPicker.count;
					}
					
					@Override
					public void onPick(PictureInfo pictureInfo, int pickCount) {
						alterPickCount(pickCount);
					}
					
					@Override
					public void onEdit(PictureInfo pictureInfo) {
						requestCrop(pictureInfo);
					}
					
					@Override
					public void onPreview(PictureInfo pictureInfo, int position) {
					}
				});
		mRecyclerViewPicture.setAdapter(mPictureListAdapter);
	}
	
	private void alterPickCount(int pickCount) {
		if (pickCount > 0) {
			mTVDone.setText(getString(R.string.sp_format_done, pickCount, mSPPicker.count));
			mTVPreview.setText(getString(R.string.sp_format_preview, pickCount));
		} else {
			mTVDone.setText(R.string.sp_done);
			mTVPreview.setText(R.string.sp_preview);
		}
		mTVDone.setEnabled(pickCount > 0);
		mTVPreview.setEnabled(pickCount > 0);
	}
	
	private void requestAlbumList() {
		if (!requestStoragePermission()) {
			return;
		}
		if (mAlbumTask != null) {
			mAlbumTask.cancel(true);
		}
		mAlbumTask = new AlbumTask(getContentResolver(),
				new AlbumTask.OnAlbumListener() {
					@Override
					public void onAlbum(ArrayList<AlbumInfo> albumInfoList) {
						mActionbar.setVisibility(View.VISIBLE);
						mAlbumListAdapter.setAlbumInfoList(albumInfoList);
						requestPictureList();
					}
				});
		mAlbumTask.execute();
	}
	
	private void requestPictureList() {
		AlbumInfo albumInfo = mAlbumListAdapter.getSelectedAlbumInfo();
		
		mTVAlbumName.setText(albumInfo.albumName);
		
		if (mPictureTask != null) {
			mPictureTask.cancel(true);
		}
		mPictureTask = new PictureTask(getContentResolver(), albumInfo,
				new PictureTask.OnPictureListener() {
					@Override
					public void onPicture(ArrayList<PictureInfo> pictureInfoList) {
						mPictureListAdapter.setPictureInfoList(pictureInfoList);
						mRecyclerViewPicture.setAdapter(mPictureListAdapter);
					}
				});
		mPictureTask.execute();
	}
	
	private void requestCapture() {
		mCachePictureInfo = mSPPicker.createPictureInfo();
		Uri pictureUri = mCachePictureInfo.getPictureContentUri(SPPicturePickerActivity.this);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
		intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			for (ResolveInfo resolveInfo : resolveInfoList) {
				String packageName = resolveInfo.activityInfo.packageName;
				grantUriPermission(packageName, pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
			}
		}
		startActivityForResult(intent, CAPTURE);
	}
	
	private void requestCrop(PictureInfo pictureInfo) {
		if (mSPPicker.aspectX > 0 && mSPPicker.aspectY > 0) {
			mCachePictureInfo = mSPPicker.createPictureInfo();
			
			TypedArray ta = getTheme().obtainStyledAttributes(new int[]{R.attr.colorPrimaryDark, R.attr.pickerToolbarBackground});
			int colorPrimaryDark = ta.getColor(0, 0);
			int toolbarBackground = ta.getColor(1, 0);
			ta.recycle();
			
			UCrop uCrop = UCrop.of(pictureInfo.pictureUri, mCachePictureInfo.pictureUri);
			UCrop.Options options = new UCrop.Options();
			options.setCropGridColumnCount(0);
			options.setCropGridRowCount(0);
			options.setHideBottomControls(true);
			options.setStatusBarColor(colorPrimaryDark);
			options.setToolbarColor(toolbarBackground);
			uCrop.withOptions(options);
			uCrop.withAspectRatio(mSPPicker.aspectX, mSPPicker.aspectY);
			if (mSPPicker.maxWidth > 0 && mSPPicker.maxHeight > 0) {
				uCrop.withMaxResultSize(mSPPicker.maxWidth, mSPPicker.maxHeight);
			}
			uCrop.start(this);
			return;
		}
		
		mCachePictureInfo = mSPPicker.createPictureInfo();
		
		try {
			Uri inputUri = pictureInfo.getPictureContentUri(this);
			Uri outputUri = mCachePictureInfo.getPictureContentUri(this);
			
			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			intent.setDataAndType(inputUri, "image/*");
			intent.putExtra("crop", "true");
			if (mSPPicker.aspectX > 0 && mSPPicker.aspectY > 0) {
				intent.putExtra("aspectX", mSPPicker.aspectX);
				intent.putExtra("aspectY", mSPPicker.aspectY);
			}
			intent.putExtra("scale", false);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				intent.setClipData(ClipData.newRawUri(MediaStore.EXTRA_OUTPUT, outputUri));
			}
			intent.putExtra("return-data", false);
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			intent.putExtra("noFaceDetection", false);
			startActivityForResult(intent, CROP);
		} catch (Exception ignored) {
			mCachePictureInfo = null;
			requestCompress(pictureInfo);
		}
	}
	
	private void requestCompress(PictureInfo pictureInfo) {
		ArrayList<PictureInfo> selectedPictureUriList = new ArrayList<>();
		selectedPictureUriList.add(pictureInfo);
		requestCompress(selectedPictureUriList);
	}
	
	private void requestCompress(ArrayList<PictureInfo> pictureInfoList) {
		if (mCompressTask != null) {
			mCompressTask.cancel(true);
		}
		
		showLoading();
		mCompressTask = new CompressTask(mSPPicker, pictureInfoList,
				new CompressTask.OnCompressListener() {
					@Override
					public void onCompress(ArrayList<PictureInfo> pictureInfoList) {
						hideLoading();
						Intent data = new Intent();
						data.putExtra(SPPicker.PICKER, pictureInfoList);
						setResult(RESULT_OK, data);
						finish();
					}
				});
		mCompressTask.execute();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE) {
			PictureInfo pictureInfo = mCachePictureInfo;
			mCachePictureInfo = null;
			if (pictureInfo == null) {
				return;
			}
			
			Uri pictureUri = pictureInfo.getPictureContentUri(this);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				revokeUriPermission(pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
			}
			
			if (resultCode == RESULT_OK) {
				if (mSPPicker.editable) {
					requestCrop(pictureInfo);
				} else {
					requestCompress(pictureInfo);
				}
				return;
			}
			pictureInfo.delete();
			return;
		}
		if (requestCode == CROP) {
			PictureInfo pictureInfo = mCachePictureInfo;
			mCachePictureInfo = null;
			if (pictureInfo == null) {
				return;
			}
			
			Uri pictureUri = pictureInfo.getPictureContentUri(this);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				revokeUriPermission(pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
			}
			
			if (resultCode == RESULT_OK) {
				requestCompress(pictureInfo);
				return;
			}
			pictureInfo.delete();
			return;
		}
		if (requestCode == UCrop.REQUEST_CROP) {
			PictureInfo pictureInfo = mCachePictureInfo;
			mCachePictureInfo = null;
			if (pictureInfo == null) {
				return;
			}
			
			if (resultCode == RESULT_OK) {
				requestCompress(pictureInfo);
				return;
			}
			pictureInfo.delete();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	void showLoading() {
		if (!isFinishing() && !mLoadingDialog.isShowing()) {
			mLoadingDialog.show();
		}
	}
	
	void hideLoading() {
		if (mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
	}
	
	/**
	 * 请求读取文件权限
	 *
	 * @return true已授权
	 */
	private boolean requestStoragePermission() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				ActivityCompat.requestPermissions(
						this,
						new String[]{
								Manifest.permission.READ_EXTERNAL_STORAGE,
								Manifest.permission.WRITE_EXTERNAL_STORAGE,
								Manifest.permission.CAMERA
						}, PERMISSION
				);
			} else {
				ActivityCompat.requestPermissions(
						this,
						new String[]{
								Manifest.permission.WRITE_EXTERNAL_STORAGE,
								Manifest.permission.CAMERA
						}, PERMISSION
				);
			}
			return false;
		}
		return true;
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION) {
			for (int grant : grantResults) {
				if (grant == PackageManager.PERMISSION_DENIED) {
					setResult(RESULT_FIRST_USER);
					finish();
					return;
				}
			}
			requestAlbumList();
			return;
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	
	@Override
	public void onBackPressed() {
		if (mAlbumLayer.getVisibility() == View.VISIBLE) {
			toggleAlbumAndLayer();
			return;
		}
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		hideLoading();
		if (mAlbumTask != null) {
			mAlbumTask.cancel(true);
		}
		if (mPictureTask != null) {
			mPictureTask.cancel(true);
		}
		if (mCompressTask != null) {
			mCompressTask.cancel(true);
		}
	}
}
