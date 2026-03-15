package com.example.drawing_canvas_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Custom View class to handle drawing logic.
 */
public class DrawingView extends View {

    // Drawing path
    private Path drawPath;
    // Drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    // Initial color
    private int paintColor = Color.BLACK;
    // Canvas to draw on
    private Canvas drawCanvas;
    // Canvas bitmap
    private Bitmap canvasBitmap;
    // Brush size
    private float brushSize;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    // Initialize drawing properties
    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        
        brushSize = 20;

        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true); // Smooths the edges of the path
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    // Called when the view size changes (e.g., on startup)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        // Create a bitmap for the entire view area
        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas newCanvas = new Canvas(newBitmap);
        newCanvas.drawColor(Color.WHITE); // Initialize with white background

        if (canvasBitmap != null) {
            // Copy existing drawing if resizing
            newCanvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        }

        canvasBitmap = newBitmap;
        drawCanvas = newCanvas;
    }

    // Draw the view content
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the bitmap containing previous strokes
        if (canvasBitmap != null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        }
        // Draw the current path being drawn
        canvas.drawPath(drawPath, drawPaint);
    }

    // Handle touch events to draw
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Move to the touched position
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                // Draw line to the new position
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                // When finger is lifted, draw the path onto the bitmap canvas
                if (drawCanvas != null) {
                    drawCanvas.drawPath(drawPath, drawPaint);
                }
                drawPath.reset();
                break;
            default:
                return false;
        }

        // Force the view to redraw
        invalidate();
        return true;
    }

    // Update the brush color
    public void setColor(int newColor) {
        paintColor = newColor;
        drawPaint.setColor(paintColor);
    }

    // Update the brush size
    public void setBrushSize(float newSize) {
        brushSize = newSize;
        drawPaint.setStrokeWidth(brushSize);
    }

    // Clear the drawing area
    public void clearCanvas() {
        if (drawCanvas != null) {
            // Clear the bitmap by filling it with white
            drawCanvas.drawColor(Color.WHITE);
            invalidate();
        }
    }

    // Get the bitmap of the current drawing
    public Bitmap getBitmap() {
        return canvasBitmap;
    }
}
