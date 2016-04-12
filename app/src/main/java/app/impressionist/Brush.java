package app.impressionist;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Brush {
    public static int DEFAULT_BRUSH_RADIUS = 20;
    public static int DEFAULT_BRUSH_TYPE = 1;
    public static int DEFAULT_BRUSH_OPACITY = 50;
    public static int BRUSH_TYPE_SQUARE = 1;
    public static int BRUSH_TYPE_TRIANGLE = 2;
    public static int BRUSH_TYPE_CIRCLE = 3;
    public static int BRUSH_TYPE_SPLASH = 4;
    public static int BRUSH_TYPE_STAR = 5;
    public int style;
    public Paint paint;
    public float radius;
    public int color = -65534;
    public int opacity;

    public Brush() {
        style = DEFAULT_BRUSH_TYPE;
        radius = DEFAULT_BRUSH_RADIUS;
        opacity = DEFAULT_BRUSH_OPACITY;
        paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(opacity);
    }

    public Brush(int brushStyle, float brushRadius, int brushOpacity) {
        style = brushStyle;
        opacity = brushOpacity;
        radius = brushRadius;
    }

    public void updatePaint() {
        paint.setColor(color);
        paint.setAlpha(opacity);
    }


    public void updatePaint(int color, int opacity) {
        paint.setColor(color);
        paint.setAlpha(opacity);
    }
    public void draw(Canvas canvasToDraw, float x, float y) {
        float i, j;
        float decrement = 2;

        if (style == BRUSH_TYPE_SQUARE) {
            canvasToDraw.drawRect(x - radius, y - radius, x + radius, y + radius, paint);

        } else if (style == BRUSH_TYPE_CIRCLE) {
            canvasToDraw.drawCircle(x, y, radius, paint);

        } else if (style == BRUSH_TYPE_TRIANGLE) {
            for (i = y - radius; i < y + radius; i++) {

                for (j = x - radius; j < x - radius + decrement; j++) {
                    canvasToDraw.drawPoint(j, i, paint);
                }

                decrement++;
            }

        } else if (style == BRUSH_TYPE_SPLASH) {
            float s_radius = 0.75f * radius;

            canvasToDraw.drawCircle(x, y, s_radius, paint);
            canvasToDraw.drawCircle(x + 2 * s_radius, y, s_radius * 0.8f, paint);
            canvasToDraw.drawCircle(x, y + s_radius * 0.7f + s_radius, s_radius * 0.7f, paint);
            canvasToDraw.drawCircle(x - s_radius - s_radius * 0.5f, y, s_radius * 0.5f, paint);
            canvasToDraw.drawCircle(x + 0.5f * s_radius, y - 0.6f * s_radius - s_radius, 0.6f * radius, paint);
            canvasToDraw.drawCircle(x + s_radius + 0.25f * s_radius, y + s_radius * 0.5f + s_radius, s_radius * 0.25f, paint);
            canvasToDraw.drawCircle(x - s_radius - 0.25f * s_radius, y + s_radius * 0.5f + s_radius, s_radius * 0.35f, paint);

        } else if (style == BRUSH_TYPE_STAR) {
            float s_radius = 0.5f * radius;

            canvasToDraw.drawCircle(x, y, s_radius, paint);
            canvasToDraw.drawCircle(x + s_radius, y, s_radius, paint);
            canvasToDraw.drawCircle(x + s_radius + s_radius * 2, y, s_radius, paint);
            canvasToDraw.drawCircle(x, y + s_radius, s_radius, paint);
            canvasToDraw.drawCircle(x, y + s_radius + s_radius * 2, s_radius, paint);
            canvasToDraw.drawCircle(x, y - s_radius, s_radius, paint);
            canvasToDraw.drawCircle(x, y - s_radius - s_radius * 2, s_radius, paint);
            canvasToDraw.drawCircle(x - s_radius, y, s_radius, paint);
            canvasToDraw.drawCircle(x - s_radius - s_radius * 2, y, s_radius, paint);
        }
    }

}
