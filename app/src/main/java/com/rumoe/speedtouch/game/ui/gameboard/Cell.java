package com.rumoe.speedtouch.game.ui.gameboard;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.rumoe.speedtouch.game.ui.gameboard.anim.BlinkInterpolator;

public class Cell {

    private static final Interpolator GROW_INTERPOLATOR     = new LinearInterpolator();
    private static final Interpolator SHRINK_INTERPOLATOR   = new AccelerateInterpolator(3.5f);
    private static final Interpolator BLINK_INTERPOLATOR    = new BlinkInterpolator();

    public static final int DEFAULT_WAIT_BEFORE_SHRINK_TIME     = 1000;
    public static final int DEFAULT_GROW_ANIMATION_DURATION     = 100;
    public static final int DEFAULT_SHRINK_ANIMATION_DURATION   = 2000;
    public static final int DEFAULT_BLINK_ANIMATION_DURATION    = 1000;

    private Thread      lifecycle;
    private Animation   animation;
    private Context context;

    private CellType type;
    private Paint paint;
    private boolean active;

    private float radius;
    private long activationTime;
    private long timeoutTime;

    public Cell(Context context) {
        this.context = context;

        radius = 0.0f;
        type = CellType.STANDARD;
        active = false;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        updatePaint();
    }

    /**
     * Refresh the cells paint (and thus its look) based on its state.
     * E.g. the current CellType.
     */
    private void updatePaint() {
        paint.setColor(CellType.getCellColor(type, context));
        paint.setShadowLayer(15.0f, 0.0f, 0.0f, CellType.getShadowColor(type, context));
    }

    /**
     * Tells if the cell is currently executing an animation.
     * @return true iff an animation is executed, false otherwise.
     */
    public boolean isActive() {
        if (lifecycle != null && !lifecycle.isInterrupted()) return true;
        return active;
    }

    /**
     * Returns the current CellType which was set the last time the cell was activated.
     * @return cellType
     */
    public CellType getType() {
        return type;
    }

    /**
     * Returns the Paint of the cell which decides is lock. The object which
     * is returned depends on the CellType passed last time the cell was activated.
     * @return Paint of the cell.
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * Returns the current radius of the cell which is a value between 0.0f and 1.0f.
     * The value 1.0f means that the cell is at its maximum value.
     * The value is changed whenever an animation is running.
     * @return radius of the cell.
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Gets the timestamp (ms since epoch) of the last activation of the cell.
     * @return activation time
     */
    public long getActivationTime() {
        return activationTime;
    }

    /**
     * Gets the timestamp (ms since epoch) at which the current cell lifecycle will end, or
     * -1 if no lifecycle is active.
     * @return timeout time.
     */
    public long getTimeoutTime() {
        return isActive()? timeoutTime : -1;
    }

    /* ---------------------------------------------------------------------------------------------
                            CELL ACTIVATION AND DEACTIVATION
    --------------------------------------------------------------------------------------------- */

    /**
     * Activates the cell without a timeout and its default grow time.
     * @param type New type of the cell.
     * @return true on success, false otherwise.
     */
    public boolean activate(CellType type) {
        return activate(type, DEFAULT_GROW_ANIMATION_DURATION);
    }

    /**
     * Activates the cell without a timeout.
     * @param type New type of the cell.
     * @param growTime Time in ms the cell needs to reach its full radius.
     * @return true on success, false otherwise.
     */
    public boolean activate(CellType type, int growTime) {
        activationTime = System.currentTimeMillis();
        timeoutTime = -1;
        return setAnimation(type, GROW_INTERPOLATOR, growTime, 0.0f, 1.0f);
    }

    /**
     * Activates the cell lifecycle with its default timing.
     * @param type New type of the cell.
     * @return true on success, false otherwise.
     */
    public boolean activateLifecycle(CellType type) {
        return activateLifecycle(type, DEFAULT_GROW_ANIMATION_DURATION,
                DEFAULT_WAIT_BEFORE_SHRINK_TIME, DEFAULT_SHRINK_ANIMATION_DURATION);
    }

    /**
     * Activates the cell lifecycle,
     * @param type New type of the cell.
     * @param growTime Time in ms the cell needs to reach its full radius.
     * @param constantTime Time in ms the cell radius will stay constant.
     * @param shrinkTime Time in ms the cell needs to reach a radius of 0.0f again.
     * @return true on success, false otherwise.
     */
    public boolean activateLifecycle(final CellType type, final int growTime,
                                     final int constantTime, final int shrinkTime) {
        activationTime = System.currentTimeMillis();
        timeoutTime = growTime + constantTime + shrinkTime;

        lifecycle = new Thread() {
            @Override
            public void run() {
                cycle :
                {
                    setAnimation(type, GROW_INTERPOLATOR, growTime, 0.0f, 1.0f);
                    if (!waitUntilAnimationEnded()) break cycle;
                    try {
                        Thread.sleep(constantTime);
                    } catch (InterruptedException e) {
                        Log.d("Cell", "Lifecycle-Thread interrupted");
                        break cycle;
                    }
                    setAnimation(type, SHRINK_INTERPOLATOR, shrinkTime, 1.0f, 0.0f);
                    if (!waitUntilAnimationEnded()) break cycle;
                }
                clearCell();
            }
        };
        lifecycle.start();

        return false;
    }

    /**
     * Stops the cell animation and sets its radius to 0.0f.
     * The CellType will stay the same.
     */
    public void clearCell() {
        if (lifecycle != null) lifecycle.interrupt();
        stopAnimation();
        active = false;
        radius = 0.0f;
    }

    /* ---------------------------------------------------------------------------------------------
                           ANIMATION OF THE RADIUS
    --------------------------------------------------------------------------------------------- */

    /**
     * Starts a new Animation which will change the cell radius depending of the following parameter.
     * @param newType Type of the cell it will have during the animation.
     * @param animInterpolator Interpolator for the animation timing.
     * @param duration Duration of the animation.
     * @param startSize The start radius of the cell.
     * @param targetSize The end radius of the cell.
     * @return true iff animation could be successfully started, false otherwise
     * (e.g. an animation was already running)
     */
    private boolean setAnimation(CellType newType, Interpolator animInterpolator, int duration,
                                 final float startSize, final float targetSize) {

        return false;
    }

    /**
     * Stops the currently running animation.
     */
    private void stopAnimation() {
        //TODO
    }

    /**
     * Locks the calling thread until the animation has ended.
     * @return
     */
    private boolean waitUntilAnimationEnded() {
        return true;
    }
}
