package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
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
import java.util.LinkedList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class MainActivity extends AppCompatActivity {

    private boolean displayFlag;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private final int sensorDelay = 50;
    private boolean stopAndExport=true;
    private int fileCounter=0;
    public String[] activities={"walking","jogging","sitting","standing","upstairs","biking","downstairs"};


    LinkedList<Float[]> readings = new LinkedList<Float[]>();
    LinkedList<Float[]> accelerometerReadings = new LinkedList<>();
    private SensorListener accelerometerListener;
    LinkedList<Float[]> magnetometerReadings = new LinkedList<>();
    private SensorListener magnetometerListener;
    LinkedList<Float[]> linearAcclReadings = new LinkedList<>();
    private SensorListener linearAcclListener;
    LinkedList<Float[]> gyroscopeReadings = new LinkedList<>();
    private SensorListener gyroscopeListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);





        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null)
            finish();
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer == null)
            finish();
        Sensor linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (linearAccelerometer == null)
            finish();
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope == null)
            finish();

        CyclicBarrier barrier = new CyclicBarrier(5);


        accelerometerListener = new SensorListener(accelerometerReadings, new SensorDelayer(barrier));
        sensorManager.registerListener(accelerometerListener,accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        new Thread(accelerometerListener.delayer).start();

        magnetometerListener = new SensorListener(magnetometerReadings, new SensorDelayer(barrier));;
        sensorManager.registerListener(magnetometerListener, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        new Thread(magnetometerListener.delayer).start();

        linearAcclListener = new SensorListener(linearAcclReadings, new SensorDelayer(barrier));;
        sensorManager.registerListener(linearAcclListener, linearAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        new Thread(linearAcclListener.delayer).start();

        gyroscopeListener = new SensorListener(gyroscopeReadings, new SensorDelayer(barrier));;
        sensorManager.registerListener(gyroscopeListener, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        new Thread(gyroscopeListener.delayer).start();

        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        //end aldo code



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





    private class SensorListener implements SensorEventListener{
        SensorDelayer delayer;
        LinkedList<Float[]> storeReadings;

        public SensorListener(LinkedList<Float[]> storingList , SensorDelayer delayer){
            this.delayer = delayer;
            storeReadings = storingList;
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (delayer.displayFlag && !stopAndExport){
                float [] value = sensorEvent.values;
                storeReadings.add(new Float[]{value[0],value[1],value[2]});
                delayer.displayFlag = false;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) { }
    }



    private class SensorDelayer implements Runnable{
        boolean displayFlag = true;
        private CyclicBarrier barrier;

        public SensorDelayer(CyclicBarrier barrier){
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                barrier.await();
            } catch (BrokenBarrierException | InterruptedException e) {
                e.printStackTrace();
            }
            while(true){
                displayFlag = true;
                try {
                    Thread.sleep(sensorDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }










    public void volleyPost(int index){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url="http://130.89.182.169:5000/handler";
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


}

