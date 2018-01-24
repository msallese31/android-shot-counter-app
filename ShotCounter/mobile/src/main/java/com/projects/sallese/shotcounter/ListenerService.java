package com.projects.sallese.shotcounter;

import android.os.AsyncTask;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
    JSONArray sessionList = new JSONArray();
    String url ="http://192.168.0.179:12345/health";


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        logSensorLevel("Message received!!");
        if (messageEvent.getPath().equals(DATA_INTERVAL_PATH)) {
                try {
                    JSONObject incomingJson = new JSONObject(new String(messageEvent.getData()));
                    logSensorLevel("incoming json: " + incomingJson);
                    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                    HttpPost request = new HttpPost(url);
                    StringEntity params = new StringEntity(incomingJson.toString());
                        request.addHeader("content-type", "application/json");
                        request.setEntity(params);
                        httpClient.execute(request);
                }catch (Exception e){
                    logSensorLevel("Exception: " + e);
                }
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
}
