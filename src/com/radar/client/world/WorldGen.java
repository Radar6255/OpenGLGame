package com.radar.client.world;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;
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
	
	public static int renderDist = 2;
	
	private int xOffset = 0;
	private int zOffset = 0;
	
	public boolean running;
	
	Player player;
	WindowUpdates window;
	
	GL2 gl;
	public WorldGen(Player player, WindowUpdates window, GL2 gl) {
		this.player = player;
		this.window = window;
		this.gl = gl;
		running = true;
		world = new ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<Integer>>>>>();
//		new Thread(this).start();
	}
	
	@Override
	public void run() {
		while (running) {
			Coord<Float> pos = player.getPos();
			float x = pos.getX();
			float z = pos.getZ();
			
			int playerChunkX = (int) (x/16);
			int playerChunkZ = (int) (z/16);
			//TODO Once memory leak is fixed
			running = false;
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
					System.out.println((currentX + playerChunkX + xOffset)+" "+(currentZ + playerChunkZ + zOffset));
					
					if (world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).size() == 0) {
						System.out.println("Creating chunk...");
						Chunk temp = new Chunk(currentX + playerChunkX, currentZ+playerChunkZ);
						for (int cubeX = 0; cubeX < 16; cubeX++) {
							world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).add(new ArrayList<ArrayList<Integer>>());
							for (int cubeZ = 0; cubeZ < 16; cubeZ++) {
								world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).get(cubeX).add(new ArrayList<Integer>());
								//Adding a layer to the bottom
								world.get(currentX + playerChunkX + xOffset).get(currentZ + playerChunkZ + zOffset).get(cubeX).get(cubeZ).add(1);
								temp.addCube(new Cube(16*(currentX + playerChunkX)+cubeX, 0, 16*(currentZ + playerChunkZ)+cubeZ, 1, 1, 1, gl));
							}
						}
						window.addChunk(temp);
					}
				}
			}
		}System.out.println("World generation ended");
	}

}
