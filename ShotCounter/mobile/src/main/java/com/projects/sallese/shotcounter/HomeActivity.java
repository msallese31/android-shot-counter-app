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
import android.widget.Button;
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
import com.projects.sallese.shotcounter.util.IabHelper;
import com.projects.sallese.shotcounter.util.IabResult;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    // TODO: 2/11/18 Refactor into global config
    // Discover service somehow
    String url ="http://35.227.124.115:8080/sign-in";
    private TextView tvShotCount;
    private GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 5;
    IabHelper mHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Set the dimensions of the sign-in button.

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("547984691715-q1tkn5gd9hrn3idq6297j7gltlkj336p.apps.googleusercontent.com")
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // TODO: 3/24/18 DONT HARDCODE THIS
        // See here: https://developer.android.com/training/in-app-billing/preparing-iab-app.html#Connect
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhrzspnsswGL5spEVMlHs1DgkQViPOUeXaE+4vYFzorPhdQBagHnOLnaQkY0DVr4YYFzgP8JUwDzaRobj8UAqRUlkRyh4PxR2AmwnJpCa6VFvGvg79OHNA2xfPdy9puvLWkwW2/I3ML50np9FsAhWjRFNs88sg1if88P3xAA3Wyo9QVRt4wkbOyIwf5t3yxhWnjLgBb7EcRLTOvsUcDlXkYmR6+SG98yj97IL/fKAtj4jxI4YTI2Sp6yWoyQZpzdsZAKNOh6uNFlQErtHgybCs7hTC3pWS7X/4HTyK61XvddeC0xh/SDSg25+iqtO/YqTYqrKck4ChiQpQka++XJwKQIDAQAB";

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    logSensorLevel("Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!
            }
        });

        signIn();
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
            if (UserSession.GetName() == null){
                setupUserSession(account);
            }
            launchCountingActivity();
        }

    }

    @Override
    public void onClick(View v) {
        signIn();
    }

    private void switchAccount() {
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void launchCountingActivity() {
        Intent countingIntent = new Intent(this, TabbedActivity.class);
        this.startActivity(countingIntent);
        this.finish();
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
            logSensorLevel(account.getId());
            logSensorLevel("successfully signed in: " + account.getEmail());
            logSensorLevel("id token: " + account.getIdToken());
            setupUserSession(account);
            signIntoBackend();
            // Signed in successfully, show authenticated UI.
            updateUI();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            logSensorLevel("signInResult:failed code=" + e.getStatusCode());
            logSensorLevel(e.toString());
            // TODO: 3/10/18 Display sign in failure to user
        }
    }

    private void setupUserSession(GoogleSignInAccount account){
        UserSession.SetIdToken(account.getIdToken());
        UserSession.SetEmail(account.getEmail());
        UserSession.SetName(account.getDisplayName());
        logSensorLevel("Successfully setup user session");
    }

    private void signIntoBackend() {
        // THIS NEEDS TO NOT BE ON MAIN THREAD
        // https://stackoverflow.com/questions/6343166/how-do-i-fix-android-os-networkonmainthreadexception
            new PostRequest().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    @Override
    protected void onDestroy() {
        signOut();
        super.onDestroy();
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
                signIntoBackendJson.put("name", UserSession.GetName());
                signIntoBackendJson.put("email", UserSession.GetEmail());
                logSensorLevel("signIntoBackendJson: " + signIntoBackendJson);
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost request = new HttpPost(url);
                StringEntity params = new StringEntity(signIntoBackendJson.toString());
                request.addHeader("content-type", "application/json");
                request.setEntity(params);
                HttpResponse response = httpClient.execute(request);
                logSensorLevel(response.toString());
                logSensorLevel(response.getStatusLine().toString());
                if (response.getStatusLine().getStatusCode() == 200) {
                    launchCountingActivity();
                } else {
                    // TODO: 3/10/18 Display failure to user, try again?
                    logSensorLevel("Non-200 response when signing into backend");
                }
            } catch (Exception e) {
                this.exception = e;
                logSensorLevel("exception in postrequest: " + e);
                // TODO: 3/10/18 Display failure to user
                return null;
            }
            return null;
        }
    }
}


