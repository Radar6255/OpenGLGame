package com.radar.client.world;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Block.Block;
import com.radar.client.world.Block.Cube;
import com.radar.client.world.Block.Fluid;
import com.radar.client.world.Block.Updateable;

/**
 * @author radar
 * A class to represent a chunk of cubes,
 * used to make generating, loading, and unloading the world easier.
 */
public class Chunk {
	/**
	 * List of all cubes in this chunk
	 */
//	private LinkedList<Cube> cubes;
	private HashMap<Coord<Integer>, Cube> cubes;
	
	/**
	 * TODO Possibly remove, uses data that is only used like once, or delete not sure
	 * List of the cubes with at least one visible face
	 */
	private LinkedList<Cube> visibleCubes;
	
	
	private LinkedList<Updateable> blockUpdates;
	
	
	public HashSet<Coord<Integer>> blocksToUpdate;
	
	/**
	 * Used to do some buffer creation on this chunks first render call
	 */
	private boolean first = true;
	
	/**
	 * Used to regenerate the chunks faces if there was a block change in this chunk or an adjacent chunk
	 */
	private boolean update = false;
	
	/**
	 * The x, z position of the chunk in the world
	 */
	private int x, z;
	
	/**
	 * Array to hold all the data for all the faces in this chunk
	 */
	private float[] faceVerts;
	
	/**
	 * Array to hold all the data for all the face normals in this chunk
	 */
	private float[] normals;
	
	/**
	 * Holds the handle to the verticies for all the cubes in this chunk
	 */
	private int[] vertexHandle = new int[1];
	
	/**
	 * Holds the handle to the normals for all the verticies in this chunk
	 */
	private int[] normalHandle = new int[1];
	
	/**
	 * Holds the number of faces visible in this chunk
	 */
	private int numFaces = 0;
	
	ArrayList<Integer> availableSpace;
	
	/**
	 * Indicies of textures for each face
	 */
	private static short[][] faceTextures = new short[][] {{1, 1, 1, 1, 3, 2}, {4, 4, 4, 4, 4, 4}, {2, 2, 2, 2, 2, 2}, {5, 5, 5, 5, 5, 5}, {6, 6, 6, 6, 7, 7}, {11, 11, 11, 11, 11, 11}};
	
	/**
	 * Creates a chunk at the specified x, z
	 * @param x The x chunk position of this chunk
	 * @param z The z chunk position of this chunk
	 */
	public Chunk(int x, int z, WorldGen gen) {
		this.x = x;
		this.z = z;
		
		blockUpdates = new LinkedList<>();
		blocksToUpdate = new HashSet<>();
		availableSpace = new ArrayList<>();
		
		load(gen);
	}
	
	public void update(WorldGen gen, WindowUpdates window) {
		boolean ran = false;
		LinkedList<Updateable> tempUpdates = (LinkedList<Updateable>) blockUpdates.clone();
		for (Updateable cube: tempUpdates) {
			cube.update(window);
			ran = true;
		}
		blockUpdates.clear();
		//Clears old blocks and replaces them with new ones
		//Allows blocks to be removed/added visually
		if (ran) {
			load(gen);
			update = true;
		}
		
	}

	/**
	 * Called by the worldGen thread, used to do any intense processes,
	 * before first render call, also called on block updates
	 */
	public void load(WorldGen gen) {
		long start = System.currentTimeMillis();
		//TODO Find a more precise way of finding number of blocks in a chunk or get a better guess
		cubes = new HashMap<>(100);
		visibleCubes = new LinkedList<>();
		
		ArrayList<ArrayList<ArrayList<Short>>> chunk = gen.getChunk(x, z);
		
		for (int tx = 0; tx < 16; tx++) {
			for (int tz = 0; tz < 16; tz++) {
				for (int ty = 0; ty < chunk.get(tx).get(tz).size(); ty++) {
					if (chunk.get(tx).get(tz).get(ty) != 0) {
						Coord<Integer> currentPos = new Coord<Integer>(x*16 + tx, ty, z*16 + tz);
						if (chunk.get(tx).get(tz).get(ty) != 6) {
							addCube(currentPos, new Block(x*16 + tx, ty, z*16 + tz, faceTextures[chunk.get(tx).get(tz).get(ty)-1], gen));
						}else {
							Fluid temp;
							
							try {
								temp = new Fluid(x*16 + tx, ty, z*16 + tz, faceTextures[chunk.get(tx).get(tz).get(ty)-1], gen.liquids.get(new Coord<Integer>(x*16 + tx, ty, z*16 + tz)), gen);
							}catch(Exception e){
								temp = new Fluid(x*16 + tx, ty, z*16 + tz, faceTextures[chunk.get(tx).get(tz).get(ty)-1], 1, gen);
							}
							
//							for (Coord<Integer> test: blocksToUpdate) {
//								System.out.println(test.toString());
//								System.out.println(test.equals(new Coord<Integer>(x*16 + tx, ty, z*16 + tz)));
//							}
							addCube(currentPos, temp);
							if (blocksToUpdate.contains(new Coord<Integer>(x*16 + tx, ty, z*16 + tz))) {
								blockUpdates.add(temp);
							}
						}
					}
				}
			}
		}
		blocksToUpdate.clear();
		generateBufferArray();
		update = true;
		System.out.println("Load Chunk "+x+" "+z+" "+(System.currentTimeMillis()-start));
	}
	
