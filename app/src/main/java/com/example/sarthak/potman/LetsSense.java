package com.example.sarthak.potman;

import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class LetsSense extends AppCompatActivity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private long lastUpdate = 0;
    private float last_z;
    double longitude, latitude;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String url = "http://10.70.26.227:5000/api/v1/potholes";
    OkHttpClient okHttpClient = new OkHttpClient();

    LocationManager lm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lets_sense);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();
            if(Math.abs(z - last_z) > 5) {
                if ((curTime - lastUpdate) > 5000) {
                    System.out.println("POTHOLE" + curTime);
                    try {
                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    } catch (SecurityException ignored) {

                    }


                    Toast.makeText(getApplicationContext(),"Pothole at " + longitude + ", " + latitude,Toast.LENGTH_SHORT).show();
                    try {
                        System.out.println(potholeJson(latitude, longitude));
                        post(potholeJson(latitude, longitude), url);
                    } catch (IOException e) {
                    }
                }
                    lastUpdate = curTime;
            }
            last_z = z;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    void post(String json, String url) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON, json);
        System.out.println(requestBody);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                System.out.println(response.code());
            }
        });

    }

    String potholeJson(double lat, double lon) {

        return "{\"lat\":\"" + lat + "\","
                + "\"long\":\"" + lon + "\","
                + "\"id\":\"" + 12345 + "\""
                +"}";
    }
}
