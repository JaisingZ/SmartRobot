package com.example.voice_recorder.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * 使用AudioRecord进行语音录制
 * 格式为pcm
 * 
 * @author Jaising
 *
 */


public class AudioManager {

	private AudioRecord mAudioRecord;
	private int audioSource = MediaRecorder.AudioSource.MIC;  
	// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025  
	private static int sampleRateInHz = 16000;// 44100;  
	// 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道  
	private static int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;// AudioFormat.CHANNEL_IN_STEREO;  
	// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。  
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;  
	// 音频大小  
	private int minBufSize;
	//是否正在录音
	private boolean isRecording;
	private double mCurrentVolume;


	private String mRecognizerDir;
	private String mPlayDir;
	private String mRecognizerPath;
	private String mPlayPath;
	private File recognizerFile = null;
	private File playFile = null;

	private static AudioManager mInstance;
	//录音准备是否完成
	private boolean isPrepared;

	private AudioManager(String recognizerDir,String playDir){
		mRecognizerDir = recognizerDir;
		mPlayDir = playDir;
	}

	public interface AudioStateListener{
		void wellPrepared();
	}


	public AudioStateListener mListener;

	public void setAudioStateListener(AudioStateListener listener){
		mListener = listener;
	}

	static AudioManager getInstance(String recognizerDir,String playDir){
		if(mInstance==null){
			synchronized (AudioManager.class) {
				if(mInstance==null){
					mInstance = new AudioManager(recognizerDir,playDir);
				}
			}
		}
		return mInstance;
	}

	/**
	 * 进行语音录制前的准备
	 */
	public void prepareAudio(){

		try {
			isPrepared = false;

			File recognizerDir = new File(mRecognizerDir);
			if(!recognizerDir.exists()){
				recognizerDir.mkdirs();
			}

			File playDir = new File(mPlayDir);
			if(!playDir.exists()){
				playDir.mkdirs();
			}

			String recognizerFileName = generateFileName(1);
			recognizerFile = new File(recognizerDir,recognizerFileName);

			String playFileName = generateFileName(2);
			playFile = new File(playDir,playFileName);

			mRecognizerPath = recognizerFile.getAbsolutePath();
			mPlayPath = playFile.getAbsolutePath();

			minBufSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig,  
					audioFormat);

			mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig,  
					audioFormat, minBufSize);

			mAudioRecord.startRecording();
			isRecording = true;
			new Thread(new WriteDataToFileThread()).start();

			//准备结束
			isPrepared = true;

			if(mListener!=null){
				mListener.wellPrepared();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class WriteDataToFileThread implements Runnable{

		@Override
		public void run() {
			// new一个byte数组用来存一些字节数据，大小为缓冲区大小  
			byte[] audiodata = new byte[minBufSize];  
			FileOutputStream fos = null;
			
			try {
				fos = new FileOutputStream(recognizerFile);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			};  
			int readsize = 0;  
			while (isRecording == true) {  
				readsize = mAudioRecord.read(audiodata, 0, minBufSize);  
				Log.i("采集大小", String.valueOf(readsize));

				mCurrentVolume = getVolume(audiodata, readsize);

				if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {  
					try {  
						fos.write(audiodata);
					} catch (IOException e) {  
						e.printStackTrace();  
					}  
				}  
			} 
			try {  
				fos.close();// 关闭写入流  
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
		}

	}

	/**
	 * 随机生成文件名称
	 * @param fileType
	 * @return
	 */
	private String generateFileName(int fileType) {
		String filePath = null;
		switch(fileType){
		case 1:
			filePath = UUID.randomUUID().toString()+".pcm";
			break;
		case 2:
			filePath = UUID.randomUUID().toString()+".wav";
		}
		return filePath;
	}

	//计算音量
	private double getVolume(byte[] data, int readSize){

		long v = 0;  
		for (int i = 0; i < data.length; i++) {  
			v += data[i] * data[i];  
		}  
		// 平方和除以数据总长度，得到音量大小。  
		double mean = v / (double) readSize;  
		double volume = 10 * Math.log10(mean)/90;

		return volume;
	}

	//得到音量等级
	public int getVoiceLevel(int maxLevel){
		if(isPrepared){
			//忽略等级错误，防止崩掉
			try{
				return (int) (maxLevel*mCurrentVolume);
			} catch(Exception e){
			}
		}
		//默认1
		return 1;
	}

	public void release(){
		isRecording = false;
		mAudioRecord.stop();
		mAudioRecord.release();
		mAudioRecord = null;
	}

	//TODO ...
	public void cancel(){
		release();
		if(mRecognizerPath!=null){
			File file = new File(mRecognizerPath);
			file.delete();
			mRecognizerPath = null;
		}
	}

	public String getRecognizerPath() {
		return mRecognizerPath;
	}

	public String getPlayPath() {
		return mPlayPath;
	}

	public File getPlayFile() {
		return playFile;
	}

	public File getRecognizerFile() {
		return recognizerFile;
	}

}
