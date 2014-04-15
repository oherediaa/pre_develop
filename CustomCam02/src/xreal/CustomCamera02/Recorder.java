package xreal.CustomCamera02;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.VideoEncoder;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import xreal.CustomCamera02.R;
import xreal.CustomCamera02.R.*;

public class Recorder extends Activity implements SurfaceHolder.Callback,
		MediaRecorder.OnInfoListener {
	private SurfaceView prSurfaceView;
	private Button prStartBtn;
	// private Button prSettingsBtn;
	private boolean prRecordInProcess;
	private SurfaceHolder prSurfaceHolder;
	private Camera prCamera;
	private final String cVideoFilePath = "/sdcard/TestVideo/";

	private Context prContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		prContext = this.getApplicationContext();
		setContentView(R.layout.activity_video);
		// Utils.
		createDirIfNotExist(cVideoFilePath);
		prSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		prStartBtn = (Button) findViewById(R.id.main_btn1);
		// prSettingsBtn = (Button) findViewById(R.id.main_btn2);
		prRecordInProcess = false;
		prStartBtn.setOnClickListener(new View.OnClickListener() {
			// @Override
			public void onClick(View v) {
				if (prRecordInProcess == false) {
					startRecording();
				} else {
					stopRecording();
				}
			}
		});

		prSurfaceHolder = prSurfaceView.getHolder();
		prSurfaceHolder.addCallback(this);
		prSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		prMediaRecorder = new MediaRecorder();
	}

	public void surfaceChanged(SurfaceHolder _holder, int _format, int _width,
			int _height) {
		try {
			prCamera.setPreviewDisplay(_holder);
			prCamera.startPreview();
			// prPreviewRunning = true;

			if (prRecordInProcess == false) {
				startRecording();
			} else {
				stopRecording();
			}
		} catch (IOException _le) {
			_le.printStackTrace();
		}
	}

	// @Override
	public void surfaceCreated(SurfaceHolder arg0) {
		prCamera = Camera.open();
		if (prCamera == null) {
			Toast.makeText(this.getApplicationContext(),
					"Camera is not available!", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	// @Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (prRecordInProcess) {
			stopRecording();
		} else {
			prCamera.stopPreview();
		}
		prMediaRecorder.release();
		prMediaRecorder = null;
		prCamera.release();
		prCamera = null;
	}

	private MediaRecorder prMediaRecorder;
	private final int cMaxRecordDurationInMs = 5000;
	private final long cMaxFileSizeInBytes = 5000000;
	private final int cFrameRate = 30;
	private File prRecordedFile;

	private boolean startRecording() {
		prCamera.stopPreview();
		try {
			prCamera.unlock();
			prMediaRecorder.setCamera(prCamera);
			prMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			prMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

			String lVideoFileFullPath;
			String lDisplayMsg = "Current container format: ";
			lDisplayMsg += "MP4\n";
			lVideoFileFullPath = ".mp4";
			prMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			prMediaRecorder.setAudioEncoder(AudioEncoder.AAC);
			lDisplayMsg += "Current encoding format: ";
			lDisplayMsg += "H264\n";
			prMediaRecorder.setVideoEncoder(VideoEncoder.H264);
			lVideoFileFullPath = cVideoFilePath
					+ String.valueOf(System.currentTimeMillis())
					+ lVideoFileFullPath;
			prRecordedFile = new File(lVideoFileFullPath);
			prMediaRecorder.setOutputFile(prRecordedFile.getPath());
			prMediaRecorder.setVideoSize(720, 480);
			Toast.makeText(prContext, lDisplayMsg, Toast.LENGTH_LONG).show();
			prMediaRecorder.setVideoFrameRate(cFrameRate);
			prMediaRecorder.setPreviewDisplay(prSurfaceHolder.getSurface());
			prMediaRecorder.setMaxDuration(cMaxRecordDurationInMs);
			prMediaRecorder.setMaxFileSize(cMaxFileSizeInBytes);
			
			prMediaRecorder.setOnInfoListener(this);
			prMediaRecorder.prepare();
			prMediaRecorder.start();
			prStartBtn.setText("Stop");
			prRecordInProcess = true;
			return true;
		} catch (IOException _le) {
			_le.printStackTrace();
			return false;
		}
	}

	private void stopRecording() {
		prMediaRecorder.stop();
		prMediaRecorder.reset();
		try {
			prCamera.reconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		prRecordInProcess = false;
		finish();
	}

	private static final int REQUEST_DECODING_OPTIONS = 0;

	public static void createDirIfNotExist(String _path) {
		File lf = new File(_path);
		try {
			if (lf.exists()) {
			} else {
				if (lf.mkdirs()) {
				} else {
					// Log.v(TAG, "createDirIfNotExist failed to create " +
					// _path);
				}
			}
		} catch (Exception e) {
			// create directory failed
			// Log.v(TAG, "createDirIfNotExist failed to create " + _path);
		}
	}

	public static void getFrame(long useconds, int opt) {
		MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

		mediaMetadataRetriever.setDataSource("sdcard/TestVideo/CAM00135.mp4");
		Bitmap bmFrame3 = mediaMetadataRetriever.getFrameAtTime(useconds, opt); // option_closest

		// metodo de almacenamiento - FUNCIONA, FALTA OBTENER EL FRAME
		// ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		// bmFrame3.compress(Bitmap.CompressFormat.PNG, 40, bytes);
		// File f = new File(path_Video + video_name + "-2.jpg");
		File file = new File("sdcard/TestVideo/2.png");
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);
			bmFrame3.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		// TODO Auto-generated method stub
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
			prRecordInProcess = false;
			
			stopRecording();
//			prMediaRecorder.release();
//			prMediaRecorder = null;
//			prCamera.release();
//			prCamera = null;
//			finish();
		}
	}
}