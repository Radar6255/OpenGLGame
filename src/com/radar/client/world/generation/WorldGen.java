package com.radar.client.world.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.radar.client.Game;
import com.radar.client.Player;
import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Chunk;
import com.radar.client.world.Coord;
import com.radar.client.world.Coord2D;
import com.radar.client.world.Dimension;
import com.radar.common.WorldIO;

/**
 * @author radar
 * Used by the client to generate terrain
 * 
 * Class to generate the world on a seperate thread
 * Plan is to create the world in a data structure
 * Then also create the cube objects on this thread
 * Then send them over to the WindowUpdates class
 * To have them be rendered there
 */
public class WorldGen extends Generation implements Runnable{
	/**
	 * Holds the data for all the blocks in world
	 */
	volatile HashMap<Coord2D<Integer>, ArrayList<ArrayList<ArrayList<Short>>>> worldDim;
	
	/**
	 * Holds height of all liquids in the world
	 */
	public HashMap<Coord<Integer>,Float> liquids;
	
	//TODO Make a setting variable
	public static byte renderDist = 4;
	
	/**
	 * Stores how many chunks to generate the terrian of the world out to
	 */
	public static byte genDist = 8;
	
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
	protected WindowUpdates window;
	
	/**
	 * The thread that get started in the constructor and ended in the stop() method
	 */
	private Thread thread;
	
	/**
	 * Used to find if a tree needs to generate at a location
	 */
	private TreeGen treeGen;
	
	/**
	 * Used to keep track of which chunks are visible so that when one isn't
	 * it gets created and sent to the window to render
	 */
	private HashSet<Coord2D<Integer>> visibleChunks;
	
	/**
	 * Keeps track of all chunks that have been modified from default generation
	 */
//	public HashSet<Coord2D<Integer>> editedChunks;
	
	public static float timeWorldUpscale = 0.5f;
	
	public boolean write = true;
	
	/**
	 * Indicies of textures for each face
	 */
//	private int[][] faceTextures = new int[][] {{1, 1, 1, 1, 3, 2}, {4, 4, 4, 4, 4, 4}, {2, 2, 2, 2, 2, 2}};
//	private int[][] faceTextures = new int[][] {{5, 6, 7, 8, 9, 10}, {4, 4, 4, 4, 4, 4}};
	
	private HashMap<Coord2D<Integer>, ArrayList<ArrayList<ArrayList<Short>>>> saved;
	
	private float[][][] gradient;
	
	private int seed = -1;
	private WorldIO worldIO;
	private Dimension dim;
	
	/**
	 * Constructor to make the world generation thread and start it
	 * @param player The player to generate chunks for
	 * @param window The window to display the chunks on
	 */
	public WorldGen(Player player, int seed, WindowUpdates window, Dimension dim) {
		this.player = player;
		player.addGen(dim, this);
		this.window = window;
		this.dim = dim;
		
		worldIO = new WorldIO();
		running = true;
		visibleChunks = new HashSet<>();
		editedChunks = new HashSet<>();

		worldDim = new HashMap<Coord2D<Integer>, ArrayList<ArrayList<ArrayList<Short>>>>();
		liquids = new HashMap<>();
		
		if (!Game.MULTIPLAYER) {
			saved = worldIO.load("dimension" + dim + ".dat", this);
			int seed_temp = worldIO.getSeed();
			if(seed_temp != -1) {
				seed = seed_temp;
				window.addSeed(seed);
			}
			if(saved == null) {
				write = false;
			}
		}
		this.seed = seed;
		gradient = genGradient(1000, seed);
		treeGen = new TreeGen(seed);
		
		thread = new Thread(this);
		thread.start();
	}
	
