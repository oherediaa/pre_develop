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
import android.graphics.drawable.shapes.ArcShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HelloOpenCvActivity extends Activity {

	protected static final String TAG = "debug";
	Button ThresholdBtn;
	Bitmap inputFrame;
	TextView display;
	ImageView imageView1;
	String DATA_PATH = "/sdcard/Tesseract/";
	String lang = "eng";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");

		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata"))
				.exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + lang
						+ ".traineddata");
				// GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/"
						+ lang + ".traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				// while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				// gin.close();
				out.close();

				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG,
						"Was unable to copy " + lang + " traineddata "
								+ e.toString());
			}
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.helloopencvlayout);

		imageView1 = (ImageView) findViewById(R.id.imageView1);
		display = (TextView) findViewById(R.id.display);
		ThresholdBtn = (Button) findViewById(R.id.button1);
		ThresholdBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				MAINFUNCTION("/sdcard/TestVideo/33.jpg");
			}
		});
	}
	
	public void MAINFUNCTION(String imagePath) {
		Mat workHere = threshold1(imagePath);
		workHere = cannyMat(workHere);
		workHere = dilateMat(workHere);
//		List<MatOfPoint> cuadcontour = detectar_cuadrilateros1(workHere);
//		workHere = drawCont(workHere, cuadcontour);
		showonImageView(workHere);
		saveMatBMP(workHere, "1testpofa");// Mat , String //
		//FINAL STEP---------------------------
		// String recognizedText =Tesseract_function("/sdcard/TestVideo/threshold_clipped2.png");
	}
	
	public Mat toGrayMat(String imagePath) {        //checkout local branch and merge remote branch
		inputFrame = BitmapFactory.decodeFile(imagePath);
		Mat imageMat = new Mat(inputFrame.getWidth(), inputFrame.getHeight(),
				CvType.CV_8UC1);
		Utils.bitmapToMat(inputFrame, imageMat);
		Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY);
		return imageMat;
	}
	
	public Mat threshold1(String imagePath) {        //checkout local branch and merge remote branch
		Mat GrayMat=toGrayMat(imagePath);
		Mat outputMat = GrayMat.clone();
		//------------------
		Imgproc.threshold(GrayMat, outputMat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
		//------------------
		saveMatBMP(outputMat, "1test");//sale bien
		return outputMat;
	}
	
	Mat cannyMat(Mat image){
		Core.convertScaleAbs(image, image, 10, 0);
		Imgproc.Canny(image, image, 66, 90);
		return image;
	}
	
	Mat dilateMat(Mat cannyMat){
		float dilation_size = 1.0f;
		Point point = new Point(dilation_size, dilation_size);
//		Imgproc.dilate(cannyMat, cannyMat, kernel, anchor, iterations);dilate(cannyMat, cannyMat, Mat(), point);
		org.opencv.core.Size s = new Size(2 * dilation_size + 1, 2 * dilation_size + 1);
		Mat element = Imgproc.getStructuringElement(2, s, point);     // dilation_type = MORPH_ELLIPSE
		Imgproc.dilate(cannyMat, cannyMat, element);
		return cannyMat;
	}

	public List<MatOfPoint> detectar_cuadrilateros1(Mat canniedMat) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat mHierarchy = new Mat();
		Imgproc.findContours(canniedMat, contours, mHierarchy,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		MatOfPoint2f approx2f=new MatOfPoint2f();
		Mat dst = canniedMat.clone();
		List<MatOfPoint2f> contours2f = ListMatofPoint2f(contours);
		int count=0;
		List<MatOfPoint> contour_cuadrilateros = new ArrayList<MatOfPoint>();
		for (int i = 0; i < contours.size(); i++) {
			Imgproc.approxPolyDP(contours2f.get(i), approx2f,Imgproc.arcLength(contours2f.get(i), true) * 0.07, true);//0.07
			MatOfPoint approx = new MatOfPoint(approx2f.toArray());
//			if((Imgproc.contourArea(contours.get(i)))> 5.0 && (Imgproc.contourArea(contours.get(i))) <200 && Imgproc.isContourConvex(approx))
			if((Imgproc.contourArea(contours.get(i)))> 1000.0 &&Imgproc.isContourConvex(approx)){// 1000<
				if (approx.toList().size() == 4)
				{
					count++;
					Log.v(TAG, "Cuadrilatero encontrado: " + count + " "+ Imgproc.contourArea(contours.get(i)));
					contour_cuadrilateros.add(approx);
				}
			}
		}
		return contour_cuadrilateros;
	}

	// ---AsyncInitialization----oPENcv------------------------------------------------
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
	// --------------------------------------------------------------------------------
	
	String Tesseract_function(String image_path) { // probada no tocar
		TessBaseAPI baseApi = new TessBaseAPI();
		// DATA_PATH = Path to the storage
		// lang = for which the language data exists, usually "eng"
		baseApi.init(DATA_PATH, lang);
		// Eg. baseApi.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata",
		// "eng");
		Bitmap inputTess = BitmapFactory.decodeFile(image_path);
		baseApi.setImage(inputTess);
		String recognizedText = baseApi.getUTF8Text();
		baseApi.end();
		// display.setText(Integer.toString(cannybmp.getPixel(100,120)));//show
		// value of a pixel
		display.setText(recognizedText);
		return recognizedText;
	}

		
	void showonImageView(Mat output){
		Utils.matToBitmap(output, inputFrame);
		imageView1.setImageBitmap(inputFrame);
	}
	Mat drawCont(Mat image, List<MatOfPoint> contours){
		Mat drawing = Mat.zeros(image.size(), CvType.CV_8UC3);
		Scalar color = new Scalar( 255, 0, 0);
		Mat hierarchy = new Mat();
		Point point = new Point(0,0);
		for( int i = 0; i< contours.size(); i++ )
	     {
			Imgproc.drawContours( drawing, contours, i, color, 2, 8, hierarchy, 0, point );
	     }
		return drawing;
	}
	void saveMatBMP(Mat imagetosave, String namepng) {
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

	private List<MatOfPoint2f> ListMatofPoint2f(List<MatOfPoint> toConvert) {
		List<MatOfPoint2f> matofPoint2f =new ArrayList<MatOfPoint2f>();
		Iterator<MatOfPoint> itr = toConvert.iterator();
		while (itr.hasNext()) {
			 MatOfPoint SrcMtx=itr.next();
			 MatOfPoint2f  NewMtx = new MatOfPoint2f(SrcMtx.toArray());
			 matofPoint2f.add(NewMtx);
		}
		return matofPoint2f;
	}
	// --no usadas--------------------------------
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
	int band_clipping(Bitmap cannybmp) {
		int[][] cannyarray = getBinary(cannybmp); // creates int [][] imgBin
		int lar = cannyarray.length;
		int alt = cannyarray[0].length;
		Bitmap bandbmp;
		int[][] band_array;

		int[] vproj = fProjectionH(cannyarray);
		int vproj_max = 0;
		// hallar maximo
		for (int i = 0; i < alt; i++) {
			if (vproj[i] > vproj_max) {
				vproj_max = vproj[i];
			}
		}
		// limite superior
		int lim_sup = 0, lim_inf = alt;
		for (int i = 0; i < alt; i++) {
			if (vproj[i] == vproj_max) {
				lim_sup = i;
				i = alt;
			}
		}
		// limite inferior
		for (int i = 0; i < alt; i++) {
			if (vproj[alt - i - 1] >= vproj_max * 0.75) {
				lim_inf = alt - i - 1;
				i = alt;
			}
		}

		// cambiar:
		// bandbmp = Bitmap.createBitmap(band_array, cannybmp.getWidth(),
		// cannybmp.getHeight(), Bitmap.Config.ARGB_8888);
		return cannybmp.getPixel(100, 100);
	}
	int band_clipping2(Mat image_canny) {
		int max_val = 0;
		Mat vert_proj = new Mat(inputFrame.getWidth(), 1, CvType.CV_8UC1);
		Mat maxx = new Mat(1, 1, CvType.CV_8UC1);

		Core.reduce(image_canny, vert_proj, 0, Core.REDUCE_SUM, CvType.CV_8UC1);
		Core.reduce(image_canny, maxx, 0, Core.REDUCE_MAX, CvType.CV_8UC1);

		for (int i = 0; i < 25; i++) {
			double[] histValues = vert_proj.get(i, 0);
			for (int j = 0; j < histValues.length; j++) {
				Log.d(TAG, "yourData=" + histValues[j]);
			}
		}
		return max_val;
	}
	public static int[] fProjectionH(int img[][]) {
		int lar = img.length;
		int alt = img[0].length;

		int vproj[] = new int[alt];
		for (int j = 0; j < alt; j++) {
			for (int i = 0; i < lar; i++) {
				if (img[i][j] == 1) {
					vproj[j] += 1;
				}
			}
		}
		return vproj;
	}
	private int[][] getBinary(Bitmap bmp) {
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		int rgb = 0;
		int[][] imgBin = new int[w][h];

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				rgb = bmp.getPixel(i, j);
				imgBin[i][j] = rgb != 0 ? 1 : 0;
			}
		}
		return imgBin;
	}
	// -----------------------------
}
