/*
 * MIT License
 *
 * Copyright (c) 2018, Stephen Xia, Columbia Intelligent and Connected Systems Lab (ICSL), Columbia University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package bashima.cs.unc.seus.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import seus.bashima.cs.unc.seus.R;

public class MyPolarView extends View {

    int width, height, delta;
    float x, y;
    public int whichPie = 0;
    public int distance = 0;

    void init()
    {
        width = 7*75;
        height = 7*75;
        delta = 75;
        x = width/2;
        y = height/2;
    }

    public MyPolarView(Context context) {
        super(context);
        init();
    }

    public MyPolarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyPolarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MyPolarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Draw the pie: 0, 1, ... 7

        Paint paint3 = new Paint();
        if(0<whichPie && whichPie<9)
        {

            if(distance==1)
            {
                paint3.setColor(getResources().getColor(R.color.blue_1));
            }
            else if(distance==2)
            {
                paint3.setColor(getResources().getColor(R.color.blue_2));
            }
            else if(distance==3)
            {
                paint3.setColor(getResources().getColor(R.color.blue_3));
            }
            else {
                paint3.setColor(Color.BLUE);
            }
        }


        else if(8<whichPie && whichPie<17) {
            if(distance==1)
            {
                paint3.setColor(getResources().getColor(R.color.green_1));
            }
            else if(distance==2)
            {
                paint3.setColor(getResources().getColor(R.color.green_2));
            }
            else if(distance==3)
            {
                paint3.setColor(getResources().getColor(R.color.green_3));
            }
            else {
            paint3.setColor(Color.GREEN);
        }
        }
        else {
            paint3.setColor(Color.TRANSPARENT);
        }
        RectF rectf = new RectF(0, 0, width, height);
        canvas.drawArc(rectf, (whichPie * 45 )-45, 45, true, paint3);
//        canvas.drawArc(rectf, (whichPie * 45 ), 90, true, paint3);
        //Draw rest of the static stuffs.
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        for(int r = 1; r <= 3; r++) {
            canvas.drawCircle(width/2, height/2, r * delta, paint);
        }
        paint.setColor(Color.DKGRAY);
        canvas.drawLine(0, height / 2, width, height / 2, paint);
        canvas.drawLine(width / 2, 0, width / 2, height, paint);

        Paint paint2 = new Paint();
        paint2.setColor(Color.rgb(0, 0, 0x80));
        canvas.drawCircle(x, y, 12, paint2);



    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = width;
        int desiredHeight = height;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

}