	public void load(int x, int y, int z, WorldGen gen) {
		ArrayList<ArrayList<ArrayList<Short>>> chunk = gen.getChunk(x, z);
		if (chunk.get(x).get(z).get(y) == 0) {
			cubes.remove(new Coord<Integer>(x,y,z));
		}else {
			if (chunk.get(x).get(z).get(y) != 6) {
				cubes.put(new Coord<Integer>(x,y,z), new Block(x,y,z, faceTextures[chunk.get(x).get(z).get(y)-1], gen));
			}else {
				Fluid temp;
				try {
					temp = new Fluid(x, y, z, faceTextures[chunk.get(x).get(z).get(y)-1], gen.liquids.get(new Coord<Integer>(x, y, z)), gen);
				}catch(Exception e){
					temp = new Fluid(x, y, z, faceTextures[chunk.get(x).get(z).get(y)-1], 1, gen);
				}
				addCube(new Coord<Integer>(x,y,z), temp);
			}
		}
	}
	
	/**
	 * A function to add a cube to this chunk
	 * @param cube The cube to add to the chunk
	 */
	public void addCube(Coord<Integer> pos,Cube cube) {
		cubes.put(pos, cube);
	}
	
	/**
	 * Function to draw all the cubes in this chunk
	 * @param gl The GL2 reference for drawing the cuebs
	 */
	public void render(GL2 gl) {
		//On the first render run it checks which cubes are actually visible
		//It then stores the visible cubes and gets the number of faces that are visible
		//This is to create the right size buffer
		if (first) {
			//Causes a little lag somethings may be able to be moved around
			initBuffers(gl);
			first = false;
			update = false;
		}else {
			if (update) {
				delete(gl);
				initBuffers(gl);
				update = false;
			}
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 5*Buffers.SIZEOF_FLOAT, 3*Buffers.SIZEOF_FLOAT);
			gl.glVertexPointer(3, GL2.GL_FLOAT, 5*Buffers.SIZEOF_FLOAT, 0l);
			
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalHandle[0]);
			gl.glNormalPointer(GL2.GL_FLOAT, 3*Buffers.SIZEOF_FLOAT, 0l);
			
			
			gl.glDrawArrays(GL2.GL_QUADS, 0, numFaces * 4);
		}
	}
	
	/**
	 * Used to generate the array to be buffered on the GPU on the first call
	 */
	private void generateBufferArray() {
		Byte[] cubeFacesNums = new Byte[cubes.size()];
		int t = 0;
		for (Cube cube: cubes.values()) {
			if (cube.isVisible()) {
				visibleCubes.add(cube);
				numFaces += cube.getNumVisibleFaces();
				cubeFacesNums[t] = cube.getNumVisibleFaces();
				t++;
			}
		}
		
		faceVerts = new float[numFaces * 4 * 5];
		normals = new float[numFaces * 4 * 3];
		int pos = 0;
		
		for (Cube cube: visibleCubes) {
			float[][] tempFaceVerts = cube.getFaceVerts(pos);
			float[][] tempNormals = cube.getNormals();
			
			for (int i = 0; i < tempFaceVerts.length; i++) {
				faceVerts[pos*5] = tempFaceVerts[i][0];
				faceVerts[(pos*5)+1] = tempFaceVerts[i][1];
				faceVerts[(pos*5)+2] = tempFaceVerts[i][2];
				faceVerts[(pos*5)+3] = tempFaceVerts[i][3];
				faceVerts[(pos*5)+4] = tempFaceVerts[i][4];
				
				normals[(pos*3)] = tempNormals[i][0];
				normals[(pos*3)+1] = tempNormals[i][1];
				normals[(pos*3)+2] = tempNormals[i][2];
				pos++;
			}
		}visibleCubes.clear();
	}
	
	/**
	 * Creates the vertex and color buffers for this chunk
	 * @param gl Used to create the buffers for this chunk
	 */
	private void initBuffers(GL2 gl) {
		FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(5 * 4 * numFaces);
		FloatBuffer normalBuffer = Buffers.newDirectFloatBuffer(3 * 4 * numFaces);
		
		normalBuffer.put(normals);
		normalBuffer.flip();
		vertexBuffer.put(faceVerts);
		vertexBuffer.flip();
		
		gl.glGenBuffers(1, vertexHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 5 * numFaces, vertexBuffer, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		
		gl.glGenBuffers(1, normalHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalHandle[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3 * numFaces, normalBuffer, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * @return This chunks x position relative to other chunks
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * @return This chunks z position relative to other chunks
	 */
	public int getZ() {
		return z;
	}
	
	/**
	 * Deletes all of the cubes buffers
	 * @param gl A new instance of openGL used to delete buffers
	 */
	public void delete(GL2 gl) {
		gl.glDeleteBuffers(1, vertexHandle, 0);
		gl.glDeleteBuffers(1, normalHandle, 0);
		cubes.clear();
		visibleCubes.clear();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + z;
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
		Chunk other = (Chunk) obj;
		if (x != other.x)
			return false;
		if (z != other.z)
			return false;
		return true;
	}

	/**
	 * Gets the distance from this chunk to the point specified
	 * @param tx The x position to get the distance to
	 * @param tz The z position to get the distance to
	 * @return The distance of the corner of this chunk to the point specified
	 */
	public float distance(float tx, float tz) {
		return (float) Math.sqrt(Math.pow(x-(tx/16),2) + Math.pow(z-(tz/16),2));
	}
}
