package com.radar.client.world.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.radar.client.world.Chunk;
import com.radar.client.world.Coord;
import com.radar.client.world.Coord2D;
import com.radar.client.world.Dimension;

public abstract class Generation {
	public HashMap<Coord<Integer>,Float> liquids;
	public HashSet<Coord2D<Integer>> editedChunks;
	public boolean write = true;
	
	public abstract ArrayList<ArrayList<ArrayList<Short>>> getChunk(int chunkX, int chunkZ);

	public abstract void placeBlock(float x, float y, float z, int chunkX, int chunkZ, short blockID);

	/**
	 * @param x The x position of the block
	 * @param y The y position of the block
	 * @param z The z position of the block
	 * @param chunkX The chunk the block is in x's position
	 * @param chunkZ The chunk the block is in z's position
	 * @return The blockID at that position, or -1 if invalid y position
	 */
	public int getBlock(float x, float y, float z) {
		int chunkX = (int) Math.floor(Math.floor(x)/16.0);
		int chunkZ = (int) Math.floor(Math.floor(z)/16.0);
		
		ArrayList<ArrayList<ArrayList<Short>>> current = getChunk(chunkX, chunkZ);
		
		if (current == null) {
			return -1;
		}
		
		byte relX;
		byte relZ;
		
		if (x < 0) {
			relX = (byte) (15 - (Math.abs(1+Math.floor(x)) % 16));
		}else {
			relX = (byte) (Math.floor(x) % 16);
		}
		if (z < 0) {
			relZ = (byte) (15 - (Math.abs(1+Math.floor(z)) % 16));
		}else {
			relZ = (byte) (Math.floor(z) % 16);
		}
		
		if (current.get(relX).get(relZ).size() > Math.floor(y) && Math.floor(y) >= 0) {
			return current.get(relX).get(relZ).get((int) Math.floor(y));
		}
		return -1;
	}
	
	public abstract Chunk loadChunk(int chunkX, int chunkZ, Dimension dim);
}
