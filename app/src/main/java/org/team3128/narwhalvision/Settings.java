package org.team3128.narwhalvision;

import android.content.SharedPreferences;

/**
 * Class to store app-wide settings.  Uses static members, so it is not affected by things like activity switches or device rotations.
 */
public class Settings
{
	//color thresholding settings
	public static int lowH, highH, lowS, highS, lowV, highV;

	//image processing options
	public static int minArea, targetSolidity;

	//one ratio saved as two parts for the settings screen
	public static int targetWidth, targetHeight;

	//path to last image used in image test screen
	//null if unselected
	public static String testImagePath;

	private static SharedPreferences prefs;

	/**
	 * Called by the app when it starts to set the preferences.
	 *
	 * Calls loadPreferences
	 * @param sharedPrefs
	 */
	static void setSharedPreferences(SharedPreferences sharedPrefs)
	{
		prefs = sharedPrefs;
		loadPreferences();
	}

	/**
	 * Loads preferences from the SharedPreferences
	 */
	static void loadPreferences()
	{
		//default settings from Tower Tracker are used here
		lowH = prefs.getInt("lowH", 0);
		lowS = prefs.getInt("lowS", 0);
		lowV = prefs.getInt("lowV", 0);

		highH = prefs.getInt("highH", 255);
		highS = prefs.getInt("highS", 255);
		highV = prefs.getInt("highV", 255);

		testImagePath = prefs.getString("testImagePath", null);

		minArea = prefs.getInt("minArea", 50);
		targetSolidity = prefs.getInt("targetSolidity", 50);
		targetHeight = prefs.getInt("targetHeight", 1);
		targetWidth = prefs.getInt("targetWidth", 1);
	}

	/**
	 * Saves the current preferences.
	 */
	static void savePreferences()
	{
		SharedPreferences.Editor editor = prefs.edit();

		editor.putInt("lowH", lowH);
		editor.putInt("lowS", lowS);
		editor.putInt("lowV", lowV);

		editor.putInt("highH", highH);
		editor.putInt("highS", highS);
		editor.putInt("highV", highV);

		editor.putString("testImagePath", testImagePath);

		editor.putInt("minArea", minArea);
		editor.putInt("targetSolidity", targetSolidity);
		editor.putInt("targetHeight", targetHeight);
		editor.putInt("targetWidth", targetWidth);

		editor.apply();
	}

	/**
	 * Get the target aspect ratio as a decimal, width over height
	 */
	static double getTargetAspectRatio()
	{
		//the phone is sideways, so horizontal and vertical are flipped
		return  targetHeight / ((double)targetWidth);
	}
}
