package org.team3128.narwhalvision;

import org.opencv.core.Rect;

/**
 * Class which describes a target
 * It is serialized and sent to the robot.
 *
 * NOTE: it cannot reference any classes available on only one side or the other
 */

public class TargetInformation
{
	// NOTE: these are kept public to give the serializer an easier time
	// we also use floats so we don't send an unneccessary amount of precision

	public float area;
	public float boundingRectLeft;
	public float boundingRectTop;
	public float boundingRectRight;
	public float boundingRectBottom;

	//NOTE: these are pixel coordinates, so the origin is in the top left of the image

	public float boundingRectHeight, boundingRectWidth;

	public float boundingRectCenterX, boundingRectCenterY;

	public int imageWidth, imageHeight;

	public float horizontalFOV, verticalFOV;

	/**
	 * Populate from image and index in image
	 *
	 * NOTE: we use the width of the target because we are looking up at the target from the ground so height will be parallax-distorted.
	 */
	public TargetInformation(Rect target, int imageWidth, int imageHeight, float horizontalFOV, float verticalFOV)
	{
		area = (float) target.area();

		boundingRectTop = (float) target.tl().y;
		boundingRectRight = (float) target.br().x;
		boundingRectLeft = (float) target.tl().x;
		boundingRectBottom = (float) target.br().y;

		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;

		this.verticalFOV = verticalFOV;
		this.horizontalFOV = horizontalFOV;

		boundingRectCenterX = (boundingRectLeft + boundingRectRight) / 2.0F;
		boundingRectCenterY = (boundingRectTop + boundingRectBottom) / 2.0f;

		boundingRectHeight = boundingRectBottom - boundingRectTop;
		boundingRectWidth = boundingRectRight - boundingRectLeft;
	}

	/**
	 * Blank constructor for serializer
	 */
	public TargetInformation()
	{
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
	public float getTargetDistance(float targetHeight, float cameraHeight)
	{
		float deltaHeight = targetHeight - cameraHeight;

		float targetDistance = (float) (deltaHeight / Math.cos(Math.toRadians(getVerticalAngle())));

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
	public float getHorizontalAngle()
	{
		float distanceFromCenter = boundingRectCenterX - imageWidth / 2.0F;

		return (float) Math.toDegrees(Math.atan(distanceFromCenter * Math.tan(Math.toRadians(horizontalFOV)) / imageWidth));
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
	public float getVerticalAngle()
	{
		float distanceFromCenter = -1 * (boundingRectCenterY - imageHeight / 2.0F);

		return (float) Math.toDegrees(Math.atan(distanceFromCenter * Math.tan(Math.toRadians(horizontalFOV)) / imageWidth));
	}

	@Override
	public String toString()
	{
		return "TargetInformation: center: (" + boundingRectCenterX + ", " + boundingRectCenterY + "), bounding box: (" + boundingRectWidth + ", " + boundingRectHeight + "), image size: (" + imageWidth + ", " + imageHeight + "), horizontal angle offset: " + getHorizontalAngle() + " deg, vertical angle offset: " + getVerticalAngle();
	}
}
