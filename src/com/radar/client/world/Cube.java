package com.radar.client.world;

import java.util.ArrayList;

/**
 * @author radar
 * Class to represent a cube.
 */
public class Cube {
	/**
	 * Verticies of the cube
	 */
	private final static float[][] verts = new float[][] {
		{0.5f, 0.5f, 0.5f, 1, 1}, {0.5f, -0.5f, 0.5f, 1, 0}, {-0.5f, -0.5f, 0.5f, 0, 0}, {-0.5f, 0.5f, 0.5f, 0, 1},
		{0.5f, 0.5f, -0.5f, 1, 1}, {0.5f, -0.5f, -0.5f, 1, 0}, {-0.5f, -0.5f, -0.5f, 0, 0}, {-0.5f, 0.5f, -0.5f, 0, 1},
		{0.5f, 0.5f, 0.5f, 1, 1}, {0.5f, -0.5f, 0.5f, 1, 0}, {0.5f, -0.5f, -0.5f, 0, 0}, {0.5f, 0.5f, -0.5f, 0, 1},
		{-0.5f, 0.5f, 0.5f, 1, 1}, {-0.5f, -0.5f, 0.5f, 1, 0}, {-0.5f, -0.5f, -0.5f, 0, 0}, {-0.5f, 0.5f, -0.5f, 0, 1},
		{0.5f, 0.5f, 0.5f, 0, 0}, {-0.5f, 0.5f, 0.5f, 0, 1}, {-0.5f, 0.5f, -0.5f, 1, 1}, {0.5f, 0.5f, -0.5f, 1, 0},
		{0.5f, -0.5f, 0.5f, 0, 0}, {-0.5f, -0.5f, 0.5f, 0, 1}, {-0.5f, -0.5f, -0.5f, 1, 1}, {0.5f, -0.5f, -0.5f, 1, 0}
	};
	
