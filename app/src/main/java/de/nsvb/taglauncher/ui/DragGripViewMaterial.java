/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.nsvb.taglauncher.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import de.nsvb.taglauncher.R;
import de.nsvb.taglauncher.util.Log;

public class DragGripViewMaterial extends View {
    private static final int[] ATTRS = new int[]{
            android.R.attr.gravity,
            android.R.attr.color,
    };

    private int mGravity = Gravity.START;
    private int mColor = 0x8a000000;

    private Paint mGripPaint;

    private float mGripHeight;

    private int mWidth;
    private int mHeight;

    public DragGripViewMaterial(Context context) {
        this(context, null, 0);
    }

    public DragGripViewMaterial(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragGripViewMaterial(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
        mGravity = a.getInteger(0, mGravity);
        mColor = a.getColor(1, mColor);
        a.recycle();

        final Resources res = getResources();
        mGripHeight = res.getDimensionPixelSize(R.dimen.drag_grip_height);

        mGripPaint = new Paint();
        mGripPaint.setColor(mColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float drawWidth = (mWidth - getPaddingLeft() - getPaddingRight());
        float drawLeft;

        switch (Gravity.getAbsoluteGravity(mGravity, LAYOUT_DIRECTION_RTL)
                & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                drawLeft = getPaddingLeft()
                        + ((mWidth - getPaddingLeft() - getPaddingRight()) - drawWidth) / 2;
                break;
            case Gravity.RIGHT:
                drawLeft = getWidth() - getPaddingRight() - drawWidth;
                break;
            default:
                drawLeft = getPaddingLeft();
        }

        int vertRidges = 3;
        float drawHeight = (mHeight - getPaddingTop() - getPaddingBottom());
        float drawTop = getPaddingTop()
                + ((mHeight - getPaddingTop() - getPaddingBottom()) - drawHeight) / 2;

        float intermediateHeight = (drawHeight - vertRidges * mGripHeight) / 2;

        Log.d("drawHeight=" + drawHeight);
        Log.d("intermediateHeight=" + intermediateHeight);
        Log.d("mGripHeight=" + mGripHeight);

        for (int y = 0; y < vertRidges; y++) {
                canvas.drawRect(
                        drawLeft,
                        drawTop + y * (intermediateHeight + mGripHeight),
                        drawLeft + drawWidth,
                        drawTop + y * (intermediateHeight + mGripHeight) + mGripHeight,
                        mGripPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;
    }
}
