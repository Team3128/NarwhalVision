package org.team3128.narwhalvision;


import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Switch;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends NarwhalVisionFragment implements CameraBridgeViewBase.CvCameraViewListener2, SeekBar.OnSeekBarChangeListener
{
	public CameraFragment()
	{
		// Required empty public constructor
	}

	private static final String TAG = "NVCamera";

	private JavaCameraView mOpenCvCameraView;

	private Switch colorFilterSwitch;

	private CheckBox roborioConnectionIndicator;

	//private SeekBar exposureSeekBar;

	private TowerTrackerPipeline pipeline;

	//class variables for touch selection
	private boolean userTouchedPixel;
	private int touchX, touchY;

	//objects used in processing code
	Mat filteredImg = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");

		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View content = inflater.inflate(R.layout.nv_camera, container, false);

		mOpenCvCameraView = (JavaCameraView) content.findViewById(R.id.nv_camera_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);

		colorFilterSwitch = (Switch) content.findViewById(R.id.colorFilterSwitch);
		roborioConnectionIndicator = (CheckBox) content.findViewById(R.id.roborioResolvedIndicator);
		//exposureSeekBar = (SeekBar) content.findViewById(R.id.exposureSlider);

		//exposureSeekBar.setOnSeekBarChangeListener(this);

		if(((NarwhalVisionActivity)getActivity()).hasFoundRIO())
		{
			onRIOConnected();
		}

		return content;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
		{
			mOpenCvCameraView.disableView();
		}
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		notifyLoadingComplete();

	}

	public void onCameraViewStarted(int width, int height)
	{
		//I made a small *ahem* change to to the OpenCV library and added JavaCameraView.getCamera().
		Camera.Parameters cameraParams = mOpenCvCameraView.getCamera().getParameters();

		if(getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
		{
			Log.i(TAG, "Turning on flashlight");
			//cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

			Log.i(TAG, "min exposure compensation: " + "");
//			cameraParams.setExposureCompensation(-30);
//			cameraParams.setAutoExposureLock(true);
//
//			exposureSeekBar.setMax(cameraParams.getMaxExposureCompensation());

			mOpenCvCameraView.getCamera().setParameters(cameraParams);
		}

		pipeline = new TowerTrackerPipeline(cameraParams.getHorizontalViewAngle(), cameraParams.getVerticalViewAngle());

		mOpenCvCameraView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				//set flag for next time a new frame is received
				userTouchedPixel = true;
				touchX = Math.round(event.getX());
				touchY = Math.round(event.getY());

				return true;
			}
		});
	}

	public void onCameraViewStopped()
	{
	}


	@Override
	public void onSwapIn()
	{
		if(pipeline != null)
		{
			pipeline.loadSettings();
		}
	}

	/**
	 * Finds the coordinates on the preview image closest to the provided coordinates on the screen
	 *
	 * Returns null if the coordinates are outside of the preview window
	 * @param x
	 * @param y
	 * @return
	 */
	private Pair<Integer, Integer> screenCoordinates2FrameCoordinates(int x, int y)
	{
		Camera.Size frameSize = mOpenCvCameraView.getCamera().getParameters().getPreviewSize();

		// X coordinate
		int horizontalBarSize = mOpenCvCameraView.getLeft();
		int previewWidth = mOpenCvCameraView.getWidth();

		if(x < horizontalBarSize || x > (horizontalBarSize + previewWidth))
		{
			//screen pixel is outside the frame
			return null;
		}

		int frameX = (int)Math.floor(((double)(x - horizontalBarSize)) * frameSize.width / previewWidth);

		// Y coordinate
		int verticalBarSize = mOpenCvCameraView.getTop();
		int previewHeight = mOpenCvCameraView.getHeight();

		if(y < verticalBarSize || y > (verticalBarSize + previewHeight))
		{
			//screen pixel is outside the frame
			return null;
		}

		int frameY = (int)Math.floor(((double)(y - verticalBarSize)) * frameSize.height / previewHeight);

		return new Pair<>(frameX, frameY);
	}

	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
	{
		Mat rgbImg = inputFrame.rgba();



		//handle touch selection
		if(userTouchedPixel)
		{
			Pair<Integer, Integer> frameCoordinates = screenCoordinates2FrameCoordinates(touchX, touchY);

			if (frameCoordinates != null)
			{

				Log.d(TAG, "User touched pixel (" + touchX + ", " + touchY + "), on the frame that's (" + frameCoordinates.first + " ," + frameCoordinates.second + ")");

				//convert screen coordinates to frame coordinates
				userTouchedPixel = false;
				double[] rgbColors = rgbImg.get(frameCoordinates.first, frameCoordinates.second);
				if (rgbColors != null)
				{

					int[] rgbColorsBytes = new int[]{(int) rgbColors[0], (int) rgbColors[1], (int) rgbColors[2]};

					final int touchSelectionRadius = 20;
					int[] hsvColors = Utils.androidColorToHSV(Utils.rgbToAndroidColor(rgbColorsBytes));

					Log.d(TAG, "User selected color " + Arrays.toString(hsvColors));

					Settings.highH = Utils.clampColor(hsvColors[0] + touchSelectionRadius);
					Settings.highS = Utils.clampColor(hsvColors[1] + touchSelectionRadius);
					Settings.highV = Utils.clampColor(hsvColors[2] + touchSelectionRadius);

					Settings.lowH = Utils.clampColor(hsvColors[0] - touchSelectionRadius);
					Settings.lowS = Utils.clampColor(hsvColors[1] - touchSelectionRadius);
					Settings.lowV = Utils.clampColor(hsvColors[2] - touchSelectionRadius);

					pipeline.loadSettings();
				}
			}
		}
		Pair<Mat, TargetInformation> result = pipeline.processImage(rgbImg, colorFilterSwitch.isChecked());

		if(result.second != null)
		{
			Log.v(TAG, "Sending Target: " + result.second);

			((NarwhalVisionActivity)getActivity()).sendTargetInformation(result.second);
		}

		return result.first;
	}

	@Override
	public void onOpenCVLoaded()
	{
		mOpenCvCameraView.enableView();
	}

	/**
	  Called to update the UI to show that the robot has been found
	 */
	public void onRIOConnected()
	{
		getActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				roborioConnectionIndicator.setChecked(true);
				roborioConnectionIndicator.setText("RIO Found");
			}
		});
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		Camera camera = mOpenCvCameraView.getCamera();

		Log.d(TAG, "Progress: " + progress);

		if(camera != null)
		{
			Camera.Parameters params = camera.getParameters();
			params.setExposureCompensation(progress);
			camera.setParameters(params);
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{

	}
}
