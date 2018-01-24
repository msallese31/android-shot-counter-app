package com.projects.sallese.shotcounter;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpPost;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.projects.sallese.shotcounter.LogHelper.logSensorLevel;

public class HomeActivity extends AppCompatActivity {
    String url ="http://192.168.0.179:12345/health";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = new Intent(this, ListenerService.class);
        startService(intent);

        try {
            logSensorLevel("about to make request");
//            new PostRequest().execute();
// handle response here...
        } catch (Exception ex) {
            // handle exception here
            logSensorLevel("Exception!!! " + ex);
        }
    }



}


