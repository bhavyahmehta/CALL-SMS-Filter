package com.example.filter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AfterBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) 
		 {
		     Intent serviceLauncher = new Intent(context, CallFilterService.class);
		     context.startService(serviceLauncher);
		     Log.v("AfterBootReceiver", "Call Filter Service loaded at start");
		  }
	}
}
