package com.projects.sallese.shotcounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.projects.sallese.shotcounter.LogHelper.logSensorLevel;

public class WearHomeActivity extends WearableActivity {

    private TextView mTextView;
    private Button startServiceButton;
    String url ="http://192.168.0.179:12345/health";
    private GoogleApiClient googleApiClient;
    private String nodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_home);
        logSensorLevel("Hello from Wear Home activity");
        mTextView = (TextView) findViewById(R.id.text);
        startServiceButton = (Button) findViewById(R.id.startService);

        LocalBroadcastManager.getInstance(this).registerReceiver(serviceDataReceiver,
                new IntentFilter("send-service-data"));

        initGoogleApiClient();


        // Enables Always-on
        setAmbientEnabled();
        try {
            logSensorLevel("about to make request");
//            new PostRequest().execute();
// handle response here...
        } catch (Exception ex) {
            // handle exception here
            logSensorLevel("Exception!!! " + ex);
        }
    }

    public void recordData (View v){
        logSensorLevel("Button Push!");
        if (startServiceButton.getText().equals("Start Service")){
            logSensorLevel("Text matched");
            startServiceButton.setText("Stop");
            startService(new Intent(this, DataCollectionService.class));
            return;
        }
        stopService(new Intent(this, DataCollectionService.class));
        startServiceButton.setText("Start Service");
    }
    private BroadcastReceiver serviceDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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
                    Wearable.MessageApi.sendMessage(googleApiClient, nodeId, "/shot-session", setJson.toString().getBytes());
                    googleApiClient.disconnect();
                }
            }).start();
        }
    }

    private class PostRequest extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPostExecute() {
            // TODO: check this.exception
            // TODO: do something with the feed
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                logSensorLevel("1");
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet request = new HttpGet(url);
                CloseableHttpResponse resp = httpClient.execute(request);
                HttpEntity entity = resp.getEntity();
                StringBuilder sb = new StringBuilder();
                try {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    logSensorLevel(sb.toString());
                }
                catch (IOException e) { logSensorLevel(e.toString()); }
                catch (Exception e) { logSensorLevel(e.toString()); }


                System.out.println("finalResult " + sb.toString());
            } catch (Exception e) {
                this.exception = e;
                logSensorLevel("exception in postrequest: " + e);
                return null;
            }
            return null;
        }
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
