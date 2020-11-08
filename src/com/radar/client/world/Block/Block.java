package com.radar.client.world.Block;

import com.radar.client.world.Dimension;
import com.radar.client.world.WorldGen;

public class Block extends Cube {
	
	public Block(int x, int y, int z, short[] faceTextures, WorldGen gen, Dimension dim) {
		super(x, y, z, faceTextures, gen, dim);
	}
}
