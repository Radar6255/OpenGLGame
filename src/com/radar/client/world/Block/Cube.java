package com.radar.client.world.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import com.radar.client.world.Coord;
import com.radar.client.world.Dimension;
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
	protected float[][] verts = new float[][] {
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
	
	protected final static float[][] normals = new float[][] {
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
	protected boolean[] visibleFaces = new boolean[] {true, true, true, true, true, true};
	
	/**
	 * Holds the block IDs of all the blocks that are transparent, used for face culling purposes
	 */
	public final static HashSet<Short> transparentBlockIDs = new HashSet<Short>(Arrays.asList((short)0, (short)6));
	
	/**
	 * Holds the number of faces visible on this cube
	 */
	protected byte numVisibleFaces;
	
	/**
	 * Holds x,y,z coordinates of the cube
	 */
	protected Coord<Integer> coords;
	
	/**
	 * Used to get the chunks when doing face culling
	 */
	protected WorldGen gen;
	
	protected Dimension dim;
	
	/**
	 * Constructor to create a cube
	 * @param x X Position of the cube
	 * @param y Y Position of the cube
	 * @param z Z Position of the cube
	 */
	public Cube(int x, int y, int z, short[] faceTextures, WorldGen gen, Dimension dim) {
		coords = new Coord<Integer>(x,y,z);
		this.faceTextures = faceTextures;
		this.gen = gen;
		this.dim = dim;
		
		adjacentFaceCull();
		
		numVisibleFaces = 0;
		for (int i = 0; i < 6; i++) {
			if (visibleFaces[i]) {
				numVisibleFaces++;
			}
		}
		for (int i = 0; i < 6; i++) {
			faceIDs[i] = -i - 1;
		}
	}
	
	/**
	 * Constructor to create a cube
	 * @param x X Position of the cube
	 * @param y Y Position of the cube
	 * @param z Z Position of the cube
	 */
	public Cube(int x, int y, int z, short[] faceTextures, WorldGen gen, Dimension dim, boolean doFaceCull) {
		coords = new Coord<Integer>(x,y,z);
		this.faceTextures = faceTextures;
		this.gen = gen;
		this.dim = dim;
		
		if(doFaceCull) {
			adjacentFaceCull();
		
			numVisibleFaces = 0;
			for (int i = 0; i < 6; i++) {
				if (visibleFaces[i]) {
					numVisibleFaces++;
				}
			}
		}
		for (int i = 0; i < 6; i++) {
			faceIDs[i] = -i - 1;
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
	
	public void facesNotVisible() {
		visibleFaces = new boolean[] {false, false, false, false, false, false};
		numVisibleFaces = 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Cube) {
			Cube cube = (Cube) o;
			return cube.coords.equals(this.coords);
		}
		return false;
	}
	
	//TODO Make so that renderUpdate also changes numVisibleFaces
	/**
	 * @return 
	 */
	public LinkedList<FaceUpdateData> renderUpdate() {
		boolean[] previousVisibleFaces = visibleFaces.clone();
		for (int i = 0; i < 6; i++) {
			visibleFaces[i] = true;
		}
		
		adjacentFaceCull();
		byte action;
		int faceID;
		LinkedList<FaceUpdateData> out = new LinkedList<>();
		for (int i = 0; i < 6; i++) {
			if (previousVisibleFaces[i] != visibleFaces[i]) {
				faceID = faceIDs[i];
				if (visibleFaces[i]) {
					action = 1;
					float[][] verts = getFaceVert(faceID);
					float[][] norms = getFaceNorm(faceID);
					out.add(new FaceUpdateData(action, faceID, verts, norms, coords));
				}else {
					action = -1;
					out.add(new FaceUpdateData(action, faceID, coords));
				}
			}
		}
//		facesNotVisible();
		return out;
	}
	
	public float[][] getFaceNorm(int faceID) {
		float[][] out = new float[4][5];
		for (int i = 0; i < 6; i++) {
			if (faceIDs[i] == faceID) {
				for (int v = 0; v < 4; v++) {
					out[v] = new float[] {normals[(i*4) + v][0], normals[(i*4) + v][1], normals[(i*4) + v][2]};
				}
			}
		}
		return out;
	}
	
	/**
	 * @param faceID The faceID to get the verticies of
	 * @return The verticies of the specified face
	 */
	public float[][] getFaceVert(int faceID) {
		float[][] out = new float[4][];
		for (int i = 0; i < 6; i++) {
			if (faceIDs[i] == faceID) {
				float[] temp = TextureMap.getTexCoords(faceTextures[i]);
				for (int v = 0; v < 4; v++) {
					out[v] = new float[] {verts[(i*4) + v][0] + coords.getX(), verts[(i*4) + v][1] + coords.getY(), verts[(i*4) + v][2] + coords.getZ(), //X, Y, Z position of face
							(verts[(i*4)+v][3]*temp[2]) + temp[0], (verts[(i*4)+v][4]*temp[2]) + temp[1]};
				}
				break;
			}
		}
		return out;
	}
	
	public void replaceId(int oldID, int newID) {
		for (int i = 0; i < 6; i++) {
			if(faceIDs[i] == oldID) {
				faceIDs[i] = newID;
//				if(newID > 0) {
//					visibleFaces[i] = true;
//					numVisibleFaces++;
//				}
				return;
			}
		}System.out.println( "Unable to replace ID:"+oldID+" with "+newID );
	}
	
	public void replaceId(int oldID) {
		for (int i = 0; i < 6; i++) {
			if(faceIDs[i] == oldID) {
				faceIDs[i] = -i - 1;
//				numVisibleFaces--;
				return;
			}
		}
	}
	
	public Coord<Integer> getPosition(){
		return coords;
	}
	
	/**
	 * Used to get rid of faces between cubes that won't be seen
	 */
	protected void adjacentFaceCull() {
		numVisibleFaces = 6;
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
				if (!transparentBlockIDs.contains(currentChunk.get(relX-1).get(relZ).get(coords.getY()))) {
					visibleFaces[3] = false;
					numVisibleFaces--;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX-1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(15).get(relZ).size() > coords.getY()) {
					if (!transparentBlockIDs.contains(adjacentChunk.get(15).get(relZ).get(coords.getY()))) {
						visibleFaces[3] = false;
						numVisibleFaces--;
					}
				}
			}
		}
		if (relZ-1 >= 0) {
			if (currentChunk.get(relX).get(relZ-1).size() > coords.getY()) {
				if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ-1).get(coords.getY()))) {
					visibleFaces[1] = false;
					numVisibleFaces--;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ-1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(15).size() > coords.getY()) {
					if (!transparentBlockIDs.contains(adjacentChunk.get(relX).get(15).get(coords.getY()))) {
						visibleFaces[1] = false;
						numVisibleFaces--;
					}
				}
			}
		}
		if (relX+1 < 16) {
			if (currentChunk.get(relX+1).get(relZ).size() > coords.getY()) {
				if (!transparentBlockIDs.contains(currentChunk.get(relX+1).get(relZ).get(coords.getY()))) {
					visibleFaces[2] = false;
					numVisibleFaces--;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX+1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(0).get(relZ).size() > coords.getY()) {
					if (!transparentBlockIDs.contains(adjacentChunk.get(0).get(relZ).get(coords.getY()))) {
						visibleFaces[2] = false;
						numVisibleFaces--;
					}
				}
			}
		}
		if (relZ+1 < 16) {
			if (currentChunk.get(relX).get(relZ+1).size() > coords.getY()) {
				if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ+1).get(coords.getY()))) {
					visibleFaces[0] = false;
					numVisibleFaces--;
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ+1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(0).size() > coords.getY()) {
					if (!transparentBlockIDs.contains(adjacentChunk.get(relX).get(0).get(coords.getY()))) {
						visibleFaces[0] = false;
						numVisibleFaces--;
					}
				}
			}
		}
		if (coords.getY()-1 >= 0) {
			if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ).get(coords.getY()-1))) {
				visibleFaces[5] = false;
				numVisibleFaces--;
			}
		}
		if (coords.getY()+1 < currentChunk.get(relX).get(relZ).size()) {
			if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ).get(coords.getY()+1))) {
				visibleFaces[4] = false;
				numVisibleFaces--;
			}
		}
	}
	
	public int[] remove() {
		int[] out = new int[numVisibleFaces];
		int temp = 0;
		for (int i = 0; i < 6; i++) {
			if(visibleFaces[i]) {
				out[temp] = faceIDs[i];
				temp++;
			}
		}
		return out;
	}
}
