package in.techgenium.canvasfun;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Narayan Acharya on 31/10/2015.
 */
public class Board extends View {

    private static Random random;
    private final List<Point> points;
    private static final String POINT_CIRCLE = "CIRCLE";
    private static final String POINT_SQUARE = "SQUARE";

    // Default value 1
    private int boardBackground;

    // Default value 6
    private int pointRadius;
    // Default value 0
    private int pointColour;
    // Default value 40
    private int pointCount;
    // Default value is "circle"
    private String pointType;

    // Default value 0
    private int lineColour;

    // Default value 2 for both
    private int speedXBarrier;
    private int speedYBarrier;

    // Default value 100
    private int distanceBarrier;

    //Default value false
    private boolean parallaxSameDirection;

    //Default value false
    private boolean touchToScatter;

    private Paint paint;

    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.points, 0, 0);
        try {
            // Fetch custom attributes from the XML file
            boardBackground = typedArray.getInteger(R.styleable.points_boardBackground, 1);
            pointRadius = typedArray.getInteger(R.styleable.points_pointRadius, 6);
            pointColour = typedArray.getInteger(R.styleable.points_pointColor, 0);
            pointCount = typedArray.getInteger(R.styleable.points_pointCount, 40);
            pointType = typedArray.getString(R.styleable.points_pointType);
            lineColour = typedArray.getInteger(R.styleable.points_lineColor, 0);
            speedXBarrier = typedArray.getInteger(R.styleable.points_speedBarrierX, 2);
            speedYBarrier = typedArray.getInteger(R.styleable.points_speedBarrierY, 2);
            distanceBarrier = typedArray.getInteger(R.styleable.points_distanceBarrier, 100);
            parallaxSameDirection = typedArray.getBoolean(R.styleable.points_speedBarrierY, false);
            touchToScatter = typedArray.getBoolean(R.styleable.points_touchToScatter, false);

            // Make a list to hold each point's configuration
            points = new ArrayList<>();
            for (int i = 0; i < pointCount; i++) {
                points.add(new Point());
            }

        } finally {
            typedArray.recycle();
        }

        random = new Random();
        paint = new Paint();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();

        // Random initialization of each point's configuration
        for (int i = 0; i < pointCount; i++) {
            points.get(i).setCenterX(random.nextInt(viewWidth));
            points.get(i).setCenterY(random.nextInt(viewHeight));
            int speedX = random.nextInt(2 * speedXBarrier) - speedXBarrier;
            int speedY = random.nextInt(2 * speedYBarrier) - speedYBarrier;
            points.get(i).setSpeedX(speedX);
            points.get(i).setSpeedY(speedY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw colour for entire background
        canvas.drawColor(boardBackground);

        // Drawing of points
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        for (int i = 0; i < pointCount; i++) {
            paint.setColor(pointColour);
            switch (pointType.toUpperCase()) {
                case POINT_CIRCLE:
                    canvas.drawCircle(points.get(i).getCenterX(), points.get(i).getCenterY(), pointRadius, paint);
                    break;
                case POINT_SQUARE:
                    canvas.drawRect(points.get(i).getCenterX() - (int) (pointRadius / Math.sqrt(2)),
                            points.get(i).getCenterY() - (int) (pointRadius / Math.sqrt(2)),
                            points.get(i).getCenterX() + (int) (pointRadius / Math.sqrt(2)),
                            points.get(i).getCenterY() + (int) (pointRadius / Math.sqrt(2)),
                            paint
                    );
                    break;
                default:
                    canvas.drawCircle(points.get(i).getCenterX(), points.get(i).getCenterY(), pointRadius, paint);
                    break;

            }


            int newCenterX = points.get(i).getCenterX() + points.get(i).getSpeedX();
            int newCenterY = points.get(i).getCenterY() + points.get(i).getSpeedY();
            if (newCenterX < 0) {
                points.get(i).setCenterX(getMeasuredWidth() + newCenterX);
            } else if (newCenterX > getMeasuredWidth()) {
                points.get(i).setCenterX(getMeasuredWidth() - newCenterX);
            } else {
                points.get(i).setCenterX(newCenterX);
            }
            if (newCenterY < 0) {
                points.get(i).setCenterY(getMeasuredHeight() + newCenterY);
            } else if (newCenterY > getMeasuredHeight()) {
                points.get(i).setCenterY(getMeasuredHeight() - newCenterY);
            } else {
                points.get(i).setCenterY(newCenterY);
            }

            // Drawing of lines between the points provided they lie in the barrier distance
            for (int j = i + 1; j < pointCount; j++) {
                if (distance(points.get(i), points.get(j)) < distanceBarrier) {
                    paint.setColor(lineColour);
                    canvas.drawLine(points.get(i).getCenterX(), points.get(i).getCenterY(),
                            points.get(j).getCenterX(), points.get(j).getCenterY(),
                            paint);
                }
            }
        }
        invalidate();
    }

    /**
     * Calculates Euclidean distance between two points.
     *
     * @param p1 First point
     * @param p2 Second point
     * @return Distance between the two points
     */
    private int distance(Point p1, Point p2) {
        return (int) Math.sqrt(Math.pow(p1.getCenterX() - p2.getCenterX(), 2) + Math.pow(p1.getCenterY() - p2.getCenterY(), 2));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int startX = 0;
        int startY = 0;
        int endX = 0;
        int endY = 0;

        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                startX = (int) event.getX();
                startY = (int) event.getY();
                return true;
            case (MotionEvent.ACTION_MOVE):
                endX = (int) event.getX();
                endY = (int) event.getY();
                parallax(startX, startY, endX, endY);
                return true;
            case (MotionEvent.ACTION_UP):
                if (touchToScatter) {
                    touchToScatter();
                }
                return true;
            case (MotionEvent.ACTION_CANCEL):
                return false;
            case (MotionEvent.ACTION_OUTSIDE):
                return false;
            default:
                return false;
        }
    }


    /**
     * Handle parallax like movement of points on the canvas
     *
     * @param startX X co-ordinate of start point of touch
     * @param startY Y co-ordinate of start point of touch
     * @param endX   X co-ordinate of end point of touch
     * @param endY   X co-ordinate of end point of touch
     */
    public void parallax(int startX, int startY, int endX, int endY) {
        if (parallaxSameDirection) {
            for (int i = 0; i < pointCount; i++) {
                points.get(i).setCenterX(points.get(i).getCenterX() + (endX - startX) / 40);
                points.get(i).setCenterY(points.get(i).getCenterY() + (endY - startY) / 40);
            }
        } else {
            for (int i = 0; i < pointCount; i++) {
                points.get(i).setCenterX(points.get(i).getCenterX() + (startX - endX) / 40);
                points.get(i).setCenterY(points.get(i).getCenterY() + (startY - endY) / 40);
            }
        }
    }

    public void touchToScatter() {
        // Random initialization of each point's configuration
        for (int i = 0; i < pointCount; i++) {
            int speedX = random.nextInt(2 * speedXBarrier) - speedXBarrier;
            int speedY = random.nextInt(2 * speedYBarrier) - speedYBarrier;
            points.get(i).setSpeedX(speedX);
            points.get(i).setSpeedY(speedY);
        }
    }

    public int getBoardBackground() {
        return boardBackground;
    }

    public void setBoardBackground(int boardBackground) {
        this.boardBackground = boardBackground;
    }

    public int getPointRadius() {
        return pointRadius;
    }

    public void setPointRadius(int pointRadius) {
        this.pointRadius = pointRadius;
    }

    public int getPointColour() {
        return pointColour;
    }

    public void setPointColour(int pointColour) {
        this.pointColour = pointColour;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public int getLineColour() {
        return lineColour;
    }

    public void setLineColour(int lineColour) {
        this.lineColour = lineColour;
    }

    public int getSpeedXBarrier() {
        return speedXBarrier;
    }

    public void setSpeedXBarrier(int speedXBarrier) {
        this.speedXBarrier = speedXBarrier;
    }

    public int getSpeedYBarrier() {
        return speedYBarrier;
    }

    public void setSpeedYBarrier(int speedYBarrier) {
        this.speedYBarrier = speedYBarrier;
    }

    public int getDistanceBarrier() {
        return distanceBarrier;
    }

    public void setDistanceBarrier(int distanceBarrier) {
        this.distanceBarrier = distanceBarrier;
    }

    public boolean isParallaxSameDirection() {
        return parallaxSameDirection;
    }

    public void setParallaxSameDirection(boolean parallaxSameDirection) {
        this.parallaxSameDirection = parallaxSameDirection;
    }
}