package org.team3128.narwhalvision;

import android.graphics.Color;

/**
 * Class for miscellaneous utility functions
 */

public class Utils
{
	/**
	 * wrapper around Android's hsv conversion method that deals with the weird arguments
	 * @param h
	 * @param s
	 * @param v
	 * @return
	 */
	public static int androidColorFromHSV(int h, int s, int v)
	{
		return Color.HSVToColor(new float[]{h * 360F / 255F, s/255F, v/255F});
	}

	public static int[] androidColorToHSV(int color)
	{
		float[] systemResult = new float[3];
		Color.colorToHSV(color, systemResult);

		int[] result = new int[3];
		result[0] = Math.round(systemResult[0] * 255F / 360F);
		result[1] = Math.round(systemResult[1] * 255F);
		result[2] = Math.round(systemResult[2] * 255F);
		return result;
	}

	public static int[] androidColorToArray(int color)
	{
		return new int[]{((color >> 16) & 0xFF), (color >> 8) & 0xFF,color & 0xFF};
	}

	public static int rgbToAndroidColor(int[] color)
	{
		return (color[0] << 16) | (color[1] << 8) | (color[2]);
	}

	/**
	 * Clamps an integer to be a valid color, between 0 and 255
	 */
	public static int clampColor(int value)
	{
		if(value > 255)
		{
			return 255;
		}
		else if(value < 0)
		{
			return 0;
		}
		else
		{
			return value;
		}
	}
}
