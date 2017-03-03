package model;

import java.util.ArrayList;
import java.util.List;

public class Smoother {
	private List<Matrix> fw;
	private List<Matrix> O_t;
	private Matrix T;
	private Matrix Tt;
	
	public Smoother(Matrix T, Matrix prior){
		this.T= T;
		Tt = Matrix.transpose(T);
		O_t = new ArrayList<Matrix>();
		fw = new ArrayList<Matrix>();
		fw.add(prior);
	}
	
	/**
	 * 
	 * @return a vector over all previous state distributions
	 */
	public Matrix[] fit(Matrix O){
		O_t.add(O);
		Matrix[] sv = new Matrix[fw.size()+1];
		List<Matrix> bw = new ArrayList<Matrix>();
		bw.add(Matrix.ones(fw.size(), 1));
		Matrix b = Matrix.ones(fw.get(0).rows(), 1);
		
		fw.add(forward(fw.get(fw.size()-1), O, Tt));
		
		double alpha;
		for(int i = fw.size()-1; i >= 0; i--){
			Matrix kryss = Matrix.dotMultiply(fw.get(i), b);
			alpha = Matrix.columnSum(kryss, 0);
			sv[i] = Matrix.multiply(kryss, 1/alpha);
			if(i > 0) b = backward(b, O_t.get(i-1), T);
		}
		return sv;
	}
	
	private Matrix forward(Matrix prior, Matrix O, Matrix Tt){
		double alpha = 1.0;
		Matrix OTt = Matrix.multiply(O,  Tt);
		Matrix OTtf = Matrix.multiply(OTt, prior);

		alpha = Matrix.columnSum(OTtf, 0);
		Matrix alphaOTtf = Matrix.multiply(OTtf, 1/alpha);
		return alphaOTtf;
	}
	
	private Matrix backward(Matrix prior, Matrix O, Matrix T){
		Matrix TOb = Matrix.multiply(Matrix.multiply(T, O), prior);
		return Matrix.multiply(TOb, 1/Matrix.columnSum(TOb, 0));
	}
}
