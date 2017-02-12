package org.team3128.narwhalvision;

import android.content.SharedPreferences;

/**
 * Class to store app-wide settings.  Uses static members, so it is not affected by things like activity switches or device rotations.
 */
public class Settings
{
	//which slot the settings will be saved from and loaded into
	//takes effect when savePreferences() and loadPreferences() are called
	public static int selectedSlot;

	public static final int NUM_SLOTS=4;

	//color thresholding settings
	public static int lowH, highH, lowS, highS, lowV, highV;

	//image processing options
	public static int minArea, targetSolidity;

	//one ratio saved as two parts for the settings screen
	public static int targetWidth, targetHeight;

	// Maximum number of targets to find
	public static int numTargets;

	//path to last image used in image test screen
	//null if unselected
	//NOTE: not affected by save slots
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
		selectedSlot = prefs.getInt("selectedSlot", 1);

		//default settings from Tower Tracker are used here
		lowH = prefs.getInt("lowH_" + selectedSlot, 0);
		lowS = prefs.getInt("lowS_" + selectedSlot, 0);
		lowV = prefs.getInt("lowV_" + selectedSlot, 0);

		highH = prefs.getInt("highH_" + selectedSlot, 255);
		highS = prefs.getInt("highS_" + selectedSlot, 255);
		highV = prefs.getInt("highV_" + selectedSlot, 255);

		testImagePath = prefs.getString("testImagePath", null);

		numTargets = prefs.getInt("numTargets_" + selectedSlot, 1);

		minArea = prefs.getInt("minArea_" + selectedSlot, 50);
		targetSolidity = prefs.getInt("targetSolidity_" + selectedSlot, 50);
		targetHeight = prefs.getInt("targetHeight_" + selectedSlot, 1);
		targetWidth = prefs.getInt("targetWidth_" + selectedSlot, 1);
	}

	/**
	 * Saves the current preferences.
	 */
	static void savePreferences()
	{
		SharedPreferences.Editor editor = prefs.edit();

		//make sure to do this one first
		editor.putInt("selectedSlot", selectedSlot);

		editor.putInt("lowH_" + selectedSlot, lowH);
		editor.putInt("lowS_" + selectedSlot, lowS);
		editor.putInt("lowV" + selectedSlot, lowV);

		editor.putInt("highH_" + selectedSlot, highH);
		editor.putInt("highS_" + selectedSlot, highS);
		editor.putInt("highV_" + selectedSlot, highV);

		editor.putString("testImagePath", testImagePath);

		editor.putInt("numTargets_" + selectedSlot, numTargets);

		editor.putInt("minArea_" + selectedSlot, minArea);
		editor.putInt("targetSolidity_" + selectedSlot, targetSolidity);
		editor.putInt("targetHeight_" + selectedSlot, targetHeight);
		editor.putInt("targetWidth_" + selectedSlot, targetWidth);

		editor.apply();
	}

	/**
	 * Change the slot in use.
	 * Reloads the preferences for this slot.
	 * @param newSlot
	 */
	static void changeSlot(int newSlot)
	{
		selectedSlot = newSlot;
		prefs.edit().putInt("selectedSlot", newSlot).apply();

		loadPreferences();
	}

	/**
	 * Get the target aspect ratio as a decimal, width over height
	 */
	static double getTargetAspectRatio()
	{
		return  targetWidth / ((double)targetHeight);
	}
}
