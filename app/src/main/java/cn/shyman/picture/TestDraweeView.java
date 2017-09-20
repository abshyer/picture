package cn.shyman.picture;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

public class TestDraweeView extends SimpleDraweeView {
	
	public TestDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
		super(context, hierarchy);
	}
	
	public TestDraweeView(Context context) {
		super(context);
	}
	
	public TestDraweeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TestDraweeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public TestDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	
	public void animateTransform(Matrix matrix) {
		invalidate();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Drawable drawable = getTopLevelDrawable();
		if (drawable != null) {
			drawable.setBounds(0, 0, w, h);
		}
	}
}
