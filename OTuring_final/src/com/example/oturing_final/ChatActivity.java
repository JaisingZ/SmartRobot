package com.example.oturing_final;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore.Audio.Media;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.iflyVoiceRecognizer.VoiceRecognizer;
import com.example.iflyVoiceSynthesizer.VoiceSynthesizer;
import com.example.segmentation.IKA;
import com.example.segmentation.Keys;
import com.example.segmentation.MyDatabaseHelper;
import com.example.segmentation.Similarity;
import com.example.turing.Turing;
import com.example.voice_recorder.MediaManager;
import com.example.voice_recorder.view.AudioRecorderButton;
import com.example.voice_recorder.view.AudioRecorderButton.AudioFinishRecorderListener;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
/**1.
 * ��ӵ��Ч������ʹ��Dialog��
 * 
 *          ��ͨ���			����
 *  
 *  ���� 	ת����			������������
 *  ����		��������			����Ч��
 *  
 *  2.
 *  ͼ��ʶ��
 *  ���񶼲���Free�ġ�����
 *  
 *  3.
 *  �������ͣ�ץȡ��ҳ��������
 *  		
 * @author Jaising
 *
 */

@SuppressLint("NewApi")
public class ChatActivity extends Activity implements 
OnClickListener,OnLongClickListener,OnItemClickListener,OnItemLongClickListener{

	private Turing mTuring;

	private EditText et_input;
	private Button btn_chatting_mode;
	private Button btn_send;
	private AudioRecorderButton mAudioRecorderButton;
	private View mAnimView;
	private RelativeLayout rl_bottom;

	private ListView msgListView;
	private MsgAdapter msgAdapter;
	private List<Msg> msgList = new ArrayList<Msg>();

	private String[] welcome_array;


	//	private static final int TAKE_PICTURE = 1;
	//	private static final int CROP_PICTURE = 2;
	//	private Uri imageUri;
	private static final int CALL = 0x40;
	private static final int SEND_MESSAGE = 0x41;
	private static final int RECORDER = 0x42;


	// ��д����
	private com.iflytek.cloud.SpeechRecognizer mIat;
	// �ϳɶ���
	private SpeechSynthesizer mTts;
	
	public String local_result = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		initial();
	}

	private void initial(){

		msgAdapter = new MsgAdapter(ChatActivity.this,msgList);
		et_input = (EditText) this.findViewById(R.id.et_send);
		et_input.setOnLongClickListener(this);
		btn_chatting_mode = (Button) this.findViewById(R.id.btn_chatting_setmode);
		btn_chatting_mode.setOnClickListener(this);
		btn_send = (Button) this.findViewById(R.id.btn_send);
		btn_send.setOnClickListener(this);
		mAudioRecorderButton = (AudioRecorderButton) findViewById(R.id.id_recorder_button);
		rl_bottom = (RelativeLayout) this.findViewById(R.id.rl_bottom);

		// ����activityʱ���Զ���������� �� ����Ϊ��������ģʽ
		getWindow().setSoftInputMode(  
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  
		rl_bottom.setVisibility(View.VISIBLE);
		mAudioRecorderButton.setVisibility(View.GONE);

		msgListView = (ListView) this.findViewById(R.id.lv_msg);
		msgListView.setAdapter(msgAdapter);
		msgListView.setOnItemClickListener(this);
		msgListView.setOnItemLongClickListener(this);

		//	��ӭ��
		getRandomWelcomeTips();

		//	����ͼ������˽ӿ�
		mTuring = new Turing();

		//	�ƴ�Ѷ������ʶ���ʼ��
		SpeechUtility.createUtility(ChatActivity.this, SpeechConstant.APPID +"=556d6bde");
		mIat = com.iflytek.cloud.SpeechRecognizer.createRecognizer(ChatActivity.this, mAsrInitListener);

		//	�ƴ�Ѷ�������ϳɳ�ʼ��
		mTts = SpeechSynthesizer.createSynthesizer(ChatActivity.this, mTtsInitListener);

	}


	// ��ʼ��Ѷ������������
	private InitListener mAsrInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				System.out.println("��ʼ��ʧ�ܣ������룺" + code);
			} else {
				System.out.println("����ʶ���������ʼ���ɹ�");
			}
		}
	};

	// ��ʼ��Ѷ�ɺϳɼ�����
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				System.out.println("��ʼ��ʧ�ܣ������룺" + code);
			} else {
				System.out.println("�����ϳɼ�������ʼ���ɹ�");
			}
		}
	};

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {

		private String received_text = null;
		Msg receiveFromTuring;

		@Override
		public void handleMessage(Message msg) {

			if (!"".equals(mTuring.getTuringResultWords())) {
				received_text = mTuring.getTuringResultWords();
			}
			else{
				received_text = "û��Ҳ������ģ��ߨq(�s^�t)�r";
			}

			receiveFromTuring = new Msg(received_text, Msg.TYPE_RECEIVED_WORD);
			msgList.add(receiveFromTuring);
			msgAdapter.notifyDataSetChanged();
			msgListView.setSelection(msgList.size());
			super.handleMessage(msg);
		}
	};
	
	@SuppressLint("HandlerLeak")
	Handler handler2 = new Handler(){
		
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
			case 1:
				Msg msg2 = new Msg((String)msg.obj, Msg.TYPE_RECEIVED);
				msgList.add(msg2);
				msgAdapter.notifyDataSetChanged();
				msgListView.setSelection(msgList.size());
				break;
			}
		}
	};

	private Vibrator mVibrator;
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.btn_send:

			//	��������Ϊ�գ������ո�ͻس�
			String inputText1 = et_input.getText().toString();
			String inputText2 = inputText1.replace(" ", "");
			final String inputText = inputText2.replace("\n", "");		//���ո�ͻس��滻Ϊ��
			et_input.setText("");
			
			Keys k = IKA.get_Keys2(inputText);

			if(inputText.equals("")){
				Toast.makeText(ChatActivity.this, "���벻��Ϊ��",
						Toast.LENGTH_SHORT).show();
				return;

			}
			else if(local_result != null){
				Msg msg = new Msg(inputText1, Msg.TYPE_SENT_WORD);
				msgList.add(msg);
				msgAdapter.notifyDataSetChanged();
				msgListView.setSelection(msgList.size());
			/*	Msg msg2 = new Msg(local_result, Msg.TYPE_RECEIVED);
				msgList.add(msg2);
				msgAdapter.notifyDataSetChanged();
				msgListView.setSelection(msgList.size());*/
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Message message = new Message();
						message.what = 1;
						message.obj = local_result;
						handler2.sendMessage(message);
					}
				}).start();
				
				
				//k.find_str("")
			} else if(inputText.contains("��")){

				mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);  
				long [] pattern = {500,2000,500,2000};   // ֹͣ ���� ֹͣ ����   
				mVibrator.vibrate(pattern,3);


				/*} else if(inputText.equals("����")){

				Msg msg = new Msg(R.drawable.robot, Msg.TYPE_RECEIVED);
				msgList.add(msg);
				msgAdapter.notifyDataSetChanged(); 
				msgListView.setSelection(msgList.size());


				File outputImage = new File(Environment.getExternalStorageDirectory(),"tempImage.jpg");
				try {
					if(outputImage.exists()){
						outputImage.delete();
					}
					outputImage.createNewFile();
				} catch (IOException e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}
				imageUri = Uri.fromFile(outputImage);
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent,TAKE_PICTURE);
				 */
			} else if(inputText.contains("��绰")){

				Msg msg = new Msg(inputText1, Msg.TYPE_SENT_WORD);
				msgList.add(msg);
				msgAdapter.notifyDataSetChanged();
				msgListView.setSelection(msgList.size());

				Intent intent = new Intent(Intent.ACTION_DIAL);
				startActivityForResult(intent, CALL);

			} else if(inputText.contains("������")){

				Msg msg = new Msg(inputText1, Msg.TYPE_SENT_WORD);
				msgList.add(msg);
				msgAdapter.notifyDataSetChanged();
				msgListView.setSelection(msgList.size());

				Uri smsUri = Uri.parse("smsto:");
				Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
				intent.setType("vnd.android-dir/mms-sms");
				startActivityForResult(intent, SEND_MESSAGE);

			} else if(inputText.contains("¼����")){

				Msg msg = new Msg(inputText1, Msg.TYPE_SENT_WORD);
				msgList.add(msg);
				msgAdapter.notifyDataSetChanged();
				msgListView.setSelection(msgList.size());

				Intent intent = new Intent(Media.RECORD_SOUND_ACTION);
				startActivityForResult(intent, RECORDER);

			}

			else {

				Msg msg = new Msg(inputText1, Msg.TYPE_SENT_WORD);
				msgList.add(msg);
				msgAdapter.notifyDataSetChanged();
				msgListView.setSelection(msgList.size());

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							mTuring.sendToTuring(inputText);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						Message message = handler.obtainMessage(); 
						handler.sendMessage(message); 
					}
				}).start();
			}
			break;

		case R.id.btn_chatting_setmode:

			if(rl_bottom.getVisibility() == View.VISIBLE){

				rl_bottom.setVisibility(View.GONE);
				mAudioRecorderButton.setVisibility(View.VISIBLE);
				btn_chatting_mode.setText("����");

				mAudioRecorderButton.setAudioFinishRecorderListener(new AudioFinishRecorderListener() {

					@Override
					public void onFinish(float seconds, String recognizerFilePath, String playFilePath) {

						Msg msg = new Msg(seconds, recognizerFilePath, playFilePath, Msg.TYPE_SENT_VOICE);
						msgList.add(msg);
						msgAdapter.notifyDataSetChanged();
						msgListView.setSelection(msgList.size()-1);

						VoiceRecognizer mVoiceRecognizer = 
								new VoiceRecognizer(recognizerFilePath,mIat,msgList,msgAdapter,msgListView,mTuring);
						mVoiceRecognizer.voiceRecognizerStart();
					}
				});
				msgListView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
							long arg3) {

						//���Ŷ���
						//ֹͣ��һ������
						if(mAnimView!=null){
							mAnimView.setBackgroundResource(R.drawable.adj);
							mAnimView = null;
						}
						mAnimView = arg1.findViewById(R.id.id_right_recorder_anim);
						mAnimView.setBackgroundResource(R.drawable.play_recorder_anim);

						mAnimView.post(new Runnable() {

							@Override
							public void run() {
								AnimationDrawable anim = (AnimationDrawable)mAnimView.getBackground();
								anim.start();
							}
						});

						//����wav����
						MediaManager.playSound(msgList.get(arg2).getPlayFilePath(),
								new MediaPlayer.OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								mAnimView.setBackgroundResource(R.drawable.adj);
							}
						});
					}
				});

			} else {

				rl_bottom.setVisibility(View.VISIBLE);
				mAudioRecorderButton.setVisibility(View.GONE);
				btn_chatting_mode.setText("����");
			}
			break;

		default:
			break;
		}

	}

	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()){

		//���������   ճ��
		case R.id.et_send:

			ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData cd = cm.getPrimaryClip();
			if(!cm.hasPrimaryClip()){
				Toast.makeText(getApplicationContext(), "û�п���ճ���Ķ�������~", Toast.LENGTH_SHORT).show();
			} else{
				et_input.append(cd.getItemAt(0).getText().toString());
			}
			return false;
		}

		return false;
	}

	//�����Ϣ�б��е���ϢЧ��
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		int type = msgList.get(position).getType();
		switch(type){

		//������Ϣ���������ϳ�
		case Msg.TYPE_RECEIVED_WORD:
		case Msg.TYPE_SENT_WORD:

			VoiceSynthesizer mSynthesizer = 
			new VoiceSynthesizer(this,msgList.get(position).getWord(), mTts);
			mSynthesizer.voiceSynthesizerStart();
			break;

		}
	}

	//������Ϣ�б��е���ϢЧ��
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id) {

		int type = msgList.get(position).getType();
		switch(type){

		//������Ϣ�����ı�����
		case Msg.TYPE_RECEIVED_WORD:
		case Msg.TYPE_SENT_WORD:
			ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			cm.setPrimaryClip(ClipData.newPlainText("data", msgList.get(position).getWord()));
			Toast.makeText(this, "���Ƴɹ�", Toast.LENGTH_SHORT).show();
			break;

			//����������Ϣ����Ч��
		case Msg.TYPE_SENT_VOICE:
			Toast.makeText(this, "û��Ч���£���~", Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}

	//��׿ϵͳ������ת֮��Ļص�
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO �Զ����ɵķ������
		String result = null;
		Msg msg = null;
		switch(requestCode){

		case CALL:

			result = "��ɱ�С����ֻ���绰��";
			msg = new Msg(result, Msg.TYPE_RECEIVED_WORD);
			msgList.add(msg);
			msgAdapter.notifyDataSetChanged();
			msgListView.setSelection(msgList.size());

			break;

		case SEND_MESSAGE:

			result = "��ɱ�С����ֻ�ᷢ���Ű�";
			msg = new Msg(result, Msg.TYPE_RECEIVED_WORD);
			msgList.add(msg);
			msgAdapter.notifyDataSetChanged();
			msgListView.setSelection(msgList.size());

			break;

		case RECORDER:

			result = "��ɱ�С����ֻ���¼������";
			msg = new Msg(result, Msg.TYPE_RECEIVED_WORD);
			msgList.add(msg);
			msgAdapter.notifyDataSetChanged();
			msgListView.setSelection(msgList.size());

			break;

			/*case TAKE_PICTURE:
			if(resultCode == RESULT_OK){
				System.out.println("YES");
				try {
					Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
			        ImageSpan imgSpan = new ImageSpan(this, bitmap);  
			        SpannableString spanString = new SpannableString("icon");
			        spanString.setSpan(imgSpan, 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			        Msg msg = new Msg(bitmap, Msg.TYPE_RECEIVED);
					msgList.add(msg);
					msgAdapter.notifyDataSetChanged();
					msgListView.setSelection(msgList.size());
					//iv_picture.setImageBitmap(bitmap);
				} catch (Exception e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}
			}
			break;*/
		default:
			break;
		}

	}


	@Override
	protected void onPause() {
		super.onPause();
		MediaManager.pause();
		mIat.cancel();
		mTts.pauseSpeaking();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MediaManager.resume();
		mTts.resumeSpeaking();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MediaManager.release();
		mIat.stopListening();
		mIat.destroy();
		mTts.stopSpeaking();
		mTts.destroy();
		mVibrator.cancel();
	}

	//	����ʺ���
	private void getRandomWelcomeTips(){

		String welcome_tips = null;
		welcome_array = this.getResources().getStringArray(R.array.welcome_tips);
		int index = (int) ((Math.random())*(welcome_array.length));
		welcome_tips = welcome_array[index];

		Msg msg = new Msg(welcome_tips,Msg.TYPE_RECEIVED_WORD);
		msgList.add(msg);

	}
	public MyDatabaseHelper dbHelper;
	final static double rate = 0;	
	public String local_found(Keys k){
		String result = null;
		Keys f = new Keys();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("chat", null, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				f.init();
				f.add(cursor.getString(cursor.getColumnIndex("key1")));
				f.add(cursor.getString(cursor.getColumnIndex("key2")));
				f.add(cursor.getString(cursor.getColumnIndex("key3")));
				f.add(cursor.getString(cursor.getColumnIndex("key4")));
				f.add(cursor.getString(cursor.getColumnIndex("key5")));
				f.add(cursor.getString(cursor.getColumnIndex("key6")));
				f.add(cursor.getString(cursor.getColumnIndex("key7")));
				f.add(cursor.getString(cursor.getColumnIndex("key8")));
				f.add(cursor.getString(cursor.getColumnIndex("key9")));
				f.add(cursor.getString(cursor.getColumnIndex("key10")));
				if(Similarity.similarity(k, f) > rate){
					result = cursor.getString(cursor.getColumnIndex("ans"));
					break;
				}
				
			}while(cursor.moveToNext());
		}
		cursor.close();
		return result;
	}

}
