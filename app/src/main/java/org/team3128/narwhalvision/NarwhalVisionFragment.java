package org.team3128.narwhalvision;

import android.support.v4.app.Fragment;

/**
 * Interface for fragments to be notified abut when they are swiped to or from
 */
public abstract class NarwhalVisionFragment extends Fragment
{
	public void onSwapIn() {}

	public void onSwapOut() {}

	/**
	 * Called by the activity when the OpenCV library is finished loading.
	 *
	 * We can't load OpenCV in the fragments because it somehow breaks the OpenCV installer
	 */
	public void onOpenCVLoaded() {}
}
