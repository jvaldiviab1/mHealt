package com.platform.mhealt.service;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.platform.mhealt.R;

public class MusicService extends Service {
    private MediaPlayer player;
    public static boolean isRunning;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        player = MediaPlayer.create(this, R.raw.sound);
        player.setLooping(true);
        player.start();

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                isRunning = true;
                int i = 0;
                while(isRunning)
                {
                    i++;
                    SystemClock.sleep(500);
                }
            }
        }).start();

        return START_STICKY;
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        player.stop();
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
