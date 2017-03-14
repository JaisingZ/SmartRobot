package com.example.iflyVoiceSynthesizer;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

public class VoiceSynthesizer {

	private SpeechSynthesizer mTts;
	private String mSynthesizerWord;
	private Context mContext;

	public VoiceSynthesizer(Context context , String synthesizerWord, SpeechSynthesizer synthesizer) {
		mContext = context;
		mTts = synthesizer;
		mSynthesizerWord = synthesizerWord;
	}

	/**
	 * ���⹫���ķ������������ϳ�
	 */
	public void voiceSynthesizerStart(){
		setParameters();
		mTts.startSpeaking(mSynthesizerWord, mSynListener);
	}

	//���������ϳɵĸ�������
	private void setParameters(){
		//���÷�����
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
		//��������
		mTts.setParameter(SpeechConstant.SPEED, "50");
		//������������Χ 0~100
		mTts.setParameter(SpeechConstant.VOLUME, "80");
		//�����ƶ�
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); 
		//���ò��źϳ���Ƶ������ֲ��ţ�Ĭ��Ϊtrue
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
		//mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
	}

	private SynthesizerListener mSynListener = new SynthesizerListener() {

		@Override
		public void onSpeakResumed() {
		}

		@Override
		public void onSpeakProgress(int arg0, int arg1, int arg2) {
		}

		@Override
		public void onSpeakPaused() {
		}

		@Override
		public void onSpeakBegin() {
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
		}

		@Override
		public void onCompleted(SpeechError error) {
			if(error.getErrorCode() == 10114||error.getErrorCode() == 20001){
				Toast.makeText(mContext, "�ף��������粻����Ŷ~", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
		}
	};
}
