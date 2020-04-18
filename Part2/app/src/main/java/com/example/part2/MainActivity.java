package com.example.part2;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;


public class MainActivity extends Activity implements SensorEventListener {

    private GLSurfaceView gLView;

    private SensorManager sensorManager;

    private float[] acceleration = new float[3];
    private float[] velocity = new float[3];
    private float[] gravity = new float[3];
    private static long oldTime = 0;

    private static float[] distance = new float[3];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = new MyGLSurfaceView(this);
        setContentView(gLView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager == null) {
            Log.i("general", "Error creating sensor manager");
        }
        else {
            Log.i("general", "Created sensor manager");
        }

        Arrays.fill(acceleration, 0);
        Arrays.fill(velocity, 0);
        Arrays.fill(gravity, 0);
        Arrays.fill(distance, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.i("general", "Created accelerometer");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.8f;

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        acceleration[0] = event.values[0] - gravity[0];
        acceleration[1] = event.values[1] - gravity[1];
        acceleration[2] = event.values[2] - gravity[2];

        float dtSeconds = (event.timestamp - oldTime)/1_000_000_000.0f;
        oldTime = event.timestamp;

        velocity[0] = velocity[0] + acceleration[0]*dtSeconds;
        velocity[1] = velocity[1] + acceleration[1]*dtSeconds;
        velocity[2] = velocity[2] + acceleration[2]*dtSeconds;

        distance[0] = velocity[0]*dtSeconds;
        distance[1] = velocity[1]*dtSeconds;
        distance[2] = velocity[2]*dtSeconds;

        Log.i("acc", distance[0] + " " + distance[1] + " " + distance[2]);
        Log.i("time", dtSeconds + "");

        if(Math.abs(distance[0]) > 0.001) {
            Log.i("dist", distance[0] + "");
        }
    }

    public static float[] getDistance() {
        return distance;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("acc", accuracy + "");
    }

    static class MyGLSurfaceView extends GLSurfaceView {

        public MyGLSurfaceView(Context context) {
            super(context);

            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);

            MyGLRenderer renderer = new MyGLRenderer();

            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(renderer);
            // Render the view only when there is a change in the drawing data
//            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
    }
}