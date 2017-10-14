package com.stone.goodjob.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.stone.goodjob.R;

/**
 * 透明度版本
 * Created by jiayuanbin on 2017/10/13.
 */

public class GoodJobView extends View {

    private int goodNum;
    private int textMoveHeight;//文字上下移动的距离的上限
    private int duration = 200;

    private float textDy;//文字上下移动的动态值
    private float shiningAlpha;
    private float shiningScale;
    private float handScale = 1.0f;
    private float textAlpha;
    private float[] widths;

    private boolean isSelected;

    private Bitmap unselected;
    private Bitmap selected;
    private Bitmap shining;
    private Rect textBounds;

    private Paint bitmapPaint;
    private Paint textPaint;
    private Paint oldTextPaint;

    public GoodJobView(Context context) {
        this(context, null);
    }

    public GoodJobView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GoodJobView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GoodJobView);
        goodNum = a.getInt(R.styleable.GoodJobView_good_num, 2022);
        a.recycle();

        textBounds = new Rect();
        widths = new float[6];//默认支持到6位数

        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oldTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        //初始化文字相关配置
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(spToPx(14));
        oldTextPaint.setColor(Color.GRAY);
        oldTextPaint.setTextSize(spToPx(14));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Resources resources = getResources();
        unselected = BitmapFactory.decodeResource(resources, R.mipmap.ic_messages_like_unselected);
        selected = BitmapFactory.decodeResource(resources, R.mipmap.ic_messages_like_selected);
        shining = BitmapFactory.decodeResource(resources, R.mipmap.ic_messages_like_selected_shining);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unselected.recycle();
        selected.recycle();
        shining.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //高度默认限定为bitmap的高度加上上下margin各10dp
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(selected.getHeight() + dpToPx(20), MeasureSpec.EXACTLY);
        //宽度默认为bitmap的宽度加上左右margin各10dp，文字的宽度和文字右侧10dp
        String s = String.valueOf(goodNum);
        float textWidth = textPaint.measureText(s, 0, s.length());
        widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (selected.getWidth() + textWidth + dpToPx(30)), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerY = height / 2;
        Bitmap handBitmap = isSelected ? selected : unselected;
        int handBitmapWidth = handBitmap.getWidth();
        int handBitmapHeight = handBitmap.getHeight();

        //画小手
        int handTop = (height - handBitmapHeight) / 2;
        canvas.save();
        canvas.scale(handScale, handScale, handBitmapWidth / 2, centerY);
        canvas.drawBitmap(handBitmap, 0, handTop, bitmapPaint);
        canvas.restore();

        //画shining
        int shiningTop = handTop - shining.getHeight() + dpToPx(6);//手动加上6dp的margin
        bitmapPaint.setAlpha((int) (255 * shiningAlpha));
        canvas.save();
        canvas.scale(shiningScale, shiningScale, handBitmapWidth / 2, handTop);
        canvas.drawBitmap(shining, 0, shiningTop, bitmapPaint);
        canvas.restore();
        //恢复bitmapPaint透明度
        bitmapPaint.setAlpha(255);

        String value = String.valueOf(goodNum);
        String oldValue;
        if (isSelected) {
            oldValue = String.valueOf(goodNum - 1);
        } else {
            oldValue = String.valueOf(goodNum + 1);
        }
        int length = value.length();
        //获取文字绘制的坐标
        textPaint.getTextBounds(value, 0, length, textBounds);
        int textY = height / 2 - (textBounds.top + textBounds.bottom) / 2;
        int textX = handBitmapWidth + dpToPx(10);//手动加上10dp的margin
        if (length != oldValue.length() || textMoveHeight == 0) {
            //直接绘制文字 没找到即刻App里面对这种情况的处理效果
            canvas.drawText(value, textX, textY, textPaint);
            return;
        }
        //把文字拆解成一个一个的字符
        textPaint.getTextWidths(value, widths);
        char[] chars = value.toCharArray();
        char[] oldChars = oldValue.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (oldChars[i] == chars[i]) {
                textPaint.setAlpha(255);
                canvas.drawText(String.valueOf(chars[i]), textX, textY, textPaint);
            } else {
                if (isSelected) {
                    oldTextPaint.setAlpha((int) (255 * (1 - textAlpha)));
                    canvas.drawText(String.valueOf(oldChars[i]), textX, textY - textMoveHeight + textDy, oldTextPaint);
                    textPaint.setAlpha((int) (255 * textAlpha));
                    canvas.drawText(String.valueOf(chars[i]), textX, textY + textDy, textPaint);
                } else {
                    oldTextPaint.setAlpha((int) (255 * (1 - textAlpha)));
                    canvas.drawText(String.valueOf(oldChars[i]), textX, textY + textMoveHeight + textDy, oldTextPaint);
                    textPaint.setAlpha((int) (255 * textAlpha));
                    canvas.drawText(String.valueOf(chars[i]), textX, textY + textDy, textPaint);
                }
            }
            textX += widths[i];
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                toggle();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void toggle() {
        isSelected = !isSelected;
        if (isSelected) {
            ObjectAnimator handScaleAnim = ObjectAnimator.ofFloat(this, "handScale", 1f, 0.8f, 1f);
            handScaleAnim.setDuration(duration);

            ObjectAnimator shiningAlphaAnim = ObjectAnimator.ofFloat(this, "shiningAlpha", 0f, 1f);
            handScaleAnim.setDuration(duration);

            ObjectAnimator shiningScaleAnim = ObjectAnimator.ofFloat(this, "shiningScale", 0f, 1f);
            handScaleAnim.setDuration(duration);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(handScaleAnim, shiningAlphaAnim, shiningScaleAnim);
            set.start();
            setGoodNum(++goodNum);
        } else {
            ObjectAnimator handScaleAnim = ObjectAnimator.ofFloat(this, "handScale", 1f, 0.8f, 1f);
            handScaleAnim.setDuration(duration);
            handScaleAnim.start();

            setShiningAlpha(0);
            setGoodNum(--goodNum);
        }
    }

    @Keep
    public void setTextAlpha(float textAlpha) {
        this.textAlpha = textAlpha;
        invalidate();
    }

    @Keep
    public void setTextDy(float textDy) {
        this.textDy = textDy;
        invalidate();
    }

    public void setGoodNum(int goodNum) {
        float startY;
        textMoveHeight = dpToPx(20);
        if (isSelected) {
            startY = textMoveHeight;
        } else {
            startY = -textMoveHeight;
        }

        ObjectAnimator textInAlphaAnim = ObjectAnimator.ofFloat(this, "textAlpha", 0f, 1f);
        textInAlphaAnim.setDuration(duration);
        ObjectAnimator dyAnim = ObjectAnimator.ofFloat(this, "textDy", startY, 0);
        dyAnim.setDuration(duration);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(textInAlphaAnim, dyAnim);
        set.start();
    }

    @Keep
    public void setShiningAlpha(float shiningAlpha) {
        this.shiningAlpha = shiningAlpha;
        invalidate();
    }

    @Keep
    public void setShiningScale(float shiningScale) {
        this.shiningScale = shiningScale;
        invalidate();
    }

    @Keep
    public void setHandScale(float handScale) {
        this.handScale = handScale;
        invalidate();
    }

    private int dpToPx(float dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f * (dp >= 0 ? 1 : -1));
    }

    public int spToPx(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
