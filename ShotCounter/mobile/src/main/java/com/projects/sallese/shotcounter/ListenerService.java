package com.projects.sallese.shotcounter;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.EditText;

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

import static com.projects.sallese.shotcounter.LogHelper.logSensorLevel;

/**
 * Created by sallese on 11/25/17.
 */

public class ListenerService extends WearableListenerService implements MessageApi.MessageListener {

    private static final String DATA_INTERVAL_PATH = "/shot-session";
    public static final String LISTENER_SERVICE_BROADCAST = ListenerService.class.getName() + "LocationBroadcast",
                INCREMENT_SHOTS = "update_count";
    JSONArray sessionList = new JSONArray();
    // TODO: 3/10/18 Put this in a global config
    String url ="http://35.227.124.115:8080/count";
    String email = UserSession.GetEmail();


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
//        logSensorLevel(idToken);
        logSensorLevel("Message received!!");
        if (messageEvent.getPath().equals(DATA_INTERVAL_PATH)) {
                try {
                    JSONObject incomingJson = new JSONObject(new String(messageEvent.getData()));
                    incomingJson = incomingJson.put("email", email);
                    logSensorLevel("incoming json: " + incomingJson);
                    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                    HttpPost request = new HttpPost(url);
                    StringEntity params = new StringEntity(incomingJson.toString());
                    request.addHeader("content-type", "application/json");
                    request.setEntity(params);
                    HttpResponse response = httpClient.execute(request);
                    ResponseHandler<String> handler = new BasicResponseHandler();
                    String body = handler.handleResponse(response);

                    JSONObject jsonResponse = new JSONObject(body);

                    logSensorLevel(body);
                    Integer shotsCounted = Integer.valueOf(jsonResponse.get("shots_counted").toString());
                    logSensorLevel(shotsCounted.toString());
                    incrementCount(shotsCounted);

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

//    private class PostRequest extends AsyncTask<Void, Void, String> {
//
//        private Exception exception;
//
//        protected void onPostExecute() {
//            // TODO: check this.exception
//            // TODO: do something with the feed
//        }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            try {
//                logSensorLevel("1");
//                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//                HttpGet request = new HttpGet(url);
//                CloseableHttpResponse resp = httpClient.execute(request);
//                HttpEntity entity = resp.getEntity();
//                StringBuilder sb = new StringBuilder();
//                try {
//                    BufferedReader reader =
//                            new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
//                    String line = null;
//
//                    while ((line = reader.readLine()) != null) {
//                        sb.append(line);
//                    }
//                    logSensorLevel(sb.toString());
//                }
//                catch (IOException e) { logSensorLevel(e.toString()); }
//                catch (Exception e) { logSensorLevel(e.toString()); }
//
//
//                System.out.println("finalResult " + sb.toString());
//            } catch (Exception e) {
//                this.exception = e;
//                logSensorLevel("exception in postrequest: " + e);
//                return null;
//            }
//            return null;
//        }
//    }
}
