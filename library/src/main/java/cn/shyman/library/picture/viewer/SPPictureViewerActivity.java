package cn.shyman.library.picture.viewer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.DraweeTransition;

import cn.shyman.library.picture.R;

public class SPPictureViewerActivity extends AppCompatActivity implements Handler.Callback {
	private SPViewer mSPViewer;
	
	private boolean mIsFullScreen;
	private View mToolbar;
	
	private ViewPager mViewPager;
	private PicturePagerAdapter mPicturePagerAdapter;
	
	private TextView mTVPageIndicator;
	private Handler mPageIndicatorHandler = new Handler(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setSharedElementEnterTransition(DraweeTransition.createTransitionSet(
					ScalingUtils.ScaleType.CENTER_CROP, ScalingUtils.ScaleType.CENTER));
			getWindow().setSharedElementReturnTransition(DraweeTransition.createTransitionSet(
					ScalingUtils.ScaleType.CENTER, ScalingUtils.ScaleType.CENTER_CROP));
		}
		
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			finish();
			return;
		}
		mSPViewer = bundle.getParcelable(SPViewer.VIEWER);
		if (mSPViewer == null) {
			finish();
			return;
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
		setTheme(mSPViewer.themeId);
		setContentView(R.layout.sp_activity_picture_viewer);
		
		observeToolbar();
		observePicturePager();
		toggleFullScreen();
	}
	
	private void observeToolbar() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}
		
		mToolbar = findViewById(R.id.spToolbar);
		View spBack = findViewById(R.id.spBack);
		spBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		View spEdit = findViewById(R.id.spEdit);
		spEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSPViewer.pictureUriList.remove(mViewPager.getCurrentItem());
				mPicturePagerAdapter.notifyDataSetChanged();
				
				Intent data = new Intent();
				data.putExtra(SPViewer.VIEWER, mSPViewer);
				setResult(RESULT_OK, data);
				if (mSPViewer.pictureUriList.size() == 0) {
					finish();
				} else {
					onPageSelected(mViewPager.getCurrentItem());
				}
			}
		});
		spEdit.setVisibility(mSPViewer.editable ? View.VISIBLE : View.GONE);
	}
	
	private void observePicturePager() {
		mViewPager = (ViewPager) findViewById(R.id.spViewPager);
		mTVPageIndicator = (TextView) findViewById(R.id.spPageIndicator);
		ViewCompat.setTransitionName(mViewPager, SPViewer.TRANSITION_NAME);
		
		mPicturePagerAdapter = new PicturePagerAdapter(
				this, mSPViewer,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent event) {
						if (mSPViewer.editable) {
							toggleFullScreen();
						} else {
							ActivityCompat.finishAfterTransition(SPPictureViewerActivity.this);
						}
						return true;
					}
				});
		// 切换动画
		mViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
			@Override
			public void transformPage(View page, float position) {
				int pageWidth = page.getWidth();
				int pageHeight = page.getHeight();
				
				if (position < -1) {// [-Infinity, -1)
					page.setAlpha(0);
				} else if (position <= 1) {// [-1, 1]
					float scaleFactor = Math.max(0.85f, 1 - Math.abs(position));
					float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
					float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;
					
					if (position < 0) {
						page.setTranslationX(horizontalMargin - verticalMargin / 2);
					} else {
						page.setTranslationY(-horizontalMargin + verticalMargin / 2);
					}
					
					page.setScaleX(scaleFactor);
					page.setScaleY(scaleFactor);
					
					page.setAlpha(0.5f + (scaleFactor - 0.85f) / (1 - 0.85f) * (1 - 0.5f));
				} else {// (1, +Infinity]
					page.setAlpha(0);
				}
			}
		});
		mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				SPPictureViewerActivity.this.onPageSelected(position);
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				mPageIndicatorHandler.removeMessages(0);
				showPageIndicator();
				if (state == ViewPager.SCROLL_STATE_IDLE) {
					mPageIndicatorHandler.sendEmptyMessageDelayed(0, 1000);
				}
			}
		});
		
		mViewPager.setAdapter(mPicturePagerAdapter);
		mViewPager.setCurrentItem(mSPViewer.position, false);
		onPageSelected(mSPViewer.position);
	}
	
	private void onPageSelected(int position) {
		mTVPageIndicator.setText((position + 1) + "/" + mPicturePagerAdapter.getCount());
		this.mPageIndicatorHandler.sendEmptyMessageDelayed(0, 1000);
	}
	
	/**
	 * 切换全屏
	 */
	private void toggleFullScreen() {
		mIsFullScreen = !mIsFullScreen;
		if (mIsFullScreen) {
			// 	// 状态栏
			// 	getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			// 	getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
			hideToolbar();
		} else {
			// 	// 状态栏
			// 	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			showToolbar();
		}
	}
	
	private void showToolbar() {
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, -1F,
				Animation.RELATIVE_TO_SELF, 0F);
		animation.setDuration(300);
		mToolbar.startAnimation(animation);
		mToolbar.setVisibility(View.VISIBLE);
	}
	
	private void hideToolbar() {
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, -1F);
		animation.setDuration(300);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mToolbar.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		mToolbar.startAnimation(animation);
	}
	
	private void showPageIndicator() {
		if (mTVPageIndicator.getVisibility() == View.VISIBLE) {
			return;
		}
		
		AlphaAnimation animation = new AlphaAnimation(0F, 1F);
		animation.setDuration(300);
		mTVPageIndicator.startAnimation(animation);
		mTVPageIndicator.setVisibility(View.VISIBLE);
	}
	
	private void hidePageIndicator() {
		if (mTVPageIndicator.getVisibility() == View.GONE) {
			return;
		}
		
		AlphaAnimation animation = new AlphaAnimation(1F, 0F);
		animation.setDuration(300);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mTVPageIndicator.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		mTVPageIndicator.startAnimation(animation);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		hidePageIndicator();
		return true;
	}
}
