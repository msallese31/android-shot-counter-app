package com.projects.sallese.shotcounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.projects.sallese.shotcounter.LogHelper.logSensorLevel;


public class TabbedActivity extends AppCompatActivity {

    private TextView mTextCount;
    private TextView mTextHistory;
//    private TextView tvDeviceNotConnected;
    private GoogleApiClient googleApiClient;
    private String nodeId;
    final String url ="http://35.227.124.115:8080/count";
    TabLayout tl;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (UserSession.GetCount() == null){
                        setUserSessionCount();
                    }
                    mTextCount.setText(UserSession.GetCount().toString());
                    updateWatchCount();
                    return true;
                case R.id.navigation_dashboard:
                    mTextCount.setText(R.string.value_history);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);
        this.setTitle("Shot Counter");

        mTextCount = (TextView) findViewById(R.id.count);
        mTextHistory = (TextView) findViewById(R.id.ComingSoon);

        initGoogleApiClient();

        tl = (TabLayout) findViewById(R.id.tab_layout);
        tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        logSensorLevel("selected tab 0");
                        mTextCount.setVisibility(View.VISIBLE);
                        mTextHistory.setVisibility(View.INVISIBLE);
                        break;

                    case 1:
                        logSensorLevel("selected tab 1");
                        mTextCount.setVisibility(View.INVISIBLE);
                        mTextHistory.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        logSensorLevel("reselected tab 0");
                        mTextCount.setVisibility(View.VISIBLE);
                        break;

                    case 1:
                        logSensorLevel("reselected tab 1");
                        mTextCount.setVisibility(View.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                logSensorLevel("not doing anything with the tab here yet");
            }
        });
//        tvDeviceNotConnected = (TextView) findViewById(R.id.tvWatchNotConnected);

        // TODO: 3/10/18 Get the users daily shot count and populate it with that value instead
        // remove this if check once we do that
        if (UserSession.GetCount() == null){
            setUserSessionCount();
        }


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (UserSession.GetName() == null){
            // TODO: 3/19/18 We need a way to call our sign in function
            logSensorLevel("Wasn't able to get the user session name in the tabbed activity onStart.  Calling the home activity");
            Intent appRestart = new Intent(this, HomeActivity.class);
            this.startActivity(appRestart);
            this.finish();
        }else{
            logSensorLevel(UserSession.GetName());
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        logSensorLevel("Received message");
                        int shot_count = intent.getIntExtra(ListenerService.INCREMENT_SHOTS, 0);
                        Log.d("doubleDebug", "increment called!");
//                        UserSession.IncrementCount(shot_count);
                        mTextCount.setText(UserSession.GetCount().toString());
                        updateWatchCount();
                    }
                }, new IntentFilter(ListenerService.LISTENER_SERVICE_BROADCAST)
        );

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                setUserSessionCount();
            }
        }, 0, 10000);//put here time 1000 milliseconds=1 second
    }

    private void setUserSessionCount(){
        // TODO: 3/11/18 Get actual daily count from DB
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        String countURL = url + "?email=" + UserSession.GetEmail();
        logSensorLevel("URL: " + countURL);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, countURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        logSensorLevel("Get Count Response: " + response);

                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            Integer shots_counted = new Integer(jsonObj.getInt("shots_counted"));
                            logSensorLevel(shots_counted.toString());
                            UserSession.SetCount(shots_counted);
                            mTextCount.setText(UserSession.GetCount().toString());
                            updateWatchCount();
                        }
                        catch (JSONException e) {
                            logSensorLevel("Json exception: " + e.toString());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logSensorLevel("Check error stack");
                error.printStackTrace();
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void updateWatchCount() {
        logSensorLevel("In updateWatchCount");
        logSensorLevel("NodeId: " + nodeId);

//        if (UserSession.GetBluetoothStatus() == false){
//            BluetoothTurnedOff();
//        } else {
//            BluetoothTurnedOn();
//        }


        if (nodeId != null) {
//            UserSession.SetBluetoothStatus(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                        logSensorLevel("Sending message");
                        googleApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
                        logSensorLevel("About to send shot session message!");
                        Wearable.MessageApi.sendMessage(googleApiClient, nodeId, "/update-count", UserSession.GetCount().toString().getBytes());
                        Wearable.MessageApi.sendMessage(googleApiClient, nodeId, "/authenticated", UserSession.GetAuthenticatedStatus().toString().getBytes());
                        logSensorLevel("Should've sent message");
                        // Disabling this because it feels wrong for now
//                        googleApiClient.disconnect();
                }
            }).start();
        }

    }


    private void initGoogleApiClient() {
        googleApiClient = getGoogleApiClient(this);
        retrieveDeviceNode();
    }

//    public void BluetoothTurnedOn(){
//        mTextCount.setVisibility(View.VISIBLE);
//        tvDeviceNotConnected.setVisibility(View.INVISIBLE);
//    }
//
//    public void BluetoothTurnedOff(){
//        mTextCount.setVisibility(View.INVISIBLE);
//        tvDeviceNotConnected.setVisibility(View.VISIBLE);
//    }

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
