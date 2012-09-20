package com.example.filter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.*;


public class CallFilterService extends Service 
{
	ArrayList<String> m_toDelete;
	TelephonyManager m_telManager;
	ContentResolver m_res;
	//SharedPreferences m_prefs;
	Resources m_resource;
	String msg_blackList;
	String msg_rejectContact;
	String msg_stranger;
	ITelephony m_telInterface;
	Method m_methodEndCall;
	CallLogObserver m_observer = new CallLogObserver(new Handler());
	boolean m_muted = false;
	AudioManager m_am;
	
	static final String LOG_TAG = "CallFilterService";
	/** Keeps track of all current registered clients. */
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_NEW_REJECT = 3;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	SharedPreferences mpref;
	
	/**
	 * Handler of incoming messages from clients.
	 */
	class IncomingHandler extends Handler 
	{
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		return mMessenger.getBinder();
	}

	/** send message to tell others that new rejected record is added. */
	private void newReject() 
	{
		for (int i = mClients.size()-1; i>=0; i--) 
		{
			try {
				mClients.get(i).send(Message.obtain(null, MSG_NEW_REJECT, 0, 0));
			} catch (RemoteException e) {
				// The client is dead.  Remove it from the list;
				// we are going through the list from back to front
				// so this is safe to do inside the loop.
				mClients.remove(i);
			}
		}
	}
	
	/** callback to check if call log is changed. */
	class CallLogObserver extends ContentObserver {
		public CallLogObserver(Handler h) {
			super(h);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return false;
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.d(LOG_TAG, "CallLogObserver.onChange(" + selfChange + ")");
			super.onChange(selfChange);
			CallFilterService.this.clearCallLogs();
		}
	};
	
	@Override
	public void onCreate() 
	{
		m_am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		m_toDelete = new ArrayList<String>();
		m_res = getContentResolver();
		m_res.registerContentObserver(CallLog.Calls.CONTENT_URI, false, m_observer); 
		m_telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mpref =this.getSharedPreferences("BLOCK",MODE_PRIVATE);
	   
		try 
		{
			// Get the getITelephony() method
			Class classTelephony = Class.forName(m_telManager.getClass().getName());
			Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");
	 
			// Ignore that the method is supposed to be private
			methodGetITelephony.setAccessible(true);
	 
			// Invoke getITelephony() to get the ITelephony interface
			m_telInterface = (ITelephony) methodGetITelephony.invoke(m_telManager);
	 
	        // Get the endCall method from ITelephony
	        Class telephonyInterfaceClass = Class.forName(m_telInterface.getClass().getName());
	        m_methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");
		} 
		catch (Exception ex) 
		{
		 // Many things can go wrong with reflection calls
	      Log.e(LOG_TAG, "error in getting endCall() method: " + ex.toString());
		}
	}

	@Override
	public void onDestroy() {
		m_res.unregisterContentObserver(m_observer);
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		if (intent == null)
			return Service.START_STICKY;
		String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		if (phone != null)
			runFilter(phone);
		return Service.START_STICKY;
	}
	
	private void runFilter(String phone)
	{
		mute();
		if (isPhoneBlocked(phone)) 
		{
			if (!killCall(phone)) 
			{
				Log.e(LOG_TAG, "Unable to kill incoming call");
    		}
//			boolean enabled = m_prefs.getBoolean("clear_call_log", false);
//			if (enabled)
			m_toDelete.add(phone);
			newReject();
		}
		unmute();
	}
	
	private void clearCallLogs() 
	{
		Log.d(LOG_TAG, "clearCallLogs enter");
		while (!m_toDelete.isEmpty()) 
		{
			String head = m_toDelete.remove(0);
			Log.d(LOG_TAG, "clearCallLogs removes call log for " + head);
			deleteRecentCall(this, head);
		}
		Log.d(LOG_TAG, "clearCallLogs exit");
	}
	
	private void deleteRecentCall(Context context, String phone) 
	{
		Uri uriCalls = CallLog.Calls.CONTENT_URI;  
	    String fields[] = {CallLog.Calls._ID};
	    String selection = CallLog.Calls.NUMBER + "=?" ;
        Cursor cursor = m_res.query(uriCalls, fields, selection, new String[] {phone}, 
        		CallLog.Calls.DATE + " DESC limit 1");
        if (!cursor.moveToFirst()) {
           	cursor.close();
        	return;
        }
        String id = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls._ID));
        cursor.close();
        int n = m_res.delete(uriCalls, CallLog.Calls._ID +"=?", new String[]{id});
        Log.d(LOG_TAG, "deleteRecentCall removed " + n + " records");
	}
	
	private boolean isPhoneBlocked(String phone) 
	{
		String str=mpref.getString("phonesms","Phone");
	    if(str.contains("Phone") && phone.contains(mpref.getString("phoneno", "0000")))
		{
			
			//Log.e(LOG_TAG,"BLOCKED===>>"+new PhoneLog().getPhone_no());	
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	public boolean killCall(String head) 
	{
		try 
		{
			m_telInterface.endCall();
			deleteRecentCall(this, head);
			Log.d(LOG_TAG, "killCall() success");
		} 
		catch (Exception ex) 
		{ // Many things can go wrong with reflection calls
			Log.e(LOG_TAG, "killCall() error: " + ex.toString());
			return false;
		}
		return true;
	}
	
	private void mute() {
		if (!m_muted) {
			m_am.setStreamMute(AudioManager.STREAM_RING, true);
			m_muted = true;
		}
	}

	private void unmute() {
		if (m_muted) {
			m_am.setStreamMute(AudioManager.STREAM_RING, false);
			m_muted = false;
		}
	}
}
