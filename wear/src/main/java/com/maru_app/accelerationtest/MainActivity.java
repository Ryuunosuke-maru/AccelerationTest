package com.maru_app.accelerationtest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends Activity implements SensorEventListener {

    private final String TAG = MainActivity.class.getName();
    private final float GAIN = 0.9f;
    private final String[] SEND_MESSAGES = {"/Action/NONE", "/Action/PUNCH", "/Action/UPPER", "/Action/HOOK"};

    private TextView mTextView;
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private String mNode;
    private float x,y,z;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle){
                        /*Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                                if(nodes.getNodes().size() > 0){
                                    mNode = nodes.getNodes().get(0).getId();
                                }
                            }
                        });*/

                        TextView textView3 =(TextView) findViewById(R.id.textView3);
                        textView3.setText("onConnectionSuccess");
                    }

                    @Override
                    public void onConnectionSuspended(int cause){

                    }
                })

                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult){
                        Log.d(TAG,"onConnectionFailed   :   " + connectionResult.toString());
                        TextView textView3 =(TextView) findViewById(R.id.textView3);
                        textView3.setText("onConnectionFailed");
                    }
                })

                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume(){
        super.onResume();

        Sensor sensor =mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);

        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = (x * GAIN + event.values[0] * (1 - GAIN));
            y = (y * GAIN + event.values[1] * (1 - GAIN));
            z = (z * GAIN + event.values[2] * (1 - GAIN));
            TextView textView = (TextView)findViewById(R.id.textView);
            textView.setText(String.format("X:%f\nY:%f\nZ:%f\n",x,y,z));
            final int motion;

            motion = detectMotion(x,y,z);
            if(motion>0 /*&& mNode != null*/) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                        for(Node node :nodes.getNodes()){
                            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient,node.getId(),SEND_MESSAGES[motion],null).await();
                            TextView textView3 = (TextView) findViewById(R.id.textView3);
                            if (!result.getStatus().isSuccess()) {
                                Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
                                textView3.setText("ERROR : failed to send Message");
                            }else{
                                textView3.setText("success");
                            }
                        }
                    }
                }).start();

                /*Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, SEND_MESSAGES[motion], null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
                            TextView textView3 = (TextView) findViewById(R.id.textView3);
                            textView3.setText("ERROR : failed to send Message");
                        }
                    }
                });*/
            }
        }
    }

    float ox,oy,oz;
    int delay;
    private int detectMotion(float x,float y, float z){
        int diffX = (int)((x - ox)*10);
        int diffY = (int)((y - oy)*10);
        int diffZ = (int)((z - oz)*10);
        int motion = 0;
        TextView textView2 = (TextView)findViewById(R.id.textView2);
        if (Math.abs(diffZ) > 20) {
            textView2.setText("upper!");
            motion = 2;
            delay = 4;
            Log.d(TAG,"upper!");
        } else if (Math.abs(diffY) > 20) {
            textView2.setText("hook!");
            motion = 3;
            delay = 4;
            Log.d(TAG,"hook!");
        } else if (diffX > 10) {
            if (delay == 0) {
                textView2.setText("punch!");
                motion = 1;
                Log.d(TAG,"punch!");
            }
        }
        if (delay > 0) delay--;
        ox = x;
        oy = y;
        oz = z;
        return motion;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}

