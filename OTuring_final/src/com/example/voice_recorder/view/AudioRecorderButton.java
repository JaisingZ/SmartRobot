package com.example.voice_recorder.view;

import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.voice_recorder.PcmToWav;
import com.example.voice_recorder.view.AudioManager.AudioStateListener;

public class AudioRecorderButton extends Button implements AudioStateListener {

	private static final int DISTANCE_Y_CANCEL = 50;
	private static final int STATE_NORMAL = 1;
	private static final int STATE_RECORDING = 2;
	private static final int STATE_WANT_CANCEL = 3;
	private boolean isRecording = false;

	private int mCurState = STATE_NORMAL;//记录当前状态

	private DialogManager mDialogManager;

	private AudioManager mAudioManager;

	private float mTime;

	//	是否触发OnLongClick()
	private boolean mReady;
	
	private PcmToWav mWavWriter;

	public AudioRecorderButton(Context context) {
		this(context,null);
	}

	public AudioRecorderButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		mDialogManager = new DialogManager(getContext());

		String recognizerDir = Environment.getExternalStorageDirectory()+"/OTuring/Voice/Recognizer";
		String playDir = Environment.getExternalStorageDirectory()+"/OTuring/Voice/Play";
		mAudioManager = AudioManager.getInstance(recognizerDir,playDir);
		mAudioManager.setAudioStateListener(this);

		setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				mReady = true;
				mAudioManager.prepareAudio();
				return false;
			}
		});
	}

	/**
	 * 录音完成后的回调
	 *
	 */
	public interface AudioFinishRecorderListener{
		void onFinish(float seconds, String recognizerFilePath, String playFilePath);
	}

	private AudioFinishRecorderListener mlistener;

	public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener){
		mlistener = listener;
	}

	/**
	 * 获取音量大小的Runnable
	 */
	private Runnable mGetVoiceLevelRunnable = new Runnable() {

		@Override
		public void run() {
			while(isRecording){
				try {
					Thread.sleep(100);
					mTime += 0.1f;
					mHandler.sendEmptyMessage(MSG_VOIECE_CHANGED);
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
	};

	private static final int MSG_AUDIO_PREPARED = 0x110;
	private static final int MSG_VOIECE_CHANGED = 0x111;
	private static final int MSG_DIALOG_DIMISS = 0x112;

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {

			case MSG_AUDIO_PREPARED:
				//显示是在audio end prepare之后
				mDialogManager.showRecordingDialog();
				isRecording = true;
				new Thread(mGetVoiceLevelRunnable).start();
				break;

			case MSG_VOIECE_CHANGED:
				mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
				break;

			case MSG_DIALOG_DIMISS:
				mDialogManager.dimissDialog();
				break;
			}
		};
	};

	@Override
	public void wellPrepared() {
		mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch(action){

		case MotionEvent.ACTION_DOWN:

			changeState(STATE_RECORDING);
			break;

		case MotionEvent.ACTION_MOVE:

			// 已经开始录音
			if(isRecording){
				//	根据x，y坐标判断是否想要取消
				if(wantToCancel(x,y)){
					changeState(STATE_WANT_CANCEL);
				} else {
					changeState(STATE_RECORDING);
				}
			}
			break;

		case MotionEvent.ACTION_UP:

			if(!mReady){
				reset();
				return super.onTouchEvent(event);
			}
			if(!isRecording||mTime<0.6f){
				
				mDialogManager.tooShort();
				mAudioManager.cancel();
				mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);

			}else if(mCurState ==STATE_RECORDING){	//	正常录制结束，将pcm转化成wav
				
				mDialogManager.dimissDialog();
				mAudioManager.release();
				if(mlistener!=null){
					
					try {
						mWavWriter = new PcmToWav(mAudioManager.getRecognizerFile(),mAudioManager.getPlayFile(), 16000);
						mWavWriter.writeHeader();
						mWavWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					mlistener.onFinish(mTime, mAudioManager.getRecognizerPath(), mAudioManager.getPlayPath());
				}

			} else if(mCurState ==STATE_WANT_CANCEL){
				mDialogManager.dimissDialog();
				mAudioManager.cancel();
			}
			reset();
			break;
		}

		return super.onTouchEvent(event);
	}

	//	恢复状态及标志位
	private void reset() {
		isRecording = false;
		mReady = false;
		mTime = 0;
		changeState(STATE_NORMAL);
	}

	// 判断手指是否超出范围
	private boolean wantToCancel(int x, int y) {

		if(x<0||x>getWidth()){
			return true;
		}
		if(y<-DISTANCE_Y_CANCEL||y>getHeight()+DISTANCE_Y_CANCEL){
			return true;
		}
		return false;
	}

	//	改变背景色和显示文本
	private void changeState(int state) {

		if(mCurState!=state){
			mCurState = state;

			switch (state) {

			case STATE_NORMAL:
				setBackgroundResource(R.drawable.btn_recorder_normal);
				setText(R.string.str_recorder_normal);
				break;

			case STATE_RECORDING:
				setBackgroundResource(R.drawable.btn_recorder_recording);
				setText(R.string.str_recorder_recording);
				if(isRecording){
					mDialogManager.recording();
				}
				break;

			case STATE_WANT_CANCEL:
				setBackgroundResource(R.drawable.btn_recorder_recording);
				setText(R.string.str_recorder_want_cancel);
				mDialogManager.wantToCancel();
				break;
			default:
				break;
			}
		}
	}
}
