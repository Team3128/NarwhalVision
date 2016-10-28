package org.team3128.narwhalvision;


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
import android.widget.Space;
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

	Space minHBlock;
	Space maxHBlock;
	Space minSBlock;
	Space maxSBlock;
	Space minVBlock;
	Space maxVBlock;


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

		maxHBlock = (Space) content.findViewById(R.id.maxHSpace);
		maxSBlock = (Space) content.findViewById(R.id.maxSSpace);
		maxVBlock = (Space) content.findViewById(R.id.maxVSpace);
		minHBlock = (Space) content.findViewById(R.id.minHSpace);
		minSBlock = (Space) content.findViewById(R.id.minSSpace);
		minVBlock = (Space) content.findViewById(R.id.minVSpace);

		//set range filters
		InputFilter[] byteFilter = new InputFilter[]{new RangeInputFilter(0, 255)};

		editMinH.setFilters(byteFilter);
		editMinS.setFilters(byteFilter);
		editMinV.setFilters(byteFilter);
		editMaxH.setFilters(byteFilter);
		editMaxS.setFilters(byteFilter);
		editMaxV.setFilters(byteFilter);

		//set change listeners
		editMinH.addTextChangedListener(this);
		editMinS.addTextChangedListener(this);
		editMinV.addTextChangedListener(this);
		editMaxH.addTextChangedListener(this);
		editMaxS.addTextChangedListener(this);
		editMaxV.addTextChangedListener(this);

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

		editArea.setText(Integer.toString(Settings.targetArea));
		editSolidity.setText(Integer.toString(Settings.targetSolidity));
		editHeight.setText(Integer.toString(Settings.targetHeight));
		editWidth.setText(Integer.toString(Settings.targetWidth));

	}

	private void loadEnteredValues()
	{
		try
		{
			Settings.lowH = Integer.parseInt(editMinH.getText().toString());
			Settings.lowS = Integer.parseInt(editMinS.getText().toString());
			Settings.lowV = Integer.parseInt(editMinV.getText().toString());

			Settings.highH = Integer.parseInt(editMaxH.getText().toString());
			Settings.highS = Integer.parseInt(editMaxS.getText().toString());
			Settings.highV = Integer.parseInt(editMaxV.getText().toString());

			Settings.targetArea = Integer.parseInt(editArea.getText().toString());
			Settings.targetSolidity = Integer.parseInt(editSolidity.getText().toString());
			Settings.targetWidth = Integer.parseInt(editHeight.getText().toString());
			Settings.targetHeight = Integer.parseInt(editWidth.getText().toString());
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

	@Override
	public void afterTextChanged(Editable s)
	{

	}
}
