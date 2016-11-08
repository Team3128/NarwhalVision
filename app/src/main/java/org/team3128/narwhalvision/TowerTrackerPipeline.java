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
	public Pair<Mat, TargetInformation> processImage(Mat frame, boolean showColorFilter)
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

		// make sure the contours that are detected are at least 20x20
		// pixels with an area of 400 and an aspect ratio greater then 1
		for(Iterator<MatOfPoint> matPointIter = foundContours.iterator(); matPointIter.hasNext();)
		{
			MatOfPoint matOfPoint = matPointIter.next();
			Rect boundingBox = Imgproc.boundingRect(matOfPoint);

			double aspect = ((double)boundingBox.width) / ((double)boundingBox.height);
			double aspectQuotient = aspect / (Settings.getTargetAspectRatio());

			if((boundingBox.area() * 100)/ (frame.width() * frame.height()) < Settings.minArea)
			{
				matPointIter.remove();
				continue;
			}

			//if the aspect ratios are within a factor of 4 of each other...
			if(aspectQuotient <= .25 || aspectQuotient >= 4)
			{
				matPointIter.remove();
				continue;
			}

			// Draw rectangles on the image to show countours
			Imgproc.rectangle(frame, boundingBox.br(), boundingBox.tl(), RED);
		}

		if(foundContours.isEmpty())
		{
			return new Pair<>(frame, null);
		}

		//now that we've removed the riff-raff, find the best contour
		MatOfPoint bestCountour = null;
		Rect winnerBoundingBox = null;
		double bestScore = 0;
		double winnerSolidity = 0;

		for(MatOfPoint matOfPoint : foundContours)
		{
			//for now, we just rank by area and solidity (the goal is NOT very solid)
			Rect boundingBox = Imgproc.boundingRect(matOfPoint);
			double area = Imgproc.contourArea(matOfPoint);

			double solidityPercent = (boundingBox.width * boundingBox.height * 100) / area;

			double aspect = ((double)boundingBox.width) / ((double)boundingBox.height);
			double aspectQuotient = aspect / Settings.getTargetAspectRatio();

			//if the target aspect ratio was the larger one, flip the fraction
			if(aspectQuotient < 1)
			{
				aspectQuotient = 1/aspectQuotient;
			}

			double score = area / (100 * Math.abs(solidityPercent - Settings.targetSolidity) * aspectQuotient);

			if(bestCountour == null || score > bestScore)
			{
				bestCountour = matOfPoint;
				bestScore = score;
				winnerSolidity = solidityPercent;
				winnerBoundingBox = boundingBox;
			}

		}

		TargetInformation targetInfo = new TargetInformation(winnerBoundingBox, frame.width(), frame.height(), horizontalFOV, verticalFOV);


		//draw a green bounding box
		if(bestCountour != null)
		{
			Imgproc.rectangle(frame, winnerBoundingBox.br(), winnerBoundingBox.tl(), GREEN);

			Log.d(TAG, "Winner solidity: " + winnerSolidity);

		}

		return new Pair<>(frame, targetInfo);


	}


}
