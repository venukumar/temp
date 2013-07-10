package com.vendsy.bartsy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class ResponsiveScrollView extends ScrollView {
	
	public static final int PAGE_SIZE = 15; // Display list of items for single sys call

    public interface OnEndScrollListener {
        public void onEndScroll();
    }

    private boolean inProgress = false;
    private boolean noMoreItems = false; // If we get all items from the server then it will be set to true
    
    private OnEndScrollListener mOnEndScrollListener;
    
	private Paint paint;

    public ResponsiveScrollView(Context context) {
        this(context, null, 0);
        paint = getNewPaintInstance();
    }

    public ResponsiveScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResponsiveScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        if (!inProgress && !noMoreItems) {
        	 View view = (View) getChildAt(getChildCount()-1);
             int diff = (view.getBottom()-(getHeight()+getScrollY()));// Calculate the scrolldiff
             if( diff == 0 ){  // if diff is zero, then the bottom has been reached
                 Log.d("ResponsiveScrollView", "MyScrollView: Bottom has been reached" );
                 if (mOnEndScrollListener != null) {
                	 inProgress = true;
                     mOnEndScrollListener.onEndScroll();
                 }
             }
             
        }
    }
    
    private Paint getNewPaintInstance() {
		Paint paint = new Paint();

		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(0xff449fc1);
		paint.setStrokeWidth(4);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);

		return paint;
	}
    
    @Override
    protected void onDraw(Canvas canvas) {
//    	if(inProgress){
//    		canvas.drawText("Loading..",0, getHeight()-30, paint);
//    	}
    	super.onDraw(canvas);
    }

    public OnEndScrollListener getOnEndScrollListener() {
        return mOnEndScrollListener;
    }

    public void setOnEndScrollListener(OnEndScrollListener mOnEndScrollListener) {
        this.mOnEndScrollListener = mOnEndScrollListener;
    }

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}

	public boolean isNoMoreItems() {
		return noMoreItems;
	}

	public void setNoMoreItems(boolean noMoreItems) {
		this.noMoreItems = noMoreItems;
	}
    
}