package com.orange.dimpleview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 描述：
 *
 * @author zj
 * @create 2020/10/22 19:58
 */
public class DimpleView extends View {
    public class Particle {
        public float x;
        public float y;
        public float radius = 2;
        public float speed;
        public int alpha = 255;
        public double angle;
        public int offset;//粒子行进的方向的位移

        public Particle() {
        }
    }

    private List<Particle> mParticleList;
    private int mParticleSize = 1000;
    private Paint mPaint;
    private float centerRadius;
    private float mMaxTransOffset;

    private Path mCirclePath;
    private PathMeasure mPathMeasure;
    private float[] mPos;
    private float[] mTan;
    private Random mRandom;
    private ValueAnimator mValueAnimator;

    public DimpleView(Context context) {
        this(context, null);
    }

    public DimpleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DimpleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mParticleList = new ArrayList<>();
        centerRadius = getScreenWidth(context) / 4f;
        mMaxTransOffset = getScreenWidth(context) / 4f;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);

        mCirclePath = new Path();
        mPathMeasure = new PathMeasure();
        mPos = new float[2];
        mTan = new float[2];
        mRandom = new Random();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final float centerX = w / 2f;
        final float centerY = h / 2f;
        mCirclePath.addCircle(centerX, centerY, centerRadius, Path.Direction.CW);
        mPathMeasure.setPath(mCirclePath, false);//添加path

        Particle particle;
        for (int i = 0; i < mParticleSize; i++) {
            particle = new Particle();
            initSpeed(particle);
            mPathMeasure.getPosTan((float) (i + 1) / mParticleSize * mPathMeasure.getLength(), mPos, mTan);
            particle.x = mPos[0] + mRandom.nextInt(10);
            particle.y = mPos[1] + mRandom.nextInt(10);
            particle.offset = mRandom.nextInt(200);
            //反余弦函数可以得到角度，是弧度值
            particle.angle = Math.acos((double) (mPos[0] - centerX) / centerRadius);
            Log.d("DimpleView", "angle = " + particle.angle);
            mParticleList.add(particle);
        }
        mValueAnimator = ValueAnimator.ofFloat(0, 1);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (Particle particle : mParticleList) {
                    if (particle.offset > mMaxTransOffset) {
                        particle.offset = 0;
                        initSpeed(particle);
                    }
                    particle.x = (float) (centerX + (centerRadius + particle.offset) * Math.cos(particle.angle));
                    if (particle.y > centerY) {
                        //因为弧度值是这样的比如10个点 弧度值分别为0.6 1.3 1.9 2.5 NaN 2.5 1.9 1.3 0.6 0 这样是没办法区分Y轴上半轴和下半轴的
                        particle.y = (float) (centerY + (centerRadius + particle.offset) * Math.sin(particle.angle));
                    } else {
                        particle.y = (float) (centerY - (centerRadius + particle.offset) * Math.sin(particle.angle));
                    }
                    particle.alpha = (int) ((1 - particle.offset / mMaxTransOffset) * 255);
                    particle.offset += particle.speed;
                }
                invalidate();
            }
        });
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.start();
    }

    private void initSpeed(Particle particle) {
        particle.speed = mRandom.nextInt(4) + 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Particle particle : mParticleList) {
            mPaint.setAlpha(particle.alpha);
            canvas.drawCircle(particle.x, particle.y, particle.radius, mPaint);
        }
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
    }
}
