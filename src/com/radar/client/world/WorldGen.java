package com.radar.client.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import com.radar.client.Player;
import com.radar.client.window.WindowUpdates;

/**
 * @author radar
 * Class to generate the world on a seperate thread
 * Plan is to create the world in a data structure
 * Then also create the cube objects on this thread
 * Then send them over to the WindowUpdates class
 * To have them be rendered there
 */
public class WorldGen implements Runnable {
	//TODO Consider changing into quadrants
	
	/**
	 * Holds the data for all the blocks in world
	 */
	ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<Integer>>>>> world;
	
	//TODO Make a setting variable
	public static int renderDist = 14;
	
	/**
	 * Stores how many chunks to generate the terrian of the world out to
	 */
	public static int genDist = 20;
	
	/**
	 * Offsets for the chunks so that we can have "negative" chunk coordinates
	 */
	private int xOffset = 0, zOffset = 0;
	
	/**
	 * Keeps the world generation running while the game is running
	 */
	private boolean running;
	
	/**
	 * The player to generate chunks around
	 */
	private Player player;
	
	/**
	 * The window to give the chunks to
	 */
	WindowUpdates window;
	
	/**
	 * The thread that get started in the constructor and ended in the stop() method
	 */
	private Thread thread;
	
	/**
	 * Used to keep track of which chunks are visible so that when one isn't
	 * it gets created and sent to the window to render
	 */
	private HashSet<Coord2D<Integer>> visibleChunks;
	
	/**
	 * Indicies of textures for each face
	 */
	private int[][] faceTextures = new int[][] {{1, 1, 1, 1, 3, 2}, {4, 4, 4, 4, 4, 4}};
	

	private float[][][] gradient;
	
	/**
	 * Constructor to make the world generation thread and start it
	 * @param player The player to generate chunks for
	 * @param window The window to display the chunks on
	 */
	public WorldGen(Player player, WindowUpdates window) {
		this.player = player;
		this.window = window;
		running = true;
		world = new ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<Integer>>>>>();
		visibleChunks = new HashSet<>();
//		generatedChunks = new HashSet<>();
		genGradient(1000);
		thread = new Thread(this);
		thread.start();
	}
	
