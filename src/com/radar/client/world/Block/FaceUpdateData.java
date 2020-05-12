package com.radar.client.world.Block;

import com.radar.client.world.Coord;

public class FaceUpdateData {
	private byte action;
	private Integer faceID;
	private float[][] verts;
	private float[][] norms;
	private Coord<Integer> cubePos;
	
	public FaceUpdateData(byte action, int faceID, float[][] verts, float[][] norms, Coord<Integer> cubePos) {
		this.action = action;
		this.faceID = faceID;
		this.verts = verts;
		this.norms = norms;
		this.cubePos = cubePos;
	}

	public FaceUpdateData(byte action, int faceID, Coord<Integer> cubePos) {
		this.action = action;
		this.faceID = faceID;
		this.cubePos = cubePos;
	}
	
	public byte getAction() {
		return action;
	}

	public Integer getFaceID() {
		return faceID;
	}

	public float[][] getVerts() {
		return verts;
	}

	public float[][] getNorms() {
		return norms;
	}
	
	public Coord<Integer> getCubePos() {
		return cubePos;
	}
}
