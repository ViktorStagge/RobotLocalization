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
	private List<Integer[]> evidences;
	private List<Integer[]> actualPos;
	private List<Matrix> Ot;
	private Matrix[] sv;
	Smoother smoother;
	private int t = 0;

	public Localizer( int rows, int cols, int head) {
		this.rows = rows;
		this.cols = cols;
		this.head = head;
		
		hmm = new HiddenMarkovModel(rows, cols);
		evidences = new ArrayList<Integer[]>();
		actualPos = new ArrayList<Integer[]>();
		actualPos.add(new Integer[]{0,0,0});
		Ot = new ArrayList<Matrix>();
		Ot.add(generateO(0, 0));
		
		double[][] array = new double[rows*cols*4][1];
		array[0][0] = 1.0;
		Matrix prior = new Matrix(array);
		smoother = new Smoother(hmm.getT(), prior);
		sv = smoother.fit(generateO(0,0));
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
		System.out.println(String.format("Tij: p=%.2f (%d,%d,%d) -> (%d,%d,%d) X=%d, Y=%d",hmm.get(nX*4 + nY*rows*4 + nH, x*4 + y*rows*4 + h), x, y, h, nX,nY,nH, x*4 + y*rows*4 + h, nX*4 + nY*rows*4 + nH));
		return hmm.get(x*4 + y*rows*4 + h, nX*4 + nY*rows*4 + nH);
	}

	public double getOrXY( int rX, int rY, int x, int y) {
		if(rX == sX && rY == sY){
			double p = 0.0;
			int index = y*rows*4+x*4;
			for(int i = 0; i < 4; i++){
				p += Ot.get(Ot.size()-1).get(index+i, index+i);
			}
			return p;
		} else {
			Matrix Ot = generateO(rX, rY);
			return Ot.get(y*rows*4+x*4, y*rows*4+x*4)*4;
		}
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
			p += sv[sv.length-1].get(index + i, 0); 
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
		actualPos.add(new Integer[]{x, y, dir});
		
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
		
		Ot.add(generateO(sX, sY));
		evidences.add(new Integer[]{sX, sY});
		t++;
		
		sv = smoother.fit(generateO(sX, sY));
		
		System.out.println(String.format("Average Hamington Distance (error): %.2f", averageHamingtonDistance()));
	}
	
	private Matrix generateO(int eX, int eY){
		int dx, dy, i;
		if(eX == 0 && eY == 0) return generateOatZero();
		
		double[][] O = new double[rows*cols*4][rows*cols*4];
		for(int y = 0; y < cols; y++){
			for(int x = 0; x < rows; x++){
				i = y*rows*4+x*4; 
				dx = Math.abs(eX - x);
				dy = Math.abs(eY - y);
				O[i][i] = Math.max(dx, dy) <= 2 ? 0.1/Math.pow(2, 2+Math.max(dx,dy)) : 0;
				O[i+1][i+1] = O[i][i]; O[i+2][i+2] = O[i][i]; O[i+3][i+3] = O[i][i];
			}
		}
		
		return new Matrix(O);
	}
	
	private Matrix generateOatZero(){
		double[][] O = new double[rows*cols*4][rows*cols*4];
		
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
					if(Math.min(maxDx, maxDy) > 0) O[i][i] += 0.05/Math.min(maxDx, maxDy);
				}
			}
			O[i][i] = (O[i][i] + 0.1)/4;
		}
		return new Matrix(O);
	}
	
	public double averageHamingtonDistance(){
		int sum = 0;
		for(int i = 0; i < actualPos.size(); i++){
			Integer[] actual = actualPos.get(i);
			
			Matrix s = sv[i];
			
			double maxSum = 0.0;
			int maxIndex = 0;
			for(int j = 0; j < s.rows(); j+=4){
				double tempSum = s.get(j,0) + s.get(j+1,0) + s.get(j+2,0) + s.get(j+3,0);
				if(tempSum > maxSum) {
					maxSum = tempSum;
					maxIndex = j;
				}
			}
			
			
			int maxX = (maxIndex%(rows*4))/4;
			int maxY = maxIndex/(rows*4);
			
			sum += Math.abs(maxX - actual[0]);
			sum += Math.abs(maxY - actual[1]);
		}
		return (double) sum / (double) actualPos.size();
	}
}