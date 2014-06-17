package com.example.helloopencv;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HelloOpenCvActivity extends Activity {

	protected static final String TAG = null;
	Button ThresholdBtn;
	Bitmap inputFrame;
	TextView display;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helloopencvlayout);
		
		display = (TextView) findViewById(R.id.textView1);

		ThresholdBtn = (Button) findViewById(R.id.button1);
		ThresholdBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				detect_plates("/sdcard/TestVideo/33.jpg");
			}
		});
	}

	// ---AsyncInitialization------------------------------------------------------------------------------------------
	// private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {

				Log.i(TAG, "OpenCV loaded successfully");
				// mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this,
				mLoaderCallback);
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	public void onDestroy() {
		super.onDestroy();
	}

	// ---------------------------------------------------------------------------------------------
	public void detect_plates(String imagePath) { // checkout local branch and
													// merge remote branch
		inputFrame = BitmapFactory.decodeFile(imagePath);
		Mat image = new Mat(inputFrame.getWidth(), inputFrame.getHeight(),
				CvType.CV_8UC1);
		Utils.bitmapToMat(inputFrame, image);
		// --Preprocessing-----------------------------------------------------------------------
		Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(image, image, 100, 255, Imgproc.THRESH_BINARY_INV);
		Core.convertScaleAbs(image, image, 10, 0);

		Imgproc.Canny(image, image, 66, 90); // canny funcional
		// hasta aqui se obtiene los bordes delineados , debajo inicia la
		// identificacion de rectangulo y se pierde los bordes internos de los
		// caracteres
		Scalar contour_color = new Scalar(255, 255, 0, 255);
		Mat mHierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<RotatedRect> out_rects = new ArrayList<RotatedRect>();
		Imgproc.findContours(image, contours, mHierarchy,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		Imgproc.drawContours(image, contours, -1, contour_color);
		double area_max=0;
		Iterator<MatOfPoint> itr = contours.iterator();
		while(itr.hasNext()){
			MatOfPoint2f tmp2f = new MatOfPoint2f(itr.next().toArray());
			RotatedRect rect1=Imgproc.minAreaRect(tmp2f);
			double area1=rect1.size.width * rect1.size.height;
			
			if(area1>area_max){
				area_max=area1;
			}
		}
		
		// --
		saveMat(image, "1test");
	}

	boolean VerifySize(RotatedRect rr) {
		// Log("rr is w %f, h %f\n", rr.size.width, rr.size.height);
		float error = 0.4f;
		float aspect = 4.8f;
		float max_area = 120 * 120 * aspect;
		float min_area = 15 * 15 * aspect;

		float min_rate = aspect - aspect * error;
		float max_rate = aspect + aspect * error;
		double area = rr.size.width * rr.size.height;
		float rate = (float) rr.size.width / (float) rr.size.height;
		if (rate < 1)
			rate = 1.0f / rate;
		if (area > max_area || area < min_area || rate < min_rate
				|| rate > max_rate)
			return false;
		return true;
	}

	// --
	void saveMat(Mat imagetosave, String namepng) {
		Utils.matToBitmap(imagetosave, inputFrame);
		File file = new File("/sdcard/TestVideo/" + namepng + ".png");
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);
			inputFrame.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}