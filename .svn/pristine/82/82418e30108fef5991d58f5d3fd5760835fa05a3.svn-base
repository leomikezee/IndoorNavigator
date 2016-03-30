package ca.uwaterloo.lab4_204_46;

import java.util.ArrayList;
import java.util.Locale;

import android.graphics.PointF;
import android.view.View;
import android.view.View.OnClickListener;

public class MyOnClickListener implements OnClickListener {
	MainActivity main;
	String buttonName;
	
	public MyOnClickListener(MainActivity main, String button) {
		this.main = main;
		buttonName = button;
	}

	@Override
	public void onClick(View v) {
		if (buttonName == "Start/Pause") {
			// Rtart or pause the state machine
			if (main.start) {
				main.start = false;
				main.startButton.setText("Start");
			} else {
				main.start = true;
				main.startButton.setText("Pause");
			}
		}
		else if (buttonName == "Reset") {
			// Reset all the data
			main.count = 0; main.startTime = 0L; main.endTime = 0L;
			main.steps[0] = 0; main.steps[1] = 0; main.start = false;
			
			main.startButton.setText("Start");
					
			main.mapView.setUserPoint(main.mapView.getStartPoint());
			ArrayList<PointF> path = RouteFinder.findPath(main.mapView);
            main.angleDiff = (path.size() > 1) ?
                				RouteFinder.calculateAngle(main.mapView, path.get(1)) :
                				RouteFinder.calculateAngle(main.mapView, main.mapView.getEndPoint());
			
			String s = String.format(Locale.getDefault(),
									"Steps: %d | North: %.2f | East: %.2f",
									main.count, main.steps[0], main.steps[1]);
			main.textViewOfNavigation.setText(s);
		}
	}
}
