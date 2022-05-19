package com.example.sensors;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView textView;
    TextView nerede;
    TextView currentAcc;
    TextView prevAcc;
    TextView changeAcc;
    SensorManager sensorManager;
    Sensor lightSensor;
    Sensor accelerometer;
    Toast toast;
    boolean hareket=false;
    double changeAcceleration;
    double lightLevel;
    private double accelerationCurrentValue;
    private double accelerationPrevValue;
    private SensorEvent sensorEvent;
    BroadcastReceiver broadcastReceiver;
    private boolean sensorRegistered = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nerede = (TextView) findViewById(R.id.nerede);
        textView = (TextView) findViewById(R.id.textView);
        currentAcc = (TextView) findViewById(R.id.currentAcc);
        prevAcc = (TextView) findViewById(R.id.prevAcc);
        changeAcc = (TextView) findViewById(R.id.changeAcc);
        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorRegistered = true;
    }
    private int hitCount = 0;
    private double hitSum = 0;
    private double hitResult = 0;

    private final int SAMPLE_SIZE = 50;
    private final double THRESHOLD = 0.2;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            accelerationCurrentValue = Math.sqrt(x*x + y*y + z*z);
            changeAcceleration = Math.abs(accelerationCurrentValue - accelerationPrevValue);
            accelerationPrevValue = accelerationCurrentValue;

            currentAcc.setText("Current: " + accelerationCurrentValue);
            prevAcc.setText("Previous: " + accelerationPrevValue);
            changeAcc.setText("Change: " + changeAcceleration);
            if (hitCount <= SAMPLE_SIZE) {
                hitCount++;
                hitSum += Math.abs(changeAcceleration);
            } else {
                hitResult = hitSum / SAMPLE_SIZE;

                Log.d("tag", String.valueOf(hitResult));

                if (hitResult > THRESHOLD) {
                    Log.d("tag", "Walking");
                    hareket=true;
                } else {
                    Log.d("tag", "Stop Walking");
                    hareket=false;

                }

                hitCount = 0;
                hitSum = 0;
                hitResult = 0;
            }
        }

        if(sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
            textView.setText("IŞIK SEVİYESİ" + sensorEvent.values[0]);
            lightLevel=sensorEvent.values[0];
        }
        Intent intent = new Intent("atilgan.urfet.SENSOR_ACTION");
        if( lightLevel<20 && hareket==true){
            nerede.setText("telefon cepte ve hareketli");
            intent.putExtra("status", "sesAc");
            Log.i("abcc","telefon cepte"+lightLevel+"  "+changeAcceleration );
            //Toast.makeText(getApplicationContext(),"telefon cepte ve hareketli",Toast.LENGTH_LONG).show();
        }else if(lightLevel>=20 && hareket==false){
            nerede.setText("telefon masada");
            intent.putExtra("status", "sesKis");
            Log.i("abc","telefon masada"+lightLevel+"  " +changeAcceleration);
            //Toast.makeText(getApplicationContext(),"telefon masada",Toast.LENGTH_LONG).show();
        }else{
            nerede.setText("telefon cepte ve hareketsiz");
            intent.putExtra("status", "sesKis");
            //Toast.makeText(getApplicationContext(),"telefon cepte ve hareketsiz",Toast.LENGTH_LONG).show();
        }
        sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("atilgan.urfet.SENSOR_ACTION");
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

}