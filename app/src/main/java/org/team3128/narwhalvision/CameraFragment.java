package org.team3128.narwhalvision;


import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends PageSwapListenerFragment implements CameraBridgeViewBase.CvCameraViewListener2
{
	public CameraFragment()
	{
		// Required empty public constructor
	}

	private static final String TAG = "NVCamera";

	private JavaCameraView mOpenCvCameraView;

	private TowerTrackerPipeline pipeline;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getContext()) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");
					mOpenCvCameraView.enableView();

					//I made a small *ahem* change to to the OpenCV library and added JavaCameraView.getCamera().
					Camera.Parameters cameraParams = mOpenCvCameraView.getCamera().getParameters();

					if(getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
					{
						Log.i(TAG, "Turning on flashlight");
						cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
						mOpenCvCameraView.getCamera().setParameters(cameraParams);
					}

					pipeline = new TowerTrackerPipeline(cameraParams.getVerticalViewAngle());
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
		}
	};

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

	@Override
	public void onResume()
	{
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, getContext(), mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
	}

	public void onCameraViewStopped()
	{
	}


	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
	{
		Mat rgbImg = inputFrame.rgba();
		return pipeline.processImage(rgbImg);
	}

}