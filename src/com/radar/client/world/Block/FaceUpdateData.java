package com.radar.client.world.Block;

public class FaceUpdateData {
	private byte action;
	private Integer faceID;
	private float[][] verts;
	private float[][] norms;
	
	public FaceUpdateData(byte action, int faceID, float[][] verts, float[][] norms) {
		this.action = action;
		this.faceID = faceID;
		this.verts = verts;
		this.norms = norms;
	}

	public FaceUpdateData(byte action, int faceID) {
		this.action = action;
		this.faceID = faceID;
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
	
	
}
