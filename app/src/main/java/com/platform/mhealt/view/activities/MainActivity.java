package com.platform.mhealt.view.activities;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.platform.mhealt.MqttService.MqttHelperService;
import com.platform.mhealt.MqttService.MqttIntentService;
import com.platform.mhealt.MqttService.ToolHelper;
import com.platform.mhealt.R;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private SensorManager sensorManager;
    private Sensor acceleratorSensor;

    private TextView txvTimer;
    private String timeString = "300";
    private long timeLeft = 300;

    private TextView txvAvg;
    private TextView txvAvgTitle;
    private String avgString;

    private LineChart lineChart;
    private Thread chartThread;
    private CountDownTimer timerThread;
    private boolean plotData = true;

    public static float average = 0;
    public static float sum = 0;
    public static float count = 0;


    private float lastMovementAverage;

    private Button btnBack;
    private Button btnStart;
    private boolean isPausedInCall;
    private boolean isPausedInExit;

    private ProgressBar prg;
    private TextView veryLow;
    private TextView low;
    private TextView high;
    private TextView veryHigh;

    private TextView txvLastMeas;


    private String lastAvgMovementString;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get all refrences from XML
        txvTimer = findViewById(R.id.txvTimerID);
        txvAvg = findViewById(R.id.txvAvgID);
        txvAvgTitle = findViewById(R.id.txvAvgTitleID);
        lineChart = (LineChart) findViewById(R.id.chartID);
        btnBack = findViewById(R.id.btnBackID);
        btnStart = findViewById(R.id.btnStartID);
        prg = (ProgressBar) findViewById(R.id.prgID);
        veryLow = findViewById(R.id.veryLowID);
        low = findViewById(R.id.lowID);
        high = findViewById(R.id.highID);
        veryHigh = findViewById(R.id.veryHighID);
        txvLastMeas = findViewById(R.id.txvLastMeasID);

        // event listeners
        btnBack.setOnClickListener(this);
        btnStart.setOnClickListener(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        acceleratorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(false);
        lineChart.setBackgroundColor(getResources().getColor(R.color.colorBackground));

        LineData data = new LineData();
        data.setValueTextColor(getResources().getColor(R.color.transparentWhite));
        lineChart.setData(data);

        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextColor(getResources().getColor(R.color.transparentWhite));

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.transparentWhite));
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(2.2f);
        leftAxis.setAxisMinimum(-2.2f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.setDrawBorders(true);
    }

    // thread that responsible to broadcast the chart continuously
    private void feedMultiple() {
        if (chartThread != null){
            chartThread.interrupt();
        }

        chartThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while(timeLeft > 0){
                    plotData = true;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        chartThread.start();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        isPausedInExit = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        sensorManager.unregisterListener(MainActivity.this);
        chartThread.interrupt();
        timerThread.cancel();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences("Corrida previa", MODE_PRIVATE);
        lastMovementAverage = sharedPreferences.getFloat("Promedio de su ultima corrida: ", 0);

        // start listen to sensor
        sensorManager.registerListener(this, acceleratorSensor, SensorManager.SENSOR_DELAY_GAME);

        if(isPausedInCall || isPausedInExit)
            countDown();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        SharedPreferences sharedPreferences = getSharedPreferences("Corrida previa", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("last movement measurement", average);
        editor.commit();

        isPausedInExit=true;

        //stop the thread
        if (chartThread!=null)
            chartThread.interrupt();

        if (timerThread!=null)
            timerThread.cancel();

        // disable sensor
        sensorManager.unregisterListener(this);
    }

    // thread that responsible for timer and display indices data
    private void countDown()
    {
        timerThread = new CountDownTimer(timeLeft*1000, 1000)
        {
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished / 1000;
                timeString = String.format("%03d", timeLeft);
                txvTimer.setText(timeString);
            }

            public void onFinish() {
                timeLeft = 0;
                txvTimer.setVisibility(View.INVISIBLE);

                average = sum/count;
                txvAvg.setVisibility(View.VISIBLE);
                avgString = String.format("%.5f",average);
                txvAvg.setText(avgString);
                txvAvgTitle.setVisibility(View.VISIBLE);

                if(average <= 4)
                {
                    prg.setVisibility(View.VISIBLE);
                    prg.getProgressDrawable().setColorFilter(getResources().getColor(R.color.progressBarGreen), android.graphics.PorterDuff.Mode.SRC_IN);
                    prg.setProgress(25);
                    veryLow.setVisibility(View.VISIBLE);
                }

                else if(average > 4 && average <= 10) {
                    prg.setVisibility(View.VISIBLE);
                    prg.getProgressDrawable().setColorFilter(getResources().getColor(R.color.progressBarYellow), android.graphics.PorterDuff.Mode.SRC_IN);
                    prg.setProgress(50);
                    low.setVisibility(View.VISIBLE);
                }

                else if(average > 10 && average < 20) {
                    prg.setVisibility(View.VISIBLE);
                    prg.getProgressDrawable().setColorFilter(getResources().getColor(R.color.progressBarOrange), android.graphics.PorterDuff.Mode.SRC_IN);
                    prg.setProgress(75);
                    high.setVisibility(View.VISIBLE);
                }

                else {
                    prg.setVisibility(View.VISIBLE);
                    prg.getProgressDrawable().setColorFilter(getResources().getColor(R.color.progressBarRed), android.graphics.PorterDuff.Mode.SRC_IN);
                    prg.setProgress(100);
                    veryHigh.setVisibility(View.VISIBLE);
                }
            }
        }.start();
    }

    // add entry to chart - all entries are movements in x-axis
    private void addEntry(float event)
    {
        LineData data = lineChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), event), 0);
            data.notifyDataChanged();

            sum+=Math.abs(event);
            count=set.getEntryCount();

            lineChart.notifyDataSetChanged();

            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(150);

            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());
        }

            lastAvgMovementString = String.format("%.5f",lastMovementAverage);
            txvLastMeas.setText("Ultimo registro " + lastAvgMovementString);

    }

    // create the chart
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Intensidad");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(getResources().getColor(R.color.orange));
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    float x, y, z;
    float currentX,currentY, currentZ;
    float mPassX,mPassY, mPassZ;

    @Override
    public void onSensorChanged(SensorEvent event) {

        x =event.values[0];
        y =event.values[0];
        z =event.values[0];

        mPassX = highPass(x, currentX, mPassX );
        currentX = x;

        mPassY = highPass(x, currentY, mPassY );
        currentY = y;

        mPassZ = highPass(z, currentZ, mPassZ );
        currentZ = z;

        float acc = (mPassX+mPassY+mPassZ)/3;



        if(plotData){
            addEntry(acc);
            plotData = false;
        }
    }

    private float highPass(float current, float last, float filtered){
        return 0.2f * (filtered + current -last);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnStartID:
                btnStart.setVisibility(View.INVISIBLE);
                countDown();
                feedMultiple();
                break;

            case R.id.btnBackID:
                publicar();
                Intent intent = new Intent(view.getContext(), MainActivity.class);

                startActivity(intent);
                break;
        }
    }

    private void initMqttService(String action) {
        String topic = "v1/devices/me/telemetry";
        int qos = 0;
        int delay = 0;
        int size = (int)Double.parseDouble(txvAvg.getText().toString());
        String topic2 = " ACtividad promedio mxseg ";

        Intent intent = new Intent(MainActivity.this, MqttHelperService.class);
        intent.putExtra(MqttIntentService.TOPIC, topic2);
        intent.putExtra(MqttIntentService.QOS, qos);
        intent.putExtra(MqttIntentService.DELAY, delay);
        intent.putExtra(MqttIntentService.DATA, size);
        intent.setAction(action);
        startService(intent);
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }*/
    }

    public void publicar(){
        try {

            initMqttService(MqttIntentService.ACTION_PUBLISH);
            String datetime2 = ToolHelper.getDateTime();
            ToolHelper.setPublishBegin(getApplicationContext(), datetime2);
            //txtAction.setText("Published at: " + datetime2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
