package com.radar.client.world;

import java.nio.FloatBuffer;
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
	
	/**
	 * The x, z position of the chunk in the world
	 */
	private int x, z;
	
	private int[] vertexHandle = new int[1];
	private int[] colorHandle = new int[1];
	
	private int numFaces = 0;
	
	/**
	 * Creates a chunk at the specified x, z
	 * @param x The x chunk position of this chunk
	 * @param z The z chunk position of this chunk
	 */
	public Chunk(int x, int z) {
		this.x = x;
		this.z = z;
		cubes = new LinkedList<>();
		visibleCubes = new LinkedList<>();
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
			for (Cube cube: cubes) {
				if (cube.isVisible()) {
					visibleCubes.add(cube);
					numFaces += cube.getNumVisibleFaces();
				}
			}
			initBuffers(gl);
			
			first = false;
		}else {
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
			gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0l);
			
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, colorHandle[0]);
			gl.glColorPointer(3, GL2.GL_FLOAT, 0, 0l);
			
			gl.glDrawArrays(GL2.GL_QUADS, 0, numFaces * 4);
		}
	}
	
	/**
	 * Creates the vertex and color buffers for this chunk
	 * @param gl Used to create the buffers for this chunk
	 */
	private void initBuffers(GL2 gl) {
		float[][] faceVerts = new float[numFaces * 4][3];
		float[][] colorVerts = new float[numFaces * 4][3];
		int pos = 0;
		
		for (Cube cube: visibleCubes) {
			float[][] tempFaceVerts = cube.getFaceVerts();
			float[][] tempColorVerts = cube.getFaceColors();
			
			for (int i = 0; i < tempFaceVerts.length; i++) {
				faceVerts[pos] = tempFaceVerts[i];
				colorVerts[pos] = tempColorVerts[i];
				pos++;
			}
		}
		
		FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * 4 * numFaces);
		FloatBuffer colorBuffer = Buffers.newDirectFloatBuffer(3 * 4 * numFaces);
		
		for (int i = 0; i < faceVerts.length; i++) {
			vertexBuffer.put(faceVerts[i]);
			colorBuffer.put(colorVerts[i]);
		}
		vertexBuffer.flip();
		colorBuffer.flip();
		
		gl.glGenBuffers(1, vertexHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3 * numFaces, vertexBuffer, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		
		gl.glGenBuffers(1, colorHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, colorHandle[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3 * numFaces, colorBuffer, GL2.GL_STATIC_DRAW);
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
		gl.glDeleteBuffers(1, colorHandle, 0);
	}


	/**
	 * @param tx The x position to get the distance to
	 * @param tz The z position to get the distance to
	 * @return The distance of the corner of this chunk to the point specified
	 */
	public float distance(float tx, float tz) {
		return (float) Math.sqrt(Math.pow(x-(tx/16),2) + Math.pow(z-(tz/16),2));
	}
}
