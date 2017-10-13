package com.stone.goodjob;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
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

/**
 * Created by jiayuanbin on 2017/10/13.
 */

public class GoodJobView extends View {

    private Paint paint;
    private Bitmap unselected;
    private Bitmap selected;
    private Bitmap shining;

    private int goodNum = 4529;
    private Rect textBounds;

    private boolean isSelected;
    private float shiningAlpha;
    private float shiningScale;
    private float handScale = 1.0f;
    private Paint textPaint;
    private Paint oldTextPaint;
    private float textInAlpha;
    private float textOutAlpha;
    private float dy;
    private int textHeight;
    private int duration = 200;

    public GoodJobView(Context context) {
        this(context, null);
    }

    public GoodJobView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GoodJobView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources resources = getResources();
        unselected = BitmapFactory.decodeResource(resources, R.mipmap.ic_messages_like_unselected);
        selected = BitmapFactory.decodeResource(resources, R.mipmap.ic_messages_like_selected);
        shining = BitmapFactory.decodeResource(resources, R.mipmap.ic_messages_like_selected_shining);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oldTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textBounds = new Rect();
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
        canvas.drawBitmap(handBitmap, 0, handTop, paint);
        canvas.restore();

        //画shining
        int shiningTop = handTop - shining.getHeight() + dpToPx(6);//手动矫正一下位置
        paint.setAlpha((int) (255 * shiningAlpha));
        canvas.save();
        canvas.scale(shiningScale, shiningScale, handBitmapWidth / 2, handTop);
        canvas.drawBitmap(shining, 0, shiningTop, paint);
        canvas.restore();
        //恢复paint透明度
        paint.setAlpha(255);

        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(spToPx(14));
        oldTextPaint.setColor(Color.GRAY);
        oldTextPaint.setTextSize(spToPx(14));

        int textX = handBitmapWidth + dpToPx(10);
        String value = String.valueOf(goodNum);
        String oldValue;
        if (isSelected) {
            oldValue = String.valueOf(goodNum - 1);
        } else {
            oldValue = String.valueOf(goodNum + 1);
        }

        int length = value.length();
        textPaint.getTextBounds(value, 0, length, textBounds);
        int minusHeight = (textBounds.top + textBounds.bottom) / 2;
        //canvas.drawText(value, x, height / 2 - minusHeight, paint);
        int textY = height / 2 - minusHeight;
        //要把文字拆解成一个一个的字符
        //假装oldValue.length = value.length
        //paint.getRunAdvance(value, 0, length, 0, length, false, length - 1)
        float textWidth = textPaint.measureText(value, 0, length);
        textHeight = textBounds.height();
        float[] widths = new float[length];
        textPaint.getTextWidths(value, widths);
        char[] chars = value.toCharArray();
        char[] oldChars = oldValue.toCharArray();
        for (int i = 0; i < widths.length; i++) {
            textX += widths[i];

            if (oldChars[i] == chars[i]) {
                //没有动效 直接画当前的chars
                canvas.drawText(String.valueOf(chars[i]), textX, textY, textPaint);
            } else {
                //有动效
                if (isSelected) {
                    //原有文字向上
                    canvas.save();
                    //现在文字渐显
                    textPaint.setAlpha((int) (255 * textInAlpha));
                    //现在文字向上

                    canvas.translate(0, dy);
                    canvas.drawText(String.valueOf(chars[i]), textX, textY, textPaint);

                    //原有文字渐隐
                    oldTextPaint.setAlpha((int) (255 * textOutAlpha));
                    canvas.drawText(String.valueOf(oldChars[i]), textX, textY, oldTextPaint);
                    canvas.restore();
                } else {
                    //原有文字向下
                    canvas.save();
                    canvas.restore();
                }
            }
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
            //变为选择状态
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
            //变为不选状态
            ObjectAnimator handScaleAnim = ObjectAnimator.ofFloat(this, "handScale", 1f, 0.8f, 1f);
            handScaleAnim.setDuration(duration);
            handScaleAnim.start();

            setShiningAlpha(0);
            setGoodNum(--goodNum);
        }
    }

    public void setTextInAlpha(float textInAlpha) {
        this.textInAlpha = textInAlpha;
        invalidate();
    }

    public void setTextOutAlpha(float textOutAlpha) {
        this.textOutAlpha = textOutAlpha;
        invalidate();
    }

    public void setDy(float dy) {
        this.dy = dy;
        invalidate();
    }

    @Keep
    public void setGoodNum(int goodNum) {
        this.goodNum = goodNum;

        ObjectAnimator textOutAlphaAnim = ObjectAnimator.ofFloat(this, "textOutAlpha", 1f, 0f);
        textOutAlphaAnim.setDuration(duration);
        ObjectAnimator textInAlphaAnim = ObjectAnimator.ofFloat(this, "textInAlpha", 0f, 1f);
        textInAlphaAnim.setDuration(duration);
        ObjectAnimator dyAnim = ObjectAnimator.ofFloat(this, "dy", -textHeight, 0);
        dyAnim.setDuration(duration);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(textOutAlphaAnim, textInAlphaAnim, dyAnim);
        set.start();

        invalidate();
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