	/**
	 * Shuts down this objects thread
	 */
	public void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.out.println("World generation thread failed to close correctly");
		}
		System.out.println("World generation thread stopped");
	}
	
	/**
	 * Function to get the contents of a chunk in the world
	 * @param x The x position of the chunk to get
	 * @param z The z position of the chunk to get
	 * @return The chunk in the world at the desired position
	 */
	public ArrayList<ArrayList<ArrayList<Integer>>> getChunk(int x, int z){
		return world.get(x + xOffset).get(z + zOffset);
	}
	
	/**
	 * Tells the world gen which chunks it may have to regenerate if loaded again
	 * @param chunkX The chunks X position that is being removed
	 * @param chunkZ The chunks Z position that is being removed
	 */
	public void removeChunk(int chunkX, int chunkZ) {
		visibleChunks.remove(new Coord2D<Integer>(chunkX, chunkZ));
	}
	
	/**
	 * Function to create the chunk containing all the cubes in the area specified
	 * @param chunkX The x position of the chunk, relative to chunks not block positions
	 * @param chunkZ The z position of the chunk, relative to chunks not block positions
	 * @return A new chunk with the cube objects in it to render
	 */
	public Chunk loadChunk(int chunkX, int chunkZ) {
		ArrayList<ArrayList<ArrayList<Integer>>> chunk = world.get(chunkX+xOffset).get(chunkZ+zOffset);
//		ArrayList<ArrayList<ArrayList<boolean>>> placed = 
		
		Chunk creating = new Chunk(chunkX, chunkZ);
		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < chunk.get(x).get(z).size(); y++) {
					if (chunk.get(x).get(z).get(y) != 0) {
						creating.addCube(new Cube(chunkX*16 + x, y, chunkZ*16 + z, 1, 1, 1, faceTextures[chunk.get(x).get(z).get(y)-1], this));
					}
				}
			}
		}
		visibleChunks.add(new Coord2D<Integer>(chunkX, chunkZ));
		return creating;
	}
	
	@Override
	public void run() {
		while (running) {
			Coord<Float> pos = player.getPos();
			float x = pos.getX();
			float z = pos.getZ();
			
			int playerChunkX = (int) (x/16);
			int playerChunkZ = (int) (z/16);
			
			for (int currentX = -genDist; currentX < genDist; currentX++) {
				while (currentX + playerChunkX + xOffset < 0) {
					xOffset++;
					world.add(0, new ArrayList<ArrayList<ArrayList<ArrayList<Integer>>>>());
					//TODO Change so it buffers the beginning chunks at least
					for (int i = 0; i < zOffset; i++) {
						world.get(0).add(new ArrayList<ArrayList<ArrayList<Integer>>>());
					}
				}
				while (currentX + playerChunkX + xOffset >= world.size()) {
					world.add(new ArrayList<ArrayList<ArrayList<ArrayList<Integer>>>>());
					for (int i = 0; i < zOffset; i++) {
						world.get(world.size()-1).add(new ArrayList<ArrayList<ArrayList<Integer>>>());
					}
				}
				for (int currentZ = -genDist; currentZ < genDist; currentZ++) {
					while (currentZ + playerChunkZ + zOffset < 0) {
						zOffset++;
						for (ArrayList<ArrayList<ArrayList<ArrayList<Integer>>>> xStrip: world) {
							xStrip.add(0, new ArrayList<ArrayList<ArrayList<Integer>>>());
						}
					}
					while (currentZ + playerChunkZ + zOffset >= world.get(currentX + playerChunkX + xOffset).size()) {
						for (ArrayList<ArrayList<ArrayList<ArrayList<Integer>>>> xStrip: world) {
							xStrip.add(new ArrayList<ArrayList<ArrayList<Integer>>>());
						}
					}
					
					if (!visibleChunks.contains(new Coord2D<Integer>(currentX+playerChunkX, currentZ+playerChunkZ))) {
						
						if (world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).size() == 0) {
						
							for (int cubeX = 0; cubeX < 16; cubeX++) {
								world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).add(new ArrayList<ArrayList<Integer>>());
								for (int cubeZ = 0; cubeZ < 16; cubeZ++) {
									world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).get(cubeX).add(new ArrayList<Integer>());
								
									int cubeXPos = 16*(currentX + playerChunkX)+cubeX;
									int cubeZPos = 16*(currentZ + playerChunkZ)+cubeZ;
								
//									for (int i = 0; i < (int) Math.sqrt(3000-Math.pow(cubeXPos,2)-Math.pow(cubeZPos,2)); i++) {
//									for (int i = 0; i < (int) 30+(15*(Math.cos(0.1*cubeXPos)))+(15*(Math.sin(0.1*cubeZPos))); i++) {
									for (int i = 0; i < (int) (80*perlin((float) cubeXPos*0.03f, (float)cubeZPos*0.03f)+32); i++) {
										world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).get(cubeX).get(cubeZ).add(2);
									}
									world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).get(cubeX).get(cubeZ).add(1);
								}
							}
						}else if (Math.abs(currentX) < renderDist && Math.abs(currentZ) < renderDist) {
							window.addChunk(loadChunk(currentX+playerChunkX, currentZ+playerChunkZ));
						}
					}
				}
			}
		}
	}

	
	//All code for the perlin noise generation was based on a code sample
	//From wikipedia, the link to it is here https://en.wikipedia.org/wiki/Perlin_noise
	//Incase you would like to do this yourself
	
	/**
	 * Function to generate a random set of weights for gradients in perlin noise
	 * @param size The size of the random matrix
	 */
	private void genGradient(int size) {
		Random rand = new Random();
		gradient = new float[size][size][2];
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				gradient[x][y][0] = rand.nextFloat();
				gradient[x][y][1] = rand.nextFloat();
			}
		}
	}
	
	/**
	 * Function to do a dot product with a point and a gradient vector
	 * @param gridX The x position of the gradient vector to do a dot product with
	 * @param gridY The y position of the gradient vector to do a dot product with
	 * @param x The x position of the point to dot product against
	 * @param y The y position of the point to dot product against
	 * @return The gradient of the desired point relative to the grid x,y
	 */
	private float dotGridGradient (int gridX, int gridY, float x, float y) {
		float dx = x - gridX;
		float dy = y - gridY;
		
		return (dx*gradient[Math.abs(gridX)%gradient.length][Math.abs(gridY)%gradient.length][0] + dy*gradient[Math.abs(gridX)%gradient.length][Math.abs(gridY)%gradient.length][1]);
	}
	
	/**
	 * Finds a point between 2 desired points? I'm not entirely sure what this means
	 * @param firstValue The first desired value
	 * @param secondValue The second desired value
	 * @param weight The point where they converge?
	 * @return The new desired value
	 */
	private float linearInterpolation(float firstValue, float secondValue, float weight) {
		return (1.0f - weight)*firstValue + weight*secondValue;
	}
	
	/**
	 * Function to get the height of the world at a point using random gradients
	 * @param x The x value of the point
	 * @param y The y value of the point
	 * @return The height from -1 - 1 of terrain
	 */
	private float perlin (float x, float y) {
		int xFloor = (int) Math.floor(x);
		int xCeiling = xFloor + 1;
		int yFloor = (int) Math.floor(y);
		int yCeiling = yFloor + 1;
		
		//X,Y position relative to grid
		float xPos = x - xFloor;
		float yPos = y - yFloor;
		
		float currentGrad, nextGrad, xGrad, yGrad, result;
		
		currentGrad = dotGridGradient(xFloor, yFloor, x, y);
		nextGrad = dotGridGradient(xCeiling, yFloor, x, y);
		xGrad = linearInterpolation(currentGrad, nextGrad, xPos);
		
		currentGrad = dotGridGradient(xFloor, yCeiling, x, y);
		nextGrad = dotGridGradient(xCeiling, yCeiling, x, y);
		yGrad = linearInterpolation(currentGrad, nextGrad, xPos);
		
		result = linearInterpolation(xGrad, yGrad, yPos);
		if (result > 1) {
			System.out.println(result);
		}
		return result;
	}
}
