package com.maru_app.accelerationtest;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,MessageApi.MessageListener {

    private static final String TAG = MainActivity.class.getName();
    private GoogleApiClient mGoogleApiClient;
    private final String[] SEND_MESSAGES = {"/Action/NONE", "/Action/PUNCH", "/Action/UPPER", "/Action/HOOK"};


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient =new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener(){
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult){
                        Log.d(TAG,"onConnectionFailed: " + connectionResult.toString());
                        TextView textView = (TextView)findViewById(R.id.textView);
                        textView.setText("onConnectionFailed");
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(null != mGoogleApiClient && mGoogleApiClient.isConnected()){
            Wearable.MessageApi.removeListener(mGoogleApiClient,this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle){
        Log.d(TAG,"onConnected");
        Wearable.MessageApi.addListener(mGoogleApiClient,this);
    }

    @Override
    public void onConnectionSuspended(int i){
        Log.d(TAG,"onConnectionSuspended");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG,"onMessageReceived : "+ messageEvent.getPath());

        String msg = messageEvent.getPath();
        TextView textView = (TextView)findViewById(R.id.textView);
        if (SEND_MESSAGES[1].equals(msg)) {
            textView.setText("punch!");
        } else if (SEND_MESSAGES[2].equals(msg)) {
            textView.setText("upper!");
        } else if (SEND_MESSAGES[3].equals(msg)) {
            textView.setText("hook!");
        }

    }
}
