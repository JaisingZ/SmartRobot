package com.example.segmentation;

public class Find_key {
	
	public static boolean found(Keys a, String t){
		boolean result = false;
		int i;
		for(i=0; i<10; i++){
			if(t.equals(a.get_str_i(i)))
				result = true;
		}
		return result;
	}

}
