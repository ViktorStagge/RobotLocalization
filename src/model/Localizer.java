package model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import control.EstimatorInterface;

public class Localizer implements EstimatorInterface {
		
	private int rows, cols, head;
	private int x, y, dir;
	private int sX, sY;
	private double[][] sProb = 
		{{0.025, 0.025, 0.025, 0.025, 0.025},
		 {0.025, 0.05,  0.05,  0.05,  0.025},
		 {0.025, 0.05,  0.1,   0.05,  0.025},
		 {0.025, 0.05,  0.05,  0.05,  0.025},
		 {0.025, 0.025, 0.025, 0.025, 0.025}};
	private HiddenMarkovModel hmm;
	private int t, totalError, nCorrect, re;
	private ForwardPrediction fp;
	private Matrix f;
	

	public Localizer( int rows, int cols, int head) {
		this.rows = rows;
		this.cols = cols;
		this.head = head;
		
		hmm = new HiddenMarkovModel(rows, cols);
		
		double[][] array = new double[rows*cols*4][1];
		for(int i = 0; i < array.length; i++){
			array[i][0] = 1.0 / array.length;
		}
		f = new Matrix(array);
		fp = new ForwardPrediction(f, hmm.getTTranspose());
	}	
	
	public int getNumRows() {
		return rows;
	}
	
	public int getNumCols() {
		return cols;
	}
	
	public int getNumHead() {
		return head;
	}
	
	public double getTProb( int x, int y, int h, int nX, int nY, int nH) {
		return hmm.get(x*4 + y*rows*4 + h, nX*4 + nY*rows*4 + nH);
	}

	public double getOrXY( int rX, int rY, int x, int y) {
		Matrix Ot = generateO(rX,rY);
		return( Ot.get((y*rows+x)*4, (y*rows+x)*4) * 4);
	}


	public int[] getCurrentTruePosition() {
		int[] ret = new int[]{x, y};
		return ret;

	}

	public int[] getCurrentReading() {
		int[] ret = new int[]{sX, sY};
		return ret;
	}


	public double getCurrentProb( int x, int y) {		
		double p = 0.0;
		int index = rows * y*4 + x*4;
		
		for(int i = 0; i < 4; i++){
			p += f.get(index + i, 0); 
		}
		return p;
	}
	
	public void update() {
		List<Integer> pDir = new ArrayList<Integer>();
		if(y > 0) pDir.add(-1); if(y < cols-1) pDir.add(1);
		if(x > 0) pDir.add(0); if(x < rows-1) pDir.add(2);
		
		if(Math.random() > 0.7 || pDir.size() < 4){
			dir = pDir.get((int) (Math.random() * pDir.size()));
		}
		
		if(dir % 2 == 0){
			x += dir - 1;
		} else {
			y += dir;
		}
		
		double random = Math.random();
		for(int i = 0; i < sProb.length; i++){
			for(int j = 0; j < sProb[0].length; j++){
				random -= sProb[i][j];
				if(random < 0) {
					sX = x + i - 2;
					sY = y + j - 2;
					break;
				}
			}
			if(random < 0) break;
		}
		if(sX < 0 || sX >= rows || sY < 0 || sY >= cols || random > 0) {sX = 0; sY = 0;}
		
		t++;
		
		f = fp.forward(generateO(sX, sY));
		int d = manhattanDistance();
		totalError += d;
		if(d == 0) nCorrect++;
		
		re += Math.abs(x-sX) + Math.abs(y-sY);
		
		System.out.printf("t_%d: md=%d, avg=%.2f (r_avg=%.2f), p_correct=%.2f\n", t, d, totalError/(double)t, re /(double)t, nCorrect/(double)t);
	}
	
	private int manhattanDistance(){
		int[] pos = new int[2];
		double max = 0.0;
		for(int y = 0; y < rows; y++){
			for(int x = 0; x < cols; x++){
				if(getCurrentProb(x, y) > max){
					max = getCurrentProb(x,y);
					pos[0] = x; 
					pos[1] = y;
				}
			}
		}
		return(Math.abs(x-pos[0]) + Math.abs(y-pos[1]));
	}
	
	private Matrix generateO(int eX, int eY){
		
		double[][] O = new double[rows*cols*4][rows*cols*4];
		
		if(eX == 0 && eY == 0) {
			for(int i = 0; i < O.length; i++){
				int x = (i%(rows*4)/4);
				int y = i/(rows*4);
				
				for(int dx = -2; dx <= 2; dx++){
					for(int dy = -2; dy <= 2; dy++){
						int maxDx = 0, maxDy = 0;
						if(rows-dx-x-1 == -2 || dx+x == -2) maxDx = 2;
						if(cols-y-dy-1 == -2 || dy+y == -2) maxDy = 2;
						if(rows-dx-x-1 == -1 || dx+x == -1) maxDx = 1;
						if(cols-y-dy-1 == -1 || dy+y == -1) maxDy = 1;
						if(Math.max(maxDx, maxDy) > 0) {
							O[i][i] += 0.05/(Math.min(maxDx, maxDy) == 0 ? Math.max(maxDx, maxDy) : Math.min(maxDx, maxDy));
						}
					}
				}
				O[i][i] = (O[i][i] + 0.1)/4;
			}
		}
		
		for(int y = 0; y < cols; y++){
			int dx, dy, i;
			for(int x = 0; x < rows; x++){
				i = y*rows*4+x*4; 
				dx = Math.abs(eX - x);
				dy = Math.abs(eY - y);
				O[i][i] += Math.max(dx, dy) <= 2 ? 0.1/Math.pow(2, 2+Math.max(dx,dy)) : 0;
				O[i+1][i+1] = O[i][i]; O[i+2][i+2] = O[i][i]; O[i+3][i+3] = O[i][i];
			}
		}
		return new Matrix(O);
	}
}