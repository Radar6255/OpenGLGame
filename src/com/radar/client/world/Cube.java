package com.radar.client.world;

/**
 * @author radar
 * Class to represent a cube.
 */
public class Cube {
	/**
	 * Constructor to set up OpenGL buffers
	 * @param x X Position of the cube
	 * @param y Y Position of the cube
	 * @param z Z Position of the cube
	 * @param w Width of the cube
	 * @param h Height of the cube
	 * @param d Depth of the cube
	 */
	private Coord coords;
	private int w, h, d;
	public Cube(int x, int y, int z, int w, int h, int d) {
		coords = new Coord(x,y,z);
		this.w = w;
		this.h = h;
		this.d = d;
	}
	
	public void initBuffers() {
		
	}
}
