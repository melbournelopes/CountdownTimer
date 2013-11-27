package com.tut.servicebroadcastcommunication;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class CountdownService extends Service {
	private final IBinder mBinder = new MyBinder();
	private CountDownTimer countDownTimer;
	SharedPreferences sharedPreferences;
	Editor editor;
	@Override
	public void onCreate() {
		super.onCreate();
		
//		Toast.makeText(getApplicationContext(),"OnCreate()" , Toast.LENGTH_SHORT).show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		Toast.makeText(getApplicationContext(),"OnStartCommand()" , Toast.LENGTH_SHORT).show();
		/**Get the default shared preferences*/
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		/**IMPORTANT: The START_STICKY parameter ensures that the service is started only once.*/
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
//		Toast.makeText(getApplicationContext(),"OnBind()" , Toast.LENGTH_SHORT).show();
		return mBinder;
	}

	public class MyBinder extends Binder {
		CountdownService getService() {
			return CountdownService.this;
		}
	}
	
	public class MyCountDownTimer extends CountDownTimer {
		Intent i = new Intent();
		public MyCountDownTimer(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onFinish() {
			editor = sharedPreferences.edit();
			editor.putBoolean("isStarted", false);
			editor.commit();
			i.setAction(Commons.BROADCAST_COUNTER_FINISH);
			getApplicationContext().sendBroadcast(i);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			i.setAction(Commons.BROADCAST_COUNTER_UPDATE);
			i.putExtra("time", String.valueOf(millisUntilFinished));

			getApplicationContext().sendBroadcast(i);
		}
	}

	public void startCounter(long startTime, long interval) {
		if(countDownTimer != null) {
			countDownTimer.cancel();
			countDownTimer = null;
		}
		countDownTimer = new MyCountDownTimer(startTime, interval);
		countDownTimer.start();
	}

	public void stopCounter() {
		countDownTimer.cancel();

	}

}
