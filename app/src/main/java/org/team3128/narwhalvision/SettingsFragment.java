package org.team3128.narwhalvision;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.stackoverflow.RangeInputFilter;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends NarwhalVisionFragment implements TextWatcher
{
	final static String TAG = "NVSettings";

	EditText editMinH;
	EditText editMinS;
	EditText editMinV;

	EditText editMaxH;
	EditText editMaxS;
	EditText editMaxV;

	EditText editArea;
	EditText editSolidity;
	EditText editHeight;
	EditText editWidth;

	View minHBlock;
	View maxHBlock;
	View minSBlock;
	View maxSBlock;
	View minVBlock;
	View maxVBlock;


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
	public void onStop()
	{
		super.onStop();
		loadEnteredValues();
		Settings.savePreferences();
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

		editArea = (EditText) content.findViewById(R.id.editArea);
		editSolidity = (EditText) content.findViewById(R.id.editSolidity);
		editWidth = (EditText) content.findViewById(R.id.editWidth);
		editHeight = (EditText) content.findViewById(R.id.editHeight);

		maxHBlock = content.findViewById(R.id.maxHView);
		maxSBlock = content.findViewById(R.id.maxSView);
		maxVBlock = content.findViewById(R.id.maxVView);
		minHBlock = content.findViewById(R.id.minHView);
		minSBlock = content.findViewById(R.id.minSView);
		minVBlock = content.findViewById(R.id.minVView);

		//set range filters
		InputFilter[] byteFilter = new InputFilter[]{new RangeInputFilter(0, 255)};

		editMinH.setFilters(byteFilter);
		editMinS.setFilters(byteFilter);
		editMinV.setFilters(byteFilter);
		editMaxH.setFilters(byteFilter);
		editMaxS.setFilters(byteFilter);
		editMaxV.setFilters(byteFilter);

		InputFilter[] percentFilter = new InputFilter[]{new RangeInputFilter(0, 100)};
		editArea.setFilters(percentFilter);
		editSolidity.setFilters(percentFilter);

		populateFromSettings();
		updateColorViews();

		//set change listeners
		//NOTE: we need to do this AFTER populating the fields, or things get screwed up
		editMinH.addTextChangedListener(this);
		editMinS.addTextChangedListener(this);
		editMinV.addTextChangedListener(this);
		editMaxH.addTextChangedListener(this);
		editMaxS.addTextChangedListener(this);
		editMaxV.addTextChangedListener(this);
		
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

		editArea.setText(Integer.toString(Settings.minArea));
		editSolidity.setText(Integer.toString(Settings.targetSolidity));
		editHeight.setText(Integer.toString(Settings.targetHeight));
		editWidth.setText(Integer.toString(Settings.targetWidth));

	}

	private void loadEnteredValues()
	{
		try
		{
			//TODO: don't parse blank fields

			Settings.lowH = Integer.parseInt(editMinH.getText().toString());
			Settings.lowS = Integer.parseInt(editMinS.getText().toString());
			Settings.lowV = Integer.parseInt(editMinV.getText().toString());

			Settings.highH = Integer.parseInt(editMaxH.getText().toString());
			Settings.highS = Integer.parseInt(editMaxS.getText().toString());
			Settings.highV = Integer.parseInt(editMaxV.getText().toString());

			Settings.minArea = Integer.parseInt(editArea.getText().toString());
			Settings.targetSolidity = Integer.parseInt(editSolidity.getText().toString());
			Settings.targetWidth = Integer.parseInt(editWidth.getText().toString());
			Settings.targetHeight = Integer.parseInt(editHeight.getText().toString());
		}
		catch(NumberFormatException ex)
		{
			//shouldn't happen if I have the right validation set up, but paranoia
			Toast.makeText(getContext(), "Invalid settings values, failed to parse!", Toast.LENGTH_SHORT).show();
			ex.printStackTrace();
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{

	}

	/**
	 * wrapper around Android's hsv conversion method that deals with the weird arguments
	 * @param h
	 * @param s
	 * @param v
	 * @return
	 */
	public int colorFromHSV(int h, int s, int v)
	{
		return Color.HSVToColor(new float[]{h * 360F / 255F, s/255F, v/255F});
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		Log.d(TAG, "Updating colors");

		loadEnteredValues();

		updateColorViews();
	}
	
	private void updateColorViews()
	{
		//update color previews
		minHBlock.setBackgroundColor(colorFromHSV(Settings.lowH, 255, 255));
		maxHBlock.setBackgroundColor(colorFromHSV(Settings.highH, 255, 255));
		minSBlock.setBackgroundColor(colorFromHSV(Settings.lowH, Settings.lowS, 255));
		maxSBlock.setBackgroundColor(colorFromHSV(Settings.highH, Settings.highS, 255));
		minVBlock.setBackgroundColor(colorFromHSV(0, 0, Settings.lowV));
		maxVBlock.setBackgroundColor(colorFromHSV(0, 0, Settings.highV));
	}
}
