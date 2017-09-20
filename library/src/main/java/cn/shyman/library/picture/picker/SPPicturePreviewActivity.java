package cn.shyman.library.picture.picker;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import cn.shyman.library.picture.R;

public class SPPicturePreviewActivity extends AppCompatActivity {
	private SPPicker mSPPicker;
	
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
		setContentView(R.layout.sp_activity_picture_preview);
		
		
	}
}
