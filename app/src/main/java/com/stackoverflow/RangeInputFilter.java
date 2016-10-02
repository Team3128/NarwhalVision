package com.stackoverflow;
import android.text.InputFilter;
import android.text.Spanned;

/**
 * from http://stackoverflow.com/questions/14212518/is-there-a-way-to-define-a-min-and-max-value-for-edittext-in-android
 */
public class RangeInputFilter implements InputFilter {

	private int min, max;

	public RangeInputFilter(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public RangeInputFilter(String min, String max) {
		this.min = Integer.parseInt(min);
		this.max = Integer.parseInt(max);
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		try {
			// Remove the string out of destination that is to be replaced
			String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
			// Add the new string in
			newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
			int input = Integer.parseInt(newVal);
			if (isInRange(min, max, input))
				return null;
		} catch (NumberFormatException nfe)
		{
			//do nothing
		}
		return "";
	}

	private boolean isInRange(int a, int b, int c) {
		return b > a ? c >= a && c <= b : c >= b && c <= a;
	}
}