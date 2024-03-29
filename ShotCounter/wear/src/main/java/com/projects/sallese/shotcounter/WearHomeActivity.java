package com.projects.sallese.shotcounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.wearable.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.projects.sallese.shotcounter.LogHelper.logSensorLevel;

public class WearHomeActivity extends WearableActivity {

    private TextView mTextViewCount;
    private Button startServiceButton;
    String url ="http://192.168.0.179:12345/health";
    private GoogleApiClient googleApiClient;
    private String nodeId;
    private Handler timerHandler;
    private int intervalTime = 5000;
    Boolean authenticated;
    Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_home);
        logSensorLevel("Hello from Wear Home activity");
        mTextViewCount = (TextView) findViewById(R.id.textViewCount);
        // WHY did this disappear??
        startServiceButton = (Button) findViewById(R.id.startService);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

//        Log.d("doubleDebug", "Register");
//        LocalBroadcastManager.getInstance(this).registerReceiver(serviceDataReceiver,
//                new IntentFilter("send-service-data"));

        initGoogleApiClient();

        Log.d("doubleDebug", "A");

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.d("doubleDebug", "B");
                        logSensorLevel("Received message");
                        Integer shot_count = intent.getIntExtra(ListenerService.INCREMENT_SHOTS, 0);
                        mTextViewCount.setText(shot_count.toString());
                    }
                }, new IntentFilter(ListenerService.LISTENER_SERVICE_BROADCAST)
        );

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        // Enables Always-on
//        setAmbientEnabled();

//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                timerAction();
//            }
//        }, 0, 10000);//put here time 1000 milliseconds=1 second
        timerHandler = new Handler();
//        timerHandler.postDelayed(runnable, intervalTime);

    }



    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */
            Log.d("doubleDebug", "C");
            timerAction();
      /* and here comes the "trick" */
            timerHandler.postDelayed(this, intervalTime);
        }
    };


    //    @Override
//    public void onEnterAmbient(Bundle ambientDetails) {
////        timerHandler.removeCallbacks(runnable);
//        super.onEnterAmbient(ambientDetails);
//        // Handle entering ambient mode
//    }

//    @Override
//    public void onExitAmbient() {
////        timerHandler = new Handler();
////        timerHandler.postDelayed(runnable, intervalTime);
//        super.onExitAmbient();
//        // Handle entering ambient mode
//    }

//    @Override
//    public void onUpdateAmbient() {
//        // TODO: 3/24/18 Right now, onUpdateAmbient is called when the time changes (lol so every minute).  This works, but could we just have our
//        // timer call this method instead?
////        timerAction();
//        super.onUpdateAmbient();
//        // Handle entering ambient mode
//    }

    public void timerAction() {
        Log.d("doubleDebug", "D");
        // Question: How much data do we lose in the stop service/start service cycle
        // Seems like there's room for improvement there
        // TODO: 3/24/18 Evaluate that loss ^^^
        Date currentTime = Calendar.getInstance().getTime();
        Log.d("doubleDebug", "timer action called at " + currentTime);
        logSensorLevel("timerAction() called at " + currentTime);
        if (startServiceButton.getText().equals("Stop")){
            v.vibrate(1000);
            stopService(new Intent(this, DataCollectionService.class));
            startService(new Intent(this, DataCollectionService.class));
        } else if (startServiceButton.getText().equals("Start Shooting")){

        }
    }

    public void recordData (View view){
        logSensorLevel("Button Push!");
        if (startServiceButton.getText().equals("Start Shooting")){
            logSensorLevel("Text matched");
            startServiceButton.setText("Stop");
            if(mTextViewCount.getText().toString() == "TextView"){
                // TODO: 3/24/18 This should be set from the DB... not 0 
                mTextViewCount.setText("0");
            }
            mTextViewCount.setVisibility(View.VISIBLE);

            startService(new Intent(this, DataCollectionService.class));
            timerHandler.postDelayed(runnable, intervalTime);
            return;
        } else {
            v.vibrate(1000);
            timerHandler.removeCallbacks(runnable);
            stopService(new Intent(this, DataCollectionService.class));
        }
//        stopService(new Intent(this, DataCollectionService.class));
        startServiceButton.setText("Start Shooting");
    }

    private BroadcastReceiver serviceDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("doubleDebug", "E");
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            logSensorLevel("Got message: " + message);
            ArrayList<Float> xAcc = (ArrayList<Float>) intent.getSerializableExtra("xAcc");
            logSensorLevel("Got xAcc: \n" + xAcc);
            ArrayList<Float> yAcc = (ArrayList<Float>) intent.getSerializableExtra("yAcc");
            ArrayList<Float> zAcc = (ArrayList<Float>) intent.getSerializableExtra("zAcc");
