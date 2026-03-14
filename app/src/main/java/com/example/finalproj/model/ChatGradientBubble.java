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
        // 1. Get the view's Y position on the screen
        getLocationInWindow(location);
        int windowY = location[1];

        // 2. Get screen height to define the full span of the gradient
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // 3. Initialize the gradient once
        if (gradient == null) {

            int colorStart = Color.parseColor("#A334FA");
            int colorMid = Color.parseColor("#F91755");
            int colorEnd = Color.parseColor("#FCA048");

            gradient = new LinearGradient(0, 0, 0, screenHeight,
                    new int[]{colorStart, colorMid, colorEnd},
                    null, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
        }

        // 4. Shift the gradient inversely to the view's Y position to fix it to the screen
        matrix.setTranslate(0, -windowY);
        gradient.setLocalMatrix(matrix);

        // 5. Draw the rounded background
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        // Draw the child views (your transparent TextView) on top
        super.onDraw(canvas);
    }
}