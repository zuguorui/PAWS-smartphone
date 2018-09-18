package bashima.cs.unc.seus.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import bashima.cs.unc.seus.constant.Constant;
import seus.bashima.cs.unc.seus.R;

public class MyPolarView extends View {

    int width, height, delta;
    float x, y;
    public int whichPie = 0;
    public int distance = 0;

    // Measurement Point
    public double xPoint, yPoint;

    // Scaling from world coordinates to graph coordinates
    double xScale, yScale;

    void init()
    {
        width = 7*75;
        height = 7*75;
        delta = 75;
        x = width/2;
        y = height/2;
        xPoint = width/2;
        yPoint = height/2;
        xScale = (width/2) / Constant.max_distance;
        yScale = (height/2) / Constant.max_distance;
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

        // Draw new point, just angle from the first localizesoundsource
        /*
        paint3.setColor(Color.RED);
        double angle = Math.tan(yPoint / xPoint) * 180 / Math.PI;
        if(xPoint <= 0 && yPoint < 0 && angle > 0) {
            angle = angle + 180;
        }
        else if(yPoint > 0 && xPoint <= 0 && angle < 0) {
            angle = angle + 180;
        }
        if(angle < 0) {
            angle = angle + 360;
        }

        // Calculate angle to draw: Android 0 degrees starts at 3 o'clock and draws clockwise
        double angle_to_draw = 360 - angle;

        Log.d("Angle", String.valueOf(angle));
        RectF rectf = new RectF(0, 0, width, height);
        canvas.drawArc(rectf, (int) (angle_to_draw - 22.5), 45, true, paint3);
        */

        // Draw new point, just angle from the second localizesoundsource (localizesoundsource2)
        /*paint3.setColor(Color.RED);

        Log.d("Angle", String.valueOf(yPoint));
        RectF rectf = new RectF(0, 0, width, height);

        // Calculate angle to draw: Android 0 degrees starts at 3 o'clock and draws clockwise
        double angle_to_draw = 360 - yPoint - 90;

        canvas.drawArc(rectf, (int) (angle_to_draw - 22.5), 45, true, paint3); // Subtract 90 because 0 degree was computed to be in front of user, but the graph's 0 degree is to the right
        */

        long timing = System.currentTimeMillis();

        //RectF rectf = new RectF(0, 0, width, height);
        //canvas.drawArc(rectf, (2 * 45 )-45, 45, true, paint3);
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

        // Draw new location based on localizesource2 and regression distance
        Log.d("Angle", String.valueOf(yPoint));
        RectF rectf = new RectF(0, 0, width, height);

        // Calculate angle to draw: Android 0 degrees starts at 3 o'clock and draws clockwise
        if(xPoint == Double.NEGATIVE_INFINITY || yPoint == Double.NEGATIVE_INFINITY) {
            return;
        }
        double angle_to_draw = 360 - yPoint - 90;
        //double distance = Math.min(Math.max(xPoint, 0), Constant.max_distance);
        //double xCoordinate = Math.min(Math.max((xScale * distance * Math.cos(Math.PI * angle_to_draw / 180)) + x, 1), width - 1);
        //double yCoordinate = Math.min(Math.max((yScale * distance * Math.sin(Math.PI * angle_to_draw / 180)) + y, 1), height - 1);

        paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawArc(rectf, (int) (angle_to_draw - 22.5), 45, true, paint);
        //canvas.drawCircle((float) xCoordinate, (float) yCoordinate, 20, paint);

        Log.d("UI Update Time", String.valueOf(System.currentTimeMillis() - timing));

        // Don't draw new point if infinity is given as location
        /*if(xPoint == Double.NEGATIVE_INFINITY || yPoint == Double.NEGATIVE_INFINITY) {
            return;
        }

        // Draw new point
        double distance = Math.sqrt((xPoint * xPoint) + (yPoint * yPoint));
        double xCoordinate = xPoint;
        double yCoordinate = yPoint;

        // If we are greater than the max distance, then map to edge of display
        if(distance > Constant.max_distance) {
            yCoordinate = Math.sqrt((Constant.max_distance * Constant.max_distance) / (1 + (yPoint * yPoint / (xPoint * xPoint))));
            xCoordinate = Math.sqrt((Constant.max_distance * Constant.max_distance) - (yCoordinate * yCoordinate));
            if(yPoint < 0) {
                yCoordinate = -yCoordinate;
            }

            if(xPoint < 0) {
                xCoordinate = -xCoordinate;
            }
        }

        // Scale real-world coordinate to graph coordinate
        xCoordinate = Math.max(Math.min(xCoordinate * xScale + x, width), 0);
        yCoordinate = Math.max(Math.min(yCoordinate * yScale + y, height - 1), 0);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawCircle((float) xCoordinate, (float) yCoordinate, 20, paint);*/
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