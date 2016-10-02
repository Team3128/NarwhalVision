package org.team3128.narwhalvision;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;

public class NarwhalVisionActivity extends FragmentActivity
{

	private final static String PREFS_FILE_NAME = "NarwhalVision";

	private final int NUM_FRAGMENTS = 3;

	private ViewPager viewPager;

	//lazily initialized as pages are created
	private PageSwapListenerFragment[] pages = new PageSwapListenerFragment[NUM_FRAGMENTS];

	//used so that we get the old page when the page changes
	private int currentPage = 0;

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

			PageSwapListenerFragment newFragment;
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


	}

	@Override
	protected void onStop()
	{
		super.onStop();
		Settings.savePreferences();
	}
}
