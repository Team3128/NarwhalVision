package org.team3128.narwhalvision;

import org.opencv.core.Rect;

import java.io.Serializable;

/**
 * Class which describes a target
 * It is serialized and sent to the robot.
 *
 * NOTE: it cannot reference any classes available on only one side or the other
 */

public class TargetInformation implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6981674676679127950L;

	private double area;
	private double boundingRectLeft;
	private double boundingRectTop;
	private double boundingRectRight;
	private double boundingRectBottom;

	private double boundingRectHeight, boundingRectWidth;

	private double boundingRectCenterX, boundingRectCenterY;

	private int imageWidth, imageHeight;

	private double horizontalFOV, verticalFOV;

	/**
	 * Populate from image and index in image
	 *
	 * NOTE: we use the width of the target because we are looking up at the target from the ground so height will be parallax-distorted.
	 */
	public TargetInformation(Rect target, int imageWidth, int imageHeight, double horizontalFOV, double verticalFOV)
	{
		area = target.area();
		boundingRectLeft = target.tl().x;
		boundingRectBottom = target.br().y;
		boundingRectTop = target.tl().y;
		boundingRectRight = target.br().x;

		boundingRectHeight = boundingRectTop - boundingRectBottom;
		boundingRectWidth = boundingRectRight - boundingRectLeft;

		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;

		this.horizontalFOV = horizontalFOV;
		this.verticalFOV = verticalFOV;
	}


	/**
	 * Computes the estimated distance to a target using the known height of the target off the ground.
	 * Only works if the camera is horizontal.
	 *
	 * @param cameraHeight distance between the center of the camera lens and the ground
	 * @param targetHeight distance between the center of the target and the ground
	 *
	 * @return The estimated distance directly to the target in cm.
	 */
	public double getTargetDistance(double targetHeight, double cameraHeight)
	{
		double deltaHeight = targetHeight - cameraHeight;

		double targetDistance = deltaHeight / Math.cos(Math.toRadians(getVerticalAngle()));

		return targetDistance;
	}


	/**
	 * Get this particle's offset in degrees from directly in front of the camera.
	 * Note that this may be less accurate at larger angles due to parallax distortion.
	 *        _
	 *       |_|
	 *     \    |    /
	 *      \   |   /
	 *       \  |  /
	 *        \ | /
	 *         \_/
	 *         | |
	 * Can be positive or negative.  If the object is in the center, it returns 0.
	 * @return
	 */
	public double getHorizontalAngle()
	{
		double distanceFromCenter = boundingRectCenterX - imageWidth / 2.0;

		return Math.toDegrees(Math.atan(distanceFromCenter * Math.tan(Math.toRadians(horizontalFOV)) / imageWidth));
	}

	/**
	 * Get this particle's vertical offset (azimuth) in degrees from directly in front of the camera.
	 * Note that this may be less accurate at larger angles due to parallax distortion.
	 *        _
	 *       |_|
	 *     \    |    /
	 *      \   |   /
	 *       \  |  /
	 *        \ | /
	 *         \_/
	 *         | |
	 * Can be positive or negative.  If the object is in the center, it returns 0.
	 * @return
	 */
	public double getVerticalAngle()
	{
		double distanceFromCenter = boundingRectCenterY - imageHeight / 2.0;

		return Math.toDegrees(Math.atan(distanceFromCenter * Math.tan(Math.toRadians(horizontalFOV)) / imageWidth));
	}
}
