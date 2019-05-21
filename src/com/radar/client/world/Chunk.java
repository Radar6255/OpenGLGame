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
	
	/**
	 * Holds the handle to the verticies for all the cubes in this chunk
	 */
	private int[] vertexHandle = new int[1];
	
	/**
	 * Holds the number of faces visible in this chunk
	 */
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

			//TODO Possibly optimize further
//			long start = System.currentTimeMillis();
			initBuffers(gl);
//			if (System.currentTimeMillis()-start > 3) {
//				System.out.println((System.currentTimeMillis()-start) + " "+numFaces);
//			}
			first = false;
		}else {
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 5*Buffers.SIZEOF_FLOAT, 3*Buffers.SIZEOF_FLOAT);
			gl.glVertexPointer(3, GL2.GL_FLOAT, 5*Buffers.SIZEOF_FLOAT, 0l);
			
			gl.glDrawArrays(GL2.GL_QUADS, 0, numFaces * 4);
		}
	}
	
	/**
	 * Creates the vertex and color buffers for this chunk
	 * @param gl Used to create the buffers for this chunk
	 */
	private void initBuffers(GL2 gl) {
		float[] faceVerts = new float[numFaces * 4 * 5];
		int pos = 0;
		
		for (Cube cube: visibleCubes) {
			float[][] tempFaceVerts = cube.getFaceVerts();
			
			for (int i = 0; i < tempFaceVerts.length; i++) {
				faceVerts[pos*5] = tempFaceVerts[i][0];
				faceVerts[(pos*5)+1] = tempFaceVerts[i][1];
				faceVerts[(pos*5)+2] = tempFaceVerts[i][2];
				faceVerts[(pos*5)+3] = tempFaceVerts[i][3];
				faceVerts[(pos*5)+4] = tempFaceVerts[i][4];
				pos++;
			}
		}
		
		FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(5 * 4 * numFaces);
		
		vertexBuffer.put(faceVerts);
		vertexBuffer.flip();
		
		gl.glGenBuffers(1, vertexHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 5 * numFaces, vertexBuffer, GL2.GL_STATIC_DRAW);
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
