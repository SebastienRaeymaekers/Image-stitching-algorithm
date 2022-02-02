package gna;

import java.util.*;
import libpract.*;

/**
 * Implement the methods stitch, seam and floodfill.
 */
public class Stitcher
{
	//Comparator class to compare pixel according to their cost calculated by the ImageCompositor.pixelSqDistance method.
    public static class PixelComparator implements Comparator<Pixel>{
    	public int compare(Pixel PixelA, Pixel PixelB) {
        	if (PixelA.getCost() < PixelB.getCost()) return -1;
        	else if (PixelA.getCost() > PixelB.getCost()) return 1;
        	else return 0;
    	}
    }

	/**
	 * Return the sequence of positions on the seam. The first position in the
	 * sequence is (0, 0) and the last is (width - 1, height - 1). Each position
	 * on the seam must be adjacent to its predecessor and successor (if any).
	 * Positions that are diagonally adjacent are considered adjacent.
	 * 
	 * image1 and image2 are both non-null and have equal dimensions.
	 *
	 * Remark: Here we use the default computer graphics coordinate system,
	 *   illustrated in the following image:
	 * 
	 *        +-------------> X
	 *        |  +---+---+
	 *        |  | A | B |
	 *        |  +---+---+
	 *        |  | C | D |
	 *        |  +---+---+
	 *      Y v 
	 * 
	 *   The historical reasons behind using this layout is explained on the following	
	 *   website: http://programarcadegames.com/index.php?chapter=introduction_to_graphics
	 * 
	 *   Position (y, x) corresponds to the pixels image1[y][x] and image2[y][x]. This
	 *   convention also means that, when an automated test mentioned that it used the array
	 *   {{A,B},{C,D}} as a test image, this corresponds to the image layout as shown in
	 *   the illustration above.
	 */
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
		return null; 															   //if openSetPQ runs out of option, no solution was found
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
		for(int i = 0; i < path.size(); i++) {
			listOfPositions.add(new Position(path.get(i).getX(), path.get(i).getY()));
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

	
	/**
	 * Apply the floodfill algorithm described in the assignment to mask. You can assume the mask
	 * contains a seam from the upper left corner to the bottom right corner. The seam is represented
	 * using Stitch.SEAM and all other positions contain the default value Stitch.EMPTY. So your
	 * algorithm must replace all Stitch.EMPTY values with either Stitch.IMAGE1 or Stitch.IMAGE2.
	 *
	 * Positions left to the seam should contain Stitch.IMAGE1, and those right to the seam
	 * should contain Stitch.IMAGE2. You can run `ant test` for a basic (but not complete) test
	 * to check whether your implementation does this properly.
	 */ 
	public void floodfill(Stitch[][] mask) {	
		int[] startPosImg1 = findSeamStartPos("IMAGE1", mask); 		
		floodfill(mask, startPosImg1, Stitch.IMAGE1);
		
		int[] startPosImg2 = findSeamStartPos("IMAGE2", mask); 
		floodfill(mask, startPosImg2, Stitch.IMAGE2);
	}
	
	private void floodfill(Stitch[][] mask, int[] startPosImg, Stitch stitchImage) {
		Stack<int[]> stack = new Stack<int[]>();
		
		stack.push(startPosImg);
		int[] currentPos = startPosImg;
		mask[currentPos[0]][currentPos[1]] = stitchImage;
		
		while(!stack.isEmpty()) {//check of there are unvisited cells
			List<int[]> neighbors = getNeighborsPosInMask(currentPos,mask, stitchImage);
			if(!neighbors.isEmpty()) {
				currentPos = neighbors.get(0);
				for(int[] neighbor : neighbors) { //first position to check is POSITION UNDER (DFS)
					mask[neighbor[0]][neighbor[1]] = stitchImage;
					stack.push(currentPos);
					break;
				}
			}
			else if(!stack.isEmpty()){ //if stack is not empty within while loop, pop element and take it as current position.
				currentPos = stack.pop();
			}
		}
	}

	private List<int[]> getNeighborsPosInMask(int[] Pos, Stitch[][] mask, Stitch stitchImage) {
		List<int[]> listOfNeighbors = new ArrayList<int[]>();
		if (Pos[0] + 1 <= mask.length - 1 && mask[Pos[0] + 1][Pos[1]] != Stitch.SEAM && mask[Pos[0] + 1][Pos[1]] != stitchImage) {
				int[] posUnder = {Pos[0] + 1,Pos[1]};
				listOfNeighbors.add(posUnder);
		}
		if (Pos[1] + 1 <= mask[0].length - 1 && mask[Pos[0]][Pos[1] + 1] != Stitch.SEAM && mask[Pos[0]][Pos[1] + 1] != stitchImage) {
				int[] posRight = {Pos[0],Pos[1] + 1};
				listOfNeighbors.add(posRight);		
		}
		if (Pos[0] - 1 >= 0 && mask[Pos[0] - 1][Pos[1]] != Stitch.SEAM && mask[Pos[0] - 1][Pos[1]] != stitchImage) {
				int[] posLeft = {Pos[0] - 1,Pos[1]};
				listOfNeighbors.add(posLeft);
		}
		if (Pos[1] - 1 >= 0 && mask[Pos[0]][Pos[1] - 1] != Stitch.SEAM && mask[Pos[0]][Pos[1] - 1] != stitchImage) {
				int[] posAbove = {Pos[0],Pos[1] - 1};
				listOfNeighbors.add(posAbove);
		}
		return listOfNeighbors;
	}

	private int[] findSeamStartPos(String string, Stitch[][] mask) {
		if (string == "IMAGE1") {
			int[] startPos = {0,0};
			while (mask[startPos[0]][startPos[1]] == Stitch.SEAM) {
				startPos[0] += 1; // move under so not on seam anymore but guaranteed in IMAGE1 (by definition).
			}
			return startPos;
		}
		if (string == "IMAGE2") {
			int[] startPos = {0,0};
			for(int i = 0; i < mask[0].length; i++) // check for first spot that is empty, should always be first IMAGE2 spot. 
			    for(int j = 0; j < mask.length; j++) {
			    	if (mask[i][j] == Stitch.EMPTY) {
			    		startPos[0] = i;
			    		startPos[1] = j;
			    		return startPos;
			    	}	
			    }
		}
		return null; //if nothing found return null (but will never happen)
	}
	
	/**
	 * Return the mask to stitch two images together. The seam runs from the upper
	 * left to the lower right corner, where in general the rightmost part comes from
	 * the second image (but remember that the seam can be complex, see the spiral example
	 * in the assignment). A pixel in the mask is Stitch.IMAGE1 on the places where
	 * image1 should be used, and Stitch.IMAGE2 where image2 should be used. On the seam
	 * record a value of Stitch.SEAM.
	 * 
	 * ImageCompositor will only call this method (not seam and floodfill) to
	 * stitch two images.
	 * 
	 * image1 and image2 are both non-null and have equal dimensions.
	 */
	public Stitch[][] stitch(int[][] image1, int[][] image2) {
		//(1) seam
		List<Position> overlapppingBorder = seam(image1, image2);
		Stitch[][] mask = new Stitch[image1.length][image1[0].length];
		
		for(int i = 0; i < overlapppingBorder.size(); i++) { //fill mask with Stitch.SEAM on positions gotten from seam function.
			mask[overlapppingBorder.get(i).getX()][overlapppingBorder.get(i).getY()] = Stitch.SEAM;
		}
		for(int i = 0; i < image1.length; i++) //fill rest with Stitch.EMPY
			for(int j = 0; j < image1[0].length; j++) {
				if (mask[i][j] != Stitch.SEAM) {
					mask[i][j] = Stitch.EMPTY;					
				}
		    }
	
		//(2) floodfill
		floodfill(mask);
		
		return mask;
	}

}



