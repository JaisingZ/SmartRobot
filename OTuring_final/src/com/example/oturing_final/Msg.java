package com.example.oturing_final;

public class Msg {

	
	public static final int TYPE_RECEIVED = 0x00;
	public static final int TYPE_RECEIVED_WORD = 0x01;		//��������
	public static final int TYPE_RECEIVED_VOICE = 0x02;		//��������
	public static final int TYPE_RECEIVED_PICTRUE = 0x03;	//����ͼƬ
	public static final int TYPE_SENT = 0x10;
	public static final int TYPE_SENT_WORD = 0x11;			//��������
	public static final int TYPE_SENT_VOICE = 0x12;			//��������

	private int type;		//��Ϣ����
	private String word;	//��������
	private float time;		//����ʱ��
	private String recognizerFilePath;	//ʶ�������洢·��
	private String playFilePath;		//���������洢·��
	private int imageId;
	
	//����������Ϣ
	public Msg(String word,int type) {
		this.word = word;
		this.type = type;
	}
	
	//����������Ϣ
	public Msg(float time,String recognizerFilePath,String playFilePath,int type) {
		this.time = time;
		this.type = type;
		this.recognizerFilePath = recognizerFilePath;
		this.playFilePath = playFilePath;
	}
	
	//����ͼƬ��Ϣ
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
