package com.example.filter;

import com.example.filter.R;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity 
{
	String result;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText text=(EditText)findViewById(R.id.phoneno);
        Button btn=(Button)findViewById(R.id.button1);
        Spinner sp1=(Spinner)findViewById(R.id.spinner1);
        final String [] array=getResources().getStringArray(R.array.block);
        result=array[0];
        
        //final PhoneLog app=(PhoneLog) getApplicationContext();
        final SharedPreferences mpref =this.getSharedPreferences("BLOCK",MODE_PRIVATE);
        
        sp1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				result=array[arg2];
				Log.i("","arg1=="+arg1+" arg2"+arg2+" arg3"+arg3+" arg0"+arg0);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				 result=array[0];
			}
		});
        
        
        
        btn.setOnClickListener(new OnClickListener()
        {
			
			@Override
			public void onClick(View v) 
			{
				SharedPreferences.Editor mSharedEditor=mpref.edit();
				mSharedEditor.putString("phoneno",text.getText().toString());
				mSharedEditor.putString("phonesms", result);
				mSharedEditor.commit();
				
				((TextView)findViewById(R.id.textView1)).setText(text.getText().toString()+" "+result+" is blocked");
			}
			
		});
    }

}
