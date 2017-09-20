package cn.shyman.picture;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.DraweeTransition;
import com.facebook.drawee.view.SimpleDraweeView;

public class DetailsActivity extends AppCompatActivity {
	private SimpleDraweeView simpleDraweeView;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setSharedElementEnterTransition(DraweeTransition.createTransitionSet(
					ScalingUtils.ScaleType.CENTER_CROP, ScalingUtils.ScaleType.CENTER));
			getWindow().setSharedElementReturnTransition(DraweeTransition.createTransitionSet(
					ScalingUtils.ScaleType.CENTER, ScalingUtils.ScaleType.CENTER_CROP));
		}
		
		setContentView(R.layout.activity_details);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			finish();
			return;
		}
		simpleDraweeView = (SimpleDraweeView) findViewById(R.id.simpleDraweeView);
		simpleDraweeView.setImageURI((Uri) bundle.getParcelable("uri"));
	}
}
