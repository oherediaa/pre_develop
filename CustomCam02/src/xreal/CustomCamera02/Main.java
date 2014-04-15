package xreal.CustomCamera02;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.os.Build;

public class Main extends Activity 
{
	private Button recorderbtn;//button for recorder
	private float xAccel;
	private float yAccel;
	private float zAccel;
	private float xPreviousAccel;
	private float yPreviousAccel;
	private float zPreviousAccel;

	private boolean firstUpdate = true;
	private final float shakeThreshold = 0.9f;
	private boolean shakeInitiated = false;	
	private SensorManager mySensorManager;
	boolean startedRecording=false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		recorderbtn = (Button)findViewById(R.id.recorder);
		//start the VideoRecorder with accelerometer
		mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); 
		mySensorManager.registerListener(mySensorEventListener, mySensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);

	    
		//start the VideoRecorder on the click of the button 
	   recorderbtn.setOnClickListener(new View.OnClickListener(){
		  	public void onClick(View view){
		  		startedRecording=true;
		  		Intent intent = new Intent(Main.this, Recorder.class);
		  		startActivity(intent);
		  	  }
	       });	
}
	
	private final SensorEventListener mySensorEventListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
	    
        	if(startedRecording==false){
        	updateAccelParameters(se.values[0], se.values[1], se.values[2]);  
        	if ((!shakeInitiated) && isAccelerationChanged()) {                                       
        		shakeInitiated = true; 
        	} else if ((shakeInitiated) && isAccelerationChanged()) {                              
    	    executeShakeAction();
        	} else if ((shakeInitiated) && (!isAccelerationChanged())) {                           
        		shakeInitiated = false;
        	}
        	}
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
	    	       }
    	};

    private void updateAccelParameters(float xNewAccel, float yNewAccel,
			float zNewAccel) {
        
		if (firstUpdate) {  
			xPreviousAccel = xNewAccel;
			yPreviousAccel = yNewAccel;
			zPreviousAccel = zNewAccel;
			firstUpdate = false;
		} else {
			xPreviousAccel = xAccel;
			yPreviousAccel = yAccel;
			zPreviousAccel = zAccel;
		}
		xAccel = xNewAccel;
		yAccel = yNewAccel;
		zAccel = zNewAccel;
	}
    private boolean isAccelerationChanged() {
		float deltaX = Math.abs(xPreviousAccel - xAccel);
		float deltaY = Math.abs(yPreviousAccel - yAccel);
		float deltaZ = Math.abs(zPreviousAccel - zAccel);
		return (deltaX > shakeThreshold && deltaY > shakeThreshold)
				|| (deltaX > shakeThreshold && deltaZ > shakeThreshold)
				|| (deltaY > shakeThreshold && deltaZ > shakeThreshold);
		
	}
    private void executeShakeAction() {
    	if(startedRecording==false){
    	startedRecording=true;
    	Intent intent = new Intent(Main.this, Recorder.class);
  	    startActivity(intent);
    	}
	}
}