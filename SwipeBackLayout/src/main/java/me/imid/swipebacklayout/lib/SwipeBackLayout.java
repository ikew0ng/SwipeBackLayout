
package me.imid.swipebacklayout.lib;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class SwipeBackLayout extends FrameLayout {
    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    // Cached ViewConfiguration and system-wide constant values

    private Activity mActivity;

    // Transient properties

    private boolean mEnable = true;

    private View mContentView;

    private ViewDragHelper mLeftDragHelper;

    private float mAnimationProgress;

    private Config mConfig;

    private OnScrollListener mOnScrollListener;

    private Drawable mShadow;

    private Paint mScrimPaint = new Paint();

    private float mScrimOpacity;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;

    private Rect mChildRect = new Rect();

    private Rect mLeftRect = new Rect();

    private MotionEvent mCurEvent;

    public SwipeBackLayout(Context context) {
        this(context, null);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;
        mLeftDragHelper = ViewDragHelper.create(this, new ViewDragCallback());
        mLeftDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        mLeftDragHelper.setMinVelocity(minVel);

        setShadow(R.drawable.shadow);
    }

    public void setContentView(View view) {
        mContentView = view;
    }

    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }

    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    public static interface OnScrollListener {
        public void onScroll();

        public void onScrollEnd();

        public void onReverseScroll();

        public void onReverseScrollEnd();
    }

    /**
     * Set a drawable used for edge shadow.
     * 
     * @param shadow Drawable to use
     */
    public void setShadow(Drawable shadow) {
        mShadow = shadow;
        invalidate();
    }

    /**
     * Set a drawable used for edge shadow.
     * 
     * @param resId Resource of drawable to use
     */
    public void setShadow(int resId) {
        setShadow(getResources().getDrawable(resId));
    }

    public static class Config {
        public static enum TouchMode {
            /**
             * Allow SwipeBackLayout to
             */
            TOUCH_MODE_MARGIN, TOUCH_MODE_FULLSCREEN, TOUCH_MODE_PERCENTAGE
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }
        mCurEvent = MotionEvent.obtain(event);
        final int action = MotionEventCompat.getActionMasked(event);
        final View contentView = mContentView;

        final boolean interceptForDrag = mLeftDragHelper.shouldInterceptTouchEvent(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return interceptForDrag;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }
        mCurEvent = MotionEvent.obtain(event);
        mLeftDragHelper.processTouchEvent(event);
        final int action = MotionEventCompat.getActionMasked(event);
        final View contentView = mContentView;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return true;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean drawContent = child == mContentView;
        drawShadow(canvas, child);
        if (mScrimOpacity > 0 && drawContent
                && mLeftDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawScrim(canvas, child);
        }

        return super.drawChild(canvas, child, drawingTime);
    }

    private void drawScrim(Canvas canvas, View child) {
        final int restoreCount = canvas.save();
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final int imag = (int) (baseAlpha * mScrimOpacity);
        final int color = imag << 24 | (mScrimColor & 0xffffff);
        mScrimPaint.setColor(color);
        canvas.clipRect(0, 0, child.getLeft(), getHeight());
        canvas.drawRect(0, 0, getWidth(), getHeight(), mScrimPaint);
        canvas.restoreToCount(restoreCount);
    }

    private void drawShadow(Canvas canvas, View child) {
        final int shadowWidth = mShadow.getIntrinsicWidth();
        final int childLeft = child.getLeft();
        mShadow.setBounds(childLeft - shadowWidth, child.getTop(), childLeft, child.getBottom());
        mShadow.draw(canvas);
    }

    public void attachToActivity(Activity activity) {
        mActivity = activity;
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[] {
            android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        decorChild.setBackgroundResource(background);
        decor.removeView(decorChild);
        addView(decorChild);
        setContentView(decorChild);
        decor.addView(this);
    }

    @Override
    public void computeScroll() {
        mScrimOpacity = 1 - mAnimationProgress;
        if (mLeftDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View view, int i) {
            final int x = (int) MotionEventCompat.getX(mCurEvent, i);
            final int y = (int) MotionEventCompat.getY(mCurEvent, i);
            mLeftRect.set(0, 0, getWidth() / 3, getHeight());
            return mLeftRect.contains(x, y);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            mAnimationProgress = (float) left
                    / (mContentView.getWidth() + mShadow.getIntrinsicWidth());
            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final int childWidth = releasedChild.getWidth();
            final float offset = (float) releasedChild.getLeft() / releasedChild.getWidth();

            int left;
            left = xvel > 0 || xvel == 0 && offset > 0.5f ? childWidth
                    + mShadow.getIntrinsicWidth() : 0;
            mLeftDragHelper.settleCapturedViewAt(left, releasedChild.getTop());
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.min(child.getWidth(), Math.max(left, 0));
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (state == ViewDragHelper.STATE_IDLE) {
                if (mAnimationProgress == 1) {
                    mActivity.finish();
                }
            }
        }
    }
}
