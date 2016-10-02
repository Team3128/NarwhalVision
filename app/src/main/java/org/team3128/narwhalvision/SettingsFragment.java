package org.team3128.narwhalvision;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.stackoverflow.RangeInputFilter;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PageSwapListenerFragment
{
	final static String TAG = "NVSettings";

	EditText editMinH;
	EditText editMinS;
	EditText editMinV;

	EditText editMaxH;
	EditText editMaxS;
	EditText editMaxV;


	public SettingsFragment()
	{
		// Required empty public constructor
		Log.d(TAG, "Constructed");
	}

	@Override
	public void onSwapOut()
	{
		loadEnteredValues();

		Log.d(TAG, "Saved Settings");
	}

	@Override
	public void onSwapIn()
	{
		//just in case something else changed a setting
		populateFromSettings();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.d("NVSettings", "onCreateView");

		View content = inflater.inflate(R.layout.nv_settings, container, false);

		editMinH = (EditText) content.findViewById(R.id.editMinH);
		editMinS = (EditText) content.findViewById(R.id.editMinS);
		editMinV = (EditText) content.findViewById(R.id.editMinV);

		editMaxH = (EditText) content.findViewById(R.id.editMaxH);
		editMaxS = (EditText) content.findViewById(R.id.editMaxS);
		editMaxV = (EditText) content.findViewById(R.id.editMaxV);

		//set range filters
		InputFilter[] byteFilter = new InputFilter[]{new RangeInputFilter(0, 255)};

		editMinH.setFilters(byteFilter);
		editMinS.setFilters(byteFilter);
		editMinV.setFilters(byteFilter);
		editMaxH.setFilters(byteFilter);
		editMaxS.setFilters(byteFilter);
		editMaxV.setFilters(byteFilter);

		populateFromSettings();

		return content;
	}

	private void populateFromSettings()
	{
		editMinH.setText(Integer.toString(Settings.lowH));
		editMinS.setText(Integer.toString(Settings.lowS));
		editMinV.setText(Integer.toString(Settings.lowV));

		editMaxH.setText(Integer.toString(Settings.highH));
		editMaxS.setText(Integer.toString(Settings.highS));
		editMaxV.setText(Integer.toString(Settings.highV));
	}

	private void loadEnteredValues()
	{
		Settings.lowH = Integer.parseInt(editMinH.getText().toString());
		Settings.lowS = Integer.parseInt(editMinS.getText().toString());
		Settings.lowV = Integer.parseInt(editMinV.getText().toString());

		Settings.highH = Integer.parseInt(editMaxH.getText().toString());
		Settings.highS = Integer.parseInt(editMaxS.getText().toString());
		Settings.highV = Integer.parseInt(editMaxV.getText().toString());

	}

}
