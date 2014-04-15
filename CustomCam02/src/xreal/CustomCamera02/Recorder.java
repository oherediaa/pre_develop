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

public class Recorder extends Activity implements SurfaceHolder.Callback {
	private SurfaceView prSurfaceView;
	private Button prStartBtn;
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

	// @Override
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
	private final long cMaxFileSizeInBytes = 5000000;
	private final int cFrameRate = 30;
	private File prRecordedFile;

	private boolean startRecording() {
		prCamera.stopPreview();
		try {
			prCamera.unlock();
			prMediaRecorder.setCamera(prCamera);
			// set audio source as Microphone, video source as camera
			// state: Initial=>Initialized
			prMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			prMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			// set the file output format: 3gp or mp4
			// state: Initialized=>DataSourceConfigured
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
			prMediaRecorder.setMaxFileSize(cMaxFileSizeInBytes);
			// prepare for capturing
			// state: DataSourceConfigured => prepared
			prMediaRecorder.prepare();
			// start recording
			// state: prepared => recording
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
		prStartBtn.setText("Start");
		prRecordInProcess = false;
		prCamera.startPreview();

	}

	public static void createDirIfNotExist(String _path) {
		File lf = new File(_path);
		try {
			if (lf.exists()) {
				// directory already exists
			} else {
				if (lf.mkdirs()) {
					// Log.v(TAG, "createDirIfNotExist created " + _path);
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
}