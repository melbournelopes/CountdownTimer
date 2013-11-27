package com.tut.servicebroadcastcommunication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.tut.servicebroadcastcommunication.CountdownService.MyBinder;
import com.websmithing.broadcasttest.R;

public class MainActivity extends Activity implements OnClickListener,OnItemSelectedListener {
	TextView lblHour,lblMin,lblSec;

	Button btnStart, btnStop;
	Spinner spnValue, spnUnit;
	private Intent intent;
	ArrayAdapter<Integer> adapterValue;
	ArrayAdapter<String> adapterUnit;
	private CountdownService mBroadcastService;
	private long startTime;
	private final long interval = 1 * 1000;
	SharedPreferences sharedPreferences;
	IntentFilter intentFilter;
	boolean hrFlag, minFlag, secFlag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (sharedPreferences.getBoolean("isStarted", false)) {
			btnStart.setVisibility(View.GONE);
			btnStop.setVisibility(View.VISIBLE);
		}else{
			btnStart.setVisibility(View.VISIBLE);
			btnStop.setVisibility(View.GONE);
		}
		/** Register the broadcast receiver */
		registerReceiver(broadcastReceiver, intentFilter);
		/**Start the Service*/
		startService(intent);
		/**Binding to the Service*/
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

	}
	
	@Override
	public void onClick(View v) {
		Editor editor;
		switch (v.getId()) {
		case R.id.btnStart:	
			editor = sharedPreferences.edit();
			editor.putBoolean("isStarted", true);
			editor.commit();

			btnStart.setVisibility(View.GONE);
			btnStop.setVisibility(View.VISIBLE);
			
			mBroadcastService.startCounter(startTime,interval);
		
			break;
		case R.id.btnStop:
			editor = sharedPreferences.edit();
			editor.putBoolean("isStarted", false);
			editor.commit();
		
			btnStart.setVisibility(View.VISIBLE);
			btnStop.setVisibility(View.GONE);
			
			mBroadcastService.stopCounter();
		
			resetAll();
			break;
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long arg3) {
		switch (adapterView.getId()) {
		case R.id.spnUnit:
			if (position == 0) {
				hrFlag = true;
				minFlag = false;
				secFlag = false;
				adapterValue = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item,Commons.arrHours());
				spnValue.setAdapter(adapterValue);
			} else if (position == 1) {
				hrFlag = false;
				minFlag = true;
				secFlag = false;
				adapterValue = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item,Commons.arrMinsSecs());
				spnValue.setAdapter(adapterValue);

			} else {
				hrFlag = false;
				minFlag = false;
				secFlag = true;
				adapterValue = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item,Commons.arrMinsSecs());
				spnValue.setAdapter(adapterValue);
			}
			break;
		case R.id.spnValue:
			if (hrFlag) {
				startTime = Long.parseLong(adapterView.getItemAtPosition(position).toString())* ((1000 * 60) * 60);
			} else if (minFlag) {
				startTime = Long.parseLong(adapterView.getItemAtPosition(position).toString())* (1000 * 60);
			} else {
				startTime = Long.parseLong(adapterView.getItemAtPosition(position).toString()) * 1000;
			}
			break;
		}

	}
	
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		/**
		 * Service connection class is used to get Service instance through
		 * binder object returned by the Service itself
		 */
		public void onServiceConnected(ComponentName className, IBinder mBinder) {
			MyBinder binder = (CountdownService.MyBinder) mBinder;
			
			/**Get the service instance*/
			mBroadcastService = binder.getService();	
		}

		public void onServiceDisconnected(ComponentName className) {
		
		}
	};

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		/** Receives the broadcast that has been fired by Service */
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction()==Commons.BROADCAST_COUNTER_UPDATE){	
				long millisUntilFinished = Long.valueOf(intent.getStringExtra("time"));
				long seconds = (millisUntilFinished / 1000) % 60 ;
				long minutes = ((millisUntilFinished / (1000*60)) % 60);
				long hours   = ((millisUntilFinished / (1000*60*60)) % 24);
				
				lblHour.setText(String.format("%02d", hours));
				lblMin.setText(String.format("%02d", minutes));
				lblSec.setText(String.format("%02d", seconds));
			}else if(intent.getAction()==Commons.BROADCAST_COUNTER_FINISH){
				btnStart.setVisibility(View.VISIBLE);
				btnStop.setVisibility(View.GONE);
				resetAll();
			}
		}
	};

	private void resetAll(){
		lblHour.setText(String.format("%02d", 00));
		lblMin.setText(String.format("%02d", 00));
		lblSec.setText(String.format("%02d", 00));
		
	}
	
	private void init(){
		lblHour = (TextView) findViewById(R.id.lblHour);
		lblMin = (TextView) findViewById(R.id.lblMin);
		lblSec = (TextView) findViewById(R.id.lblSec);
		btnStart = (Button) findViewById(R.id.btnStart);
		btnStop = (Button) findViewById(R.id.btnStop);
		spnValue = (Spinner) findViewById(R.id.spnValue);
		spnUnit = (Spinner) findViewById(R.id.spnUnit);
		
		adapterValue = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_spinner_item, Commons.arrHours());
		spnValue.setAdapter(adapterValue);
		adapterUnit = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Commons.arrUnits());
		spnUnit.setAdapter(adapterUnit);
		
		/**Create intent for Broadcast service*/
		intent = new Intent(this, CountdownService.class);
		
		/**Get the default shared preferences*/
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	
		/**Add intent filter for the broadcasts with respective intents to receive*/
		intentFilter=new IntentFilter();
		intentFilter.addAction(Commons.BROADCAST_COUNTER_UPDATE);
		intentFilter.addAction(Commons.BROADCAST_COUNTER_FINISH);
		
		btnStart.setOnClickListener(this);
		btnStop.setOnClickListener(this);
		spnUnit.setOnItemSelectedListener(this);
		spnValue.setOnItemSelectedListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		/** Unregister the broadcast receiver */
		unregisterReceiver(broadcastReceiver);
		
		/** Unbind the service */
		unbindService(mConnection);
	
	}
	
}