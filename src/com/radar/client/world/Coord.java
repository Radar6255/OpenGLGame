package com.radar.client.world;

/**
 * @author radar
 * Class to represent a 3D Coordinate
 */
public class Coord {
	private int x, y, z;
	/**
	 * Constructor to save point positions
	 * @param x The x coordinate of the point
	 * @param y The y coordinate of the point
	 * @param z The z coordinate of the point
	 */
	public Coord (int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getZ() {
		return z;
	}
	public void setZ(int z) {
		this.z = z;
	}
}
