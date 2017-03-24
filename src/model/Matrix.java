package model;

import java.util.InputMismatchException;

public class Matrix {
	private double[][] array;
	private int rows, cols;
	
	public Matrix(double[][] array){
		this.array = array;
		rows = array.length;
		cols = array[0].length;
	}
	
	public double get(int x, int y){
		return array[x][y];
	}
	
	public int cols(){
		return cols;
	}
	
	public int rows(){
		return rows;
	}
		
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(double[] row : array){
			for(double d : row){
				sb.append(String.format(" %.3f ", d));
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public static Matrix multiply(Matrix m, Matrix o){
		if(m.cols != o.rows) throw new InputMismatchException();
		
		double[][] result = new double[m.rows][o.cols];
		for(int i = 0; i < m.rows; i++){
			for(int j = 0; j < o.cols; j++){
				for(int k = 0; k < m.cols ; k++){
					result[i][j] += m.get(i, k) * o.get(k, j);
				}
			}
		}
		return new Matrix(result);
	}
	
	public static Matrix multiply(Matrix m, double k){
		double[][] result = new double[m.rows][m.cols];
		for(int i = 0; i < m.rows; i++){
			for(int j = 0; j < m.cols; j++){
				result[i][j] = k*m.array[i][j];
			}
		}
		return new Matrix(result);
	}
	
	public static Matrix transpose(Matrix m){
		double[][] next = new double[m.cols][m.rows];
		for(int y = 0; y < m.rows; y++){
			for(int x = 0; x < m.cols; x++){
				next[x][y] = m.get(y,x);
			}
		}
		return new Matrix(next);
	}
	
	public static double columnSum(Matrix m, int col){
		double sum = 0.0;
		for(int i = 0; i < m.rows; i++){
			sum += m.get(i, col);
		}
		return sum;
	}
	
	public static Matrix dotMultiply(Matrix m, Matrix o){
		if(m.rows != o.rows || m.cols != o.cols || m.cols != 1){
			//System.out.println(String.format("dotMultiply: (%d,%d)x(%d,%d)", m.rows, m.cols, o.rows, o.cols));
			throw new InputMismatchException();
		}
		
		double[][] result = new double[m.rows][1];
		for(int i = 0; i < m.rows; i++){
			result[i][0] = m.get(i,0) * o.get(i,0);
		}
		return new Matrix(result);
	}

	public static Matrix ones(int rows, int cols){
		double[][] result = new double[rows][cols];
		for(int i = 0; i < result.length; i++){
			for(int j = 0; j < result[0].length; j++){
				result[i][j] = 1;
			}
		}
		return new Matrix(result);
	}
	
	public String toSparseString(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < array.length; i++){
			StringBuilder rb = new StringBuilder();
			for(int j = 0; j < array[i].length; j++){
				if(Double.compare(array[i][j], 0.0) != 0) {
					rb.append(String.format("%d: %.3f ", j, array[i][j]));
				}
			}
			if(rb.length() > 0) sb.append(i).append("::\t").append(rb.toString()).append('\n');
		}
		return sb.toString();
	}
}
