package com.helloworld.app.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WDrawerLayout extends FrameLayout {
    private View leftMenu;
    private View mainView;
    private FrameLayout mainMask;

    private float percent;

    private int mLastX;
    private int mLastY;
    private GestureDetector gestureDetector;
    private VelocityTracker mTracker;

    private boolean drawerOpened;


    public WDrawerLayout(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public WDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);

    }

    private void initView(Context context, AttributeSet attrs) {
        mTracker = VelocityTracker.obtain();
        percent = 0.75f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //1 决定自己的宽高
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);

        //2 决定 mainView 宽高
        mainView.measure(widthMeasureSpec, heightMeasureSpec);
        mainMask.measure(widthMeasureSpec, heightMeasureSpec);

        //3 决定leftMenu宽度
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int realWidth = judgeLeftMenuWidth(width);
        int widthSpec = MeasureSpec.makeMeasureSpec(realWidth, MeasureSpec.EXACTLY);
        leftMenu.measure(widthSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //1 摆放 mainView 的位置
        mainView.layout(left, top, right, bottom);
        mainMask.layout(left, top, right, bottom);

        //2 摆放leftMenu的位置
        int leftWidth = leftMenu.getMeasuredWidth();
        leftMenu.layout(left - leftWidth, top, left, bottom);
    }

    private int judgeLeftMenuWidth(int width) {
        if (percent > 0 && percent < 1) {
            return (int) (percent * width);
        }

        return width;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) ev.getX();
                mLastY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs((int) ev.getX() - mLastX);
                int dy = Math.abs((int) ev.getY() - mLastY);
                if (dx > dy) {
                    if( drawerOpened || Math.abs(ev.getRawX()) < 150){
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mTracker.addMovement(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                super.onTouchEvent(ev);
                mLastX = (int) ev.getX();
                mLastY = (int) ev.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs((int) ev.getX() - mLastX);
                int dy = Math.abs((int) ev.getY() - mLastY);
                if (dx > dy) {//水平
                    int left, right;
                    if (ev.getX() < mLastX) { //向左
                        left = leftMenu.getLeft() - dx;
                        if (left < -leftMenu.getWidth()) {
                            left = -leftMenu.getWidth();
                        }
                    } else {//向右
                        left = leftMenu.getLeft() + dx;
                        if (left > 0) {
                            left = 0;
                        }
                    }
                    right = left + leftMenu.getWidth();
                    leftMenu.layout(left, leftMenu.getTop(), right, leftMenu.getBottom());
                    float alpha = (float) leftMenu.getRight() / (float) leftMenu.getWidth();
                    mainMask.setAlpha(Math.abs(alpha));
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTracker.computeCurrentVelocity(1000, ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity());
                int initialVelocity = (int) mTracker.getXVelocity();
                //Log.i("zh88", initialVelocity > 0 ? "向右滑动，速度是：" + Math.abs(initialVelocity) : "向左滑动，速度是：" + Math.abs(initialVelocity));
                boolean leftDirection = initialVelocity < 0 ? true : false;
                boolean fast = Math.abs(initialVelocity) > 2000 ? true : false;
                //Log.e("zh88","left=" + leftDirection + " fast=" +fast);

                springBack(leftDirection,fast);
                mLastX = (int) ev.getX();
                mLastY = (int) ev.getY();
        }
        return super.onTouchEvent(ev);
    }

    //回弹
    private void springBack(boolean leftDirection , boolean fast) {
        int right = leftMenu.getRight();
        int width = leftMenu.getWidth();
        ValueAnimator valueAnimator;

        int start,end;
        if( leftDirection ){//左
            if(width - right > width / 2 || fast){
                start = right;
                end = 0;
            }else {
                start = right;
                end = width;
            }
        }else {//右
            if(right >= width / 2 || fast){
                start = right;
                end = width;
            }else {
                start = right;
                end = 0;
            }
        }

        valueAnimator = ValueAnimator.ofInt(start, end).setDuration(170);
        valueAnimator.addUpdateListener(animation -> {
            int curRight = (int) animation.getAnimatedValue();
            int left = curRight - width;
            int top = leftMenu.getTop();
            int bottom = leftMenu.getBottom();
            leftMenu.layout(left, top, curRight, bottom);
            float alpha = (float) leftMenu.getRight() / (float) leftMenu.getWidth();
            mainMask.setAlpha(Math.abs(alpha));
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                drawerOpened = leftMenu.getRight() > 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    public void openDrawerLayout() {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, leftMenu.getWidth()).setDuration(200);
        valueAnimator.addUpdateListener(animation -> {
            int right = (int) animation.getAnimatedValue();
            int left = right - leftMenu.getWidth();
            leftMenu.layout(left, leftMenu.getTop(), right, leftMenu.getBottom());
            float alpha = (float) leftMenu.getRight() / (float) leftMenu.getWidth();
            mainMask.setAlpha(Math.abs(alpha));
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                drawerOpened = leftMenu.getRight() > 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    public void closeDrawerLayout() {
        Log.e("zh11", "closeDrawerLayout");
        ValueAnimator valueAnimator = ValueAnimator.ofInt(leftMenu.getWidth(), 0).setDuration(200);
        valueAnimator.addUpdateListener(animation -> {
            int right = (int) animation.getAnimatedValue();
            int left = right - leftMenu.getWidth();
            leftMenu.layout(left, leftMenu.getTop(), right, leftMenu.getBottom());
            float alpha = (float) leftMenu.getRight() / (float) leftMenu.getWidth();
            mainMask.setAlpha(Math.abs(alpha));
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                drawerOpened = leftMenu.getRight() > 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("WDrawerLayout有且只能有2个子view");
        }

        leftMenu = getChildAt(0);
        mainView = getChildAt(1);
        removeAllViews();

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (leftMenu.getRight() == leftMenu.getWidth() && e.getX() > leftMenu.getWidth()) {
                    closeDrawerLayout();
                }
                return super.onSingleTapUp(e);
            }
        });

        mainMask = new FrameLayout(getContext());
        mainMask.setBackgroundColor(Color.parseColor("#814f4f4f"));
        mainMask.setAlpha(0);
        mainMask.setOnTouchListener((v, event) -> {
            if (leftMenu.getRight() == 0) {
                return false;
            }
            return gestureDetector.onTouchEvent(event);
        });

        addView(mainView);
        addView(mainMask);
        addView(leftMenu);
    }
}
