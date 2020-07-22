package com.akb.seetalk;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class SplashActivity extends Activity {

    Animation topAnim, bottomAnim;
    ImageView imageView;
    TextView sikamiceria;

    ProgressBar progress_circular;

    FirebaseAuth auth;
    FirebaseUser user;

    private static int SPLASH_SCREEN = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        topAnim = AnimationUtils.loadAnimation( this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation( this, R.anim.bottom_animation);

        imageView = findViewById(R.id.logo);
        sikamiceria = findViewById(R.id.sikamiceria);
        progress_circular = findViewById(R.id.progress_circular);

        imageView.setAnimation(topAnim);
        sikamiceria.setAnimation(bottomAnim);
        progress_circular.setAnimation(bottomAnim);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(user != null){
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    this.selesai();
                    finish();
                }else{
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    this.selesai();
                    finish();
                }
            }

            private void selesai() {
            }
        },SPLASH_SCREEN);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
