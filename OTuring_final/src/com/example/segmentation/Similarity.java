package com.example.segmentation;

public class Similarity {

	final static int N = 10;
	//a为输入，b为从数据库查�?
	public static double similarity(Keys a, Keys b){
		int result = 0;
		int sum = 0;
		int i,j;
		for(i=0; i<N; i++){
			if("".equals(a.get_str_i(i)))
				break;
		}
		sum = i;
		for(i=0; i<sum; i++){
			for(j=0; j<N; j++){
				if(a.get_str_i(i).equals(b.get_str_i(j))){
					result = result + 1;
					j=N-1;
				}
			}
		}
		if(sum == 0)
			return 0;
		else
			return result/sum;
	}
}
