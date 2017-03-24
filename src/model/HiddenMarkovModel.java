package model;

public class HiddenMarkovModel {
	private Matrix TTranspose;
	private Matrix T;
	
	public HiddenMarkovModel(int rows, int cols){
		double[][] TArray = new double[rows*cols*4][rows*cols*4];
		int[] index = {-1, -1, -1, -1};
		int n = 0;		
		
		for(int i = 0; i < TArray.length; i++){

			index[0] = (i-i%4) % (rows*4) == 0 ? 	-1 : i - 4 - (i%4) + 0;
			index[3] = i < rows*4 ? 	-1 : i - 4*rows - (i%4) + 3;
			index[2] = (i+4-i%4) % (rows*4) == 0 ? 	-1 : i + 4 - (i%4) + 2;
			index[1] = i / (rows*4) == cols-1 ? -1 : i + 4*rows - (i%4) + 1;
			
			n = 0;
			for(int j = 0; j < index.length; j++){
				if(index[j] >= 0 && j != (i%4)) n++;
			}
			
			double forwardChance = index[i%4] <= -1 ? 0.0 : 0.7;
			
			for(int j = 0; j < index.length; j++){
				if(index[j] >= 0 && j != (i%4)) TArray[i][index[j]] = (1-forwardChance) / n;
			}
			if(index[i%4] >= 0) TArray[i][index[i%4]] = forwardChance;
		}
		T = new Matrix(TArray);
		TTranspose = Matrix.transpose(T);
	}
	
	public double get(int x, int y){
		return T.get(x, y);
	}
	
	public Matrix getTTranspose(){
		return TTranspose;
	}
	
	public Matrix getT(){
		return T;
	}
}
