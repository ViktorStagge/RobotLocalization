package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.*;

import model.Matrix;

public class Testa {
	@Test
	public void matrixMultiply(){
		Matrix A = new Matrix(new double[][]{{31,13},{12,2}});
		Matrix B = new Matrix(new double[][]{{3,19},{10,4}});
		Matrix C = Matrix.multiply(A, B);
		assertTrue(Math.abs(A.get(0, 0) * B.get(0,1) + A.get(0,1) * B.get(1,1) - C.get(0, 1)) <= 0.00005);
		
	}
	
	@Test
	public void fwWiki(){
		List<Integer> ev = new ArrayList<Integer>();
		ev.add(0); ev.add(0); ev.add(1); ev.add(0); ev.add(0);
		Matrix prior = new Matrix(new double[][]{{0.5},{0.5}});
		Matrix[] sv = forwardBackward(ev, prior);
		for(int i = 0; i < sv.length; i++){
			System.out.println(String.format("%d:\n%s", i, sv[i]));
		}
	}
	
	public Matrix[] forwardBackward(List<Integer> ev, Matrix prior){
		Matrix[] sv = new Matrix[ev.size()+1];
		List<Matrix> bw = new ArrayList<Matrix>();
		bw.add(Matrix.ones(ev.size(), 1));
		Matrix b = Matrix.ones(prior.rows(), 1);
		List<Matrix> fw = new ArrayList<Matrix>();
		fw.add(prior);
		Matrix T = new Matrix(new double[][]{{1.0, 0.0},{0.3, 0.7}});
		Matrix Tt = Matrix.transpose(T);
		
			// de fuck?
		for(int i = 0; i < ev.size(); i++){
			Matrix O;
			if(ev.get(i) == 1){
				O = new Matrix(new double[][]{{0.1, 0.0},{0.0,0.8}});
			} else {
				O = new Matrix(new double[][]{{0.9, 0.0},{0.0,0.2}});
			}
			fw.add(forward(fw.get(i), O, Tt));
			System.out.println(String.format("forward: sum=%.3f, f_%d=%s", Matrix.columnSum(fw.get(i+1), 0), (i+1), Matrix.transpose(fw.get(i+1))));
		}
		double alpha;
		for(int i = ev.size(); i >= 0; i--){
			Matrix kryss = Matrix.dotMultiply(fw.get(i), b);
			alpha = Matrix.columnSum(kryss, 0);
			sv[i] = Matrix.multiply(kryss, 1/alpha);
			System.out.println(String.format("backward: sum=%.3f, b_%d=%s", Matrix.columnSum(b, 0), i, Matrix.transpose(b)));
			if(i > 0) b = backward(ev.get(i-1), b, T);
		}
		
		for(int i = 0; i < sv.length; i++){
			System.out.println(String.format("backtracking: sum=%.3f, sv_%d=%s", Matrix.columnSum(sv[i], 0), i, Matrix.transpose(sv[i])));
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
	
	/*private Matrix forward(int ev, Matrix prior, Matrix Tt){
		double alpha = 1.0;
		Matrix O;
		if(ev == 1){
			O = new Matrix(new double[][]{{0.1, 0.0},{0.0,0.8}});
		} else {
			O = new Matrix(new double[][]{{0.9, 0.0},{0.0,0.2}});
		}
		Matrix OTt = Matrix.multiply(O,  Tt);
		Matrix OTtf = Matrix.multiply(OTt, prior);

		alpha = Matrix.columnSum(OTtf, 0);
		Matrix alphaOTtf = Matrix.multiply(OTtf, 1/alpha);
		return alphaOTtf;
	} */
	
	private Matrix backward(int ev, Matrix prior, Matrix T){
		Matrix O;
		if(ev == 1){
			O = new Matrix(new double[][]{{0.1, 0.0},{0.0,0.8}});
		} else {
			O = new Matrix(new double[][]{{0.9, 0.0},{0.0,0.2}});
		}
		Matrix TOb = Matrix.multiply(Matrix.multiply(T, O), prior);
		return Matrix.multiply(TOb, 1/Matrix.columnSum(TOb, 0));
	}
}
