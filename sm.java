import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class SeamCarving {
	/**
	 * This next method is the core of our dynamic programming algorithm. We will
	 * use the top-down approach with the given hash table (which you should initialize).
	 * The key to the hash table is a pixel position. The value stored at each key
	 * is the "seam" that starts with this pixel all the way to the bottom
	 * of the image and its cost.
	 * <p>
	 * The method takes the position of a pixel and returns the seam from this pixel
	 * and its cost using the following steps:
	 * - compute the energy of the given pixel
	 * - get the list of neighbors below the current pixel
	 * - Base case: if the list of neighbors is empty, return the following pair:
	 * < [<h,w>], energy >
	 * the first component of the pair is a list containing just one position
	 * (the current one); the second component of the pair is the current energy.
	 * - Recursive case: we will consider each of the neighbors below the current
	 * pixel and choose the one with the cheapest seam.
	 */

	// hashTable we'll use
	Map<Position, Pair<List<Position>, Integer>> hash = new WeakHashMap<>();
	private int[] pixels;


	// Field getters
	private int type, height, width;

	int[] getPixels() {
		return pixels;
	}

	int getHeight() {
		return height;
	}


	// Read and write images

	int getWidth() {
		return width;
	}

	void readImage(String filename) throws IOException {
		BufferedImage image = ImageIO.read(new File(filename));
		type = image.getType();
		height = image.getHeight();
		width = image.getWidth();
		pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);
	}

	// Accessing pixels and their neighbors

	void writeImage(String filename) throws IOException {
		BufferedImage image = new BufferedImage(width, height, type);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		ImageIO.write(image, "jpg", new File(filename));
	}

	/***
	 * By convention, h is the vertical index and w and the horizontal index.
	 * The array of pixels is stored as follows:
	 * [(0,0), (0,1), (0,2), ... (0,width-1), (1,0), (1,1), (1,2), ... (1,width-1), ...]
	 */
	Color getColor(int h, int w) {
		int pixel = pixels[w + h * width];
		return new Color(pixel, true);
	}

	/**
	 * This method takes the position of a pixel (h,w) and returns a list of its
	 * neighbors' positions in the horizontal and vertical directions.
	 * In the general case, these would be at  positions:
	 * (h+1,w), (h-1,w), (h,w+1), (h,w-1).
	 * Of course, care must be taken when dealing with pixels along the boundaries.
	 */

	ArrayList<Position> getHVneighbors(int h, int w) {
		// change w
		Position right;
		Position left;
		// change h
		Position top;
		Position bottom;

		ArrayList<Position> neighbors = new ArrayList<Position>();

		// prevent w from looking left if at left boundary
		if (w == 0) {
			left = null;
		} else {
			left = new Position(h, w - 1);
			neighbors.add(left);
		}

		// prevent w from looking right if at right boundary
		if (w == this.width - 1) {
			right = null;
		} else {
			right = new Position(h, w + 1);
			neighbors.add(right);
		}

		// prevent h from looking up at boundary
		if (h == 0) {
			top = null;
		} else {
			top = new Position(h - 1, w);
			neighbors.add(top);
		}

		// prevent h from looking down at boundary
		if (h == this.height - 1) {
			bottom = null;
		} else {
			bottom = new Position(h + 1, w);
			neighbors.add(bottom);
		}

		return neighbors;
	}

	/**
	 * This method takes the position of a pixel (h,w) and returns a list of its
	 * neighbors' positions that are below and touching it.
	 * In the general case, these would be at  positions:
	 * (h+1,w-1), (h+1,w), (h+1,w+1)
	 * diag left, bottom,  diag right
	 * Of course, care must be taken when dealing with pixels along the boundaries.
	 */

	ArrayList<Position> getBelowNeighbors(int h, int w) {

		ArrayList<Position> below = new ArrayList<>();
		Position diagLeft;
		Position diagRight;
		Position bottom;
		boolean lookLeft = true;
		boolean lookBottom = true;
		boolean lookRight = true;

		if (h == this.height - 1) {
			lookBottom = false;
		}

		if (w == 0 || w == this.width - 1) {
			if (w == 0) {
				lookLeft = false;
			} else if (w == this.width - 1) {
				lookRight = false;
			}
		}

		if (lookBottom) {
			bottom = new Position(h + 1, w);
			below.add(bottom);
		}

		if (lookRight && lookBottom) {
			diagRight = new Position(h + 1, w + 1);
			below.add(diagRight);
		}

		if (lookLeft && lookBottom) {
			diagLeft = new Position(h + 1, w - 1);
			below.add(diagLeft);
		}

		return below;
	}

	/**
	 * This method takes the position of a pixel (h,w) and computes its 'energy'
	 * which is an estimate of how it differs from its neighbors. The computation
	 * is as follows. First, using the method getColor, get the colors of the pixel
	 * and all its neighbors in the horizontal and vertical dimensions. The energy
	 * is the sum of the squares of the differences along each of the RGB components
	 * of the color. For example, given two colors c1 and c2 (for the current pixel
	 * and one of its neighbors), we would compute this component of the energy as:
	 * square (c1.getRed() - c2.getRed()) +
	 * square (c1.getGreen() - c2.getGreen()) +
	 * square (c1.getBlue() - c2.getBlue())
	 * The total energy is this quantity summed over all the neighbors in the
	 * horizontal and vertical dimensions.
	 */

	int computeEnergy(int h, int w) {

		ArrayList<Position> neighbors = getHVneighbors(h, w);
		int sum = 0;
		final Color current = getColor(h, w);

		for (Position p : neighbors) {
			int red = getColor(p.getFirst(), p.getSecond()).getRed();
			int green = getColor(p.getFirst(), p.getSecond()).getGreen();
			int blue = getColor(p.getFirst(), p.getSecond()).getBlue();

			int sRed = (int) Math.pow((current.getRed() - red), 2);
			int sGreen = (int) Math.pow((current.getGreen() - green), 2);
			int sBlue = (int) Math.pow((current.getBlue() - blue), 2);
			int tempAll = sRed + sGreen + sBlue;

			sum += tempAll;
		}

		return sum;
	}

	Pair<List<Position>, Integer> findSeam(int h, int w) {

		Pair<List<Position>, Integer> cheapestSeam = null;
		int bestSoFar = Integer.MAX_VALUE;

		ArrayList<Position> belowNeighbors = getBelowNeighbors(h, w);

		// base case
		if (belowNeighbors.isEmpty()) {
			return new Pair<List<Position>, Integer>((List<Position>) new Node<Position>(new Position(h, w), new Empty<>()), computeEnergy(h, w));
		}

		// compute current position and energy
		int currentEnergy = computeEnergy(h, w);
		Position currentPixel = new Position(h, w);

		Pair<List<Position>, Integer> finalSeam = null;

		// check if we've seen this pixel before
		if (hash.containsKey(currentPixel)) {
			return hash.get(currentPixel);
		} else {
			// if we haven't seen this pixel position, compute its value

			// get the pixels below the current one
			for (Position childPixel : belowNeighbors) {
				// childSeam houses the recursive calls.
				Pair<List<Position>, Integer> childSeam = findSeam(childPixel.getFirst(), childPixel.getSecond());

				// check and store the best childSeam (lowest energy) in cheapestSeam
				if (childSeam.getSecond() < bestSoFar) {
					bestSoFar = childSeam.getSecond();
					cheapestSeam = childSeam;
				}

			}
			// at this point, we know the cheapest seam

			// incorporate the current pixel into the list because we only have the child seam
			finalSeam = new Pair<>(new Node<>(currentPixel, cheapestSeam.getFirst()), currentEnergy + cheapestSeam.getSecond());

			// store the value
			hash.put(currentPixel, finalSeam);

		}

		// return the value we computed
		return finalSeam;

	}

	/**
	 * This next method is relatively short. It performs the following actions:
	 * - clears the hash table
	 * - iterate over the first row of the image, computing the seam
	 * from its position and returning the best one.
	 */

	Pair<List<Position>, Integer> bestSeam() {

		hash.clear();

		Pair<List<Position>, Integer> bestRowSeam = new Pair<>(new Empty<>(), Integer.MAX_VALUE);
		Pair<List<Position>, Integer> candidate = null;

		for (int i = 0; i < getWidth(); i++) {

			candidate = findSeam(0, i);
			if (candidate.getSecond() < bestRowSeam.getSecond()) {
				bestRowSeam = candidate;
			}
		}

		return bestRowSeam;
	}

	/**
	 * The last method puts its all together:
	 * - it finds the best seam
	 * - then it creates a new array of pixels representing an image of dimensions
	 * (height,width-1)
	 * - it then copies the old array pixels to the new arrays skipping the pixels
	 * in the seam
	 * - the method does not return anything: instead it updates the width and
	 * pixels instance variables to the new values.
	 */
	void cutSeam() {
		// holds the best seam object
		Pair<List<Position>, Integer> bestSeam = bestSeam();
		// holds only the list of positions from best seam
		List<Position> list = bestSeam.getFirst();
		//System.out.println("Best seam is: " + list.length());
		// holds the new pixels which don't have any best seam pixels
		//ArrayList<Integer> newPixel = new ArrayList<>();

		/*
		// holds the pixels to be removed
		ArrayList<Position> toRemove = new ArrayList<>();

		// convert the best seam list into an arraylist
		int listSize = list.length();
		try {
			for (int i = 0; i < listSize; i++) {
				toRemove.add(list.getFirst());
				list = list.getRest();
			}
		} catch (EmptyListE e) {
			// this will never run because the length we iterate over is checked
			e.printStackTrace();
		}
		//System.out.println("toRemove is: " + toRemove.size());

		ArrayList<Integer> toRemovePixels = new ArrayList<>();

		try {
			for (Position position : toRemove) {
				toRemovePixels.add(position.getSecond() + (position.getFirst() * width));
			}
		}catch (NoSuchElementException e){
			e.printStackTrace();
		}
		System.out.println("toRemovePixels is: " + toRemovePixels.size());
		*/
		/*
		int counter = 0;
		for (int i = 0; i < pixels.length; i++) {
			// if the pixel belongs to the best seam, do not add to new pixel array
			if (toRemovePixels.contains(pixels[i])) {
				//counter++;
				continue;
			} else {
				// add pixel to new pixel array
				//newPixel[i - counter] = pixels[i];
				newPixel.add(pixels[i]);

			}
		}



		int[] finalArray = new int[height * (width - 1)];
		System.out.println("finalArray size is: " + finalArray.length);
		System.out.println("newPixel arraylist is: " + newPixel.size());
		for(int i = 0; i < newPixel.size(); i++){
			finalArray[i] = newPixel.get(0);
		}

		 */



		/*
		int counter = 0;
		for (int i = 0; i < newPixel.length; i++) {
			// if the pixel belongs to the best seam, do not add to new pixel array
			if (toRemove.contains(pixels[i])) {
				counter++;
				continue;
			} else {
				// add pixel to new pixel array
				newPixel[i - counter] = pixels[i];
			}
		}

		 */

		int[] newPixel = new int[height * (width - 1)];
		try{
			List<Position> seam = bestSeam.getFirst();
			Position firstPosition = seam.getFirst();

			// outer loop controls up and down (h)
			for(int h = 0; h < height; h++){

				// inner loop controls side to side (w)
				int newWidth = 0;
				for (int w = 0; w < width; w++){

					if (seam.isEmpty()){
						// base case. Add everything
						newPixel[h * (width - 1) + newWidth] = pixels[h + newWidth];
						newWidth++;
					}else if((seam.getFirst().getFirst() == h) && (seam.getFirst().getSecond() == w)){
						// do not add pixel because we found a seam match
						 //newPixel[height * h + newWidth] =  pixels[height * h + newWidth + 1];
						 seam = seam.getRest();

					}else {
						// add pixel
						//[h * (width - 1)]
						newPixel[h * (width - 1) + newWidth] = pixels[h + newWidth];
						newWidth++;
					}
					//firstPosition = seam.getFirst();

				}

			}
		}catch (EmptyListE e){
			e.printStackTrace();
		}





		// update pixels instance variable
		pixels = newPixel;
		// reduce width because each cut reduce width by one
		width--;
	}
}


