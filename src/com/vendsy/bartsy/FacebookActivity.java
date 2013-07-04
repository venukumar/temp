package com.vendsy.bartsy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.vendsy.bartsy.model.UserProfile;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class FacebookActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    setContentView(R.layout.facebook_layout);

    // start Facebook Login
    Session.openActiveSession(this, true, new Session.StatusCallback() {

      // callback when session changes state
      @Override
      public void call(Session session, SessionState state, Exception exception) {
        if (session.isOpened()) {

          // make request to the /me API
          Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

            // callback after Graph API response with user object
            @Override
            public void onCompleted(GraphUser user, Response response) {
              if (user != null) {
            	// It will go back to Init Activity
            	  
            	BartsyApplication app = (BartsyApplication)getApplication();
            	app.mUserProfileActivityInput = null;
            	app.mUserProfileActivityInput = new UserProfile(user);
            	
  				Intent result = new Intent();
  				setResult(InitActivity.RESULT_OK, result);
  				
              }
              finish();
            }
            
            
          });
        }else{
        	finish();
        }
      }
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
  }

}