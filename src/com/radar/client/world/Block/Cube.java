package com.radar.client.world.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.radar.client.world.Coord;
import com.radar.client.world.TextureMap;
import com.radar.client.world.WorldGen;

/**
 * @author radar
 * Class to represent a cube.
 */
public abstract class Cube {
	
	/**
	 * Verticies of the cube
	 */
	private float[][] verts = new float[][] {
		{0.5f, 0.5f, 0.5f, 1, 1}, {0.5f, -0.5f, 0.5f, 1, 0}, {-0.5f, -0.5f, 0.5f, 0, 0}, {-0.5f, 0.5f, 0.5f, 0, 1},
		{0.5f, 0.5f, -0.5f, 1, 1}, {0.5f, -0.5f, -0.5f, 1, 0}, {-0.5f, -0.5f, -0.5f, 0, 0}, {-0.5f, 0.5f, -0.5f, 0, 1},
		{0.5f, 0.5f, 0.5f, 1, 1}, {0.5f, -0.5f, 0.5f, 1, 0}, {0.5f, -0.5f, -0.5f, 0, 0}, {0.5f, 0.5f, -0.5f, 0, 1},
		{-0.5f, 0.5f, 0.5f, 1, 1}, {-0.5f, -0.5f, 0.5f, 1, 0}, {-0.5f, -0.5f, -0.5f, 0, 0}, {-0.5f, 0.5f, -0.5f, 0, 1},
		{0.5f, 0.5f, 0.5f, 0, 0}, {-0.5f, 0.5f, 0.5f, 0, 1}, {-0.5f, 0.5f, -0.5f, 1, 1}, {0.5f, 0.5f, -0.5f, 1, 0},
		{0.5f, -0.5f, 0.5f, 0, 0}, {-0.5f, -0.5f, 0.5f, 0, 1}, {-0.5f, -0.5f, -0.5f, 1, 1}, {0.5f, -0.5f, -0.5f, 1, 0}
	};
	
	/**
	 * Used to store which the ID of each face in the chunks array buffer
	 */
	private Integer[] faceIDs = new Integer[6];
	
