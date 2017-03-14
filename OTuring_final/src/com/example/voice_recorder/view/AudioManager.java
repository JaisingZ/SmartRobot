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
 * ʹ��AudioRecord��������¼��
 * ��ʽΪpcm
 * 
 * @author Jaising
 *
 */


public class AudioManager {

	private AudioRecord mAudioRecord;
	private int audioSource = MediaRecorder.AudioSource.MIC;  
	// ������Ƶ�����ʣ�44100��Ŀǰ�ı�׼������ĳЩ�豸��Ȼ֧��22050��16000��11025  
	private static int sampleRateInHz = 16000;// 44100;  
	// ������Ƶ��¼�Ƶ�����CHANNEL_IN_STEREOΪ˫������CHANNEL_CONFIGURATION_MONOΪ������  
	private static int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;// AudioFormat.CHANNEL_IN_STEREO;  
	// ��Ƶ���ݸ�ʽ:PCM 16λÿ����������֤�豸֧�֡�PCM 8λÿ����������һ���ܵõ��豸֧�֡�  
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;  
	// ��Ƶ��С  
	private int minBufSize;
	//�Ƿ�����¼��
	private boolean isRecording;
	private double mCurrentVolume;


	private String mRecognizerDir;
	private String mPlayDir;
	private String mRecognizerPath;
	private String mPlayPath;
	private File recognizerFile = null;
	private File playFile = null;

	private static AudioManager mInstance;
	//¼��׼���Ƿ����
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
	 * ��������¼��ǰ��׼��
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

			//׼������
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
			// newһ��byte����������һЩ�ֽ����ݣ���СΪ��������С  
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
				Log.i("�ɼ���С", String.valueOf(readsize));

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
				fos.close();// �ر�д����  
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
		}

	}

	/**
	 * ��������ļ�����
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

	//��������
	private double getVolume(byte[] data, int readSize){

		long v = 0;  
		for (int i = 0; i < data.length; i++) {  
			v += data[i] * data[i];  
		}  
		// ƽ���ͳ��������ܳ��ȣ��õ�������С��  
		double mean = v / (double) readSize;  
		double volume = 10 * Math.log10(mean)/90;

		return volume;
	}

	//�õ������ȼ�
	public int getVoiceLevel(int maxLevel){
		if(isPrepared){
			//���Եȼ����󣬷�ֹ����
			try{
				return (int) (maxLevel*mCurrentVolume);
			} catch(Exception e){
			}
		}
		//Ĭ��1
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
