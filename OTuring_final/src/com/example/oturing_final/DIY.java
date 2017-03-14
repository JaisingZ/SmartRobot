package com.example.oturing_final;



import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.segmentation.IKA;
import com.example.segmentation.Keys;
import com.example.segmentation.MyDatabaseHelper;

public class DIY extends Activity implements OnClickListener{

	public MyDatabaseHelper dbHelper;
	private EditText ask;
	private EditText ans;
	private Button add;
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.diy);
		dbHelper = new MyDatabaseHelper(this, "test.db", 2);
		dbHelper.getWritableDatabase();
		ask = (EditText)findViewById(R.id.editTextin);
		ans = (EditText)findViewById(R.id.editTextout);
		add = (Button)findViewById(R.id.add);
		add.setOnClickListener(this);
		ask.setOnClickListener(this);
		ans.setOnClickListener(this);
	
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
		case R.id.add:
			if("".equals(ask.getText().toString())|| "".equals(ans.getText().toString())){
				Toast.makeText(this, "涓嶈兘涓虹┖锛�", Toast.LENGTH_LONG).show();
			}
			else{
			
				final SQLiteDatabase db = dbHelper.getWritableDatabase();
		    	final String t1 = ask.getText().toString();	
		    	final String t2 = ans.getText().toString();
		    	ask.setText("");
		    	ans.setText("");
		    	new Thread(new Runnable() {			
		    		@Override			
		    		public void run()  {			
		    			ContentValues values = new ContentValues();
						Keys res = new Keys();
						res = IKA.get_Keys2(t1);
						values.put("ask", t1);
						values.put("ans", t2);
						values.put("key1", res.get_str_i(0));
						values.put("key2", res.get_str_i(1));
						values.put("key3", res.get_str_i(2));
						values.put("key4", res.get_str_i(3));
						values.put("key5", res.get_str_i(4));
						values.put("key6", res.get_str_i(5));
						values.put("key7", res.get_str_i(6));
						values.put("key8", res.get_str_i(7));
						values.put("key9", res.get_str_i(8));
						values.put("key10", res.get_str_i(9));
						db.insert("chat", null, values);
						values.clear();		
		    		}		
		    	}).start();
		    	Toast.makeText(DIY.this, "娣诲姞鎴愬姛", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.editTextin:
			break;
		case R.id.editTextout:
			break;
		default:
			break;		
		}
		
	}
}
