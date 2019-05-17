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
	private LinkedList<Cube> cubes;
	
	/**
	 * List of the cubes with at least one visible face
	 */
	private LinkedList<Cube> visibleCubes;
	
	/**
	 * Used to do some buffer creation on this chunks first render call
	 */
	private boolean first = true;
	
	/**
	 * The x, z position of the chunk in the world
	 */
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
		visibleCubes = new LinkedList<>();
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
		//On the first render run it checks which cubes are actually visible
		//It then stores the visible cubes and only renders those
		if (first) {
			for (Cube cube: cubes) {
				cube.render(gl);
				if (cube.isVisible()) {
					visibleCubes.add(cube);
				}
			}
			first = false;
		}else {
			for (Cube cube: visibleCubes) {
				cube.render(gl);
			}
		}
	}
	
	/**
	 * @return This chunks x position relative to other chunks
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * @return This chunks z position relative to other chunks
	 */
	public int getZ() {
		return z;
	}
	
	/**
	 * Deletes all of the cubes buffers
	 * @param gl A new instance of openGL used to delete buffers
	 */
	public void delete(GL2 gl) {
		for (Cube cube: visibleCubes) {
			cube.removeBuffers(gl);
		}
	}


	/**
	 * @param tx The x position to get the distance to
	 * @param tz The z position to get the distance to
	 * @return The distance of the corner of this chunk to the point specified
	 */
	public float distance(float tx, float tz) {
		return (float) Math.sqrt(Math.pow(x-(tx/16),2) + Math.pow(z-(tz/16),2));
	}
}
