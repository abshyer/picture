package cn.shyman.library.picture.widget;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;

public abstract class AbstractAnimatedZoomableController extends DefaultZoomableController {
	private final float[] mStartValues = new float[9];
	private final float[] mStopValues = new float[9];
	private final float[] mCurrentValues = new float[9];
	private final Matrix mNewTransform = new Matrix();
	private final Matrix mWorkingTransform = new Matrix();
	private boolean mIsAnimating;


	public AbstractAnimatedZoomableController(TransformGestureDetector transformGestureDetector) {
		super(transformGestureDetector);
	}

	@Override
	public void reset() {
		stopAnimation();
		mWorkingTransform.reset();
		mNewTransform.reset();
		super.reset();
	}

	/**
	 * Returns true if the zoomable transform is identity matrix, and the controller is idle.
	 */
	@Override
	public boolean isIdentity() {
		return !isAnimating() && super.isIdentity();
	}

	/**
	 * Zooms to the desired scale and positions the image so that the given image point corresponds
	 * to the given view point.
	 * <p>
	 * <p>If this method is called while an animation or gesture is already in progress,
	 * the current animation or gesture will be stopped first.
	 *
	 * @param scale      desired scale, will be limited to {min, max} scale factor
	 * @param imagePoint 2D point in image's relative coordinate system (i.e. 0 <= x, y <= 1)
	 * @param viewPoint  2D point in view's absolute coordinate system
	 */
	@Override
	public void zoomToPoint(
			float scale,
			PointF imagePoint,
			PointF viewPoint) {
		zoomToPoint(scale, imagePoint, viewPoint, LIMIT_ALL, 0, null);
	}

	/**
	 * Zooms to the desired scale and positions the image so that the given image point corresponds
	 * to the given view point.
	 * <p>
	 * <p>If this method is called while an animation or gesture is already in progress,
	 * the current animation or gesture will be stopped first.
	 *
	 * @param scale               desired scale, will be limited to {min, max} scale factor
	 * @param imagePoint          2D point in image's relative coordinate system (i.e. 0 <= x, y <= 1)
	 * @param viewPoint           2D point in view's absolute coordinate system
	 * @param limitFlags          whether to limit translation and/or scale.
	 * @param durationMs          length of animation of the zoom, or 0 if no animation desired
	 * @param onAnimationComplete code to run when the animation completes. Ignored if durationMs=0
	 */
	public void zoomToPoint(
			float scale,
			PointF imagePoint,
			PointF viewPoint,
			@LimitFlag int limitFlags,
			long durationMs,
			@Nullable Runnable onAnimationComplete) {
		calculateZoomToPointTransform(
				mNewTransform,
				scale,
				imagePoint,
				viewPoint,
				limitFlags);
		setTransform(mNewTransform, durationMs, onAnimationComplete);
	}

	/**
	 * Sets a new zoomable transformation and animates to it if desired.
	 * <p>
	 * <p>If this method is called while an animation or gesture is already in progress,
	 * the current animation or gesture will be stopped first.
	 *
	 * @param newTransform        new transform to make active
	 * @param durationMs          duration of the animation, or 0 to not animate
	 * @param onAnimationComplete code to run when the animation completes. Ignored if durationMs=0
	 */
	public void setTransform(
			Matrix newTransform,
			long durationMs,
			@Nullable Runnable onAnimationComplete) {
		if (durationMs <= 0) {
			setTransformImmediate(newTransform);
		} else {
			setTransformAnimated(newTransform, durationMs, onAnimationComplete);
		}
	}

	private void setTransformImmediate(final Matrix newTransform) {
		stopAnimation();
		mWorkingTransform.set(newTransform);
		super.setTransform(newTransform);
		getDetector().restartGesture();
	}

	protected boolean isAnimating() {
		return mIsAnimating;
	}

	protected void setAnimating(boolean isAnimating) {
		mIsAnimating = isAnimating;
	}

	protected float[] getStartValues() {
		return mStartValues;
	}

	protected float[] getStopValues() {
		return mStopValues;
	}

	protected Matrix getWorkingTransform() {
		return mWorkingTransform;
	}

	@Override
	public void onGestureBegin(TransformGestureDetector detector) {
		stopAnimation();
		super.onGestureBegin(detector);
	}

	@Override
	public void onGestureUpdate(TransformGestureDetector detector) {
		if (isAnimating()) {
			return;
		}
		super.onGestureUpdate(detector);
	}

	protected void calculateInterpolation(Matrix outMatrix, float fraction) {
		for (int i = 0; i < 9; i++) {
			mCurrentValues[i] = (1 - fraction) * mStartValues[i] + fraction * mStopValues[i];
		}
		outMatrix.setValues(mCurrentValues);
	}

	public abstract void setTransformAnimated(
			final Matrix newTransform,
			long durationMs,
			@Nullable final Runnable onAnimationComplete);

	protected abstract void stopAnimation();

}
