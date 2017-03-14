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
	 * 对外公布的方法进行语音合成
	 */
	public void voiceSynthesizerStart(){
		setParameters();
		mTts.startSpeaking(mSynthesizerWord, mSynListener);
	}

	//设置语音合成的各个参数
	private void setParameters(){
		//设置发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
		//设置语速
		mTts.setParameter(SpeechConstant.SPEED, "50");
		//设置音量，范围 0~100
		mTts.setParameter(SpeechConstant.VOLUME, "80");
		//设置云端
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); 
		//设置播放合成音频打断音乐播放，默认为true
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
				Toast.makeText(mContext, "亲，您的网络不给力哦~", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
		}
	};
}
