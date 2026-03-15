package com.example.cellular_frequency_reuse_game_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends View {

    private Paint paint;
    private float radius = 60f;
    private List<HexCell> cells = new ArrayList<>();
    private HexCell referenceCell;
    private int score = 0;
    private OnScoreChangeListener scoreChangeListener;

    public interface OnScoreChangeListener {
        void onScoreChanged(int score);
    }

    public void setOnScoreChangeListener(OnScoreChangeListener listener) {
        this.scoreChangeListener = listener;
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        initGame();
    }

    private void initGame() {
        cells.clear();
        score = 0;
        if (scoreChangeListener != null) scoreChangeListener.onScoreChanged(score);
        
        // Generate a grid of hexagons
        int rows = 10;
        int cols = 8;
        float width = radius * (float) Math.sqrt(3);
        float height = radius * 2;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float x = c * width;
                if (r % 2 == 1) {
                    x += width / 2;
                }
                float y = r * height * 0.75f;
                cells.add(new HexCell(x + 100, y + 100, r, c));
            }
        }

        // Select a random reference cell
        Random rand = new Random();
        if (!cells.isEmpty()) {
            referenceCell = cells.get(rand.nextInt(cells.size()));
            referenceCell.type = HexCell.Type.REFERENCE;
            
            // Determine co-channel cells based on N=7 (i=2, j=1)
            calculateCoChannelCells(2, 1);
        }
        
        invalidate();
    }

    private void calculateCoChannelCells(int i, int j) {
        for (HexCell cell : cells) {
            if (cell == referenceCell) continue;
            if (isCoChannel(referenceCell.row, referenceCell.col, cell.row, cell.col, i, j)) {
                cell.isCoChannelTarget = true;
            }
        }
    }

    private boolean isCoChannel(int r1, int c1, int r2, int c2, int i, int j) {
        // Axial coordinates conversion
        int q1 = c1 - (r1 - (r1 & 1)) / 2;
        int r_coord1 = r1;
        int q2 = c2 - (r2 - (r2 & 1)) / 2;
        int r_coord2 = r2;
        
        int dq = q2 - q1;
        int dr = r_coord2 - r_coord1;
        
        // Co-channel vectors for N=7 (i=2, j=1)
        int[][] targets = {
            {2, 1}, {1, 3}, {-1, 2}, {-2, -1}, {-1, -3}, {1, -2}
        };
        
        for (int[] target : targets) {
            if (dq == target[0] && dr == target[1]) return true;
        }
        return false;
    }

    public void resetGame() {
        initGame();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (HexCell cell : cells) {
            drawHexagon(canvas, cell);
        }
    }

    private void drawHexagon(Canvas canvas, HexCell cell) {
        Path path = new Path();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 30);
            float x = (float) (cell.centerX + radius * Math.cos(angle));
            float y = (float) (cell.centerY + radius * Math.sin(angle));
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();

        if (cell.type == HexCell.Type.REFERENCE) {
            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(path, paint);
        } else if (cell.status == HexCell.Status.CORRECT) {
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(path, paint);
        } else if (cell.status == HexCell.Status.WRONG) {
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(path, paint);
        } else {
            paint.setColor(Color.LTGRAY);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, paint);
        }

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
            float x = event.getX();
            float y = event.getY();
            for (HexCell cell : cells) {
                if (isInside(x, y, cell)) {
                    if (cell.type == HexCell.Type.REFERENCE) return true;
                    if (cell.status != HexCell.Status.NONE) return true;

                    if (cell.isCoChannelTarget) {
                        cell.status = HexCell.Status.CORRECT;
                        score += 10;
                    } else {
                        cell.status = HexCell.Status.WRONG;
                        score -= 5;
                    }
                    if (scoreChangeListener != null) scoreChangeListener.onScoreChanged(score);
                    invalidate();
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private boolean isInside(float x, float y, HexCell cell) {
        float dx = Math.abs(x - cell.centerX);
        float dy = Math.abs(y - cell.centerY);
        float h = (float) (Math.sqrt(3) / 2 * radius);
        if (dx > h || dy > radius) return false;
        return radius * radius - radius * dy / 2 > dx * dx + dy * dy;
    }

    private static class HexCell {
        float centerX, centerY;
        int row, col;
        enum Type { NORMAL, REFERENCE }
        enum Status { NONE, CORRECT, WRONG }
        Type type = Type.NORMAL;
        Status status = Status.NONE;
        boolean isCoChannelTarget = false;

        HexCell(float x, float y, int r, int c) {
            this.centerX = x;
            this.centerY = y;
            this.row = r;
            this.col = c;
        }
    }
}
