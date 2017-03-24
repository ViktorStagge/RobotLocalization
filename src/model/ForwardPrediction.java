package model;

import java.util.LinkedList;

public class ForwardPrediction {
	private LinkedList<Matrix> fw;
	private Matrix Tt;
	
	public ForwardPrediction(Matrix f0, Matrix Tt){
		fw = new LinkedList<Matrix>();
		fw.add(f0);
		this.Tt = Tt;
	}
	
	public Matrix forward(Matrix O){
		double alpha = 1.0;
		Matrix prior = fw.getLast();
		Matrix OTt = Matrix.multiply(O,  Tt);
		Matrix OTtf = Matrix.multiply(OTt, prior);

		alpha = Matrix.columnSum(OTtf, 0);
		Matrix alphaOTtf = Matrix.multiply(OTtf, 1/alpha);
		fw.addLast(alphaOTtf);
		return alphaOTtf;
	}
}
