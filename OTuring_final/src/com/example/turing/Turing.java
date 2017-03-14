package com.example.turing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class Turing {
	
	private String TuringResult = null;
	
	public String getTuringResult() {
		return TuringResult;
	}
	
	//返回解析后的图灵文本
	public String getTuringResultWords(){
		
		String result = "";
		if(!getTuringResult().equals("")){

			JSONObject js;
			try {
				js = new JSONObject(getTuringResult());
				result = js.getString("text");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	//	图灵机器人语义接口
	public void sendToTuring(String content) throws UnsupportedEncodingException {

		String target="";
		TuringResult = "";
		target = "http://www.tuling123.com/openapi/api?key=985f791a3c6d5d4f19226dc8a653ed90&info="
				+ URLEncoder.encode(content,"utf-8");
		URL url;
		try {
			url = new URL(target);
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			InputStreamReader in = new InputStreamReader(
					urlConn.getInputStream()); 
			BufferedReader buffer = new BufferedReader(in); 
			String inputLine = null;

			while ((inputLine = buffer.readLine()) != null) {
				TuringResult += inputLine + "\n";
			}

			urlConn.disconnect();
			in.close();	
			buffer.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
