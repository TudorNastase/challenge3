package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;


public class MainActivity extends AppCompatActivity {

    private static final int SAMPLE_SIZE = 50;
    private boolean displayFlag;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private final int sensorDelay = 20;
    private boolean stopAndExport=false;
    private int fileCounter=0;
    //final TextView textView = (TextView) findViewById(R.id.textView);
    public String[] activities={"walking","jogging","sitting","standing","upstairs","biking","downstairs"};
    public Classifier classifier;
    boolean sw=false;


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


        //print model
        InputStream in = getResources().openRawResource(R.raw.rpocket_no_mag);
//        try {
//            //RandomTree rf = (RandomTree) (new ObjectInputStream(in)).readObject();
//            //System.out.println(rf);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        AssetManager assetManager = getAssets();
        try {
            classifier = (Classifier) (new ObjectInputStream(in)).readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // Weka "catch'em all!"
            e.printStackTrace();
        }
        Toast.makeText(this, "Model loaded.", Toast.LENGTH_SHORT).show();





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
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                DoStuff doStuff= new DoStuff();
                doStuff.start();

            }
        });

        //the stop button sets the stopAndExport boolean to false, therby stopping the recording and creating a csv
        Button stopButton = (Button) findViewById(R.id.button2);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                if (!sw)
//                    sw=true;
//                else sw=false;


                final Attribute accelerometerX = new Attribute("accelerometerX");
                final Attribute accelerometerY = new Attribute("accelerometerY");
                final Attribute accelerometerZ = new Attribute("accelerometerZ");
