package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity {

    private boolean displayFlag;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private final int sensorDelay = 50;
    private boolean stopAndExport=true;
    private int fileCounter=0;

    LinkedList<Float[]> readings = new LinkedList<Float[]>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TinyWebServer.startServer("localhost",9000, "/web/public_html");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        new Thread(sensorDelayer).start();

        //the start button starts the recording by setting the stopAndExport to false
        Button startButton = (Button) findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            stopAndExport=false;
            }
        });

        //the stop button sets the stopAndExport boolean to false, therby stopping the recording and creating a csv
        Button stopButton = (Button) findViewById(R.id.button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            stopAndExport=true;
            createCSV();
            }
        });
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
    @Override
    public void onDestroy(){
        super.onDestroy();
        //stop webserver on destroy of service or process
        TinyWebServer.stopServer();
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