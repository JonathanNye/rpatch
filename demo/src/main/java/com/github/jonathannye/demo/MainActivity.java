package com.github.jonathannye.demo;

import com.actionbarsherlock.app.SherlockActivity;
import com.github.jonathannye.rpatch.RPatch;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends SherlockActivity {

    private static final List<Integer> DRAWABLE_RES_IDS = Arrays.asList(
            R.drawable.test_rpatch,
            R.drawable.test_rpatch2,
            R.drawable.test_rpatch3,
            R.drawable.stamp,
            R.drawable.stripes);
    private static final List<Integer> DRAWABLE_STRING_IDS = Arrays.asList(
            R.string.dots,
            R.string.squiggles,
            R.string.notepad,
            R.string.stamp,
            R.string.stripes);

    private int currentDrawableIndex = -1;

    private View patchView, patchContainer;
    private Button nextDrawableButton;
    private CheckBox centerCheck;

    private ArrayList<CheckableTextView> innerModeTextViews, outerModeTextViews, repeatBehaviorTextViews;
    private ArrayList<Integer> innerModes, outerModes, repeatModes;

    private boolean drawPatchCentered;
    private int innerRepeatMode = RPatch.REPEAT_INNER_BOTH;
    private int outerRepeatMode = RPatch.REPEAT_OUTER_ALL;
    private int repeatBehavior = RPatch.REPEAT_MODE_DISCRETE;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        patchContainer = findViewById(R.id.rpatch_container);
        patchView = findViewById(R.id.rpatch_view);
        nextDrawableButton = (Button) findViewById(R.id.next_drawable);
        centerCheck = (CheckBox) findViewById(R.id.centering_check);

        innerModeTextViews = new ArrayList<CheckableTextView>();
        innerModeTextViews.add((CheckableTextView) findViewById(R.id.i_both));
        innerModeTextViews.add((CheckableTextView) findViewById(R.id.i_none));
        innerModeTextViews.add((CheckableTextView) findViewById(R.id.i_x));
        innerModeTextViews.add((CheckableTextView) findViewById(R.id.i_y));

        outerModeTextViews = new ArrayList<CheckableTextView>();
        outerModeTextViews.add((CheckableTextView) findViewById(R.id.o_all));
        outerModeTextViews.add((CheckableTextView) findViewById(R.id.o_none));
        outerModeTextViews.add((CheckableTextView) findViewById(R.id.o_left));
        outerModeTextViews.add((CheckableTextView) findViewById(R.id.o_right));
        outerModeTextViews.add((CheckableTextView) findViewById(R.id.o_top));
        outerModeTextViews.add((CheckableTextView) findViewById(R.id.o_bottom));

        repeatBehaviorTextViews = new ArrayList<CheckableTextView>();
        repeatBehaviorTextViews.add((CheckableTextView) findViewById(R.id.rm_discrete));
        repeatBehaviorTextViews.add((CheckableTextView) findViewById(R.id.rm_cutoff));

        innerModes = new ArrayList<Integer>();
        innerModes.add(RPatch.REPEAT_INNER_BOTH);
        innerModes.add(RPatch.REPEAT_INNER_NONE);
        innerModes.add(RPatch.REPEAT_INNER_X);
        innerModes.add(RPatch.REPEAT_INNER_Y);

        outerModes = new ArrayList<Integer>();
        outerModes.add(RPatch.REPEAT_OUTER_ALL);
        outerModes.add(RPatch.REPEAT_OUTER_NONE);
        outerModes.add(RPatch.REPEAT_OUTER_LEFT);
        outerModes.add(RPatch.REPEAT_OUTER_RIGHT);
        outerModes.add(RPatch.REPEAT_OUTER_TOP);
        outerModes.add(RPatch.REPEAT_OUTER_BOTTOM);

        repeatModes = new ArrayList<Integer>();
        repeatModes.add(RPatch.REPEAT_MODE_DISCRETE);
        repeatModes.add(RPatch.REPEAT_MODE_CUTOFF);

        for (CheckableTextView ctv : innerModeTextViews) {
            ctv.setOnClickListener(innerModeClickListener);
        }
        for (CheckableTextView ctv : outerModeTextViews) {
            ctv.setOnClickListener(outerModeClickListener);
        }
        for (CheckableTextView ctv : repeatBehaviorTextViews) {
            ctv.setOnClickListener(repeatBehaviorClickListener);
        }

        patchContainer.setOnTouchListener(new View.OnTouchListener() {
            private float origX;
            private float origY;
            private int origWidth;
            private int origHeight;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        origX = motionEvent.getX();
                        origY = motionEvent.getY();
                        origWidth = view.getWidth();
                        origHeight = view.getHeight();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = motionEvent.getX() - origX;
                        float dy = motionEvent.getY() - origY;
                        ViewGroup.LayoutParams newParams = view.getLayoutParams();
                        newParams.width = origWidth + (int) dx;
                        newParams.height = origHeight + (int) dy;
                        view.setLayoutParams(newParams);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return true;
            }
        });

        centerCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                drawPatchCentered = b;
                ((RPatch) patchView.getBackground()).setDrawCentered(drawPatchCentered);
                patchView.invalidate();
            }
        });
        // Init defaults
        innerModeTextViews.get(0).setChecked(true);
        outerModeTextViews.get(0).setChecked(true);
        repeatBehaviorTextViews.get(0).setChecked(true);

        nextDrawable(null);

        centerCheck.setChecked(true);
    }

    private void setRepeatFlags() {
        ((RPatch) patchView.getBackground()).setRepeatFlags(innerRepeatMode | outerRepeatMode | repeatBehavior);
        patchView.invalidate();
    }

    private View.OnClickListener innerModeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Single choice mode
            if (((CheckableTextView) v).isChecked()) {
                return;
            }
            for (CheckableTextView ctv : innerModeTextViews) {
                ctv.setChecked(false);
            }
            ((CheckableTextView) v).toggle();

            innerRepeatMode = innerModes.get(innerModeTextViews.indexOf(v));
            setRepeatFlags();
        }
    };

    private View.OnClickListener outerModeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean willBeChecked = !((CheckableTextView) v).isChecked();
            if (willBeChecked) {
                if (outerModeTextViews.indexOf(v) == 0) {
                    // All, uncheck all other options
                    outerModeTextViews.get(1).setChecked(false);
                    outerModeTextViews.get(2).setChecked(false);
                    outerModeTextViews.get(3).setChecked(false);
                    outerModeTextViews.get(4).setChecked(false);
                    outerModeTextViews.get(5).setChecked(false);

                    ((CheckableTextView) v).toggle();
                    outerRepeatMode = outerModes.get(0);
                    setRepeatFlags();
                    return;
                } else if (outerModeTextViews.indexOf(v) == 1) {
                    // None, uncheck all other options
                    outerModeTextViews.get(0).setChecked(false);
                    outerModeTextViews.get(2).setChecked(false);
                    outerModeTextViews.get(3).setChecked(false);
                    outerModeTextViews.get(4).setChecked(false);
                    outerModeTextViews.get(5).setChecked(false);

                    ((CheckableTextView) v).toggle();
                    outerRepeatMode = outerModes.get(1);
                    setRepeatFlags();
                    return;
                } else {
                    // Uncheck None and all
                    outerModeTextViews.get(0).setChecked(false);
                    outerModeTextViews.get(1).setChecked(false);
                }
            }
            ((CheckableTextView) v).toggle();
            boolean areAnyChecked = false;
            for (CheckableTextView ctv : outerModeTextViews) {
                if (ctv.isChecked()) {
                    areAnyChecked = true;
                }
            }
            if (!areAnyChecked) {
                outerModeTextViews.get(0).setChecked(true);
            }

            // Set to none
            outerRepeatMode = outerModes.get(1);
            // Add directional flags
            for (int i = 2; i < outerModeTextViews.size(); i++) {
                CheckableTextView ctv = outerModeTextViews.get(i);
                if (ctv.isChecked()) {
                    outerRepeatMode |= outerModes.get(i);
                }
            }
            setRepeatFlags();
        }
    };

    private View.OnClickListener repeatBehaviorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Single choice mode
            if (((CheckableTextView) v).isChecked()) {
                return;
            }
            for (CheckableTextView ctv : repeatBehaviorTextViews) {
                ctv.setChecked(false);
            }
            ((CheckableTextView) v).toggle();

            repeatBehavior = repeatModes.get(repeatBehaviorTextViews.indexOf(v));
            setRepeatFlags();
        }
    };

    public void nextDrawable(View v) {
        currentDrawableIndex++;
        if (currentDrawableIndex >= DRAWABLE_RES_IDS.size()) {
            currentDrawableIndex = 0;
        }
        nextDrawableButton.setText(DRAWABLE_STRING_IDS.get(currentDrawableIndex));
        RPatch patch = new RPatch(this, DRAWABLE_RES_IDS.get(currentDrawableIndex));
        patch.setDrawCentered(drawPatchCentered);
        patch.setRepeatFlags(innerRepeatMode | outerRepeatMode | repeatBehavior);
        patchView.setBackgroundDrawable(patch);

        ViewGroup dbgContainer = (ViewGroup) findViewById(R.id.patch_segment_container);
        dbgContainer.removeAllViews();
        for (int i = 0; i < 9; i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            iv.setBackgroundColor(0xffff0000);
            iv.setPadding(10, 10, 10, 10);
            iv.setScaleType(ImageView.ScaleType.CENTER);
            iv.setImageBitmap(patch.dbgGetPatch(i));
            dbgContainer.addView(iv);
        }
    }

    public void decreaseViewSizeClicked(View v) {
        modifyPatchSize(false);
    }

    public void increaseViewSizeClicked(View v) {
        modifyPatchSize(true);
    }

    private void modifyPatchSize(boolean increase) {
        int mod = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        int newSize = patchContainer.getWidth() + (increase ? mod : mod * -1);
        if (newSize > mod * 50 || newSize < mod * 3) {
            return;
        }
        ViewGroup.LayoutParams newParams = patchContainer.getLayoutParams();
        newParams.width = newSize;
        newParams.height = newSize;

        patchContainer.setLayoutParams(newParams);
    }
}
