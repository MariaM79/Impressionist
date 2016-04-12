package app.impressionist;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorListener implements SensorEventListener {
    Context context;
    Sensor sensor;
    private SensorManager sensorManager;

    public SensorListener(Context con) {
        context = con;
        sensorManager = (SensorManager) con.getSystemService(con.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onSensorChanged(SensorEvent p1) {
    }

    @Override
    public void onAccuracyChanged(Sensor p1, int p2) {
    }

    public void onResumeRoutine() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPauseRoutine() {
        sensorManager.unregisterListener(this);
    }

}
