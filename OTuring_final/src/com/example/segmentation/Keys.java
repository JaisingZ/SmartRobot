package com.example.segmentation;


public class Keys {
	
	final static int N=10;
	
	private static String[] key = new String[N];
	
	public Keys(){
		for(int i=0; i<N; i++){
			key[i] = "";
		}
	}
	
	public void copy(Keys a){
		for(int i=0; i<N; i++){
			key[i] = a.get_str_i(i);
		}
	}
	
	public void init(){
		for(int i=0; i<N; i++){
			key[i] = "";
		}
	}
	
	public void add(String a){
		for(int i=0; i<N; i++){	
			if("".equals(key[i])){
				key[i] = a;
				break;
			}
		}
	}
	
	public String get_str_i(int i){
		return key[i];
	}
	
	public boolean find_str(String s){
		boolean result = false;
		int i;
		for(i=0; i<10; i++){
			if(s.equals(key[i])){
				result=true;
				break;
			}
		}
		return result;
	}
	
}
