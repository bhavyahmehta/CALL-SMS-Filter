package com.example.filter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSStateReceiver extends BroadcastReceiver 
{

	public void onReceive(Context context, Intent intent) 
	{ 
	    String MSG_TYPE=intent.getAction();
	    
	    
	     if(MSG_TYPE.equals("android.provider.Telephony.SMS_RECEIVED"))
	    {
			 Toast toast = Toast.makeText(context,"SMS Received: "+MSG_TYPE , Toast.LENGTH_LONG);
			 toast.show();
	
		    Bundle bundle = intent.getExtras();
		    Object messages[] = (Object[]) bundle.get("pdus");
		    SmsMessage smsMessage[] = new SmsMessage[messages.length];
		    for (int n = 0; n < messages.length; n++) 
		    {
		        smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
		    }
		    
		    // show first message
		      Log.e(""," smsMessage[0].getDisplayOriginatingAddress()=="+smsMessage[0].getDisplayOriginatingAddress()+"  smsMessage[0].getOriginatingAddress()=="+smsMessage[0].getOriginatingAddress()+"  smsMessage[0].getServiceCenterAddress()=="+smsMessage[0].getServiceCenterAddress());
		      final SharedPreferences mpref =context.getSharedPreferences("BLOCK",context.MODE_PRIVATE);
		      String str=mpref.getString("phonesms","Phone");
		      Log.e("phone no==",""+smsMessage[0].getOriginatingAddress());
		      if(str.contains("SMS") && smsMessage[0].getOriginatingAddress().equals(mpref.getString("phoneno", "0000")))
			  {
				  	toast = Toast.makeText(context,"BLOCKED Received SMS: " + smsMessage[0].getMessageBody(), Toast.LENGTH_LONG);
				    toast.show();
			        abortBroadcast();
			  }
	    	
	
	    }
	    else if(MSG_TYPE.equals("android.provider.Telephony.SEND_SMS"))
	    {
//	        Toast toast = Toast.makeText(context,"SMS SENT: "+MSG_TYPE , Toast.LENGTH_LONG);
//	        toast.show();
//	        abortBroadcast();
//	        for(int i=0;i<8;i++)
//	        {
//	            System.out.println("Blocking SMS **********************");
//	        }
	
	    }
	    else
	    {
	
	        Toast toast = Toast.makeText(context,"SIN ELSE: "+MSG_TYPE , Toast.LENGTH_LONG);
	        toast.show();
	        abortBroadcast();
	        for(int i=0;i<8;i++)
	        {
	            System.out.println("Blocking SMS **********************");
	        }
	
	    }
	
	}

}