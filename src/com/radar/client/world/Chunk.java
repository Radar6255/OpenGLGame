package com.radar.client.world;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

/**
 * @author radar
 * A class to represent a chunk of cubes,
 * used to make generating, loading, and unloading the world easier.
 */
public class Chunk {
	/**
	 * List of all cubes in this chunk
	 */
	private LinkedList<Cube> cubes;
	
	/**
	 * List of the cubes with at least one visible face
	 */
	private LinkedList<Cube> visibleCubes;
	
	/**
	 * Used to do some buffer creation on this chunks first render call
	 */
	private boolean first = true;
	
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
	
	/**
	 * Indicies of textures for each face
	 */
	private static int[][] faceTextures = new int[][] {{1, 1, 1, 1, 3, 2}, {4, 4, 4, 4, 4, 4}};
	
	/**
	 * Creates a chunk at the specified x, z
	 * @param x The x chunk position of this chunk
	 * @param z The z chunk position of this chunk
	 */
	public Chunk(int x, int z, WorldGen gen) {
		this.x = x;
		this.z = z;
		
		load(gen);
	}
	
	public void update(WorldGen gen) {
		load(gen);
		update = true;
	}

	/**
	 * Called by the worldGen thread, used to do any intense processes,
	 * before first render call, also called on block updates
	 */
	public void load(WorldGen gen) {
		cubes = new LinkedList<>();
		visibleCubes = new LinkedList<>();
		
		ArrayList<ArrayList<ArrayList<Integer>>> chunk = gen.getChunk(x, z);
		
		for (int tx = 0; tx < 16; tx++) {
			for (int tz = 0; tz < 16; tz++) {
				for (int ty = 0; ty < chunk.get(tx).get(tz).size(); ty++) {
					if (chunk.get(tx).get(tz).get(ty) != 0) {
						addCube(new Cube(x*16 + tx, ty, z*16 + tz, 1, 1, 1, faceTextures[chunk.get(tx).get(tz).get(ty)-1], gen));
					}
				}
			}
		}
		
		generateBufferArray();
	}
	
	/**
	 * A function to add a cube to this chunk
	 * @param cube The cube to add to the chunk
	 */
	public void addCube(Cube cube) {
		cubes.add(cube);
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
		for (Cube cube: cubes) {
			if (cube.isVisible()) {
				visibleCubes.add(cube);
				numFaces += cube.getNumVisibleFaces();
			}
		}
		
		faceVerts = new float[numFaces * 4 * 5];
		normals = new float[numFaces * 4 * 3];
		int pos = 0;
		
		for (Cube cube: visibleCubes) {
			float[][] tempFaceVerts = cube.getFaceVerts();
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
		}
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
