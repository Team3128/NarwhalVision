package org.team3128.narwhalvision;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

/**
 * Viewer which runs images through the pipeline instead of the camera data.
 */
public class ImageFileFragment extends NarwhalVisionFragment
{
	public ImageFileFragment()
	{
		// Required empty public constructor
	}

	private static final String TAG = "NVImage";
	private static final int IMAGE_CHOOSER_REQUEST_CODE = 1;

	private ImageView resultView;
	private Button loadImageButton;

	private TowerTrackerPipeline pipeline;
	private ToggleButton colorThresholdToggle;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");

		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View content = inflater.inflate(R.layout.nv_image, container, false);

		loadImageButton = (Button) content.findViewById(R.id.loadButton);
		resultView = (ImageView) content.findViewById(R.id.imageView);
		colorThresholdToggle = (ToggleButton) content.findViewById(R.id.colorThresholdToggle);

		loadImageButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				invokeImageChooser();
			}
		});

		colorThresholdToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(hasValidImage())
				{
					refreshImage();
				}
			}
		});

		notifyLoadingComplete();

		return content;
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}


	@Override
	public void onSwapIn()
	{
		if(pipeline != null)
		{
			pipeline.loadSettings();
			refreshImage();
		}
	}

	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onOpenCVLoaded()
	{
		//these are the Tower Tracker defaults
		pipeline = new TowerTrackerPipeline(67F, 67F);

		if(hasValidImage())
		{
			refreshImage();
		}
		else
		{
			//get a new file path
			invokeImageChooser();
			refreshImage();
		}
	}

	/**
	 * Returns true if refreshImage() can be called, false if invokeImageChooser() must be called first.
	 * @return
	 */
	private boolean hasValidImage()
	{
		return Settings.testImagePath != null && new File(Settings.testImagePath).exists();
	}

	private void refreshImage()
	{
		Log.i(TAG, "Loading file: " + Settings.testImagePath);

		File imageFile = new File(Settings.testImagePath);
		if(!(imageFile.exists() && imageFile.canRead()))
		{
			Log.e(TAG, "Cannot access image file!");
		}

		Mat testImage = Imgcodecs.imread(Settings.testImagePath);

		Mat rgbaImg = new Mat();

		Imgproc.cvtColor(testImage, rgbaImg, Imgproc.COLOR_BGRA2RGBA);

		Mat result = pipeline.processImage(rgbaImg, colorThresholdToggle.isChecked()).first;

		//convert Mat to Bitmap
		Bitmap resultBitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(result, resultBitmap);

		resultView.setImageBitmap(resultBitmap);
	}

	private void invokeImageChooser()
	{
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "Choose Image"), IMAGE_CHOOSER_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode)
		{
			case IMAGE_CHOOSER_REQUEST_CODE:
				if(resultCode == Activity.RESULT_OK)
				{
					Uri imageUri = data.getData();

					Settings.testImagePath = FileUtils.getPath(getContext(), imageUri);

					Log.d(TAG, "Received file URI: " + imageUri.toString());

					//process the image
					refreshImage();
				}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);

		}

	}
}
