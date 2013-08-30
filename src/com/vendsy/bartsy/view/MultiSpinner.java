package com.vendsy.bartsy.view;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * This is a custom implementation of MultiSpinner. It looks similar to
 * normal spinner, but it has checkboxes instead of radiobuttons. Selected
 * values are displayed on the spinner divided by comma. All values are unchecked
 * by default.
 * 
 * @author Spoorthy
 * 
 */
public class MultiSpinner extends Spinner implements
		OnMultiChoiceClickListener, OnCancelListener {

	private List<String> items;
	private boolean[] selected;
	private String defaultText;
	private MultiSpinnerListener listener;

	public MultiSpinner(Context context) {
		super(context);
	}

	public MultiSpinner(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
	}

	public MultiSpinner(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		if (isChecked)
			selected[which] = true;
		else
			selected[which] = false;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// Refresh text on spinner
		StringBuffer spinnerBuffer = new StringBuffer();
		boolean someUnselected = false;
		// Append text with comma separated
		for (int i = 0; i < items.size(); i++) {
			if (selected[i] == true) {
				spinnerBuffer.append(items.get(i));
				spinnerBuffer.append(", ");
			} else {
				someUnselected = true;
			}
		}
		String spinnerText;
		if (someUnselected) {
			spinnerText = spinnerBuffer.toString();
			if (spinnerText.length() > 2)
				spinnerText = spinnerText
						.substring(0, spinnerText.length() - 2);
		} else {
			spinnerText = defaultText;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item,
				new String[] { spinnerText });
		setAdapter(adapter);
		if (listener != null)
			listener.onItemsSelected(selected);
	}

	@Override
	public boolean performClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMultiChoiceItems(
				items.toArray(new CharSequence[items.size()]), selected, this);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		builder.setOnCancelListener(this);
		builder.show();
		return true;
	}
	
	// To set selected items to the spinner as comma separated text
	public void setItems(List<String> items, String allText,
			MultiSpinnerListener listener) {
		this.items = items;
		this.defaultText = allText;
		this.listener = listener;

		// All unselected by default
		selected = new boolean[items.size()];
		for (int i = 0; i < selected.length; i++)
			selected[i] = false;

		// All text on the spinner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item, new String[] { allText });
		setAdapter(adapter);

	}

	public interface MultiSpinnerListener {
		public void onItemsSelected(boolean[] selected);
	}
}