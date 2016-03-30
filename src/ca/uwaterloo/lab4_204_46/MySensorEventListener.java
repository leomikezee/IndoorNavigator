package ca.uwaterloo.lab4_204_46;

import java.util.ArrayList;
import java.util.Locale;

import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Gravity;
import android.widget.Toast;

public class MySensorEventListener implements SensorEventListener {
	MainActivity main;

    // Constants
    private int C = 10;
    private float lowThresold = 0.75f;
    private float highThresold = 3.25f;
    private float G = 9.81f;
    private float stepLength = 1.5f;

    private int state = 0;
    private float currentAcceleration = 0;
    private float previousAcceleration = 0;
    private float[] linearAccReadings = { 0, 0, 0 };
    private float[] factors = new float[3];
    private float[] Rotation = new float[9];
    private float[] rotationReadings = { 0, 0, 0 };
    private float[] orientation = { 0, 0, 0 };
    
    public MySensorEventListener(MainActivity main) {
    	this.main = main;
	}

    private float[] lowPassFilter(float[] input, float[] output) {
        for (int i = 0; i < 3; i++) {
            output[i] += (input[i] - output[i]) / C;
        }
        return output;
    }

    private float lowPassFilter(float input, float output) {
        output += (input - output) / C;
        return output;
    }

    private boolean speedUp() {
        return currentAcceleration > previousAcceleration;
    }

    private boolean slowDown() {
        return currentAcceleration < previousAcceleration;
    }

    private boolean underThresold() {
        return Math.abs(currentAcceleration) < lowThresold;
    }

    private boolean overThresold() {
        return Math.abs(currentAcceleration) > highThresold;
    }

    private boolean inThresold() {
        return !underThresold() && !overThresold();
    }

    private void changeState() {
        switch (state) {
            case 0:
                state = speedUp() && underThresold() ? 1 : 0;
                break;
            case 1:
                state = inThresold() ? 2 : 1;
                state = overThresold() ? -1 : state;
                break;
            case 2:
                main.startTime = System.currentTimeMillis();
                state = slowDown() && underThresold() ? 3 : 2;
                break;
            case 3:
                main.startTime = (main.startTime + System.currentTimeMillis()) / 2;
                state = speedUp() && underThresold() ? 4 : 3;
                break;
            case 4:
                state = inThresold() ? 5 : 4;
                state = overThresold() ? -1 : state;
                break;
            case 5:
                main.endTime = System.currentTimeMillis();
                state = slowDown() && underThresold() ? 6 : 5;
                break;
            case 6:
                main.endTime = (main.endTime + System.currentTimeMillis()) / 2;
                float timeDiff = ((float) (main.endTime - main.startTime)) / 1000f;
                state = (timeDiff >= 0.075 && timeDiff <= 0.75) ? 7 : -1;
                break;
            case 7:
                state = 0;
                main.count++;
                main.steps[0] += Math.cos(Math.toRadians(main.angle));
                main.steps[1] += Math.sin(Math.toRadians(main.angle));

                PointF newPos = new PointF(main.mapView.getUserPoint().x +
                						   (float) Math.sin(Math.toRadians(main.angle)) * stepLength,
                						   main.mapView.getUserPoint().y -
                						   (float) Math.cos(Math.toRadians(main.angle)) * stepLength);
                if (main.mapView.calculateIntersections(main.mapView.getUserPoint(), newPos).size() != 0) {
                	newPos = main.mapView.getUserPoint();
                }
                main.mapView.setUserPoint(newPos);

                if (RouteFinder.checkArrive(newPos, main.mapView.getEndPoint())) {
                	Toast toast = Toast.makeText(main.getApplicationContext(), "Arrived!", Toast.LENGTH_LONG);
                	toast.setGravity(Gravity.CENTER, 0, 0);
                	toast.show();
                }

                ArrayList<PointF> path = RouteFinder.findPath(main.mapView);
                main.angleDiff = (path.size() > 1) ?
                					RouteFinder.calculateAngle(main.mapView, path.get(1)) :
                					RouteFinder.calculateAngle(main.mapView, main.mapView.getEndPoint());
                break;
            default:
                state = 0;
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent se) {
        switch (se.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                for (int i = 0; i < 3; i++) {
                    factors[i] = se.values[i] / G;
                }
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                float[] verticalAccelerations = new float[3];
                linearAccReadings = lowPassFilter(se.values, linearAccReadings);
                for (int i = 0; i < 3; i++) {
                    verticalAccelerations[i] = linearAccReadings[i] * factors[i];
                }
                currentAcceleration = Math.abs(verticalAccelerations[0] +
                							   verticalAccelerations[1] +
                							   verticalAccelerations[2]);
                currentAcceleration = lowPassFilter(currentAcceleration, previousAcceleration);
                if (main.start) {
                    changeState();
                }
                previousAcceleration = currentAcceleration;
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                rotationReadings = se.values;
                break;
        }

        SensorManager.getRotationMatrixFromVector(Rotation, rotationReadings);
        orientation = SensorManager.getOrientation(Rotation, orientation);
        main.angle = (int) (Math.toDegrees(orientation[0]) + 360) % 360;
        String s = String.format(Locale.getDefault(),
        						 "Steps: %d | North: %.2f | East: %.2f",
        						 main.count, main.steps[0], main.steps[1]);
        main.textViewOfNavigation.setText(s);

     	s = String.format(Locale.getDefault(), "Compass: %d - Go This Direction: %d", main.angle, main.angleDiff); 
     	main.textViewOfDirection.setText(s);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}