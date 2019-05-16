package com.radar.client.world;

import java.util.ArrayList;
import java.util.HashSet;

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
	public static int renderDist = 3;
	
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
	
	private HashSet<Coord2D<Integer>> visibleChunks;
	
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
	
	public void removeChunk(int chunkX, int chunkZ) {
//		System.out.println(visibleChunks.contains(new Coord2D<Integer>(chunkX, chunkZ)));
		visibleChunks.remove(new Coord2D<Integer>(chunkX, chunkZ));
//		System.out.println(visibleChunks.contains(new Coord2D<Integer>(chunkX, chunkZ)));
	}
	
	/**
	 * Function to create the chunk containing all the cubes in the area specified
	 * @param chunkX The x position of the chunk, relative to chunks not block positions
	 * @param chunkZ The z position of the chunk, relative to chunks not block positions
	 * @return A new chunk with the cube objects in it to render
	 */
	public Chunk loadChunk(int chunkX, int chunkZ) {
		ArrayList<ArrayList<ArrayList<Integer>>> chunk = world.get(chunkX+xOffset).get(chunkZ+zOffset);
		
		Chunk creating = new Chunk(chunkX, chunkZ);
		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < chunk.get(x).get(z).size(); y++) {
					
					if (chunk.get(x).get(z).get(y) != 0) {
						creating.addCube(new Cube(chunkX*16 + x, y, chunkZ*16 + z, 1, 1, 1, this));
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
			
			for (int currentX = -renderDist; currentX < renderDist; currentX++) {
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
				for (int currentZ = -renderDist; currentZ < renderDist; currentZ++) {
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
							visibleChunks.add(new Coord2D<Integer>(currentX+playerChunkX, currentZ+playerChunkZ));
							Chunk temp = new Chunk(currentX + playerChunkX, currentZ+playerChunkZ);
						
							for (int cubeX = 0; cubeX < 16; cubeX++) {
								world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).add(new ArrayList<ArrayList<Integer>>());
								for (int cubeZ = 0; cubeZ < 16; cubeZ++) {
									world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).get(cubeX).add(new ArrayList<Integer>());
								
									int cubeXPos = 16*(currentX + playerChunkX)+cubeX;
									int cubeZPos = 16*(currentZ + playerChunkZ)+cubeZ;
								
									for (int i = 0; i < (int) Math.sqrt(2000-Math.pow(cubeXPos,2)-Math.pow(cubeZPos,2)); i++) {
										world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).get(cubeX).get(cubeZ).add(0);
									}
									world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).get(cubeX).get(cubeZ).add(1);
//									temp.addCube(new Cube(cubeXPos, (int) Math.sqrt(2000-Math.pow(cubeXPos,2)-Math.pow(cubeZPos,2)), cubeZPos, 1, 1, 1, this));
									temp.addCube(new Cube(cubeXPos, world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).get(cubeX).get(cubeZ).size()-1, cubeZPos, 1, 1, 1, this));
								}
							}
							window.addChunk(temp);
						}else {
							window.addChunk(loadChunk(currentX+playerChunkX, currentZ+playerChunkZ));
						}
					}
				}
			}
		}
	}

}
