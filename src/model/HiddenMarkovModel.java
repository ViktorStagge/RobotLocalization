package model;

public class HiddenMarkovModel {
	private double[][] T;
	
	public HiddenMarkovModel(int rows, int cols){
		double[][] T = new double[rows*cols*4][rows*cols*4];
		int[] index = {-1, -1, -1, -1};
		int n = 0;		
//		System.out.println("indexing: length=" + T.length);
		for(int i = 0; i < T.length; i++){
			index[0] = (i-i%4) % (cols*4) == 0 ? 	-1 : i - 4 - (i%4);
			index[1] = i < cols*4 ? 	-1 : i - 4*cols - (i%4) + 1;
			index[2] = (i+4-i%4) % (cols*4) == 0 ? 	-1 : i + 6 - (i%4);
			index[3] = i / (cols*4) == rows-1 ? -1 : i + 4*cols - (i%4) + 3;
			
//			System.out.println("indexes: AT=" + i +", left="+index[0] + ", "
//					+ "up=" + index[1] + ", right=" + index[2] + ", down=" +index[3]);
			
			n = 0;
			for(int j = 0; j < index.length; j++){
				if(index[j] >= 0 && j != (i%4)) n++;
			}
			
			double forwardChance = index[i%4] <= -1 ? 0.0 : 0.7;
			
//			System.out.println("probability: f=" + forwardChance + ", n=" + n);
			for(int j = 0; j < index.length; j++){
				if(index[j] >= 0 && j != (i%4)) T[i][index[j]] = (1-forwardChance) / n;
			}
			if(index[i%4] >= 0) T[i][index[i%4]] = forwardChance;
		}
		
		for(double[] ti : T){
			for(double tij : ti){
//				System.out.print(" " + String.format("%.3f", tij) + " ");
			}
//			System.out.println();
		}
	}
	
	public double get(int x, int y){
		return T[x][y];
	}
}
