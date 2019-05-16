package com.radar.client.world;

import java.util.LinkedList;

import com.jogamp.opengl.GL2;

/**
 * @author radar
 * A class to represent a chunk of cubes,
 * used to make generating, loading, and unloading the world easier.
 */
public class Chunk {
	/**
	 * List of all cubes in this chunk
	 */
	LinkedList<Cube> cubes;
	
	private int x, z;
	
	/**
	 * Creates a chunk at the specified x, z
	 * @param x The x chunk position of this chunk
	 * @param z The z chunk position of this chunk
	 */
	public Chunk(int x, int z) {
		this.x = x;
		this.z = z;
		cubes = new LinkedList<>();
	}
	
	/**
	 * A function to add a cube to this chunk
	 * @param cube The cube to add to the chunk
	 */
	public void addCube(Cube cube) {
		cubes.add(cube);
	}
	
	/**
	 * Function to draw all the cubes in this chunk
	 * @param gl The GL2 reference for drawing the cuebs
	 */
	public void render(GL2 gl) {
		for (Cube cube: cubes) {
			cube.render(gl);
		}
	}
	
	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}


	public float distance(float tx, float tz) {
		return (float) Math.sqrt(Math.pow(x-(tx/16),2) + Math.pow(z-(tz/16),2));
	}
}
