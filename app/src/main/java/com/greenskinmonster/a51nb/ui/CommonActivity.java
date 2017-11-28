package com.greenskinmonster.a51nb.ui;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;

import com.greenskinmonster.a51nb.R;

/**
 * Created by GreenSkinMonster on 2017-08-09.
 */

public class CommonActivity extends SwipeBaseActivity {

    public static final String FRAGMENT_KEY = "FRAGMENT_KEY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post);
        mRootView = findViewById(R.id.main_activity_root_view);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle arguments = getIntent().getExtras();

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.main_frame_container) == null
                && arguments.containsKey(FRAGMENT_KEY)) {
            String fragmentValue = arguments.getString(FRAGMENT_KEY);
            if (WarrantyFragment.class.getName().equals(fragmentValue)) {
                WarrantyFragment fragment = new WarrantyFragment();
                fragment.setArguments(arguments);
                fragmentManager.beginTransaction()
                        .add(R.id.main_frame_container, fragment).commit();
            }
        }
    }

}