//            workoutObject.setAccel(xAcc, yAcc, zAcc);
            JSONArray xAccJson = new JSONArray(xAcc);
            JSONArray yAccJson = new JSONArray(yAcc);
            JSONArray zAccJson = new JSONArray(zAcc);


            try {
                JSONObject shotSessionJsonObject = new JSONObject();
                shotSessionJsonObject.put("X_Acc", xAccJson);
                shotSessionJsonObject.put("Y_Acc", yAccJson);
                shotSessionJsonObject.put("Z_Acc", zAccJson);
                logSensorLevel("Shot Session json object: " + shotSessionJsonObject);
                logSensorLevel(shotSessionJsonObject.toString());
                sendMessage(shotSessionJsonObject);
            }catch (JSONException e){
                logSensorLevel("Json Exception!" + e);
            }


        }
    };

    private void sendMessage(final JSONObject setJson) {
        Log.d("doubleDebug", "F");
        Log.d("doubleDebug", "Sending message");
        logSensorLevel("In sendMessage");
        logSensorLevel("NodeId: " + nodeId);
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    logSensorLevel("Sending message");
                    googleApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
//                    JSONObject setJson = new JSONObject();
//                    try {
//                        setJson.put("Name", "hello");
//                    }catch (JSONException je){
//                        logSensorLevel("json exception: " + je);
//                    }
                    logSensorLevel("About to send shot session message!");
                    Wearable.MessageApi.sendMessage(googleApiClient, nodeId, "/shot-session", setJson.toString().getBytes());
                    googleApiClient.disconnect();
                }
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        v.cancel();
        Log.d("doubleDebug", "Unregister");
        unregisterReceiver(serviceDataReceiver);
        timerHandler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        v.cancel();
        Log.d("doubleDebug", "Unregister");
        unregisterReceiver(serviceDataReceiver);
        timerHandler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    protected void onStop() {
        v.cancel();
        Log.d("doubleDebug", "Unregister");
        unregisterReceiver(serviceDataReceiver);
//        timerHandler.removeCallbacks(runnable);
        super.onStop();
    }

    @Override
    protected void onResume() {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        Log.d("doubleDebug", "Register");
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceDataReceiver,
                new IntentFilter("send-service-data"));
//        timerHandler.postDelayed(runnable, intervalTime);
        super.onResume();
    }

    @Override
    protected void onRestart() {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // not sure if this is right
        Log.d("doubleDebug", "Register");
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceDataReceiver,
                new IntentFilter("send-service-data"));
//        timerHandler.postDelayed(runnable, intervalTime);
        super.onRestart();
    }

    private void initGoogleApiClient() {
        googleApiClient = getGoogleApiClient(this);
        retrieveDeviceNode();
    }

    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private void retrieveDeviceNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (googleApiClient != null && !(googleApiClient.isConnected() || googleApiClient.isConnecting()))
                    googleApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);

                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

                List<Node> nodes = result.getNodes();

                if (nodes.size() > 0)
                    nodeId = nodes.get(0).getId();

                logSensorLevel("Node ID of phone: " + nodeId);

                googleApiClient.disconnect();
            }
        }).start();
    }
}
