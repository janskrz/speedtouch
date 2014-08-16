package com.rumoe.speedtouch.gameboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.rumoe.speedtouch.R;

import java.util.ArrayList;

public class Cell extends SurfaceView implements SurfaceHolder.Callback{

    // TODO add timeout and cell type

    private CellAnimation animation;
    private ArrayList<CellObserver> observer;
    private boolean isActive;

    public Cell(Context context) {
        super(context);

        int backgroundColor = getResources().getColor(R.color.game_board_background);
        int cellColor = getResources().getColor(R.color.cell_standard_color);
        animation = new CellAnimation(getHolder(), cellColor, backgroundColor);
        isActive = false;

        // a cell is part of one game board -> in most cases there is exactly one observer
        // (except multiplayer)
        observer = new ArrayList<CellObserver>(1);

        int cellPadding = (int) getResources().getDimension(R.dimen.board_cell_padding);
        setPadding(cellPadding, cellPadding, cellPadding, cellPadding);

        // registers the listener
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        animation.clearBackground();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //TODO stop thread
        Log.i("Cell", "surface destroyed");
        isActive = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        int cellPadding = (int) getResources().getDimension(R.dimen.board_cell_padding);
        animation.setDimensions(width, height, cellPadding);
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {
        super.onTouchEvent(e);

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            if (isActive) {
                //TODO check if missed etc
            } else {
                animation.startAnimation();
            }
        }
        return true;
    }

    /**
     * Adds an CellObserver to the cell which will be notified on multiple events:
     * <ul>
     *     <li>Cell is activated</li>
     *     <li>Cell is deactivated through timeout</li>
     *     <li>Cell is deactivated through touch</li>
     *     <li>Cell registers ACTION_DOWN MotionEvent outside its active area</li>
     * </ul>
     * @param obs The CellObserver to be added.
     * @return true
     */
    public boolean registerObserver(CellObserver obs) {
        observer.add(obs);
        return true;
    }

    /**
     * Removes an CellObserver. Is the same observer registered multiple times then it will
     * be removed only once.
     * @param obs The CellObserver to be removed
     * @return true iff the CellObserver was registered and removed, false otherwise
     */
    public boolean removeObserver(CellObserver obs) {
        for (int i = observer.size() - 1; i >= 0; i++) {
            if (obs.equals(observer.get(i))) {
                observer.remove(i);
                return true;
            }
        }
        return false;
    }

    private void notifyAllOnActive() {
        for (CellObserver obs : observer) {
            obs.notifyOnActive(this);
        }
    }
}