package com.radar.client.world;

/**
 * @author radar
 * Class to represent a 3D Coordinate
 */
public class Coord2D<E> {
	private E x, z;
	/**
	 * Constructor to save point positions
	 * @param x The x coordinate of the point
	 * @param z The z coordinate of the point
	 */
	public Coord2D (E x, E z) {
		this.x = x;
		this.z = z;
	}
	public E getX() {
		return x;
	}
	public void setX(E x) {
		this.x = x;
	}
	public E getZ() {
		return z;
	}
	public void setZ(E z) {
		this.z = z;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((z == null) ? 0 : z.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coord2D<Integer> other = (Coord2D<Integer>) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (z == null) {
			if (other.z != null)
				return false;
		} else if (!z.equals(other.z))
			return false;
		return true;
	}
	
	
}
