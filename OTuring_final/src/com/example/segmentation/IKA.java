package com.example.segmentation;

import java.io.IOException;
import java.io.StringReader;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class IKA {

	public static String get_key(String text)throws IOException{
		StringReader sr=new StringReader(text);  
        IKSegmenter ik=new IKSegmenter(sr, true);  
        Lexeme lex=null;  
        String result = "|";
        while((lex=ik.next())!=null){  
            result +=lex.getLexemeText()+"|";  
        }  
        return result;
	}
	public static Keys get_Keys(String text){
		Keys r = new Keys();
		int len = text.length();
		int i,j;
		for(i=0; i<len; ){
			for(j=i+1; j<len; j++){
				if(text.charAt(j) == '|')
					break;
			}
			if(j-i > 2)
				r.add(text.substring(i+1, j));
			i=j;
		}
		return r;
	}
	public static Keys get_Keys2(String text){
		try {
			text = get_key(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Keys a = new Keys();
		a = get_Keys(text);
		return a;
	}
}