	/**
	 * Shuts down this objects thread
	 */
	public void stop() {
		if(write)
			worldIO.save(worldDim, editedChunks, this, seed, dim);
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.out.println("World generation thread failed to close correctly");
		}
		System.out.println("World generation thread stopped for dimension "+dim);
	}
	
	/**
	 * Function to get the contents of a chunk in the world
	 * @param x The x position of the chunk to get
	 * @param z The z position of the chunk to get
	 * @return The chunk in the world at the desired position, null if it doesn't exist
	 */
	public ArrayList<ArrayList<ArrayList<Short>>> getChunk(int x, int z){
		return worldDim.get(new Coord2D<Integer>(x, z));
	}
	
	public void putChunk(int x, int z, ArrayList<ArrayList<ArrayList<Short>>> data) {
		worldDim.put(new Coord2D<Integer>(x, z), data);
//		Thread temp = new Thread({world.put(new Coord2D<Integer>(x, z), data);});
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
	public Chunk loadChunk(int chunkX, int chunkZ, Dimension dim) {
		visibleChunks.add(new Coord2D<Integer>(chunkX, chunkZ));
		Chunk creating = new Chunk(chunkX, chunkZ, this, window);
		return creating;
	}
	
	/**
	 * @param x The x position of the block
	 * @param y The y position of the block
	 * @param z The z position of the block
	 * @param chunkX The chunk the block is in x's position
	 * @param chunkZ The chunk the block is in z's position
	 * @param blockID The blockID be placed at this position
	 */
	public void placeBlock(float x, float y, float z, int chunkX, int chunkZ, short blockID) {
		ArrayList<ArrayList<ArrayList<Short>>> current = getChunk(chunkX, chunkZ);
		
		if (current == null) {
			return;
		}
		
		int relX;
		int relZ;
		
		if (x < 0) {
			relX = (int) (15 - (Math.abs(1+Math.floor(x)) % 16));
		}else {
			relX = (int) (Math.floor(x) % 16);
		}
		if (z < 0) {
			relZ = (int) (15 - (Math.abs(1+Math.floor(z)) % 16));
		}else {
			relZ = (int) (Math.floor(z) % 16);
		}
		
		if (current.get(relX).get(relZ).size() > Math.floor(y) && Math.floor(y) >= 0) {
			current.get(relX).get(relZ).set((int) Math.floor(y), blockID);
		}
	}
	
	@Override
	public void run() {
		while (running) {
			Coord<Float> pos = player.getPos();
			float x = pos.getX();
			float z = pos.getZ();
			
			switch(dim) {
			case TIME:
				x = x * timeWorldUpscale;
				z = z * timeWorldUpscale;
				break;
			default:
				break;
			}
			
			int playerChunkX = (int) (x/16);
			int playerChunkZ = (int) (z/16);
			
			for (int currentX = -genDist; currentX < genDist; currentX++) {
				for (int currentZ = -genDist; currentZ < genDist; currentZ++) {
					if (!visibleChunks.contains(new Coord2D<Integer>(currentX+playerChunkX, currentZ+playerChunkZ))) {
						
						//If the chunk has been loaded from file, use that instead of regenerating the chunk
						if (saved != null && saved.containsKey(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ)) && dim == Dimension.NORMAL) {
							worldDim.put(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ), saved.get(new Coord2D<Integer>(currentX + playerChunkX,currentZ + playerChunkZ)));
							saved.remove(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ));
							editedChunks.add(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ));
						}
						//TODO Error about null pointer
						else if (!worldDim.containsKey(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ))){
							ArrayList<ArrayList<ArrayList<Short>>> chunk = new ArrayList<ArrayList<ArrayList<Short>>>();
							worldDim.put(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ), chunk);
							for (int cubeX = 0; cubeX < 16; cubeX++) {
								worldDim.get(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ)).add(new ArrayList<ArrayList<Short>>());
								for (int cubeZ = 0; cubeZ < 16; cubeZ++) {
									worldDim.get(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ)).get(cubeX).add(new ArrayList<Short>());
									int cubeXPos, cubeZPos;
									switch (dim) {
									case NORMAL:
										cubeXPos = 16*(currentX + playerChunkX)+cubeX;
										cubeZPos = 16*(currentZ + playerChunkZ)+cubeZ;
										break;
									case TIME:
										cubeXPos = 16*(currentX + playerChunkX)+cubeX;
										cubeZPos = 16*(currentZ + playerChunkZ)+cubeZ;
										break;
									default:
										cubeXPos = 16*(currentX + playerChunkX)+cubeX;
										cubeZPos = 16*(currentZ + playerChunkZ)+cubeZ;
									}
									
									worldDim.get(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ)).get(cubeX).get(cubeZ).add((short) 4);
									
//									for (int i = 0; i < (int) Math.sqrt(3000-Math.pow(cubeXPos,2)-Math.pow(cubeZPos,2)); i++) {
//									for (int i = 0; i < (int) 30+(15*(Math.cos(0.1*cubeXPos)))+(15*(Math.sin(0.1*cubeZPos))); i++) {
									if(dim == Dimension.NORMAL) {
										for (int i = 0; i < (int) (80*perlin((float) cubeXPos*0.03f, (float)cubeZPos*0.03f, gradient)+32)-5; i++) {
											worldDim.get(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ)).get(cubeX).get(cubeZ).add((short) 2);
										}
									}else {
										for (int i = 0; i < (int) (80*perlin((float) cubeXPos*0.03f/timeWorldUpscale, (float)cubeZPos*0.03f/timeWorldUpscale, gradient)+32)-5; i++) {
											worldDim.get(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ)).get(cubeX).get(cubeZ).add((short) 2);
										}
									}
									
