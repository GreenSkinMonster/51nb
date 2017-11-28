package com.greenskinmonster.a51nb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;

import com.greenskinmonster.a51nb.R;


/**
 * Created by GreenSkinMonster on 2017-11-07.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final FragmentArgs args = FragmentUtils.parse(getIntent());
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainFrameActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(SplashActivity.this, 0, 0);
                ActivityCompat.startActivity(SplashActivity.this, intent,
                        options.toBundle());

                if (args != null) {
                    args.setSkipEnterAnim(true);
                    FragmentUtils.show(SplashActivity.this, args);
                }
                finish();
            }

        }, args != null ? 300 : 700);
    }

}
