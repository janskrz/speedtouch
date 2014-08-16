package com.rumoe.speedtouch.gameboard;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import com.rumoe.speedtouch.R;


// TODO handle onpause and onresume
public class CellAnimation implements Runnable {

    private SurfaceHolder cellSurface;
    private final Paint cellPaint;
    private int backgroundColor;
    private int shadowColor;

    private float maxCellRadius;
    private float cellXCenter;
    private float cellYCenter;

    private Thread drawThread;

    public CellAnimation(SurfaceHolder surface) {
        cellSurface = surface;

        backgroundColor = Resources.getSystem().getColor(R.color.game_board_background);
        shadowColor     = Resources.getSystem().getColor(R.color.cell_shadow);

        cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setColor(Resources.getSystem().getColor(R.color.cell_standard));
        cellPaint.setShadowLayer(10.0f, 0.0f, 5.0f, shadowColor);
    }

    /**
     * This should be called whenever the dimensions of the cells are changed.
     * Updates the cell center as well as the maximal circle size.
     * @param cellWidth The width of the cell-surface
     * @param cellHeight The height of the cell-surface
     * @param cellPadding The padding of the cell
     */
    public void setDimensions(int cellWidth, int cellHeight, int cellPadding) {
        maxCellRadius = (Math.min(cellWidth, cellHeight) - 2 * cellPadding) * 0.5f;

        cellXCenter = cellWidth / 2.0f;
        cellYCenter = cellHeight / 2.0f;
    }

    /**
     * Fills the cell with its background color.
     */
    public void clearBackground() {
        if (cellSurface.getSurface().isValid()) {
            Canvas canvas = cellSurface.lockCanvas();
            canvas.drawColor(backgroundColor);
            cellSurface.unlockCanvasAndPost(canvas);
        } else {
            Log.e("CellAnimation", "Surface invalid while clearing background");
        }
    }

    public boolean startAnimation() {
        drawThread = new Thread(this);
        drawThread.start();

        return true;
    }

    @Override
    public void run() {
        if (cellSurface.getSurface().isValid()) {
            Canvas canvas = cellSurface.lockCanvas();

            canvas.drawColor(backgroundColor);  // over-paint everything from previous frame

            canvas.drawCircle(cellXCenter, cellYCenter, maxCellRadius, cellPaint);

            cellSurface.unlockCanvasAndPost(canvas);
        } else {
            Log.e("CellAnimation", "Cell surface invalid while attempting to draw");
        }
    }
}
