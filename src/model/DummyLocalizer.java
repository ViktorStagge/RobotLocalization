package model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import control.EstimatorInterface;

public class DummyLocalizer implements EstimatorInterface {
		
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
	private List<Integer> evidences;

	public DummyLocalizer( int rows, int cols, int head) {
		this.rows = rows;
		this.cols = cols;
		this.head = head;
		
		hmm = new HiddenMarkovModel(rows, cols);
		evidences = new LinkedList<Integer>();

		                                                                                                                 
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
		return 0.0;
	}

	public double getOrXY( int rX, int rY, int x, int y) {
		return 0.1;
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
		double ret = 0.0;
		return ret;
	}
	
	public void update() {
		List<Integer> pDir = new ArrayList<Integer>();
		if(y != 0) pDir.add(-1); if(y != 3) pDir.add(1);
		if(x != 0) pDir.add(0); if(x != 3) pDir.add(2);
		
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
					System.out.println("updating: dsX=" + (i-2) + ", dsY=" + (j-2));
					sX = x + i - 2;
					sY = y + j - 2;
					break;
				}
			}
			if(random < 0) break;
		}
		if(sX < 0 || sX >= rows || sY < 0 || sY >= cols || random > 0) {sX = 0; sY = 0;}
		
		System.out.println("updated: (x,y)=(" + x + "," + y + "), (sX,sY)=(" + sX + "," + sY + ")");
	}
	
	private double[][] generateO(int e){
		int eX = e / (cols*4);
		int eY = e / (rows*4);
		int dx, dy;
		
		double[][] O = new double[rows*cols*4][rows*cols*4];
		for(int i = 0; i < O.length; i++){
			dx = eX - i / (cols*4);
			dy = eY - i / (rows*4);
			O[i][i] = Math.max(dx, dy) <= 2 ? 0.1/Math.pow(2, Math.max(dx,dy)) : 0;
		}
		return O;
	}
}