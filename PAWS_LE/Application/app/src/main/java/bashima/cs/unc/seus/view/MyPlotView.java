package bashima.cs.unc.seus.view;

/**
 * Created by bashimaislam on 9/8/16.
 */
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.sql.Array;
import java.util.ArrayList;

import seus.bashima.cs.unc.seus.R;

public class MyPlotView extends View {

    int width, height, delta;
    int plotWidth;
    public ArrayList<Integer> colorArray;
    public ArrayList<Integer> distArray;
    public ArrayList<Double> yval;

    void init()
    {
        colorArray = new ArrayList<Integer>();
        distArray = new ArrayList<Integer>();
        width = 1000;
        height = 350;
        delta = 50;
        plotWidth = (width - delta * 4);
        yval = new ArrayList<Double>();
    }

    public void insertPoint(double v, int col, int dist) {
        yval.add(v);
        colorArray.add(col);
        distArray.add(dist);

        if(yval.size() >= (double)(plotWidth) / 10) {
            yval.remove(0);
            colorArray.remove(0);
            distArray.remove(0);
        }
    }

    public MyPlotView(Context context) {
        super(context);
        init();
    }

    public MyPlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyPlotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MyPlotView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Log.v("Draw ", "draw" );

        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
//        canvas.drawRect(0, 0, plotWidth, height, paint);

        int x, y;
        paint.setColor(Color.DKGRAY);
        for (x = 0; x <= plotWidth; x += delta) {
            canvas.drawLine(x, 0, x, height, paint);
        }
        for (y = 0; y <= height; y += delta) {
            canvas.drawLine(0, y, plotWidth, y, paint);
        }
        canvas.drawLine(0, height-1, plotWidth, height-1, paint);

        x = plotWidth + delta/2;
        y = delta/2;
        paint.setColor(Color.GREEN);
        canvas.drawCircle(x, y, 15, paint);
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(35);
        canvas.drawText("Honk", x + delta / 2, y + 10, paint);

        x = plotWidth + delta/2;
        y = delta;
        paint.setColor(Color.BLUE);
        canvas.drawCircle(x, y + delta / 2, 15, paint);
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(35);
        canvas.drawText("Nearby", x + delta / 2, y + 10 + delta / 2, paint);
        canvas.drawText("Car", x+delta/2, y+4+delta+delta/2, paint);


        int size = 80;
            int a = (int) Math.floor(yval.size()/size);
            float oldVal = 0;
            int j=0;
        Log.d("yval size:", String.valueOf(yval.size()));
        Log.d("color array size:", String.valueOf(colorArray.size()));
        Log.d("distance array size:", String.valueOf(distArray.size()));
        Log.d("a size:", String.valueOf(a));
        Log.d("Plot Width:, ", String.valueOf(plotWidth));
            for (int i = (a*size); i < yval.size(); i++) {
                if (colorArray.get(i) == 0) {

                        paint.setColor(Color.RED);

                }
                else if(colorArray.get(i) == 2)
                {
//                    if(distArray.get(i)==1)
//                    {
//                        paint.setColor(getResources().getColor(R.color.green_1));
//                    }
//                    else if(distArray.get(i)==2)
//                    {
//                        paint.setColor(getResources().getColor(R.color.green_2));
//                    }
//                    else if(distArray.get(i)==3)
//                    {
//                        paint.setColor(getResources().getColor(R.color.green_3));
//                    }
//                    else {
                        paint.setColor(Color.GREEN);
//                    }
                }
                else {
//                    if(distArray.get(i)==1)
//                    {
//                        paint.setColor(getResources().getColor(R.color.blue_1));
//                    }
//                    else if(distArray.get(i)==2)
//                    {
//                        paint.setColor(getResources().getColor(R.color.blue_2));
//                    }
//                    else if(distArray.get(i)==3)
//                    {
//                        paint.setColor(getResources().getColor(R.color.blue_3));
//                    }
//                    else {
                        paint.setColor(Color.BLUE);
//                    }
                }
                float val =  (int) (yval.get(i) * 60.00) + 30;


                canvas.drawCircle(j * 10, val, 5, paint);
                canvas.drawLine(j * 10, val, j*10, height, paint);
                if (j==0){}
                else {
                    canvas.drawLine(j * 10, val, (j - 1) * 10, oldVal, paint);
                }
                j++;
                oldVal = val;

            Log.d("Value",val+" "+j);
            }

//        Log.v("Draw 2: ", "" + System.currentTimeMillis());

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