	//TODO Calculate face normals
	private final static float[][] normals = new float[][] {
		{0.0f, 0.0f, 1.0f}, {0.0f, -0.0f, 1.0f}, {0.0f, 0.0f, 1.0f}, {-0.0f, 0.0f, 1.0f}, 
		{0.0f, 0.0f, -1.0f}, {0.0f, -0.0f, -1.0f}, {0.0f, 0.0f, -1.0f}, {-0.0f, 0.0f, -1.0f}, 
		{1.0f, -0.0f, -0.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, 
		{-1.0f, -0.0f, -0.0f}, {-1.0f, 0.0f, 0.0f}, {-1.0f, 0.0f, 0.0f}, {-1.0f, 0.0f, 0.0f}, 
		{0.0f, 1.0f, 0.0f}, {-0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, -0.0f}, //Up face
		{0.0f, -1.0f, 0.0f}, {-0.0f, -1.0f, 0.0f}, {0.0f, -1.0f, 0.0f}, {0.0f, -1.0f, -0.0f}
	};
	
	/**
	 * Indicies of textures for each face, current is default
	 */
	private int[] faceTextures = new int[] {1, 1, 1, 1, 3, 2};
	
	/**
	 * Holds which faces are visible to the player or not used to cull faces between cubes
	 */
	private boolean[] visibleFaces = new boolean[] {true, true, true, true, true, true};
	
	/**
	 * Holds the number of faces visible on this cube
	 */
	private int numVisibleFaces;
	
	/**
	 * Holds x,y,z coordinates of the cube
	 */
	private Coord<Integer> coords;
	
	/**
	 * Holds width, height, and depth of the cube repsepecively
	 */
	private int w, h, d;
	
	/**
	 * Used to get the chunks when doing face culling
	 */
	private WorldGen gen;
	
	/**
	 * Constructor to create a cube
	 * @param x X Position of the cube
	 * @param y Y Position of the cube
	 * @param z Z Position of the cube
	 * @param w Width of the cube
	 * @param h Height of the cube
	 * @param d Depth of the cube
	 */
	public Cube(int x, int y, int z, int w, int h, int d, int[] faceTextures, WorldGen gen) {
		coords = new Coord<Integer>(x,y,z);
		this.w = w;
		this.h = h;
		this.d = d;
		this.faceTextures = faceTextures;
		this.gen = gen;
		
		adjacentFaceCull();
		
		numVisibleFaces = 0;
		for (int i = 0; i < 6; i++) {
			if (visibleFaces[i]) {
				numVisibleFaces++;
			}
		}
	}
	
	/**
	 * Function to get the number of visible faces of this cube
	 * @return The number of visible faces on this cube
	 */
	public int getNumVisibleFaces() {
		return numVisibleFaces;
	}
	
	/**
	 * Gets all the verticies of the visible faces on this cube
	 * @return An array containing all the points for all visible faces on this cube
	 */
	public float[][] getFaceVerts(){
		float[][] faceVerts = new float[numVisibleFaces * 4][5];
		
		int visibleFace = 0;
		for (int i = 0; i < 6; i++) {
			if (visibleFaces[i]) {
				float[] temp = TextureMap.getTexCoords(faceTextures[i]);
				for (int v = 0; v < 4; v++) {
					faceVerts[(visibleFace*4)+v] = new float[] {verts[(i*4) + v][0] + coords.getX(),verts[(i*4) + v][1] + coords.getY(),verts[(i*4) + v][2] + coords.getZ(), //X, Y, Z position of face
							(verts[(i*4)+v][3]*temp[2]) + temp[0], (verts[(i*4)+v][4]*temp[2]) + temp[1]}; //X, Y position of texture
//							verts[(i*4)+v][3], verts[(i*4)+v][4]};
				}visibleFace++;
			}
		}
		return faceVerts;
	}
	
	public float[][] getNormals() {
		float[][] pointNormals = new float[numVisibleFaces*4][3];
		int visibleFace = 0;
		for (int i = 0; i < 6; i++) {
			if (visibleFaces[i]) {
				for (int v = 0; v < 4; v++) {
					pointNormals[(visibleFace*4)+v] = new float[] {normals[(i*4) + v][0], normals[(i*4) + v][1], normals[(i*4) + v][2]};
				}visibleFace++;
			}
		}
		return pointNormals;
	}
	
	/**
	 * Finds if this cube has at least one visible face
	 * @return True if this cube is visible, false if not
	 */
	public boolean isVisible() {
		for (boolean visible: visibleFaces) {
			if (visible) {
				return true;
			}
		}return false;
	}
	
	/**
	 * Used to get rid of faces between cubes that won't be seen
	 */
	private void adjacentFaceCull() {
		int chunkX, chunkZ;
		
		chunkX = (int) Math.floor(coords.getX()/16.0);
		chunkZ = (int) Math.floor(coords.getZ()/16.0);
		
		
		ArrayList<ArrayList<ArrayList<Integer>>> currentChunk = gen.getChunk(chunkX, chunkZ);
		ArrayList<ArrayList<ArrayList<Integer>>> adjacentChunk;
		
		//Finding this cubes position relative to the corner of the chunk it is in
		int relX, relZ;
		if (coords.getX() < 0) {
			relX = 15 - (Math.abs(coords.getX()+1) % 16);
		}else {
			relX = Math.abs(coords.getX()) % 16;
		}
		if (coords.getZ() < 0) {
			relZ = 15 - (Math.abs(coords.getZ()+1) % 16);
		}else {
			relZ = Math.abs(coords.getZ()) % 16;
		}
		
		if (relX-1 >= 0) {
			if (currentChunk.get(relX-1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) != 0) {
					visibleFaces[3] = false;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX-1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(15).get(relZ).size() > coords.getY()) {
					if (adjacentChunk.get(15).get(relZ).get(coords.getY()) != 0) {
						visibleFaces[3] = false;
					}
				}
			}
		}
		if (relZ-1 >= 0) {
			if (currentChunk.get(relX).get(relZ-1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ-1).get(coords.getY()) != 0) {
					visibleFaces[1] = false;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ-1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(15).size() > coords.getY()) {
					if (adjacentChunk.get(relX).get(15).get(coords.getY()) != 0) {
						visibleFaces[1] = false;
					}
				}
			}
		}
		if (relX+1 < 16) {
			if (currentChunk.get(relX+1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX+1).get(relZ).get(coords.getY()) != 0) {
					visibleFaces[2] = false;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX+1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(0).get(relZ).size() > coords.getY()) {
					if (adjacentChunk.get(0).get(relZ).get(coords.getY()) != 0) {
						visibleFaces[2] = false;
					}
				}
			}
		}
		if (relZ+1 < 16) {
			if (currentChunk.get(relX).get(relZ+1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ+1).get(coords.getY()) != 0) {
					visibleFaces[0] = false;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ+1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(0).size() > coords.getY()) {
					if (adjacentChunk.get(relX).get(0).get(coords.getY()) != 0) {
						visibleFaces[0] = false;
					}
				}
			}
		}
		if (coords.getY()-1 >= 0) {
			if (currentChunk.get(relX).get(relZ).get(coords.getY()-1) != 0) {
				visibleFaces[5] = false;
			}
		}
		if (coords.getY()+1 < currentChunk.get(relX).get(relZ).size()) {
			if (currentChunk.get(relX).get(relZ).get(coords.getY()+1) != 0) {
				visibleFaces[4] = false;
			}
		}
	}
}
