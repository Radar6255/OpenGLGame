package com.radar.common;

import com.radar.client.world.Coord2D;

public class PointConversion {
	public static Coord2D<Integer> absoluteToRelative(Coord2D<Integer> in){
		int relX;
		int relZ;
		
		if (in.getX() < 0) {
			relX = (int) (15 - (Math.abs(1+in.getX()) % 16));
		}else {
			relX = (int) in.getX() % 16;
		}
		if (in.getZ() < 0) {
			relZ = (int) (15 - (Math.abs(1+in.getZ()) % 16));
		}else {
			relZ = (int) in.getZ() % 16;
		}
		
		return new Coord2D<>(relX, relZ);
	}
}
