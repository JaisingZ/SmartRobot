package com.example.oturing_final;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.example.segmentation.IKA;
import com.example.segmentation.Keys;

public class MainActivity extends Activity implements OnClickListener{

	private Button chat;
	private Button diy;
	private Button exit;
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		chat = (Button) this.findViewById(R.id.button1);
		diy = (Button)findViewById(R.id.button2);
		exit = (Button)findViewById(R.id.button3);
		chat.setOnClickListener(this);
		diy.setOnClickListener(this);
		exit.setOnClickListener(this);	
		
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.button1:
			Intent intent1 = new Intent(MainActivity.this, ChatActivity.class);
			startActivity(intent1);
			break;
		case R.id.button2:
	//		Intent intent2 = new Intent(MainActivity.this, DIY.class);
	//		startActivity(intent2);
			String result = "鍩轰簬java璇█寮�鍙戠殑杞婚噺绾х殑涓枃鍒嗚瘝宸ュ叿鍖�";
		    int i;
		    Keys k = IKA.get_Keys2(result);
		    for(i=0;i<10;i++){
		    	Log.d("aaa", k.get_str_i(i));
		    }
			break;
		case R.id.button3:
			AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
			alert.setTitle("鎻愮�?");
			alert.setMessage("纭畾閫�鍑猴紵");
			alert.setPositiveButton("鏄�", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialogInterface, int which) {
					finish();
				}
			});
			alert.setNegativeButton("鍚�", new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				    return ;
				}
			});
			alert.show();
			break;
		default:
			break;
		}
		
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
			alert.setTitle("鎻愮�?");
			alert.setMessage("纭畾閫�鍑猴紵");
			alert.setPositiveButton("鏄�", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialogInterface, int which) {
					finish();
				}
			});
			alert.setNegativeButton("鍚�", new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				    return ;
				}
			});
			alert.show();
			break;
		default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
