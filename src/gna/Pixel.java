package gna;

import libpract.Position;

public class Pixel extends Position {

	public Pixel(int arg0, int arg1, Pixel previous) {
		super(arg0, arg1);
			setPreviousPixel(previous);
	}
	
	Pixel previousPixel;
	int cost;
	
	public Pixel getPreviousPixel() {
		return this.previousPixel;
	}
	
	public void setPreviousPixel(Pixel previousPixel) {
		this.previousPixel = previousPixel;
	}
	
	public int getCost() {
		return this.cost;
	}
	
	public void setCost(int cost) {
		this.cost = cost;
	}
}
