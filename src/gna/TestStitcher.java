package gna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.junit.*;

import gna.Stitcher.PixelComparator;
import junit.framework.TestCase;
import libpract.Position;
import libpract.Stitch;

public class TestStitcher extends TestCase {

	public int[][] image1 = {{458752, 256, 1}, {131072, 2048, 5}, {65536, 256, 8}} ; 
	
	int colorBlack = 0;
	public int[][] image2 = {{colorBlack, colorBlack, colorBlack}, {colorBlack, colorBlack, colorBlack}, {colorBlack, colorBlack, colorBlack}} ; 

	//Comparator class to compare pixel according to their cost calculated by the ImageCompositor.pixelSqDistance method.
    public static class PixelComparator implements Comparator<Pixel>{
    	public int compare(Pixel PixelA, Pixel PixelB) {
        	if (ImageCompositor.pixelSqDistance(PixelA.getX(), PixelA.getY())
        			< ImageCompositor.pixelSqDistance(PixelB.getX(), PixelB.getY())) return -1;
        	else if (ImageCompositor.pixelSqDistance(PixelA.getX(), PixelA.getY())
        			> ImageCompositor.pixelSqDistance(PixelB.getX(), PixelB.getY())) return 1;
        	else return 0;
    	}
    }
    
    public List<Position> seam(int[][] image1, int[][] image2) {
    	Comparator<Pixel> comparator = new PixelComparator(); 					   // initializing priority queue + comparator
		PriorityQueue<Pixel> openSetPQ = new PriorityQueue<Pixel>(10, comparator); //pixels that still need to be evaluated.
		Set<Pixel> closedSet = new HashSet<Pixel>(); 							   //all pixel that are finished being evaluated.
		
		Pixel startPosition = new Pixel(0,getBestPixelInRow(image1, image2, 0),null);
		Pixel finishPosition = new Pixel(image1.length-1, getBestPixelInRow(image1, image2, image1.length-1),null); //width = image1[0].length, heigth = image1.length.
		
		openSetPQ.add(startPosition);
		startPosition.setCost(0);

		Pixel currentPixel = startPosition;
		while(!openSetPQ.isEmpty()) { 											   //if no more pixels to examine => no solution
			currentPixel = openSetPQ.poll(); 									   //pull best pixel from openSet
			if((currentPixel.getX() == finishPosition.getX()) && (currentPixel.getY() == finishPosition.getY())) { 
				List<Pixel> path = getPath(currentPixel);
				System.out.println("path");
				System.out.println(path);
				return extractPositionsFromPath(path);
			}
			openSetPQ.remove(currentPixel); 									   //remove current pixel + add to closedSet because this pixel is handled with
			closedSet.add(currentPixel);
			List<Pixel> neighboringPixels = getAllNeighboringPixels(currentPixel, image1); //get all neighbors from pixel (maakt niet uit of één of ander image is(gewoon nodig als referentiekader)).
			for(Pixel neighbor: neighboringPixels) { 
				if(!closedSet.contains(neighbor)) { 							   //if neighbor is already handled with, skip all steps & go to next neighbor
					int newCost = currentPixel.getCost()
						+ ImageCompositor.pixelSqDistance(image1[currentPixel.getX()][currentPixel.getY()],
														  image2[neighbor.getX()][neighbor.getY()]);
					boolean betterOrNewPath = false;
					if(openSetPQ.contains(neighbor)) { 							   //if neighbor has already been visited => check if better score and alter 
						if (newCost < neighbor.getCost()) {//(1)
							neighbor.setCost(newCost);
							betterOrNewPath = true;
						}
					}
					else { 														   //if not already visited => just add calculated cost & add neighbor to seenSet (2)
						neighbor.setCost(newCost);
						betterOrNewPath = true;
						openSetPQ.add(neighbor);
					}
					if(betterOrNewPath) { 										   //only update neighbors previous if the cost has improved (1) or if new path was found (2). 
						neighbor.setCost(newCost);
						neighbor.setPreviousPixel(currentPixel);
					}
				}
			}			
		}
		return null; 																		   //if openSetPQ runs out of option, no solution was found
	}

