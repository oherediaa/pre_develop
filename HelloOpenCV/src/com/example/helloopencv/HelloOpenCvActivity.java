package com.example.helloopencv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgproc.Imgproc;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.res.AssetManager;
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
	String DATA_PATH= "/sdcard/Tesseract/";
	String lang= "eng";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		
		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH
						+ "tessdata/" + lang + ".traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				//while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				//gin.close();
				out.close();
				
				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
			}
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helloopencvlayout);
		
		display = (TextView) findViewById(R.id.display);
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
	public void detect_plates(String imagePath) {        //checkout local branch and merge remote branch
		inputFrame = BitmapFactory.decodeFile(imagePath);
		Bitmap cannybmp = inputFrame;
		Mat image = new Mat(inputFrame.getWidth(), inputFrame.getHeight(),
				CvType.CV_8UC1);
		Mat image_canny = new Mat(inputFrame.getWidth(), inputFrame.getHeight(),
				CvType.CV_8UC1);
		Utils.bitmapToMat(inputFrame, image);
		//--Preprocessing-----------------------------------------------------------------------
		Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(image, image, 100, 255, Imgproc.THRESH_BINARY_INV);
		Core.convertScaleAbs(image, image, 10, 0); //este img es el usado para tesseract
		Imgproc.Canny(image, image_canny, 66, 90); // canny funcional
		//hasta aqui se obtiene los bordes delineados , debajo inicia la identificacion de rectangulo
//		Imgproc.threshold(image_canny, image_canny, 100, 255, Imgproc.THRESH_BINARY);
		Utils.matToBitmap(image_canny, cannybmp);
		
		int val_max = band_clipping2(image_canny); // crea bandbmp
		
		//saveMat(image, "1test");
		saveMat(image_canny, "1test");
		
		TessBaseAPI baseApi = new TessBaseAPI();
		// DATA_PATH = Path to the storage
		// lang = for which the language data exists, usually "eng"
		baseApi.init(DATA_PATH, lang);
		// Eg. baseApi.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata", "eng");
		Bitmap inputTess = BitmapFactory.decodeFile("/sdcard/TestVideo/threshold_clipped2.png");
		baseApi.setImage(inputTess);
		String recognizedText = baseApi.getUTF8Text();
		baseApi.end();
		display.setText(Integer.toString(cannybmp.getPixel(100,120)));
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
	
	int band_clipping2(Mat image_canny){
		int max_val=0;
		Mat vert_proj=new Mat(inputFrame.getWidth(), 1,CvType.CV_8UC1);
		Mat maxx=new Mat(1, 1,CvType.CV_8UC1);
		
		Core.reduce(image_canny, vert_proj, 0, Core.REDUCE_SUM, CvType.CV_8UC1);
		Core.reduce(image_canny, maxx, 0, Core.REDUCE_MAX, CvType.CV_8UC1);
		
		for (int i = 0; i< 25; i++) {
		    double[] histValues = vert_proj.get(i, 0);
		    for (int j = 0; j < histValues.length; j++) {
		        Log.d(TAG, "yourData=" + histValues[j]);
		    }
		}
		return max_val;
	}
	
	//--
	int band_clipping(Bitmap cannybmp){
		int [][] cannyarray=getBinary(cannybmp); //creates int [][] imgBin
		int lar = cannyarray.length;
	    int alt = cannyarray[0].length;
		Bitmap bandbmp;
		int[][] band_array;
		
		int[] vproj = fProjectionH(cannyarray);
		int vproj_max=0;
		//hallar maximo
		for(int i = 0; i < alt; i++)
        {
            if(vproj[i] > vproj_max){vproj_max=vproj[i];}
        }
		//limite superior
		int lim_sup=0,lim_inf=alt;
		for(int i = 0; i < alt; i++)
        {
            if(vproj[i] == vproj_max){lim_sup = i; i =alt;}
        }
		//limite inferior
		for(int i = 0; i < alt; i++)
        {
            if(vproj[alt-i-1] >= vproj_max*0.75){lim_inf = alt-i-1; i=alt;}
        }
        
        //cambiar:
//        bandbmp = Bitmap.createBitmap(band_array, cannybmp.getWidth(), cannybmp.getHeight(), Bitmap.Config.ARGB_8888);
		return cannybmp.getPixel(100,100);
	}
	
	public static int[] fProjectionH(int img[][])
	{
	    int lar = img.length;
	    int alt = img[0].length;
	 
	    int vproj[] = new int[alt];
	    for(int j = 0; j < alt; j++)
	    {
	        for(int i = 0; i < lar; i++)
	        {
	            if(img[i][j] == 1){vproj[j] += 1;}
	        }
	    }
	    return vproj;
	}
	
	private int[][] getBinary(Bitmap bmp)
	{
	    int w = bmp.getWidth();
	    int h = bmp.getHeight();
	    int rgb = 0;
	    int[][] imgBin = new int[w][h];
	 
	    for(int i = 0; i < w; i++)
	    {
	        for(int j = 0; j < h; j++)
	        {
	            rgb = bmp.getPixel(i, j);
	            imgBin[i][j] = rgb != 0 ? 1 : 0;
	        }
	    }
	    return imgBin;
	}
}



























