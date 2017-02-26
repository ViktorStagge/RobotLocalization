package model;

public class HiddenMarkovModel {
	private double[][] T;
	
	public HiddenMarkovModel(int rows, int cols){
		T = new double[rows*cols*4][rows*cols*4];
		int del = rows*4;
		for(int i = del; i < T.length - del; i++){
			int index = i-i%4; 
			T[i][index-1] = i%4 == 0 ? 0.7 : 0.1;
			T[i][index+6] = i%4 == 2 ? 0.7 : 0.1;
			T[i][index-rows*4] =  i%4 == 1 ? 0.7 : 0.1;
			T[i][index+rows*4+2] = i%4 == 3 ? 0.7 : 0.1;
		}
		
		for(double[] ti : T){
			for(double tij : ti){
				System.out.print(" " + tij + " ");
			}
			System.out.println();
		}
	}
}