//                final Attribute magnetometerX = new Attribute("magnetometerX");
//                final Attribute magnetometerY = new Attribute("magnetometerY");
//                final Attribute magnetometerZ = new Attribute("magnetometerZ");
                final Attribute gyroscopeX = new Attribute("gyroscopeX");
                final Attribute gyroscopeY = new Attribute("gyroscopeY");
                final Attribute gyroscopeZ = new Attribute("gyroscopeZ");
                final Attribute linearX = new Attribute("linearX");
                final Attribute linearY = new Attribute("linearY");
                final Attribute linearZ = new Attribute("linearZ");
                final List<String> classes = new ArrayList<String>() {
                    {
                        add("walking"); // cls nr 1
                        add("standing"); // cls nr 2
                        add("jogging"); // cls nr 3
                        add("sitting"); // cls nr 4
                        add("biking"); // cls nr 5
                        add("upstairs"); // cls nr 6
                        add("downstairs"); // cls nr 7
                    }
                };
                ArrayList<Attribute> attributeList = new ArrayList<Attribute>(2) {
                    {
                        add(accelerometerX);
                        add(accelerometerY);
                        add(accelerometerZ);
                        add(linearX);
                        add(linearY);
                        add(linearZ);
                        add(gyroscopeX);
                        add(gyroscopeY);
                        add(gyroscopeZ);
//                        add(magnetometerX);
//                        add(magnetometerY);
//                        add(magnetometerZ);

                        Attribute attributeClass = new Attribute("@@class@@", classes);
                        add(attributeClass);


                    }
                };
                Instances dataUnpredicted = new Instances("TestInstances",
                        attributeList, 1);
                dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);
                DenseInstance newInstance = new DenseInstance(dataUnpredicted.numAttributes()) {
                    {
                        int acc=accelerometerReadings.size()-1;
                        int lin=linearAcclReadings.size()-1;
                        int gyro=gyroscopeReadings.size()-1;
                        int mag=magnetometerReadings.size()-1;
                        setValue(accelerometerX,accelerometerReadings.get(acc)[0]);
                        setValue(accelerometerY,accelerometerReadings.get(acc)[1]);
                        setValue(accelerometerZ,accelerometerReadings.get(acc)[2]);
                        setValue(linearX,linearAcclReadings.get(lin)[0]);
                        setValue(linearY,linearAcclReadings.get(lin)[1]);
                        setValue(linearZ,linearAcclReadings.get(lin)[2]);
                        setValue(gyroscopeX,gyroscopeReadings.get(gyro)[0]);
                        setValue(gyroscopeY,gyroscopeReadings.get(gyro)[1]);
                        setValue(gyroscopeZ,gyroscopeReadings.get(gyro)[2]);
//                        setValue(magnetometerX,magnetometerReadings.get(mag)[0]);
//                        setValue(magnetometerY,magnetometerReadings.get(mag)[1]);
//                        setValue(magnetometerZ,magnetometerReadings.get(mag)[2]);
                    }
                };
                newInstance.setDataset(dataUnpredicted);
                try {
                    double result = classifier.classifyInstance(newInstance);
                    String className = classes.get(new Double(result).intValue());
                    String msg = className ;
                    System.out.println(msg);
                    volleyPost(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
//            if(!accelerometerReadings.equals(null)) {
//
//            }
//                for(int i = 0; i < accelerometerReadings.size(); i++)
//                    System.out.println( accelerometerReadings.get(i)[2] );
//            }
//            else{
//                System.out.println("accelerometer list emty");
//            }


            }
        });




    }

    public String mostFrequent(ArrayList<String> arr){
        HashMap<String,Integer> map=new HashMap<String,Integer>();
        map.put("walking",0);
        map.put("biking",0);
        map.put("sitting",0);
        map.put("upstairs",0);
        map.put("downstairs",0);
        map.put("jogging",0);
        map.put("standing",0);
        map.put("nothing",0);
        String mostFrequentKey=null;
        if (arr.size()>=SAMPLE_SIZE) {

            for (String k:arr) {
                int val = map.get(k);
                map.replace(k, val + 1);
            }

            int maxi =0;

            for (Map.Entry<String,Integer>entry:map.entrySet()){
                if(entry.getValue()>maxi){
                    maxi=entry.getValue();
                    mostFrequentKey=entry.getKey();
                }
            }

        }
        return mostFrequentKey;
    }




    private class SensorListener implements SensorEventListener {
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
                    Thread.sleep(50);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }










    public void volleyPost(String message){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url="http://130.89.179.228:5000/handler";
        JSONObject postData = new JSONObject();
        try {
            postData.put("activity",message );

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

public String guess(){
    final Attribute accelerometerX = new Attribute("accelerometerX");
    final Attribute accelerometerY = new Attribute("accelerometerY");
    final Attribute accelerometerZ = new Attribute("accelerometerZ");
    final Attribute magnetometerX = new Attribute("magnetometerX");
    final Attribute magnetometerY = new Attribute("magnetometerY");
    final Attribute magnetometerZ = new Attribute("magnetometerZ");
    final Attribute gyroscopeX = new Attribute("gyroscopeX");
    final Attribute gyroscopeY = new Attribute("gyroscopeY");
    final Attribute gyroscopeZ = new Attribute("gyroscopeZ");
    final Attribute linearX = new Attribute("linearX");
    final Attribute linearY = new Attribute("linearY");
    final Attribute linearZ = new Attribute("linearZ");
    final List<String> classes = new ArrayList<String>() {
        {
            add("walking"); // cls nr 1
            add("standing"); // cls nr 2
            add("jogging"); // cls nr 3
            add("sitting"); // cls nr 4
            add("biking"); // cls nr 5
            add("upstairs"); // cls nr 6
            add("downstairs"); // cls nr 7
        }
    };
    ArrayList<Attribute> attributeList = new ArrayList<Attribute>(2) {
        {
            add(accelerometerX);
            add(accelerometerY);
            add(accelerometerZ);
            add(linearX);
            add(linearY);
            add(linearZ);
            add(gyroscopeX);
            add(gyroscopeY);
            add(gyroscopeZ);
            add(magnetometerX);
            add(magnetometerY);
            add(magnetometerZ);

            Attribute attributeClass = new Attribute("@@class@@", classes);
            add(attributeClass);


        }
    };
    Instances dataUnpredicted = new Instances("TestInstances",
            attributeList, 1);
    dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);
    DenseInstance newInstance = new DenseInstance(dataUnpredicted.numAttributes()) {
        {
            int acc=accelerometerReadings.size()-1;
            int lin=linearAcclReadings.size()-1;
            int gyro=gyroscopeReadings.size()-1;
            int mag=magnetometerReadings.size()-1;
            setValue(accelerometerX,accelerometerReadings.get(acc)[0]);
            setValue(accelerometerY,accelerometerReadings.get(acc)[1]);
            setValue(accelerometerZ,accelerometerReadings.get(acc)[2]);
            setValue(linearX,linearAcclReadings.get(lin)[0]);
            setValue(linearY,linearAcclReadings.get(lin)[1]);
            setValue(linearZ,linearAcclReadings.get(lin)[2]);
            setValue(gyroscopeX,gyroscopeReadings.get(gyro)[0]);
            setValue(gyroscopeY,gyroscopeReadings.get(gyro)[1]);
            setValue(gyroscopeZ,gyroscopeReadings.get(gyro)[2]);
            setValue(magnetometerX,magnetometerReadings.get(mag)[0]);
            setValue(magnetometerY,magnetometerReadings.get(mag)[1]);
            setValue(magnetometerZ,magnetometerReadings.get(mag)[2]);
        }
    };
    newInstance.setDataset(dataUnpredicted);
    try {
        double result = classifier.classifyInstance(newInstance);
        String className = classes.get(new Double(result).intValue());

        return className;

    } catch (Exception e) {
        e.printStackTrace();
    }
    return "nothing";
}
public class DoStuff extends Thread {

        public ArrayList<String> readingsG= new ArrayList<String>();


    @Override
    public void run() {
        ArrayList<String> readings=new ArrayList<String>();
        while(true){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String reading=guess();
            readings.add(reading);
            String postMessage=mostFrequent(readings);
            if(postMessage!=(null)){
                volleyPost(postMessage);
                readings=new ArrayList<String>();
            }

        }

    }
}
}

