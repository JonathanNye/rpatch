package com.github.jonathannye.rpatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class RPatch extends Drawable {

    // Patch indices
    private static final int IDX_TOP_LEFT = 0;
    private static final int IDX_TOP_MID = 1;
    private static final int IDX_TOP_RIGHT = 2;
    private static final int IDX_MID_LEFT = 3;
    private static final int IDX_MID = 4;
    private static final int IDX_MID_RIGHT = 5;
    private static final int IDX_BOT_LEFT = 6;
    private static final int IDX_BOT_MID = 7;
    private static final int IDX_BOT_RIGHT = 8;

    // Patch repetition flags
    public static final int REPEAT_INNER_X = 1;
    public static final int REPEAT_INNER_Y = 1 << 1;
    public static final int REPEAT_INNER_BOTH = REPEAT_INNER_X | REPEAT_INNER_Y;
    public static final int REPEAT_INNER_NONE = 0;
    public static final int REPEAT_OUTER_TOP = 1 << 2;
    public static final int REPEAT_OUTER_BOTTOM = 1 << 3;
    public static final int REPEAT_OUTER_LEFT = 1 << 4;
    public static final int REPEAT_OUTER_RIGHT = 1 << 5;
    public static final int REPEAT_OUTER_ALL = REPEAT_OUTER_TOP | REPEAT_OUTER_BOTTOM
            | REPEAT_OUTER_LEFT | REPEAT_OUTER_RIGHT;
    public static final int REPEAT_OUTER_NONE = 0;

    // Repetition
    public static final int REPEAT_MODE_CUTOFF = 1 << 6;
    public static final int REPEAT_MODE_DISCRETE = 0;

    private static BitmapFactory.Options LOAD_OPTIONS;

    static {
        LOAD_OPTIONS = new BitmapFactory.Options();
        LOAD_OPTIONS.inDither = false;
        LOAD_OPTIONS.inScaled = false;
    }

    private static final Matrix IDENTITY_MATRIX = new Matrix();


    private Bitmap[] patches = new Bitmap[9];

    // 0 implies REPEAT_MODE_DISCRETE, REPEAT_INNER_BOTH, REPEAT_OUTER_NONE
    private int repeatFlags = 0;
    private boolean drawCentered;

    private int leftWidth;
    private int midWidth;
    private int rightWidth;
    private int topHeight;
    private int midHeight;
    private int botHeight;

    public RPatch(Bitmap b) {
        parseBitmap(b);
    }

    public RPatch(Context c, int resId) {
        this(BitmapFactory.decodeResource(c.getResources(), resId, LOAD_OPTIONS));
    }

    public Bitmap dbgGetPatch(int idx) {
        return patches[idx];
    }

    public void setRepeatFlags(int flags) {
        repeatFlags = flags;
    }

    public void setDrawCentered(boolean drawCentered) {
        this.drawCentered = drawCentered;
    }

    // TODO: repeatable patch inset from indicated by one on v and h
    // TODO: density considerations?
    private void parseBitmap(Bitmap srcBitmap) {
        int vTop = -1, vBottom = -1, hLeft = -1, hRight = -1;
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        int px;

        int[] pixelChunk = new int[width];
        srcBitmap.getPixels(pixelChunk, 0, width, 0, 0, width, 1);

        for (int i = 0; i < width; i++) {
            px = pixelChunk[i];
            if ((px & 0xFF000000) != 0) {
                hLeft = i;
                break;
            }
        }

        for (int i = width - 1; i != 0; i--) {
            px = pixelChunk[i];
            if ((px & 0xFF000000) != 0) {
                hRight = i;
                break;
            }
        }

        pixelChunk = new int[height];
        srcBitmap.getPixels(pixelChunk, 0, 1, 0, 0, 1, height);

        for (int i = 0; i < height; i++) {
            px = pixelChunk[i];
            if ((px & 0xFF000000) != 0) {
                vTop = i;
                break;
            }
        }

        for (int i = height - 1; i != 0; i--) {
            px = pixelChunk[i];
            if ((px & 0xFF000000) != 0) {
                vBottom = i;
                break;
            }
        }

        leftWidth = hLeft - 1;
        midWidth = hRight - leftWidth;
        rightWidth = width - midWidth - leftWidth - 2;
        topHeight = vTop - 1;
        midHeight = vBottom - topHeight;
        botHeight = height - midHeight - topHeight -2;

        patches[IDX_TOP_LEFT] = Bitmap.createBitmap(srcBitmap, 1, 1, leftWidth, topHeight);
        patches[IDX_TOP_MID] = Bitmap.createBitmap(srcBitmap, hLeft, 1, midWidth, topHeight);
        patches[IDX_TOP_RIGHT] = Bitmap.createBitmap(srcBitmap, hRight + 1, 1, rightWidth, topHeight);
        patches[IDX_MID_LEFT] = Bitmap.createBitmap(srcBitmap, 1, vTop, leftWidth, midHeight);
        patches[IDX_MID] = Bitmap.createBitmap(srcBitmap, hLeft, vTop, midWidth, midHeight);
        patches[IDX_MID_RIGHT] = Bitmap
                .createBitmap(srcBitmap, hRight + 1, vTop, rightWidth, midHeight);
        patches[IDX_BOT_LEFT] = Bitmap
                .createBitmap(srcBitmap, 1, vBottom + 1, leftWidth, botHeight);
        patches[IDX_BOT_MID] = Bitmap
                .createBitmap(srcBitmap, hLeft, vBottom + 1, midWidth, botHeight);
        patches[IDX_BOT_RIGHT] = Bitmap
                .createBitmap(srcBitmap, hRight, vBottom + 1, rightWidth, botHeight);
    }

    // TODO: Edge patches drawn on cutoffs are inset by one (bottom only?)
    @Override
    public void draw(Canvas canvas) {

        Rect bounds = new Rect(getBounds());

        // If it's not cutoff mode, we have to modify the bounds
        if((repeatFlags & REPEAT_MODE_CUTOFF) == 0) {

            int maxMidWidth = bounds.width() - leftWidth - rightWidth;
            int maxMidHeight = bounds.height() - topHeight - botHeight;
            int numXReps = maxMidWidth / midWidth;
            int numYReps = maxMidHeight / midHeight;

            if(drawCentered) {
                // Diffs are inset into original bounds to center
                int xDiff = maxMidWidth - numXReps * midWidth;
                bounds.left += (xDiff / 2);
                bounds.right -= (xDiff / 2);
                int yDiff = maxMidHeight - numYReps * midHeight;
                bounds.top += (yDiff / 2);
                bounds.bottom -= (yDiff / 2);
            } else {
                bounds.right = leftWidth + rightWidth + numXReps * midWidth;
                bounds.bottom = topHeight + botHeight + numYReps * midHeight;
            }
        }

        // Helper Rects
        Rect topLeft = new Rect(bounds.left,
                bounds.top,
                bounds.left + leftWidth,
                bounds.top + topHeight);
        Rect topRight = new Rect(bounds.right - rightWidth,
                bounds.top,
                bounds.right,
                bounds.top + topHeight);
        Rect botLeft = new Rect(bounds.left,
                bounds.bottom - botHeight,
                bounds.left + leftWidth,
                bounds.bottom);
        Rect botRight = new Rect(bounds.right - rightWidth,
                bounds.bottom - botHeight,
                bounds.right,
                bounds.bottom);
        Rect middle = new Rect(topLeft.right,
                topLeft.bottom,
                topRight.left,
                botLeft.top);

        Paint p = new Paint();
        Paint cornerPaint = new Paint();
        BitmapShader shader;
        Matrix m = new Matrix();


        canvas.drawBitmap(patches[IDX_TOP_LEFT], bounds.left, bounds.top, cornerPaint);

        m.postTranslate(topLeft.right, topLeft.top);
        if((repeatFlags & REPEAT_OUTER_TOP) != 0) {
            shader = new BitmapShader(patches[IDX_TOP_MID], Shader.TileMode.REPEAT,
                    Shader.TileMode.CLAMP);

        } else {
            shader = new BitmapShader(patches[IDX_TOP_MID], Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP);
            m.preScale((topRight.left - topLeft.right) / (float)patches[IDX_TOP_MID].getWidth(), 1.0f);
        }
        shader.setLocalMatrix(m);
        p.setShader(shader);
        canvas.drawRect(topLeft.right, topLeft.top, topRight.left, topRight.bottom, p);

        canvas.drawBitmap(patches[IDX_TOP_RIGHT], topRight.left, bounds.top, cornerPaint);

        m.set(IDENTITY_MATRIX);
        m.postTranslate(bounds.left, topLeft.bottom);
        if((repeatFlags & REPEAT_OUTER_LEFT) != 0) {
            shader = new BitmapShader(patches[IDX_MID_LEFT], Shader.TileMode.CLAMP,
                    Shader.TileMode.REPEAT);
        } else {
            shader = new BitmapShader(patches[IDX_MID_LEFT], Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP);
            m.preScale(1.0f, (botLeft.top - topRight.bottom) / (float)patches[IDX_MID_LEFT].getHeight());
        }
        shader.setLocalMatrix(m);
        p.setShader(shader);
        canvas.drawRect(bounds.left, topLeft.bottom, topLeft.right, botLeft.top, p);

        m.set(IDENTITY_MATRIX);
        m.postTranslate(middle.left, middle.top);
        switch(repeatFlags & REPEAT_INNER_BOTH) {
            case REPEAT_INNER_BOTH:
                shader = new BitmapShader(patches[IDX_MID], Shader.TileMode.REPEAT,
                        Shader.TileMode.REPEAT);
                break;
            case REPEAT_INNER_NONE:
                shader = new BitmapShader(patches[IDX_MID], Shader.TileMode.CLAMP,
                        Shader.TileMode.CLAMP);
                m.preScale(middle.width() / (float) patches[IDX_MID].getWidth(),
                        middle.height() / (float) patches[IDX_MID].getHeight());
                break;
            case REPEAT_INNER_Y:
                shader = new BitmapShader(patches[IDX_MID], Shader.TileMode.CLAMP,
                        Shader.TileMode.REPEAT);
                m.preScale(middle.width() / (float)patches[IDX_MID].getWidth(), 1.0f);
                break;
            case REPEAT_INNER_X:
            default:
                shader = new BitmapShader(patches[IDX_MID], Shader.TileMode.REPEAT,
                        Shader.TileMode.CLAMP);
                m.preScale(1.0f, middle.height() / (float)patches[IDX_MID].getHeight());
                break;
        }
        shader.setLocalMatrix(m);
        p.setShader(shader);
        canvas.drawRect(middle, p);

        m.set(IDENTITY_MATRIX);
        m.postTranslate(topRight.left, topRight.bottom);
        if((repeatFlags & REPEAT_OUTER_RIGHT) != 0) {
            shader = new BitmapShader(patches[IDX_MID_RIGHT], Shader.TileMode.CLAMP,
                    Shader.TileMode.REPEAT);
        } else {
            shader = new BitmapShader(patches[IDX_MID_RIGHT], Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP);
            m.preScale(1.0f, (botRight.top - topRight.bottom) / (float)patches[IDX_MID_RIGHT].getHeight());
        }
        shader.setLocalMatrix(m);
        p.setShader(shader);
        canvas.drawRect(topRight.left, topRight.bottom, bounds.right, botRight.top, p);

        canvas.drawBitmap(patches[IDX_BOT_LEFT], bounds.left, botRight.top - 1, cornerPaint);

        m.set(IDENTITY_MATRIX);
        m.postTranslate(botLeft.right, botLeft.top);
        if((repeatFlags & REPEAT_OUTER_BOTTOM) != 0) {
            shader = new BitmapShader(patches[IDX_BOT_MID], Shader.TileMode.REPEAT,
                    Shader.TileMode.CLAMP);
        } else {
            shader = new BitmapShader(patches[IDX_BOT_MID], Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP);
            m.preScale((botRight.left - botLeft.right) / (float)patches[IDX_BOT_MID].getWidth(), 1.0f);
        }
        shader.setLocalMatrix(m);
        p.setShader(shader);
        canvas.drawRect(botLeft.right, botLeft.top, botRight.left, botLeft.bottom, p);

        canvas.drawBitmap(patches[IDX_BOT_RIGHT], botRight.left, botRight.top - 1, cornerPaint);

    }

    @Override
    public int getMinimumWidth() {
        return patches[IDX_TOP_LEFT].getWidth() + patches[IDX_TOP_MID].getHeight() +
                patches[IDX_TOP_RIGHT].getHeight();
    }

    @Override
    public int getMinimumHeight() {
        return patches[IDX_TOP_LEFT].getHeight() + patches[IDX_MID_LEFT].getHeight() +
                patches[IDX_BOT_LEFT].getHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return getMinimumWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return getMinimumHeight();
    }

    @Override
    public void setAlpha(int i) {
        // TODO setAlpha(int)
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // TODO setColorFilter(ColorFilter)
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
