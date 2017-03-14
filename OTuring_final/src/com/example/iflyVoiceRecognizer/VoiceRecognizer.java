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

	// ��HashMap�洢��д���
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
	 * ���⹫���ķ���
	 * ����ʶ��ʼ
	 */
	public void voiceRecognizerStart() {

		byte[] data = getPcmFile();
		ArrayList<byte[]> buffers = splitBuffer(data,data.length, 1280);
		writeaudio(buffers);
	}


	//�õ�¼�Ƶ�pcm�����ļ�
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

	//���ֽڻ��������չ̶���С���зָ������
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

	//��������ʶ��
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

	// ��д������
	private RecognizerListener recognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
		}

		@Override
		public void onError(SpeechError error) {
			String result = null;

			//û����Ч��������
			if(error.getErrorCode() == 10114||error.getErrorCode() == 20001){
				result = "�ף��������粻����Ŷ~";
				Msg msg = new Msg(result, Msg.TYPE_RECEIVED_WORD);
				mMsgList.add(msg);
				mMsgAdapter.notifyDataSetChanged();
				mMsgListView.setSelection(mMsgList.size());
			}
			//����¼��ʱû��˵��
			if(error.getErrorCode() == 10118){
				result = "��˵��˭֪������ʲô";
				Msg msg = new Msg(result, Msg.TYPE_RECEIVED_WORD);
				mMsgList.add(msg);
				mMsgAdapter.notifyDataSetChanged();
				mMsgListView.setSelection(mMsgList.size());
			}
		}

		@Override
		public void onEndOfSpeech() {
			System.out.println("����˵��");
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
			System.out.println("��ǰ����˵����������С��" + volume);
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
				received_text = "û��Ҳ������ģ��ߨq(�s^�t)�r";
			}

			receiveFromTuring = new Msg(received_text, Msg.TYPE_RECEIVED_WORD);
			mMsgList.add(receiveFromTuring);
			mMsgAdapter.notifyDataSetChanged();
			mMsgListView.setSelection(mMsgList.size());
			super.handleMessage(msg);
		}
	};

	//�õ�����ʶ��Ľ��
	private void ifyRecognizerResult(RecognizerResult results, boolean isSendToTuring) {

		String text = JsonParser.parseIatResult(results.getResultString());
		String sn = null;
		// ��ȡjson����е�sn�ֶ�
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
