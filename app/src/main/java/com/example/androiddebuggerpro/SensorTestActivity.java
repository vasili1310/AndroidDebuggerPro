package com.example.androiddebuggerpro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class SensorTestActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Map<Integer, TextView> sensorTextView = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // UI
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(layout);

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        // Show all sensors
        for (Sensor sensor : sensorList) {
            TextView sensorName = new TextView(this);
            sensorName.setText(sensor.getName() + "(Type: " + sensor.getType() + ")");
            layout.addView(sensorName);

            // Only register live ones
            if (isSensorSupported(sensor.getType())) {
                TextView sensorData = new TextView(this);
                sensorData.setText("Waiting for data...");
                layout.addView(sensorData);
                sensorTextView.put(sensor.getType(), sensorData);
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
            }
        }

        setContentView(scrollView);
    }

    private boolean isSensorSupported(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_LIGHT:
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_PROXIMITY:
                return true;
            default:
                return false;
        }
    }

    public void onSensorChanged(SensorEvent event) {
        TextView tv = sensorTextView.get(event.sensor.getType());
        if (tv != null) {
            StringBuilder sb = new StringBuilder("Values: ");
            for (float value : event.values) {
                sb.append(String.format("%.2f", value));
            }
            tv.setText(sb.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re register listeners
        for(Map.Entry<Integer, TextView> entry : sensorTextView.entrySet()) {
            Sensor sensor = sensorManager.getDefaultSensor(entry.getKey());
            if (sensor != null) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