	//TODO Calculate face normals
	private final static float[][] normals = new float[][] {
		{0.0f, 0.0f, 1.0f}, {0.0f, 0.0f, 1.0f}, {0.0f, 0.0f, 1.0f}, {0.0f, 0.0f, 1.0f}, 
		{0.0f, 0.0f, -1.0f}, {0.0f, 0.0f, -1.0f}, {0.0f, 0.0f, -1.0f}, {0.0f, 0.0f, -1.0f}, 
		{1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, 
		{-1.0f, 0.0f, 0.0f}, {-1.0f, 0.0f, 0.0f}, {-1.0f, 0.0f, 0.0f}, {-1.0f, 0.0f, 0.0f}, 
		{0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, //Up face
		{0.0f, -1.0f, 0.0f}, {0.0f, -1.0f, 0.0f}, {0.0f, -1.0f, 0.0f}, {0.0f, -1.0f, 0.0f}
	};
	
	/**
	 * Indicies of textures for each face, current is default
	 */
	private short[] faceTextures = new short[] {1, 1, 1, 1, 3, 2};
	
	/**
	 * Holds which faces are visible to the player or not used to cull faces between cubes
	 */
	private boolean[] visibleFaces = new boolean[] {true, true, true, true, true, true};
	
	/**
	 * Holds the block IDs of all the blocks that are transparent, used for face culling purposes
	 */
	public final static HashSet<Short> transparentBlockIDs = new HashSet<Short>(Arrays.asList((short)0, (short)6));
	
	/**
	 * Holds the number of faces visible on this cube
	 */
	private byte numVisibleFaces;
	
	/**
	 * Holds x,y,z coordinates of the cube
	 */
	protected Coord<Integer> coords;
	
	/**
	 * Used to get the chunks when doing face culling
	 */
	protected WorldGen gen;
	
	/**
	 * Constructor to create a cube
	 * @param x X Position of the cube
	 * @param y Y Position of the cube
	 * @param z Z Position of the cube
	 */
	public Cube(int x, int y, int z, short[] faceTextures, WorldGen gen) {
		coords = new Coord<Integer>(x,y,z);
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
	
	public void setVerticies(float[][] verts) {
		this.verts = verts;
	}
	
	/**
	 * Function to get the number of visible faces of this cube
	 * @return The number of visible faces on this cube
	 */
	public byte getNumVisibleFaces() {
		return numVisibleFaces;
	}
	
	/**
	 * Gets all the verticies of the visible faces on this cube
	 * @return An array containing all the points for all visible faces on this cube
	 */
	public float[][] getFaceVerts(int startingFaceID){
		float[][] faceVerts = new float[numVisibleFaces * 4][5];
		
		byte visibleFace = 0;
		for (int i = 0; i < 6; i++) {
			if (visibleFaces[i]) {
				faceIDs[i] = startingFaceID;
				startingFaceID++;
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
		byte visibleFace = 0;
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
	
	public void renderUpdate() {
		boolean[] previousVisibleFaces = visibleFaces.clone();
		byte[] out = new byte[6];
		adjacentFaceCull();
		//TODO Return whether faces are removed, added, changed, or remained the same
		for (int i = 0; i < 6; i++) {
			if (previousVisibleFaces[i] != visibleFaces[i]) {
				if (visibleFaces[i] && !previousVisibleFaces[i]) {
					
				}
			}else {
				out[i] = 0;
			}
		}
	}
	
	/**
	 * Used to get rid of faces between cubes that won't be seen
	 */
	private void adjacentFaceCull() {
		int chunkX, chunkZ;
		
		chunkX = (int) Math.floor(coords.getX()/16.0);
		chunkZ = (int) Math.floor(coords.getZ()/16.0);
		
		
		ArrayList<ArrayList<ArrayList<Short>>> currentChunk = gen.getChunk(chunkX, chunkZ);
		ArrayList<ArrayList<ArrayList<Short>>> adjacentChunk;
		
		//Finding this cubes position relative to the corner of the chunk it is in
		byte relX, relZ;
		if (coords.getX() < 0) {
			relX = (byte) (15 - (Math.abs(coords.getX()+1) % 16));
		}else {
			relX = (byte) (Math.abs(coords.getX()) % 16);
		}
		if (coords.getZ() < 0) {
			relZ = (byte) (15 - (Math.abs(coords.getZ()+1) % 16));
		}else {
			relZ = (byte) (Math.abs(coords.getZ()) % 16);
		}
		
		if (relX-1 >= 0) {
			if (currentChunk.get(relX-1).get(relZ).size() > coords.getY()) {
//				if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) != 0) {
				if (!transparentBlockIDs.contains(currentChunk.get(relX-1).get(relZ).get(coords.getY()))) {
					visibleFaces[3] = false;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX-1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(15).get(relZ).size() > coords.getY()) {
					if (!transparentBlockIDs.contains(adjacentChunk.get(15).get(relZ).get(coords.getY()))) {
						visibleFaces[3] = false;
					}
				}
			}
		}
		if (relZ-1 >= 0) {
			if (currentChunk.get(relX).get(relZ-1).size() > coords.getY()) {
				if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ-1).get(coords.getY()))) {
					visibleFaces[1] = false;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ-1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(15).size() > coords.getY()) {
					if (!transparentBlockIDs.contains(adjacentChunk.get(relX).get(15).get(coords.getY()))) {
						visibleFaces[1] = false;
					}
				}
			}
		}
		if (relX+1 < 16) {
			if (currentChunk.get(relX+1).get(relZ).size() > coords.getY()) {
				if (!transparentBlockIDs.contains(currentChunk.get(relX+1).get(relZ).get(coords.getY()))) {
					visibleFaces[2] = false;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX+1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(0).get(relZ).size() > coords.getY()) {
					if (!transparentBlockIDs.contains(adjacentChunk.get(0).get(relZ).get(coords.getY()))) {
						visibleFaces[2] = false;
					}
				}
			}
		}
		if (relZ+1 < 16) {
			if (currentChunk.get(relX).get(relZ+1).size() > coords.getY()) {
				if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ+1).get(coords.getY()))) {
					visibleFaces[0] = false;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ+1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(0).size() > coords.getY()) {
					if (!transparentBlockIDs.contains(adjacentChunk.get(relX).get(0).get(coords.getY()))) {
						visibleFaces[0] = false;
					}
				}
			}
		}
		if (coords.getY()-1 >= 0) {
			if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ).get(coords.getY()-1))) {
				visibleFaces[5] = false;
			}
		}
		if (coords.getY()+1 < currentChunk.get(relX).get(relZ).size()) {
			if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ).get(coords.getY()+1))) {
				visibleFaces[4] = false;
			}
		}
	}
}