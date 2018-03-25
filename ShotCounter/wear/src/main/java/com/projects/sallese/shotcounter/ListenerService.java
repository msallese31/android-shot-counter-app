package com.projects.sallese.shotcounter;

/**
 * Created by sallese on 3/11/18.
 */

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static com.projects.sallese.shotcounter.LogHelper.logSensorLevel;

/**
 * Created by sallese on 11/25/17.
 */

public class ListenerService extends WearableListenerService implements MessageApi.MessageListener {

    private static final String UPDATE_COUNT_PATH = "/update-count";
    public static final String LISTENER_SERVICE_BROADCAST = ListenerService.class.getName() + "LocationBroadcast",
            INCREMENT_SHOTS = "update_count";
    Integer count;


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
//        logSensorLevel(idToken);
        logSensorLevel("Message received in wearable listener service!!");
        logSensorLevel(messageEvent.getPath());
        if (messageEvent.getPath().equals(UPDATE_COUNT_PATH)) {
            try {
                final Integer count = Integer.valueOf(new String(messageEvent.getData(), Charset.forName("UTF-8")));
                logSensorLevel("recieved count: " + count);
                sendBroadcastMessage(count);

            }catch (Exception e){
                logSensorLevel("Exception: " + e);
            }
        }
    }

    private void incrementCount(Integer count){
        sendBroadcastMessage(count);
    }

    private void sendBroadcastMessage(Integer count) {
        Intent intent = new Intent(LISTENER_SERVICE_BROADCAST);
        intent.putExtra(INCREMENT_SHOTS, count);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}

