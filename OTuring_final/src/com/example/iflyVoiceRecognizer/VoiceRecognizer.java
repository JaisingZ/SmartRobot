package com.example.iflyVoiceRecognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;

import com.example.oturing_final.Msg;
import com.example.oturing_final.MsgAdapter;
import com.example.turing.Turing;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

public class VoiceRecognizer {

	private Turing mTuring;
	private ListView mMsgListView;
	private MsgAdapter mMsgAdapter;
	private List<Msg> mMsgList = new ArrayList<Msg>();
	private String mFilePath;
	private SpeechRecognizer mIat;

	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

	public VoiceRecognizer(String filePath, SpeechRecognizer recognizer, 
			List<Msg> msgList, MsgAdapter msgAdapter, ListView msgListView, Turing turing) {
		mFilePath = filePath;
		mIat = recognizer;
		mTuring = turing;
		mMsgAdapter = msgAdapter;
		mMsgList = msgList;
		mMsgListView = msgListView;
	}

	/**
	 * 对外公布的方法
	 * 语音识别开始
	 */
	public void voiceRecognizerStart() {

		byte[] data = getPcmFile();
		ArrayList<byte[]> buffers = splitBuffer(data,data.length, 1280);
		writeaudio(buffers);
	}


	//得到录制的pcm语音文件
	private byte[] getPcmFile(){

		byte[] buffer = null;
		File file = null;
		FileInputStream in = null;

		try {
			file = new File(mFilePath);
			in = new FileInputStream(file);
			buffer = new byte[in.available()];
			in.read(buffer);

		} catch (Exception e) {
			e.printStackTrace();
		}finally
		{
			try {
				if(in != null)
				{
					in.close();
					in = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return buffer;
	}

	//将字节缓冲区按照固定大小进行分割成数组
	private ArrayList<byte[]> splitBuffer(byte[] buffer,int length,int spsize)
	{
		ArrayList<byte[]> array = new ArrayList<byte[]>();
		if(spsize <= 0 || length <= 0 || buffer == null || buffer.length < length)
			return array;
		int size = 0;
		while(size < length)
		{
			int left = length - size;
			if(spsize < left)
			{
				byte[] sdata = new byte[spsize];
				System.arraycopy(buffer,size,sdata,0,spsize);
				array.add(sdata);
				size += spsize;
			}else
			{
				byte[] sdata = new byte[left];
				System.arraycopy(buffer,size,sdata,0,left);
				array.add(sdata);
				size += left;
			}
		}
		return array;
	}

	//进行语音识别
	private void writeaudio(final ArrayList<byte[]> buffers){
		new Thread(new Runnable()
		{
			@Override
			public void run() {

				mIat.setParameter(SpeechConstant.DOMAIN, "iat");
				mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
				mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
				mIat.startListening(recognizerListener);
				for(int i = 0; i < buffers.size(); i++)
				{
					try {
						mIat.writeAudio(buffers.get(i),0,buffers.get(i).length);
						Thread.sleep(40);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				mIat.stopListening();
			}
		}).start();
	}

	// 听写监听器
	private RecognizerListener recognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
		}

		@Override
		public void onError(SpeechError error) {
			String result = null;

			//没有有效网络连接
			if(error.getErrorCode() == 10114||error.getErrorCode() == 20001){
				result = "亲，您的网络不给力哦~";
				Msg msg = new Msg(result, Msg.TYPE_RECEIVED_WORD);
				mMsgList.add(msg);
				mMsgAdapter.notifyDataSetChanged();
				mMsgListView.setSelection(mMsgList.size());
			}
			//语音录制时没有说话
			if(error.getErrorCode() == 10118){
				result = "不说话谁知道你想什么";
				Msg msg = new Msg(result, Msg.TYPE_RECEIVED_WORD);
				mMsgList.add(msg);
				mMsgAdapter.notifyDataSetChanged();
				mMsgListView.setSelection(mMsgList.size());
			}
		}

		@Override
		public void onEndOfSpeech() {
			System.out.println("结束说话");
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {

			if (isLast) {
					ifyRecognizerResult(results,true);
			} else {
				ifyRecognizerResult(results,false);
			}
		}

		@Override
		public void onVolumeChanged(int volume) {
			System.out.println("当前正在说话，音量大小：" + volume);
		}


		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
	};

	Handler handler = new Handler() {

		private String received_text = null;
		Msg receiveFromTuring;

		@Override
		public void handleMessage(Message msg) {

			if (!"".equals(mTuring.getTuringResultWords())) {
				received_text = mTuring.getTuringResultWords();
			}
			else{
				received_text = "没网也想和我聊，哼╭(╯^╰)╮";
			}

			receiveFromTuring = new Msg(received_text, Msg.TYPE_RECEIVED_WORD);
			mMsgList.add(receiveFromTuring);
			mMsgAdapter.notifyDataSetChanged();
			mMsgListView.setSelection(mMsgList.size());
			super.handleMessage(msg);
		}
	};

	//得到语音识别的结果
	private void ifyRecognizerResult(RecognizerResult results, boolean isSendToTuring) {

		String text = JsonParser.parseIatResult(results.getResultString());
		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();

		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}

		final String result = resultBuffer.toString();
		if(isSendToTuring){

			Msg msg = new Msg(result, Msg.TYPE_SENT_WORD);
			mMsgList.add(msg);
			mMsgAdapter.notifyDataSetChanged();
			mMsgListView.setSelection(mMsgList.size());

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						mTuring.sendToTuring(result);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					Message message = handler.obtainMessage(); 
					handler.sendMessage(message); 
				}
			}).start();
		}
	}
}
