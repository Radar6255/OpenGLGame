package com.radar.client.world;

/**
 * @author radar
 * Class to represent a 3D Coordinate
 */
public class Coord<E> {
	private E x, y, z;
	/**
	 * Constructor to save point positions
	 * @param x The x coordinate of the point
	 * @param y The y coordinate of the point
	 * @param z The z coordinate of the point
	 */
	public Coord (E x, E y, E z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public E getX() {
		return x;
	}
	public void setX(E x) {
		this.x = x;
	}
	public E getY() {
		return y;
	}
	public void setY(E y) {
		this.y = y;
	}
	public E getZ() {
		return z;
	}
	public void setZ(E z) {
		this.z = z;
	}
}
