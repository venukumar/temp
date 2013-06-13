package com.vendsy.bartsy.dialog;

import com.vendsy.bartsy.R;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class OfferDrinkDialog extends Dialog {
	private LinearLayout villageLayout = null;
	private Context context;
	private Spinner sectionSpinner;
	private int typeId;

	public OfferDrinkDialog(Context context) {
		super(context);
		this.context = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.offer_drink_dialog);
		setCancelable(true);
		getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		
		
	}
}