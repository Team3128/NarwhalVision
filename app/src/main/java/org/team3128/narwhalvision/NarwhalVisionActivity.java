package org.team3128.narwhalvision;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.WindowManager;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

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
	//OpenCV objects cannot be created until the library has been loaded
	private boolean openCVLoaded;


	private NsdManager mdnsDiscoverer;
	private RoborioDiscoveryListener roborioDiscoveryListener;
	private RoborioResolveListener roborioResolveListener;
	private final static String ROBORIO_SERVICE_TYPE="_http._tcp.";

	//------------------------------------
	private Kryo kryo;
	private ByteBufferOutput packetWriter;


	private DatagramSocket roborioSocket;

	//legal port per the 2016 game manual
	final static int NARWHAL_VISION_PORT = 3128;

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
			if(serviceInfo.getServiceName().contains("roboRIO-3128-FRC"))
			{
				Log.i(TAG, "It's the RoboRIO mDNS!  Hooray!");
				mdnsDiscoverer.resolveService(serviceInfo, roborioResolveListener);
			}
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

	private class RoborioResolveListener implements NsdManager.ResolveListener
	{

		@Override
		public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode)
		{
			Log.e(TAG, "Failed to resolve RoboRIO: error " + errorCode);
		}

		@Override
		public void onServiceResolved(NsdServiceInfo serviceInfo)
		{
			onGetNewRoborioAddress(serviceInfo.getHost());
		}
	}

	/**
	 * Called when we get a new RoboRIO address over mDNS
	 */
	private void onGetNewRoborioAddress(InetAddress roborioAddress)
	{
		//create it if it hasn't been created yet
		if (roborioSocket == null)
		{
			try
			{
				roborioSocket = new DatagramSocket();
			}
			catch (SocketException e)
			{
				Log.e(TAG, "Failed to create roborio socket!");
				e.printStackTrace();
			}
		}

		if (roborioSocket.getInetAddress() != roborioAddress) //check to make sure it isn't the same one
		{
			roborioSocket.connect(roborioAddress, NARWHAL_VISION_PORT);

			if (pages[0] != null && currentPage == 0)
			{
				((CameraFragment) pages[0]).onRIOConnected();
			}
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

		//-------------------------------------------------------------------
		// Set up paged layout
		//-------------------------------------------------------------------

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

		//-------------------------------------------------------------------
		// Set up mDNS
		//-------------------------------------------------------------------

		mdnsDiscoverer = (NsdManager) getSystemService(Context.NSD_SERVICE);
		roborioDiscoveryListener = new RoborioDiscoveryListener();
		roborioResolveListener = new RoborioResolveListener();
		mdnsDiscoverer.discoverServices(ROBORIO_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, roborioDiscoveryListener);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		try
		{
			onGetNewRoborioAddress(InetAddress.getByName("192.168.1.162"));
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}

		//-------------------------------------------------------------------
		// Set up serializer
		//-------------------------------------------------------------------

		kryo = new Kryo();
		kryo.register(TargetInformation.class);

		packetWriter = new ByteBufferOutput();
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

	boolean notifyFirstFragmentOpenCVLoaded = false;

	// callback called when OpenCV is loaded
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");
					openCVLoaded = true;

					/*
					There is a race condition here.  We kick off the OpenCV loader and the Fragment loader at the same time.  If the fragments finish first, everything is fine,
					as we call onOpenCVLoaded() here. If OpenCV loads first, however, we need to wait until the fragments are finished loading.
					 */
					if(pages[currentPage] == null)
					{
						notifyFirstFragmentOpenCVLoaded = true;
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

	void onFragmentLoaded(NarwhalVisionFragment fragment)
	{
		if(notifyFirstFragmentOpenCVLoaded)
		{
			fragment.onOpenCVLoaded();
		}
	}

	public boolean hasFoundRIO()
	{
		return roborioSocket != null && roborioSocket.isConnected();
	}

	// static buffer used to hold serialized objects
	// no, as far as I can tell, there's no way to not have a fixed size buffer
	final static int SERIALIZATION_BUFFER_SIZE=1024;

	public void sendTargetInformation(ArrayList<TargetInformation> infoList)
	{
		if(roborioSocket != null)
		{
			ByteBuffer serializedBytes = ByteBuffer.allocate(SERIALIZATION_BUFFER_SIZE);
			packetWriter.setBuffer(serializedBytes);

			for(TargetInformation info : infoList)
			{
				kryo.writeObject(packetWriter, info);
			}

			packetWriter.flush();

			DatagramPacket packet = new DatagramPacket(serializedBytes.array(), SERIALIZATION_BUFFER_SIZE);

			try
			{
				roborioSocket.send(packet);
			}
			catch (IOException e)
			{
				Log.e(TAG, "Failed to send target information to RoboRIO: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
