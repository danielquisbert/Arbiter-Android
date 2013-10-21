package com.lmn.Arbiter_Android;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.CordovaPlugin;

import com.lmn.Arbiter_Android.DatabaseHelpers.DbHelpers;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandList;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ToggleButton;

public class MapActivity extends FragmentActivity implements CordovaInterface{
       // private MapMenuEvents menuEvents;
        private ArbiterDialogs dialogs;
        private boolean welcomed;
        
        private CordovaWebView cordovaWebview;
        private String TAG = "MAP_ACTIVITY";
        private final ExecutorService threadPool = Executors.newCachedThreadPool();
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map);
            Config.init(this);
            
            Init(savedInstanceState);
            
            dialogs = new ArbiterDialogs(getResources(), getSupportFragmentManager());

            cordovaWebview = (CordovaWebView) findViewById(R.id.webView1);
            
            
            String url = "file:///android_asset/www/index.html";
            cordovaWebview.loadUrl(url, 5000);
            
            if(!welcomed){
            	displayWelcomeDialog();
            	welcomed = true;
            }
        }

        public void Init(Bundle savedInstanceState){
        	restoreState(savedInstanceState);
            setListeners();
            InitDatabases();
            InitCommandList();
        }
        
        public void InitDatabases(){
        	DbHelpers.getDbHelpers(getApplicationContext());
        }

        /**
         * Get's the CommandList singleton, which starts the CommandExecutor thread
         */
        public void InitCommandList(){
        	CommandList.getCommandList();
        }
        
        /**
         * Set listeners
         */
        public void setListeners(){
        	ImageButton imgButton = (ImageButton) findViewById(R.id.layers_button);
        	imgButton.setOnClickListener(new OnClickListener(){
        		@Override
        		public void onClick(View v){
        			dialogs.showLayersDialog();
        		}
        	});
        }
        
        @Override
        protected void onSaveInstanceState(Bundle outState){
        	saveState(outState);
        	super.onSaveInstanceState(outState);
        }
        
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_map, menu);
            return true;
        }
        
        @Override
        public boolean onOptionsItemSelected(MenuItem item){
        	switch (item.getItemId()) {
        		case R.id.action_new_feature:
        			//menuEvents.activateAddFeatures();;
        			return true;
	        	
        		case R.id.action_servers:
	        		dialogs.showServersDialog();
	        		return true;
	        		
	        	case R.id.action_projects:
	        		Intent projectsIntent = new Intent(this, ProjectsActivity.class);
	        		this.startActivity(projectsIntent);
	        		
	        		return true;
	        	
	        	case R.id.action_make_available_offline:
	        		
	        		return true;
	        		
	        	case R.id.action_settings:
	        		//menuEvents.showSettings(this);
	        		return true;
        		
        		default:
        			return super.onOptionsItemSelected(item);
        	}
        }
        
        public void saveState(Bundle outState){
        	outState.putBoolean("welcomed", welcomed);
        }
        
        public void restoreState(Bundle savedInstanceState){
        	if(savedInstanceState != null){
        		welcomed = savedInstanceState.getBoolean("welcomed");
        	}
        }
        
        public void displayWelcomeDialog(){
        	dialogs.showWelcomeDialog();
        }
        
        public void toggleLayerVisibility(View view){
    		// Is the toggle on?
    		boolean on = ((ToggleButton) view).isChecked();

    		if (on) {

    		} else {

    		}
    	}

        @Override
        protected void onPause() {
                super.onPause();
                Log.d(TAG, "onPause");
        }
        
        @Override 
        protected void onResume(){
        	super.onResume();
        	Log.d(TAG, "onResume");
        }
        
        @Override
        protected void onDestroy(){
        	super.onDestroy();
        	if(this.cordovaWebview != null){
        		cordovaWebview.handleDestroy();
        	}
        }
        
        @Override
        public void onConfigurationChanged(Configuration newConfig) 
        {
            super.onConfigurationChanged(newConfig);
        }
        
        /**
         * Cordova methods
         */
		@Override
		public Activity getActivity() {
			return this;
		}

		@Override
		public ExecutorService getThreadPool() {
			return threadPool;
		}

		@Override
		public Object onMessage(String message, Object obj) {
			Log.d(TAG, message);
            if (message.equalsIgnoreCase("exit")) {
                    super.finish();
            }
            return null;
		}
		
		@Override
		public void setActivityResultCallback(CordovaPlugin cordovaPlugin) {
			Log.d(TAG, "setActivityResultCallback is unimplemented");
			
		}

		@Override
		public void startActivityForResult(CordovaPlugin cordovaPlugin, Intent intent, int resultCode) {
			Log.d(TAG, "startActivityForResult is unimplemented");
			
		}
}

