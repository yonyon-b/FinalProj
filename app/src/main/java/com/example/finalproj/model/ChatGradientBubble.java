package com.example.finalproj.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ChatGradientBubble extends FrameLayout {
    private Paint paint;
    private LinearGradient gradient;
    private Matrix matrix;
    private RectF rectF;
    private float cornerRadius;
    private int[] location = new int[2];

    public ChatGradientBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        matrix = new Matrix();
        rectF = new RectF();
        cornerRadius = 15 * context.getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rectF.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getLocationInWindow(location);
        int windowY = location[1];

        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        if (gradient == null) {

            int colorStart = Color.parseColor("#A334FA");
            int colorMid = Color.parseColor("#F91755");
            int colorEnd = Color.parseColor("#FCA048");

            gradient = new LinearGradient(0, 0, 0, screenHeight,
                    new int[]{colorStart, colorMid, colorEnd},
                    null, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
        }

        matrix.setTranslate(0, -windowY);
        gradient.setLocalMatrix(matrix);

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        super.onDraw(canvas);
    }
}