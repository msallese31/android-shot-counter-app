package com.projects.sallese.shotcounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpPost;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.projects.sallese.shotcounter.LogHelper.logSensorLevel;
import com.projects.sallese.shotcounter.UserSession;


public class HomeActivity extends AppCompatActivity {
    // TODO: 2/11/18 Refactor into global config
    String url ="http://192.168.0.179:8080/sign-in";
    private TextView tvShotCount;
    private GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.sign_in_button).setOnClickListener(signInButton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("547984691715-q1tkn5gd9hrn3idq6297j7gltlkj336p.apps.googleusercontent.com")
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signIn();

//        try {
//            logSensorLevel("about to make request");
////            new PostRequest().execute();
//// handle response here...
//        } catch (Exception ex) {
//            // handle exception here
//            logSensorLevel("Exception!!! " + ex);
//        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int shot_count = intent.getIntExtra(ListenerService.INCREMENT_SHOTS, 0);
                    }
                }, new IntentFilter(ListenerService.LISTENER_SERVICE_BROADCAST)
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void signIn(){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            logSensorLevel("already signed in: " + account.getEmail());
            logSensorLevel("token:" +account.getIdToken());
            logSensorLevel("id token: " + account.getIdToken());
            logSensorLevel("oldtoken: eyJhbGciOiJSUzI1NiIsImtpZCI6ImUyNzY3MWQ3M2EyNjA1Y2NkNDU0NDEzYzRjOTRlMjViM2Y2NmNkZWEifQ.eyJhenAiOiI1NDc5ODQ2OTE3MTUtYjVpZnYzOWV2OGEyMHQ1OThsNjVlbmV1ZDN0NG83OWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NDc5ODQ2OTE3MTUtcTF0a241Z2Q5aHJuM2lkcTYyOTdqN2dsdGxrajMzNnAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTM1OTA2NjY5MzI2NTI5MzUyNTIiLCJlbWFpbCI6Im1zYWxsZXNlMzFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImV4cCI6MTUxODQwMTU0MywiaXNzIjoiaHR0cHM6Ly9hY2NvdW50cy5nb29nbGUuY29tIiwiaWF0IjoxNTE4Mzk3OTQzLCJuYW1lIjoiTWlrZSBTYWxsZXNlIiwicGljdHVyZSI6Imh0dHBzOi8vbGg0Lmdvb2dsZXVzZXJjb250ZW50LmNvbS8tU1ptR09mNnNfZncvQUFBQUFBQUFBQUkvQUFBQUFBQUFBQUEvQUNTSUxqWGhReXY2Z0htdkltY0diZEszVEx5a2ZaUVVydy9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoiTWlrZSIsImZhbWlseV9uYW1lIjoiU2FsbGVzZSIsImxvY2FsZSI6ImVuIn0.MQffEJfbs4nqaugUacv3Bh3SIbu0qC2M00InGxXo6oByOXx02b1OWtM8WjSFnzXuQJEEWtAmpvp5FECreyOdP3aVfbDU-kPg0yIw6IfTTFk8tK3fQlMmM_-HS2Nhw8yT9BdoDTCdX4buRkpYZPcb4BvDXOX7If8J2YbhWZnE51uWuIg_aws9vaWtWeeeisM0-26L6xi45C3HJYPIw1luhoZJ-Ot6yVrbUL7MjFk7O_ZQe_TVO7Vb7kkyLKSQZuBJyspXZW-0cNi_z-hed7aT3tTOzSuY_VqWT3oP2AKQmrPZ7-mnUqT6M6NmAS6k1CWOFvwsEIRMdy82ZjH7vESqIQ");
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            logSensorLevel("successfully signed in: " + account.getEmail());
            logSensorLevel("id token: " + account.getIdToken());
            logSensorLevel("oldtoken: eyJhbGciOiJSUzI1NiIsImtpZCI6ImUyNzY3MWQ3M2EyNjA1Y2NkNDU0NDEzYzRjOTRlMjViM2Y2NmNkZWEifQ.eyJhenAiOiI1NDc5ODQ2OTE3MTUtYjVpZnYzOWV2OGEyMHQ1OThsNjVlbmV1ZDN0NG83OWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NDc5ODQ2OTE3MTUtcTF0a241Z2Q5aHJuM2lkcTYyOTdqN2dsdGxrajMzNnAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTM1OTA2NjY5MzI2NTI5MzUyNTIiLCJlbWFpbCI6Im1zYWxsZXNlMzFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImV4cCI6MTUxODQwMTU0MywiaXNzIjoiaHR0cHM6Ly9hY2NvdW50cy5nb29nbGUuY29tIiwiaWF0IjoxNTE4Mzk3OTQzLCJuYW1lIjoiTWlrZSBTYWxsZXNlIiwicGljdHVyZSI6Imh0dHBzOi8vbGg0Lmdvb2dsZXVzZXJjb250ZW50LmNvbS8tU1ptR09mNnNfZncvQUFBQUFBQUFBQUkvQUFBQUFBQUFBQUEvQUNTSUxqWGhReXY2Z0htdkltY0diZEszVEx5a2ZaUVVydy9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoiTWlrZSIsImZhbWlseV9uYW1lIjoiU2FsbGVzZSIsImxvY2FsZSI6ImVuIn0.MQffEJfbs4nqaugUacv3Bh3SIbu0qC2M00InGxXo6oByOXx02b1OWtM8WjSFnzXuQJEEWtAmpvp5FECreyOdP3aVfbDU-kPg0yIw6IfTTFk8tK3fQlMmM_-HS2Nhw8yT9BdoDTCdX4buRkpYZPcb4BvDXOX7If8J2YbhWZnE51uWuIg_aws9vaWtWeeeisM0-26L6xi45C3HJYPIw1luhoZJ-Ot6yVrbUL7MjFk7O_ZQe_TVO7Vb7kkyLKSQZuBJyspXZW-0cNi_z-hed7aT3tTOzSuY_VqWT3oP2AKQmrPZ7-mnUqT6M6NmAS6k1CWOFvwsEIRMdy82ZjH7vESqIQ");
            UserSession.SetIdToken(account.getIdToken());
            UserSession.SetEmail(account.getEmail());
            signIntoBackend();
            // Signed in successfully, show authenticated UI.
            updateUI();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            logSensorLevel("signInResult:failed code=" + e.getStatusCode());
            logSensorLevel(e.toString());
            updateUI();
        }
    }

    private void signIntoBackend() {
        // THIS NEEDS TO NOT BE ON MAIN THREAD
        // https://stackoverflow.com/questions/6343166/how-do-i-fix-android-os-networkonmainthreadexception
            new PostRequest().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        signOut();
        mGoogleSignInClient.signOut();
    }

    private void updateUI() {
        logSensorLevel("ui update event");
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logSensorLevel("Done signing out");
                    }
                });
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
                JSONObject signIntoBackendJson = new JSONObject();
                signIntoBackendJson.put("idToken", UserSession.GetIdToken());
                signIntoBackendJson.put("email", UserSession.GetEmail());
                logSensorLevel("signIntoBackendJson: " + signIntoBackendJson);
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost request = new HttpPost(url);
                StringEntity params = new StringEntity(signIntoBackendJson.toString());
                request.addHeader("content-type", "application/json");
                request.setEntity(params);
                HttpResponse response = httpClient.execute(request);
                logSensorLevel(response.toString());
//                ResponseHandler<String> handler = new BasicResponseHandler();
//                String body = handler.handleResponse(response).getBytes().toString();
                logSensorLevel(response.getStatusLine().toString());

            } catch (Exception e) {
                this.exception = e;
                logSensorLevel("exception in postrequest: " + e);
                return null;
            }
            return null;
        }
    }
}


