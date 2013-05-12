/*
 * Copyright 2011, Qualcomm Innovation Center, Inc.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package wifi;

import android.app.Activity;
import android.app.Dialog;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import android.util.Log;

import java.util.List;
import com.kellislabs.bartsy.*;
import com.kellislabs.bartsy.R.id;
import com.kellislabs.bartsy.R.layout;

public class AllJoynDialogBuilder {
    private static final String TAG = "Bartsy";
    
    
    public Dialog createAllJoynErrorDialog(Activity activity, final BartsyApplication application) {
       	Log.i(TAG, "createAllJoynErrorDialog()");
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.alljoynerrordialog);
    	
    	TextView errorText = (TextView)dialog.findViewById(R.id.errorDescription);
        errorText.setText(application.getErrorString());
	        	       	
    	Button yes = (Button)dialog.findViewById(R.id.errorOk);
    	yes.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			dialog.cancel();
    		}
    	});
    	
    	return dialog;
    }
}
