package com.github.rpatch.demo;

import com.github.rpatch.RPatch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

// User: jnye
// Date: 2/2/13
// Time: 9:31 AM
// Copyright (c) 2013 WillowTree Apps, Inc. All rights reserved.

public class MainActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        View container = findViewById(R.id.main_demo_container);

        RPatch test = new RPatch(this, R.drawable.test_rpatch);
        test.setRepeatFlags(RPatch.REPEAT_INNER_BOTH);
        container.setBackground(test);
    }
}