	private int getBestPixelInRow(int[][] image1, int[][] image2, int row) {
		int bestColToStart = 0;
		int bestCost = ImageCompositor.pixelSqDistance(image1[row][0], image2[row][0]);
		for(int i = 1; i < image1[0].length; i++) {
			int cost = ImageCompositor.pixelSqDistance(image1[row][i], image2[row][i]);
			if(cost < bestCost) {
				bestCost = cost;
				bestColToStart = i;
			}
		}
		return bestColToStart;
	}

	private List<Position> extractPositionsFromPath(List<Pixel> path) {
		List<Position> listOfPositions = new ArrayList<Position>();
		for(Pixel pixel: path) {
			listOfPositions.add(new Position(pixel.getX(), pixel.getX()));
		}
		return listOfPositions;
	}

	private List<Pixel> getPath(Pixel currentPixel) {
		List<Pixel> path = new ArrayList<Pixel>();	//backtrack trough allPreviousPositions matrix in reverse.
		while(currentPixel.getPreviousPixel() != null) {
			path.add(currentPixel);
			currentPixel = currentPixel.getPreviousPixel();
		}
		path.add(currentPixel);
		Collections.reverse(path);
		return path;

	}
	
	/**
	 * Geeft alle buren terug van een pixel indien deze niet buiten de grenzen gaan..
	 * @param currentPixel
	 * @param image 
	 * @return
	 */
	public List<Pixel> getAllNeighboringPixels(Pixel currentPixel, int[][] image) {
		List<Pixel> listOfNeighbors = new ArrayList<Pixel>();
		if (currentPixel.getY() + 1 <= image[0].length - 1) {
			listOfNeighbors.add(new Pixel(currentPixel.getX(), currentPixel.getY() + 1, currentPixel));
		}
		if (currentPixel.getY() - 1 >= 0) {
			listOfNeighbors.add(new Pixel(currentPixel.getX(), currentPixel.getY() - 1, currentPixel));
		}
		if (currentPixel.getX() + 1 <= image.length - 1) {
			listOfNeighbors.add(new Pixel(currentPixel.getX() + 1, currentPixel.getY(), currentPixel));
		}
		if (currentPixel.getX() - 1 >= 0) {
			listOfNeighbors.add(new Pixel(currentPixel.getX() - 1, currentPixel.getY(), currentPixel));
		}
		if ((currentPixel.getX() + 1 <= image.length - 1) && (currentPixel.getY() + 1 <= image[0].length - 1)) {
			listOfNeighbors.add(new Pixel(currentPixel.getX() + 1, currentPixel.getY() + 1, currentPixel));
		}
		if ((currentPixel.getX() - 1 >= 0) && (currentPixel.getY() + 1 <= image[0].length - 1)) {
			listOfNeighbors.add(new Pixel(currentPixel.getX() - 1, currentPixel.getY() + 1, currentPixel));
		}
		if ((currentPixel.getX() + 1 <= image.length - 1) && (currentPixel.getY() - 1 >= 0)) {
			listOfNeighbors.add(new Pixel(currentPixel.getX() + 1, currentPixel.getY() - 1, currentPixel));
		}
		if ((currentPixel.getX() - 1 >= 0) && (currentPixel.getY() - 1 >= 0)) {
			listOfNeighbors.add(new Pixel(currentPixel.getX() - 1, currentPixel.getY() - 1, currentPixel));
		}
		return listOfNeighbors;
	}
	
	@Test
	public void testPath() {
		/*seam(image1, image2);	
		
				int cost = ImageCompositor.pixelSqDistance(image1[2][0],
						  image2[1][1]);
				System.out.println("from");
				System.out.println(2);
				System.out.println(0);
				System.out.println("to");
				System.out.println(1);
				System.out.println(1);
				
				System.out.println("cost");
				System.out.println(cost);

		*/
//		System.out.println(ImageCompositor.pixelSqDistance(0, 8));
//		System.out.println(ImageCompositor.pixelSqDistance(0, 2));
//		System.out.println(ImageCompositor.pixelSqDistance(0, 3));

		
	}
	

}
