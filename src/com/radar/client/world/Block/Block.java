package com.radar.client.world.Block;

import com.radar.client.world.generation.Generation;

public class Block extends Cube {
	
	public Block(int x, int y, int z, short[] faceTextures, Generation gen) {
		super(x, y, z, faceTextures, gen);
	}
}
