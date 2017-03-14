package com.example.oturing_final;

public class Msg {

	
	public static final int TYPE_RECEIVED = 0x00;
	public static final int TYPE_RECEIVED_WORD = 0x01;		//接收文字
	public static final int TYPE_RECEIVED_VOICE = 0x02;		//接收语音
	public static final int TYPE_RECEIVED_PICTRUE = 0x03;	//接受图片
	public static final int TYPE_SENT = 0x10;
	public static final int TYPE_SENT_WORD = 0x11;			//发送文字
	public static final int TYPE_SENT_VOICE = 0x12;			//发送语音

	private int type;		//消息类型
	private String word;	//输入文字
	private float time;		//语音时长
	private String recognizerFilePath;	//识别语音存储路径
	private String playFilePath;		//播放语音存储路径
	private int imageId;
	
	//接收文字消息
	public Msg(String word,int type) {
		this.word = word;
		this.type = type;
	}
	
	//接收语音消息
	public Msg(float time,String recognizerFilePath,String playFilePath,int type) {
		this.time = time;
		this.type = type;
		this.recognizerFilePath = recognizerFilePath;
		this.playFilePath = playFilePath;
	}
	
	//接受图片消息
	public Msg(int imageId, int type){
		this.imageId = imageId;
		this.type = type;
	}
	
	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public String getWord() {
		return word;
	}
	
	public int getType() {
		return type;
	}
	
	public String getRecognizerFilePath() {
		return recognizerFilePath;
	}
	
	public String getPlayFilePath() {
		return playFilePath;
	}

}
