package com.example.filter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateReceiver extends BroadcastReceiver 
{
	public final static String LOG_TAG = "PhoneStateReceiver";

	@Override
    public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.PHONE_STATE")) { 
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(LOG_TAG, "Call State=" + state);
 
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d("PhoneStateReceiver", "Idle");
            } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) { 
                // Incoming call
                String incomingNumber =  intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(LOG_TAG, "Incoming call " + incomingNumber);
                
//                if(incomingNumber.equals("15555215556"))
//        		{	
//                	abortBroadcast();
//        		}
                
//                // Implicitly start a Service
        		Intent myIntent = new Intent(context, CallFilterService.class);
        		myIntent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, incomingNumber);
        		context.startService(myIntent);
        	} else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Log.d(LOG_TAG, "Offhook");
            }
        }
		/* 
		else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) { 
            // Outgoing call
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(LOG_TAG, "Outgoing call " + outgoingNumber);
            //setResultData(null); // Kills the outgoing call
         } else {
            Log.e(LOG_TAG, "unexpected intent.action=" + intent.getAction());
        }
        */
    }
}