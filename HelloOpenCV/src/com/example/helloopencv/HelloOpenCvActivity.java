package com.example.helloopencv;

import java.io.File;
import java.io.FileOutputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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

public class HelloOpenCvActivity extends Activity {

	protected static final String TAG = null;
	Button ThresholdBtn;
	Bitmap inputFrame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helloopencvlayout);
		
		ThresholdBtn = (Button) findViewById(R.id.button1);
		ThresholdBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				thresholdFrame("/sdcard/TestVideo/33.jpg");
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
	public void thresholdFrame(String imagePath) {
//		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
//				new Size(19, 19));
		Mat closed = new Mat(); // closed will have type CV_32F
		inputFrame = BitmapFactory.decodeFile(imagePath);
		// --
		Mat image = new Mat(inputFrame.getWidth(), inputFrame.getHeight(),
				CvType.CV_8UC1);
		Mat Ximage = new Mat(inputFrame.getWidth(), inputFrame.getHeight(),
				CvType.CV_8UC1);
		Mat Yimage = new Mat(inputFrame.getWidth(), inputFrame.getHeight(),
				CvType.CV_8UC1);
		Utils.bitmapToMat(inputFrame, image);
		Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
		// --
////		Imgproc.morphologyEx(image, closed, Imgproc.MORPH_CLOSE, kernel);
////		Core.divide(image, closed, closed, 1, CvType.CV_32F);
////		Core.normalize(closed, image, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);
////		Imgproc.threshold(image, image, -1, 255, Imgproc.THRESH_BINARY_INV
////				+ Imgproc.THRESH_OTSU);
//		Imgproc.threshold(image, image, 100, 255, Imgproc.THRESH_BINARY_INV);
////		Imgproc.Sobel(image, image, CvType.CV_8UC1, 1, 1);
//		Core.convertScaleAbs(image, image, 10, 0);
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
		Mat temp = new Mat(); 

		Imgproc.resize(image, temp, new Size(image.cols()/4, image.rows()/4));
		Imgproc.morphologyEx(temp, temp, Imgproc.MORPH_CLOSE, kernel);
		Imgproc.resize(temp, temp, new Size(image.cols(), image.rows()));

		Core.divide(image, temp, temp, 1, CvType.CV_32F); // temp will now have type CV_32F
		Core.normalize(temp, image, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);

		Imgproc.threshold(image, image, -1, 255, 
		    Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
		Imgproc.Canny(image, image, 66, 90);
		//-- sumar X e Y
//		Imgproc.Sobel(image, Yimage, CvType.CV_8UC1, 1, 0);
//		Imgproc.Sobel(image, Ximage, CvType.CV_8UC1, 0, 1);
//		Core.addWeighted(Yimage, 1, Ximage, 1, 0, image, -1);
		
		saveMat(image, "1firstOpencv");
	}
		// --
		void saveMat(Mat imagetosave, String namepng){
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