//									for (int i = 0; i < (int) (80*perlin((float) cubeXPos*0.03f*(1.0f/timeWorldUpscale), (float)cubeZPos*0.03f*(1.0f/timeWorldUpscale))+32)-5; i++) {
//										timeWorld.get(new Coord2D<Integer>(currentX + playerChunkX * timeWorldUpscale, currentZ + playerChunkZ * timeWorldUpscale)).get(cubeX).get(cubeZ).add((short) 2);
//									}
//									for (int i = (int) (80*perlin((float) cubeXPos*0.03f, (float)cubeZPos*0.03f)+32)-5; i < (int) (80*perlin((float) cubeXPos*0.03f, (float)cubeZPos*0.03f)+32); i++) {
//									for (int i = 0; i < (int) Math.sqrt(3000-Math.pow(cubeXPos,2)-Math.pow(cubeZPos,2))-5; i++) {
//										worldDim.get(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ)).get(cubeX).get(cubeZ).add((short) 3);
//									}
//									for (int i = 0; i < (int) Math.sqrt(3000-Math.pow(cubeXPos,2)-Math.pow(cubeZPos,2))-5; i++) {
//										timeWorld.get(new Coord2D<Integer>(currentX + playerChunkX * timeWorldUpscale, currentZ + playerChunkZ * timeWorldUpscale)).get(cubeX).get(cubeZ).add((short) 3);
//									}
									
									worldDim.get(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ)).get(cubeX).get(cubeZ).add((short) 1);
									
									if(treeGen.isTree(cubeXPos, cubeZPos)) {
										worldDim.get(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ)).get(cubeX).get(cubeZ).add((short) 5);
									}
									if(cubeX + cubeZ == 0 || cubeX + cubeZ == 1) {
										worldDim.get(new Coord2D<Integer>(currentX + playerChunkX, currentZ + playerChunkZ)).get(cubeX).get(cubeZ).add((short) 4);
									}
								}
							}
						}else if (Math.abs(currentX) < renderDist && Math.abs(currentZ) < renderDist) {
							window.addChunk(loadChunk(currentX+playerChunkX, currentZ+playerChunkZ, dim), dim);
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
	 * Uses the current seed value if there is one
	 * @param size The size of the random matrix
	 */
	static float[][][] genGradient(int size, int seed) {
		Random rand;
		float[][][] gradient;
		if (seed != -1) {
			rand = new Random(seed);
		}else {
			seed = new Random().nextInt();
			rand = new Random(seed);
		}
		gradient = new float[size][size][2];
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				gradient[x][y][0] = rand.nextFloat();
				gradient[x][y][1] = rand.nextFloat();
			}
		}
		return gradient;
	}
	
	/**
	 * Function to do a dot product with a point and a gradient vector
	 * @param gridX The x position of the gradient vector to do a dot product with
	 * @param gridY The y position of the gradient vector to do a dot product with
	 * @param x The x position of the point to dot product against
	 * @param y The y position of the point to dot product against
	 * @return The gradient of the desired point relative to the grid x,y
	 */
	private static float dotGridGradient (int gridX, int gridY, float x, float y, float[][][] gradient) {
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
	private static float linearInterpolation(float firstValue, float secondValue, float weight) {
		return (1.0f - weight)*firstValue + weight*secondValue;
	}
	
	/**
	 * Function to get the height of the world at a point using random gradients
	 * @param x The x value of the point
	 * @param y The y value of the point
	 * @return The height from -1 - 1 of terrain
	 */
	static float perlin (float x, float y, float[][][] gradient) {
		int xFloor = (int) Math.floor(x);
		int xCeiling = xFloor + 1;
		int yFloor = (int) Math.floor(y);
		int yCeiling = yFloor + 1;
		
		//X,Y position relative to grid
		float xPos = x - xFloor;
		float yPos = y - yFloor;
		
		float currentGrad, nextGrad, xGrad, yGrad, result;
		
		currentGrad = dotGridGradient(xFloor, yFloor, x, y, gradient);
		nextGrad = dotGridGradient(xCeiling, yFloor, x, y, gradient);
		xGrad = linearInterpolation(currentGrad, nextGrad, xPos);
		
		currentGrad = dotGridGradient(xFloor, yCeiling, x, y, gradient);
		nextGrad = dotGridGradient(xCeiling, yCeiling, x, y, gradient);
		yGrad = linearInterpolation(currentGrad, nextGrad, xPos);
		
		result = linearInterpolation(xGrad, yGrad, yPos);
		return result;
	}
}
