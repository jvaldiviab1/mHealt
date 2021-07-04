package com.platform.mhealt.view.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.platform.mhealt.R;
import com.platform.mhealt.service.MusicService;
import com.platform.mhealt.view.activities.ui.SliderAdapter;

public class BaseActivity extends AppCompatActivity {
    private ViewPager slideViewPager;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private SliderAdapter sliderAdapter;
    private Button btnStart;
    private ImageView imgPlay;
    private ImageView imgStop;
    private boolean isPlaying;

    public static int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        slideViewPager = findViewById(R.id.slideViewPagerID);
        dotsLayout = findViewById(R.id.dotsLayoutID);
        btnStart = findViewById(R.id.btnStartID);
        imgPlay = findViewById(R.id.imgPlayID);
        imgStop = findViewById(R.id.imgStopID);

        sliderAdapter = new SliderAdapter(this);
        slideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);

        slideViewPager.addOnPageChangeListener(viewListener);

        // event listeners
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(view.getContext(), MusicService.class));
                imgPlay.setVisibility(View.INVISIBLE);
                imgStop.setVisibility(View.VISIBLE);
            }
        });

        imgStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(view.getContext(), MusicService.class));
                imgStop.setVisibility(View.INVISIBLE);
                imgPlay.setVisibility(View.VISIBLE);
            }
        });
    }

    // create 3 dots to slider
    public void addDotsIndicator(int position){
        dots = new TextView[1];
        dotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.gray));

            dotsLayout.addView(dots[i]);
        }

        if(dots.length > 0){
            dots[position].setTextColor(getResources().getColor(R.color.green));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            currentPage = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    private void showExitDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(R.drawable.ic_exit);
        alertDialog.setTitle("Exit App");
        alertDialog.setMessage("Do you really want to exit?");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //Toast.makeText(SplashActivity.this, "Bye Bye!", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //        Toast.makeText(SplashActivity.this, "NO", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentPage = 0;

        // display an icon depending on the state of the music
        SharedPreferences sharedPreferences = getSharedPreferences("music", MODE_PRIVATE);
        isPlaying = sharedPreferences.getBoolean("isPlaying", false);

        if(isPlaying) {
            imgPlay.setVisibility(View.INVISIBLE);
            imgStop.setVisibility(View.VISIBLE);
        }
        else{
            imgPlay.setVisibility(View.VISIBLE);
            imgStop.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // save music state
        SharedPreferences sharedPreferences = getSharedPreferences("music", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(MusicService.isRunning)
            editor.putBoolean("isPlaying", true);
        else
            editor.putBoolean("isPlaying", false);
        editor.commit();
    }
}
