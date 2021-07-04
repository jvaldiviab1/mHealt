package com.platform.mhealt.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.platform.mhealt.R;
import com.platform.mhealt.databinding.ActivitySplashBinding;

import static com.platform.mhealt.util.constants.Constants.DURATION_SPLASH;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_arriba);
        Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_abajo);

        binding.textName.setAnimation(animation2);
        binding.imgLogoView.setAnimation(animation1);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
            }
        }, DURATION_SPLASH);

    }
}