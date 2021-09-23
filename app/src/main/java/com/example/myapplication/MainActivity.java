package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.LinkedList;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;


public class MainActivity extends AppCompatActivity {

    private boolean displayFlag;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private final int sensorDelay = 50;
    private boolean stopAndExport=true;
    private int fileCounter=0;
    //final TextView textView = (TextView) findViewById(R.id.textView);
    public String[] activities={"walking","jogging","sitting","standing","upstairs","biking","downstairs"};
    LinkedList<Float[]> readings = new LinkedList<Float[]>();






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        new Thread(sensorDelayer).start();
        InputStream in = getResources().openRawResource(R.raw.r_pocket_random_tree);
        try {
            RandomTree rf = (RandomTree) (new ObjectInputStream(in)).readObject();
            System.out.println(rf);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        //the start button starts the recording by setting the stopAndExport to false
        Button startButton = (Button) findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //send request
                volleyPost(0);
            }
        });
//
//        //the stop button sets the stopAndExport boolean to false, therby stopping the recording and creating a csv
//        Button stopButton = (Button) findViewById(R.id.button2);
//        stopButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//            stopAndExport=true;
//            createCSV();
//            }
//        });
    }

    private final Runnable sensorDelayer = new Runnable() {
        @Override
        public void run() {
            while(true){
                displayFlag = true;
                try {
                    Thread.sleep(sensorDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void volleyPost(int index){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url="http://130.89.179.70:5000/handler";
        JSONObject postData = new JSONObject();
        try {
            String activity=activities[0];
            postData.put("activity",activity );

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer && !stopAndExport){ // won't do anything while stopAndExport is true
        Float[] arr=new Float[3];
        arr[0]=event.values[0];
        arr[1]=event.values[1];
        arr[2]=event.values[2];
        readings.add(arr);
        //add the
        }
    }
    public void createCSV(){
        //TODO: copy the contents of the readings list to a new csv file.
    }
}