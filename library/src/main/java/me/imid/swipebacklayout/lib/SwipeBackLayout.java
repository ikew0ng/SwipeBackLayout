
package me.imid.swipebacklayout.lib;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
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

    /**
     * Edge flag indicating that the left edge should be affected.
     */
    public static final int EDGE_LEFT = 1 << 0;

    /**
     * Edge flag indicating that the right edge should be affected.
     */
    public static final int EDGE_RIGHT = 1 << 1;

    /**
     * Edge flag indicating that the bottom edge should be affected.
     */
    public static final int EDGE_BOTTOM = 1 << 3;

    /**
     * Edge flag set indicating all edges should be affected.
     */
    public static final int EDGE_ALL = EDGE_LEFT | EDGE_RIGHT | EDGE_BOTTOM;

    private int mEdgeFlag = EDGE_LEFT;

    private Activity mActivity;

    private boolean mEnable = true;

    private View mContentView;

    private ViewDragHelper mDragHelper;

    private float mScrollPercent;

    private int mContentLeft;

    private int mContentTop;

    private OnScrollListener mOnScrollListener;

    private Drawable mShadowLeft;

    private Drawable mShadowRight;

    private Drawable mShadowBottom;

    private Paint mScrimPaint = new Paint();

    private float mScrimOpacity;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;

    private boolean mInLayout;

    private Rect mTmpRect = new Rect();

    public SwipeBackLayout(Context context) {
        this(context, null);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.SwipeBackLayoutStyle);

        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;

        mDragHelper = ViewDragHelper.create(this, new ViewDragCallback());
        mDragHelper.setEdgeTrackingEnabled(mEdgeFlag);
        mDragHelper.setMinVelocity(minVel);

        setShadow(R.drawable.shadow_left, EDGE_LEFT);
        setShadow(R.drawable.shadow_right, EDGE_RIGHT);
        setShadow(R.drawable.shadow_bottom, EDGE_BOTTOM);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

    }

    /**
     * Set up contentView which will be moved by user gesture
     * 
     * @param view
     */
    private void setContentView(View view) {
        mContentView = view;
    }

    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }

    /**
     * Set a color to use for the scrim that obscures primary content while a
     * drawer is open.
     * 
     * @param color Color to use in 0xAARRGGBB format.
     */
    public void setScrimColor(int color) {
        mScrimColor = color;
        invalidate();
    }

    /**
     * Set the size of an edge. This is the range in pixels along the edges of
     * this view that will actively detect edge touches or drags if edge
     * tracking is enabled.
     * 
     * @param size The size of an edge in pixels
     */
    public void setEdgeSize(int size) {
        mDragHelper.setEdgeSize(size);
    }

    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    public static interface OnScrollListener {
        public void onScrollStateChange(int state, float scrollPercent);
    }

    /**
     * Set a drawable used for edge shadow.
     * 
     * @param shadow Drawable to use
     * @param edgeFlags Combination of edge flags describing the edge to set
     * @see #EDGE_LEFT
     * @see #EDGE_RIGHT
     * @see #EDGE_BOTTOM
     */
    public void setShadow(Drawable shadow, int edgeFlag) {
        if ((edgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft = shadow;
        } else if ((edgeFlag & EDGE_RIGHT) != 0) {
            mShadowRight = shadow;
        } else if ((edgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom = shadow;
        }
        invalidate();
    }

    /**
     * Set a drawable used for edge shadow.
     * 
     * @param resId Resource of drawable to use
     * @param edgeFlags Combination of edge flags describing the edge to set
     * @see #EDGE_LEFT
     * @see #EDGE_RIGHT
     * @see #EDGE_BOTTOM
     */
    public void setShadow(int resId, int edgeFlag) {
        setShadow(getResources().getDrawable(resId), edgeFlag);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }
        return mDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mInLayout = true;
        mContentView.layout(mContentLeft, mContentTop,
                mContentLeft + mContentView.getMeasuredWidth(),
                mContentTop + mContentView.getMeasuredHeight());
        mInLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean drawContent = child == mContentView;
        drawShadow(canvas, child);
        if (mScrimOpacity > 0 && drawContent
                && mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
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
        Region region = new Region();
        region.union(new Rect(0, 0, child.getLeft(), getHeight()));
        region.union(new Rect(child.getRight(), 0, getRight(), getBottom()));
        region.union(new Rect(child.getLeft(), child.getBottom(), getRight(), getHeight()));
        canvas.clipRegion(region);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mScrimPaint);
        canvas.restoreToCount(restoreCount);
    }

    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = mTmpRect;
        child.getHitRect(childRect);

        if ((mEdgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,
                    childRect.left, childRect.bottom);
            mShadowLeft.draw(canvas);
        }

        if ((mEdgeFlag & EDGE_RIGHT) != 0) {
            mShadowRight.setBounds(childRect.right, childRect.top,
                    childRect.right + mShadowLeft.getIntrinsicWidth(), childRect.bottom);
            mShadowRight.draw(canvas);
        }

        if ((mEdgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom.setBounds(childRect.left, childRect.bottom, childRect.right,
                    childRect.bottom + mShadowBottom.getIntrinsicHeight());
            mShadowBottom.draw(canvas);
        }
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
        mScrimOpacity = 1 - mScrollPercent;
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {
        private int mTouchEdge;

        @Override
        public boolean tryCaptureView(View view, int i) {
            boolean ret = true;
            if (mDragHelper.isEdgeTouched(EDGE_LEFT, i)) {
                mTouchEdge = EDGE_LEFT;
            } else if (mDragHelper.isEdgeTouched(EDGE_RIGHT, i)) {
                mTouchEdge = EDGE_RIGHT;
            } else if (mDragHelper.isEdgeTouched(EDGE_BOTTOM, i)) {
                mTouchEdge = EDGE_BOTTOM;
            } else {
                ret = false;
            }
            return ret;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 1;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if ((mTouchEdge & (EDGE_LEFT | EDGE_RIGHT)) != 0) {
                mScrollPercent = Math.abs((float) left
                        / (mContentView.getWidth() + mShadowRight.getIntrinsicWidth()));
            } else if ((mTouchEdge & EDGE_BOTTOM) != 0) {
                mScrollPercent = Math.abs((float) top
                        / (mContentView.getHeight() + mShadowBottom.getIntrinsicHeight()));
            }
            mContentLeft = left;
            mContentTop = top;
            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final int childWidth = releasedChild.getWidth();
            final int childHeight = releasedChild.getHeight();

            int left = 0, top = 0;
            if ((mTouchEdge & EDGE_LEFT) != 0) {
                left = xvel > 0 || xvel == 0 && mScrollPercent > 0.5f ? childWidth
                        + mShadowLeft.getIntrinsicWidth() : 0;
            } else if ((mTouchEdge & EDGE_RIGHT) != 0) {
                left = xvel < 0 || xvel == 0 && mScrollPercent > 0.5f ? -(childWidth + mShadowLeft
                        .getIntrinsicWidth()) : 0;
            } else if ((mTouchEdge & EDGE_BOTTOM) != 0) {
                top = yvel < 0 || yvel == 0 && mScrollPercent > 0.5f ? -(childHeight + mShadowBottom
                        .getIntrinsicHeight()) : 0;
            }

            mDragHelper.settleCapturedViewAt(left, top);
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int ret = 0;
            if ((mTouchEdge & EDGE_LEFT) != 0) {
                ret = Math.min(child.getWidth(), Math.max(left, 0));
            } else if ((mTouchEdge & EDGE_RIGHT) != 0) {
                ret = Math.min(0, Math.max(left, -child.getWidth()));
            }
            return ret;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int ret = 0;
            if ((mTouchEdge & EDGE_BOTTOM) != 0) {
                ret = Math.min(0, Math.max(top, -child.getHeight()));
            }
            return ret;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChange(state, mScrollPercent);
            }
            if (state == ViewDragHelper.STATE_IDLE) {
                ViewCompat.setLayerType(SwipeBackLayout.this, ViewCompat.LAYER_TYPE_NONE, null);
                if (mScrollPercent == 1) {
                    mActivity.finish();
                }
            } else if (state == ViewDragHelper.STATE_SETTLING) {
                ViewCompat.setLayerType(SwipeBackLayout.this, ViewCompat.LAYER_TYPE_HARDWARE, null);
            }
        }
    }
}
