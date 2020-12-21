package com.radar.client.world.generation;

/**
 * @author Riley Adams
 * Class to place trees into the terrain
 */
public class TreeGen {
	private float[][][] firstGrad, secondGrad, thirdGrad;
	
	/**
	 * Generating gradients for the tree generation
	 */
	public TreeGen(int seed) {
		firstGrad = WorldGen.genGradient(600, seed);
		secondGrad = WorldGen.genGradient(800, (int) Math.floor(firstGrad[500][500][1] * 10000));
		thirdGrad = WorldGen.genGradient(1000, (int) Math.floor(secondGrad[500][500][1] * 10000));
	}
	
	/**
	 * Function to find if a tree should generate at a given x, y position
	 * @param x The x position to poll for a tree
	 * @param y The y position to poll for a tree
	 * @return True if a tree should generate here, false otherwise
	 */
	public boolean isTree(int x, int y) {
		
		// Find height of position to check
		float mid = (WorldGen.perlin((x-2000)*0.02f, (y-2000)*0.02f, firstGrad));
		mid = mid - (WorldGen.perlin((x-2000)*0.1f, (y-2000)*0.1f, secondGrad));
		mid = mid + (WorldGen.perlin((x-2000)*0.2f, (y-2000)*0.2f, thirdGrad));
		
		// Check the middle height to all adjacent position to determine if its a maximum
		for(int tx = -1; tx < 2; tx++) {
			for(int ty = -1; ty < 2; ty++) {
				float cur = (WorldGen.perlin((x+tx-2000)*0.02f, (y+ty-2000)*0.02f, firstGrad));
				cur = cur - (WorldGen.perlin((x+tx-2000)*0.1f, (y+ty-2000)*0.1f, secondGrad));
				cur = cur + (WorldGen.perlin((x+tx-2000)*0.2f, (y+ty-2000)*0.2f, thirdGrad));

				if(cur > mid) {
					return false;
				}
			}
		}
		
		return true;
	}
}
