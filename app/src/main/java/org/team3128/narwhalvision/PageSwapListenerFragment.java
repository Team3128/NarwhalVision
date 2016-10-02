package org.team3128.narwhalvision;

import android.support.v4.app.Fragment;

/**
 * Interface for fragments to be notified abut when they are swiped to or from
 */
public abstract class PageSwapListenerFragment extends Fragment
{
	public void onSwapIn() {}

	public void onSwapOut() {}
}
