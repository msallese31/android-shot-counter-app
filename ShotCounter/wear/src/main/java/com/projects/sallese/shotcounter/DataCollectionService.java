package com.projects.sallese.shotcounter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

import static com.projects.sallese.shotcounter.LogHelper.logSensorLevel;

public class DataCollectionService extends Service implements SensorEventListener {

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    public ArrayList<Float> XAcc;
    public ArrayList<Float> YAcc;
    public ArrayList<Float> ZAcc;

    public ArrayList<Float> XGyro;
    public ArrayList<Float> YGyro;
    public ArrayList<Float> ZGyro;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logSensorLevel("Got to start command");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager
                .getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_GAME, new Handler());
//        mSensorManager.registerListener(this, mGyroscope,
//                SensorManager.SENSOR_DELAY_NORMAL, new Handler());
        // TODO: 8/5/17 Research more sensors to leverage
        XAcc = new ArrayList<Float>();
        YAcc = new ArrayList<Float>();
        ZAcc = new ArrayList<Float>();
        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        logSensorLevel("X: " + x + "\nY: " + y + "\nZ:" + z + "\n");
        XAcc.add(x);
        YAcc.add(y);
        ZAcc.add(z);
        // TODO: 8/19/17 Some sort of synchronization..
    }

    @Override
    public void onDestroy() {
        // TODO: 8/5/17 Clean up after sensors (turn off, save data, etc)
        logSensorLevel("Stop service called on destroy method");
        this.mSensorManager.unregisterListener(this);
        sendDataToStartStopActivity();
        super.onDestroy();
    }

    private void sendDataToStartStopActivity(){
        logSensorLevel("Sending data from service after ondestroy was called.");
        Intent intent = new Intent("send-service-data");
        intent.putExtra("message", "Hello from DataCollectionService");
        intent.putExtra("xAcc", XAcc);
        intent.putExtra("yAcc", YAcc);
        intent.putExtra("zAcc", ZAcc);
        // TODO: 8/19/17 GYRO!!!
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        logSensorLevel("Sent broadcast!");
    }
}
