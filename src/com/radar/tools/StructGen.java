package com.radar.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.radar.client.Player;
import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Chunk;
import com.radar.client.world.Coord;
import com.radar.client.world.Dimension;
import com.radar.client.world.generation.Generation;
import com.radar.client.world.generation.Structure;

public class StructGen extends Generation{
	WindowUpdates window;
	
	ArrayList<ArrayList<ArrayList<Short>>> chunk;
	
	private Coord<Integer> bottomCorner, topCorner;
	
	public StructGen(Player player, WindowUpdates window) {
		this.window = window;
		super.editedChunks = new HashSet<>();
		super.liquids = new HashMap<Coord<Integer>, Float>();
		
		Structure loaded = Structure.loadStructure("struct.dat");
	}

	/**
	 * Creating a new chunk that is just a flat layer of blocks to build on
	 */
	public Chunk genChunk(int x, int z) {
		ArrayList<ArrayList<ArrayList<Short>>> out = new ArrayList<ArrayList<ArrayList<Short>>>();
		
		for(int tx = 0; tx < 16; tx++) {
			out.add(new ArrayList<ArrayList<Short>>());
			for(int tz = 0; tz < 16; tz++) {
				out.get(tx).add(new ArrayList<Short>());
				out.get(tx).get(tz).add((short) 4);
				out.get(tx).get(tz).add((short) 4);
			}
		}
		chunk = out;
		
		Chunk outChunk = new Chunk(x, z, this, window);
		return outChunk;
	}
	
	public Coord<Integer> getBottomCorner(){
		return bottomCorner;
	}
	
	public Coord<Integer> getTopCorner(){
		return topCorner;
	}
	
	
	@Override
	public ArrayList<ArrayList<ArrayList<Short>>> getChunk(int chunkX, int chunkZ) {
		return chunk;
	}

	@Override
	public void placeBlock(float x, float y, float z, int chunkX, int chunkZ, short blockID) {
		// TODO Auto-generated method stub
	}

	@Override
	public Chunk loadChunk(int chunkX, int chunkZ, Dimension dim) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Shuts down this objects thread
	 */
	public void stop() {
		Structure.saveStructure(this, "struct");
		
		System.out.println("Structure world gen stopped.");
	}
	
	/**
	 * Getting the borders of the structure built to save later
	 */
	@Override
	public void updatePosition(Coord<Integer> pos) {
		if(topCorner == null) {
			topCorner = pos.clone();
			bottomCorner = pos.clone();
			return;
		}
		
		if(topCorner.getX() < pos.getX()) {
			topCorner.setX(pos.getX());
		}if(topCorner.getY() < pos.getY()) {
			topCorner.setY(pos.getY());
		}if(topCorner.getZ() < pos.getZ()) {
			topCorner.setZ(pos.getZ());
		}
		
		if(bottomCorner.getX() > pos.getX()) {
			bottomCorner.setX(pos.getX());
		}if(bottomCorner.getY() > pos.getY()) {
			bottomCorner.setY(pos.getY());
		}if(bottomCorner.getZ() > pos.getZ()) {
			bottomCorner.setZ(pos.getZ());
		}
	}
	
}
