package org.team3128.narwhalvision;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class NarwhalVisionActivity extends FragmentActivity
{

	private final static String TAG = "NarwhalVision";
	private final static String PREFS_FILE_NAME = TAG;

	private final int NUM_FRAGMENTS = 3;

	private ViewPager viewPager;

	//lazily initialized as pages are created
	private NarwhalVisionFragment[] pages = new NarwhalVisionFragment[NUM_FRAGMENTS];

	//used so that we get the old page when the page changes
	private int currentPage = 0;

	private NsdManager mdnsDiscoverer;

	//OpenCV objects cannot be created until the library has been loaded
	private boolean openCVLoaded;

	private class RoborioDiscoveryListener implements NsdManager.DiscoveryListener
	{
		@Override
		public void onServiceLost(NsdServiceInfo service) {
			// When the network service is no longer available.
			// Internal bookkeeping code goes here.
			Log.e(TAG, "service lost" + service);
		}

		@Override
		public void onDiscoveryStopped(String serviceType) {
			Log.i(TAG, "Discovery stopped: " + serviceType);
		}

		@Override
		public void onServiceFound(NsdServiceInfo serviceInfo)
		{
			Log.i(TAG, "Found service: " + serviceInfo.toString());
			//if(serviceInfo.getServiceType().equals())
		}

		@Override
		public void onStartDiscoveryFailed(String serviceType, int errorCode) {
			Log.e(TAG, "Discovery failed: Error code:" + errorCode);
		}

		@Override
		public void onStopDiscoveryFailed(String serviceType, int errorCode) {
			Log.e(TAG, "Discovery failed: Error code:" + errorCode);
		}

		@Override
		public void onDiscoveryStarted(String serviceType)
		{

		}

	}


	public class NVPagerAdapter extends FragmentPagerAdapter
	{

		public NVPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		// Returns total number of pages
		@Override
		public int getCount() {
			return NUM_FRAGMENTS;
		}

		// Returns the fragment to display for that page
		@Override
		public Fragment getItem(int position) {

			NarwhalVisionFragment newFragment;
			switch (position)
			{
				case 0:
					newFragment = new CameraFragment();
					break;
				case 1:
					newFragment = new SettingsFragment();
					break;
				case 2:
					newFragment = new ImageFileFragment();
					break;
				default:
					newFragment = null;
			}

			//cache the new fragment for the OnPageChangeListener
			pages[position] = newFragment;

			return newFragment;
		}

		// Returns the page title for the top indicator
		@Override
		public CharSequence getPageTitle(int position) {
			return "Page " + position;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Settings.setSharedPreferences(getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE));

		viewPager = new ViewPager(this);
		viewPager.setAdapter(new NVPagerAdapter(getSupportFragmentManager()));
		setContentView(viewPager);
		viewPager.setId(R.id.viewPager);

		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
			}

			@Override
			public void onPageSelected(int position)
			{
				if(pages != null)
				{
					if (pages[currentPage] != null)
					{
						pages[currentPage].onSwapOut();
					}

					currentPage = position;

					if (pages[position] != null)
					{
						pages[position].onSwapIn();
						if(openCVLoaded)
						{
							pages[position].onOpenCVLoaded();
						}
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{

			}
		});

		currentPage = viewPager.getCurrentItem();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mdnsDiscoverer = (NsdManager) getSystemService(Context.NSD_SERVICE);
		mdnsDiscoverer.discoverServices("*", NsdManager.PROTOCOL_DNS_SD, new RoborioDiscoveryListener());
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		Settings.savePreferences();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	// callback called when OpenCV is loaded
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");
					openCVLoaded = true;

					if(pages[currentPage] == null)
					{
						Log.e(TAG, "...what?  OpenCV loaded before the UI did");
					}
					else
					{
						pages[currentPage].onOpenCVLoaded();
					}

				} break;
				default:
				{
					Log.i(TAG, "OpenCV loading failed: status " + status); //why couldn't they use an actual enum?

					super.onManagerConnected(status);
				} break;
			}
		}
	};
}
