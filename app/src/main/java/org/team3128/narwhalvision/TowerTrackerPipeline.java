package org.team3128.narwhalvision;

import android.util.Log;
import android.util.Pair;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Class which processes frames of the camera and recognizes features.
 *
 * Based on the Tower Tracker program created by Team 3019 (https://github.com/fauge7/TowerTracker/blob/master/src/com/fauge/robotics/towertracker/TowerTracker.java)
 */
public class TowerTrackerPipeline
{
	final static String TAG = "TTPipeline";

	Mat rgbImg, hsvImage, filteredImage, outputImage, hierarchy;

	Scalar upperLimit;
	Scalar lowerLimit;

	ArrayList<MatOfPoint> foundContours;

	final Scalar RED = new Scalar(255, 0, 0);
	final Scalar GREEN = new Scalar(0, 255, 0);

	//used for constructing TargetInformation classes
	final private float horizontalFOV, verticalFOV;

	private class TargetData implements Comparable<TargetData>
	{
		Mat sourceFrame;
		Rect boundingBox;

		// area of the arbitrarily shaped surface that makes up the target
		double contourArea;

		// area of the bounding box around the target
		double boundingBoxArea;

		//aspect ratio (width per height)
		double aspect;

		//factor between this contour's aspect ratio and the target ratio
		double aspectQuotient;

		// ratio between 1 and 0 of how solid this contour is
		double solidity;

		// how far away from the target aspect ratio a countour can be before it is disqualified
		final static double ASPECT_RATIO_FUZZ_FACTOR = 3;

		// number representing how well this contour mateches the targeting criteria
		private double score;

		public TargetData(Mat sourceFrame, MatOfPoint contour)
		{
			this.sourceFrame = sourceFrame;

			boundingBox = Imgproc.boundingRect(contour);

			aspect = ((double)boundingBox.width) / ((double)boundingBox.height);
			aspectQuotient = aspect / (Settings.getTargetAspectRatio());

			contourArea = Imgproc.contourArea(contour);
			boundingBoxArea = boundingBox.area();

			solidity = boundingBoxArea / contourArea;

			// now we calculate the score
			//for now, we just rank by area and solidity (the goal is NOT very solid)

			double aspectQuotient = aspect / Settings.getTargetAspectRatio();

			//if the target aspect ratio was the larger one, flip the fraction
			if(aspectQuotient < 1)
			{
				aspectQuotient = 1/aspectQuotient;
			}

			score =  contourArea / (1000 * Math.abs(100 * solidity - Settings.targetSolidity) * aspectQuotient);
		}

		/**
		 *
		 * @return true if this contour could possibly be a target
		 */
		boolean isPotentialTarget()
		{
			// too small?
			if((boundingBox.area() * 100)/ (sourceFrame.width() * sourceFrame.height()) < Settings.minArea)
			{
				return false;
			}

			//is the aspect ratio too far off?
			if(aspectQuotient <= 1/ASPECT_RATIO_FUZZ_FACTOR || aspectQuotient >= ASPECT_RATIO_FUZZ_FACTOR)
			{
				return false;
			}

			return true;
		}

		/**
		 * Draw this contour's bounding box on the source frame
		 */
		void drawBoundingBox(Scalar color)
		{
			Imgproc.rectangle(sourceFrame, boundingBox.br(), boundingBox.tl(), color);
		}

		@Override
		public int compareTo(TargetData another)
		{
			return (int)(score - another.score);
		}
	}

	/*
	Only construct this class after OpenCV is loaded.
	This allows it to use OpenCV constructs as class variables.

	Settings from the Settings class are read and stored when the pipeline is constructed.  Changes to them will not take affect
	until loadSettings() is called.
	 */
	public TowerTrackerPipeline(float horizontalFOV, float verticalFOV)
	{
		this.horizontalFOV = horizontalFOV;
		this.verticalFOV = verticalFOV;

		rgbImg = new Mat();
		hsvImage = new Mat();
		filteredImage = new Mat();
		outputImage = new Mat();
		hierarchy = new Mat();

		foundContours = new ArrayList<>();

		loadSettings();
	}

	/**
	 * Reloads the color settings from the global variables.
	 * THe other settings are used as soon as they are changed in the Settings class
	 */
	public void loadSettings()
	{
		lowerLimit = new Scalar(Settings.lowH, Settings.lowS, Settings.lowV);
		upperLimit = new Scalar(Settings.highH, Settings.highS, Settings.highV);

		Log.i(TAG, "Thresholding from " + lowerLimit.toString() + " to " + upperLimit.toString());
	}

	/**
	 * Process a frame according the the current settings.
	 *
	 * @return What should be displayed on the phone screen, and information about the target if one was found.
	 */
	public Pair<Mat, ArrayList<TargetInformation>> processImage(Mat frame, boolean showColorFilter)
	{
		//RGBA to RGB
		Imgproc.cvtColor(frame, rgbImg, Imgproc.COLOR_RGBA2RGB);

		//RGB to HSV
		Imgproc.cvtColor(rgbImg, hsvImage, Imgproc.COLOR_RGB2HSV);

		//HSV threshold
		Core.inRange(hsvImage, lowerLimit, upperLimit, filteredImage);

		if(showColorFilter)
		{
			Imgproc.cvtColor(filteredImage, outputImage, Imgproc.COLOR_GRAY2RGBA);
			return new Pair<>(outputImage, null);
		}

		//find contours
		foundContours.clear();
		Imgproc.findContours(filteredImage, foundContours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		Log.i(TAG, "Found contours: " + foundContours.size());

		ArrayList<TargetData> candidateContours = new ArrayList<>();

		// make sure the contours that are detected are at least 20x20
		// pixels with an area of 400 and an aspect ratio greater then 1
		for(Iterator<MatOfPoint> matPointIter = foundContours.iterator(); matPointIter.hasNext();)
		{
			MatOfPoint matOfPoint = matPointIter.next();

			TargetData contourData = new TargetData(frame, matOfPoint);
			if(contourData.isPotentialTarget())
			{
				candidateContours.add(contourData);
				contourData.drawBoundingBox(RED);
			}
		}

		ArrayList<TargetInformation> foundTargetInformation = new ArrayList<>();

		if(!candidateContours.isEmpty())
		{
			//now that we've removed the riff-raff, find the best contour
			Collections.sort(candidateContours);

			for(int index = 1; index < Math.min(candidateContours.size(), Settings.numTargets); ++index)
			{
				TargetData selectedContour = candidateContours.get(index);

				selectedContour.drawBoundingBox(GREEN);

				TargetInformation targetInfo = new TargetInformation(selectedContour.boundingBox, frame.width(), frame.height(), horizontalFOV, verticalFOV, index);
				foundTargetInformation.add(targetInfo);

			}
		}

		return new Pair<>(frame, foundTargetInformation);


	}


}
