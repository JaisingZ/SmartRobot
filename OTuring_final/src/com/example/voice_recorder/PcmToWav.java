package com.example.voice_recorder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 将pcm语音文件转化成wav格式
 * @author Jaising
 *
 */

public class PcmToWav {

	private static final int SIZE_OF_WAVE_HEADER = 44;
	private static final String CHUNK_ID = "RIFF";
	private static final String FORMAT = "WAVE";
	private static final String SUB_CHUNK1_ID = "fmt ";
	private static final int SUB_CHUNK1_SIZE = 16;
	private static final String SUB_CHUNK2_ID = "data";
	private static final short FORMAT_PCM = 1; // Indicates PCM format.
	private static final short DEFAULT_NUM_CHANNELS = 1;
	private static final short DEFAULT_BITS_PER_SAMPLE = 16;

	private RandomAccessFile mReader;
	private RandomAccessFile mWriter;
	private short mNumChannels;
	private int mSampleRate;
	private short mBitsPerSample;

	public PcmToWav(File recognizerFile,File playFile,int sample) throws IOException {
		init(recognizerFile,
				   playFile, 
				   DEFAULT_NUM_CHANNELS, 
				   sample,
				   DEFAULT_BITS_PER_SAMPLE);
	}

	private boolean init(File recognizerFile, File playFile, short numChannels, int sampleRate,
			short bitsPerSample) throws IOException {

		mReader = new RandomAccessFile(recognizerFile, "rw");
		mWriter = new RandomAccessFile(playFile, "rw");
		
		//pcm文件复制给wav文件
		byte[] tempData = new byte[513];
		int len;
		while((len=mReader.read(tempData))!=-1){
			mWriter.write(tempData,0,len);
		}

		mNumChannels = numChannels;
		mSampleRate = sampleRate;
		mBitsPerSample = bitsPerSample;
		byte[] buffer = new byte[SIZE_OF_WAVE_HEADER];
		mWriter.write(buffer);
		return true;
	}

	public void write(byte[] buffer) throws IOException {
		mWriter.write(buffer);
	}

	public void writeChars(String val) throws IOException {
		for (int i = 0; i < val.length(); i++)
			mWriter.write(val.charAt(i));
	}

	public void writeInt(int val) throws IOException {
		mWriter.write(val >> 0);
		mWriter.write(val >> 8);
		mWriter.write(val >> 16);
		mWriter.write(val >> 24);
	}

	public void writeShort(short val) throws IOException {
		mWriter.write(val >> 0);
		mWriter.write(val >> 8);
	}

	public int getDataSize() throws IOException {
		return (int) (mWriter.length() - SIZE_OF_WAVE_HEADER);
	}

	//对wav添加文件头，使得可以用MediaPlayer进行播放
	public void writeHeader() throws IOException {

		mWriter.seek(0);
		writeChars(CHUNK_ID);

		writeInt(36 + getDataSize());
		writeChars(FORMAT);

		writeChars(SUB_CHUNK1_ID);
		writeInt(SUB_CHUNK1_SIZE);
		writeShort(FORMAT_PCM);
		writeShort(mNumChannels);
		writeInt(mSampleRate);

		writeInt(mNumChannels * mSampleRate * mBitsPerSample / 8);
		writeShort((short) (mNumChannels * mBitsPerSample / 8));
		writeShort(mBitsPerSample);

		writeChars(SUB_CHUNK2_ID);
		writeInt(getDataSize());
	}

	public void close() throws IOException {
		if (mWriter != null) {
			mWriter.close();
			mWriter = null;
		}
		if (mReader != null) {
			mReader.close();
			mReader = null;
		}
	}
}
