package org.team3128.narwhalvision;


import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Switch;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends NarwhalVisionFragment implements CameraBridgeViewBase.CvCameraViewListener2
{
	public CameraFragment()
	{
		// Required empty public constructor
	}

	private static final String TAG = "NVCamera";

	private JavaCameraView mOpenCvCameraView;

	private Switch colorFilterSwitch;

	private CheckBox roborioConnectionIndicator;

	private TowerTrackerPipeline pipeline;

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
			cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			mOpenCvCameraView.getCamera().setParameters(cameraParams);
		}

		pipeline = new TowerTrackerPipeline(cameraParams.getHorizontalViewAngle(), cameraParams.getVerticalViewAngle());
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

	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
	{
		Mat rgbImg = inputFrame.rgba();
		Pair<Mat, TargetInformation> result = pipeline.processImage(rgbImg, colorFilterSwitch.isChecked());

		if(result.second != null)
		{
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
}
