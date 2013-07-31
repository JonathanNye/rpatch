package com.github.jonathannye.demo;

import com.github.jonathannye.rpatch.RPatch;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

public class MainActivity extends Activity {
    int patchIdx = -1;

    View patchView;
    View patchContainer;
    CheckBox centerCheck;
    Spinner innerRepeatSpinner;
    Spinner outerRepeatSpinner;
    Spinner repeatBehaviorSpinner;
    ScrollView scrollView;

    private boolean drawPatchCentered;
    private int innerRepeatMode = RPatch.REPEAT_INNER_BOTH;
    private int outerRepeatMode = RPatch.REPEAT_OUTER_NONE;
    private int repeatBehavior = RPatch.REPEAT_MODE_DISCRETE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        patchContainer = findViewById(R.id.rpatch_container);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        patchContainer.setOnTouchListener(new View.OnTouchListener() {

            float origX;
            float origY;
            int origWidth;
            int origHeight;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
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
                        newParams.height = origHeight + (int)dy;
                        view.setLayoutParams(newParams);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return true;
            }
        });

        patchView = findViewById(R.id.rpatch_view);
        centerCheck = (CheckBox) findViewById(R.id.centering_check);
        centerCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                drawPatchCentered = b;
                ((RPatch)patchView.getBackground()).setDrawCentered(drawPatchCentered);
                patchView.invalidate();

            }
        });

        repeatBehaviorSpinner = (Spinner) findViewById(R.id.repeat_behavior_spinner);
        String[] behaviorChoices = {"REPEAT_MODE_DISCRETE", "REPEAT_MODE_CUTOFF"};
        repeatBehaviorSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, behaviorChoices));
        repeatBehaviorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0:
                        repeatBehavior = RPatch.REPEAT_MODE_DISCRETE;
                        break;
                    default:
                        repeatBehavior = RPatch.REPEAT_MODE_CUTOFF;
                        break;
                }
                ((RPatch)patchView.getBackground()).setRepeatFlags(
                        innerRepeatMode | outerRepeatMode | repeatBehavior);
                patchView.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        innerRepeatSpinner = (Spinner) findViewById(R.id.inner_repeat_spinner);
        String[] innerChoices = {"REPEAT_INNER_BOTH", "REPEAT_INNER_X", "REPEAT_INNER_Y", "REPEAT_INNER_NONE"};
        innerRepeatSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, innerChoices));
        innerRepeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0:
                        innerRepeatMode = RPatch.REPEAT_INNER_BOTH;
                        break;
                    case 1:
                        innerRepeatMode = RPatch.REPEAT_INNER_X;
                        break;
                    case 2:
                        innerRepeatMode = RPatch.REPEAT_INNER_Y;
                        break;
                    default:
                        innerRepeatMode = RPatch.REPEAT_INNER_NONE;
                        break;
                }
                ((RPatch)patchView.getBackground()).setRepeatFlags(
                        innerRepeatMode | outerRepeatMode | repeatBehavior);
                patchView.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        outerRepeatSpinner = (Spinner) findViewById(R.id.outer_repeat_spinner);
        String[] outerChoices = {"REPEAT_OUTER_ALL", "REPEAT_OUTER_NONE", "REPEAT_OUTER_LEFT",
                "REPEAT_OUTER_RIGHT", "REPEAT_OUTER_TOP", "REPEAT_OUTER_BOTTOM"};
        outerRepeatSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, outerChoices));
        outerRepeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0:
                        outerRepeatMode = RPatch.REPEAT_OUTER_ALL;
                        break;
                    case 1:
                        outerRepeatMode = RPatch.REPEAT_OUTER_NONE;
                        break;
                    case 2:
                        outerRepeatMode = RPatch.REPEAT_OUTER_LEFT;
                        break;
                    case 3:
                        outerRepeatMode = RPatch.REPEAT_OUTER_RIGHT;
                        break;
                    case 4:
                        outerRepeatMode = RPatch.REPEAT_OUTER_TOP;
                        break;
                    default:
                        outerRepeatMode = RPatch.REPEAT_OUTER_BOTTOM;
                        break;
                }
                ((RPatch)patchView.getBackground()).setRepeatFlags(innerRepeatMode | outerRepeatMode | repeatBehavior);
                patchView.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        // To reflect the default state
        outerRepeatSpinner.setSelection(1);
        nextDrawable(null);
    }

    public void nextDrawable(View v) {
        int drawableId;
        patchIdx++;
        if(patchIdx >= 3) {
            patchIdx = 0;
        }
        switch(patchIdx) {
            case 0:
                drawableId = R.drawable.test_rpatch;
                break;
            case 1:
                drawableId = R.drawable.test_rpatch2;
                break;
            case 2:
                drawableId = R.drawable.test_rpatch3;
                break;
            default:
                drawableId = R.drawable.test_rpatch;
                break;
        }
        RPatch patch = new RPatch(this, drawableId);
        patch.setDrawCentered(drawPatchCentered);
        patch.setRepeatFlags(innerRepeatMode | outerRepeatMode | repeatBehavior);
        patchView.setBackgroundDrawable(patch);

        ViewGroup dbgContainer = (ViewGroup) findViewById(R.id.debug_patch_container);
        dbgContainer.removeAllViews();
        for(int i = 0; i < 9; i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
        if(newSize > mod * 50 || newSize < mod * 3) {
            return;
        }
        ViewGroup.LayoutParams newParams = patchContainer.getLayoutParams();
        newParams.width = newSize;
        newParams.height = newSize;

        patchContainer.setLayoutParams(newParams);
    }
}
