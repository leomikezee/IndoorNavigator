package ca.uwaterloo.lab4_204_46;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import mapper.IMapperListener;
import mapper.MapLoader;
import mapper.Mapper;
import mapper.PedometerMap;

public class MainActivity extends Activity implements IMapperListener {

	TextView textViewOfNavigation; TextView textViewOfDirection; Mapper mapView;
	int next = 0; int count = 0; int angle = 0; int angleDiff = 0;
	float[] steps = { 0, 0 };
	Long startTime = 0L; Long endTime = 0L;
	Button startButton; Button resetButton;
	boolean start = false; boolean first = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Setup Layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.main);
		layout.setVisibility(View.VISIBLE);
		layout.setOrientation(LinearLayout.VERTICAL);

		// Setup Title
		TextView textViewOfTitle = (TextView) findViewById(R.id.title);
		textViewOfTitle.setTextSize(20);
		textViewOfTitle.setText("Navigation");
		
		// Setup Map
		mapView = new Mapper(getApplicationContext(), 900, 900, 35, 35);
		MapLoader mapLoader = new MapLoader();
		PedometerMap map = mapLoader.loadMap(getExternalFilesDir(null), "map.svg");
		mapView.setMap(map);
		mapView.addListener(this);
		layout.addView(mapView);
		registerForContextMenu(mapView);
		
     	// Setup Direction information
     	textViewOfDirection = new TextView(getApplicationContext());
     	layout.addView(textViewOfDirection);
     	String s = String.format(Locale.getDefault(), "Compass: %d - Go This Direction: %d", angle, angle); 
     	textViewOfDirection.setTextColor(Color.BLACK);
     	textViewOfDirection.setText(s);

		// Setup Navigation information
		textViewOfNavigation = new TextView(getApplicationContext());
		layout.addView(textViewOfNavigation);
		s = String.format(Locale.getDefault(),
						  "Steps: %d | North: %.2f | East: %.2f",
						  count, steps[0], steps[1]);
		textViewOfNavigation.setTextColor(Color.BLACK);
		textViewOfNavigation.setText(s);
		
		// Setup Start/Pause Button
		startButton = new Button(getApplicationContext()); layout.addView(startButton);
		startButton.setTextSize(14); startButton.setText("Start");
		startButton.setOnClickListener(new MyOnClickListener(this, "Start/Pause"));
		
		// Setup Reset Button
		resetButton = new Button(getApplicationContext()); layout.addView(resetButton);
		resetButton.setTextSize(14); resetButton.setText("Reset");
		resetButton.setOnClickListener(new MyOnClickListener(this, "Reset"));
		
		// Setup Sensors
		SensorManager sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
		Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		Sensor accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		MySensorEventListener mySensorEventListener = new MySensorEventListener(this);
		sensorManager.registerListener(mySensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(mySensorEventListener, accelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(mySensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);

	}
	
	@Override
	public void onCreateContextMenu(android.view.ContextMenu menu, View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		mapView.onCreateContextMenu(menu, v, menuInfo);
	};
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item) || mapView.onContextItemSelected(item);
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void locationChanged(Mapper source, PointF loc) {
		source.setUserPoint(loc);
	}
	
	@Override
	public void DestinationChanged(Mapper source, PointF dest) {
		ArrayList<PointF> path = RouteFinder.findPath(source);
		path.remove(0); path.add(0, source.getStartPoint());
		source.setUserPath(path);
		angleDiff = (path.size() > 1) ?
						RouteFinder.calculateAngle(source, path.get(1)) :
						RouteFinder.calculateAngle(source, source.getEndPoint());
	}
}