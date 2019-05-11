package com.radar.client.world;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

/**
 * @author radar
 * Class to represent a cube.
 */
public class Cube {
	/**
	 * Verticies of the cube
	 */
	private final static float[][] verts = new float[][] {{0.5f, 0.5f, 0.5f}, {0.5f, -0.5f, 0.5f}, {-0.5f, -0.5f, 0.5f}, {-0.5f, 0.5f, 0.5f},
		{0.5f, 0.5f, -0.5f}, {0.5f, -0.5f, -0.5f}, {-0.5f, -0.5f, -0.5f}, {-0.5f, 0.5f, -0.5f},
		{0.5f, 0.5f, 0.5f}, {0.5f, -0.5f, 0.5f}, {0.5f, -0.5f, -0.5f}, {0.5f, 0.5f, -0.5f},
		{-0.5f, 0.5f, 0.5f}, {-0.5f, -0.5f, 0.5f}, {-0.5f, -0.5f, -0.5f}, {-0.5f, 0.5f, -0.5f},
		{0.5f, 0.5f, 0.5f}, {-0.5f, 0.5f, 0.5f}, {-0.5f, 0.5f, -0.5f}, {0.5f, 0.5f, -0.5f},
		{0.5f, -0.5f, 0.5f}, {-0.5f, -0.5f, 0.5f}, {-0.5f, -0.5f, -0.5f}, {0.5f, -0.5f, -0.5f}};
	/**
	 * Holds the handles for the VBOs of a face
	 */
	private int[] faceHandles = new int[6];
	private int[] colorHandles = new int[6];
	
	
	/**
	 * Holds x,y,z coordinates of the cube
	 */
	private Coord<Integer> coords;
	
	/**
	 * Holds width, height, and depth of the cube repsepecively
	 */
	private int w, h, d;
	
	float rquad = 0;
	/**
	 * Constructor to set up OpenGL buffers
	 * @param x X Position of the cube
	 * @param y Y Position of the cube
	 * @param z Z Position of the cube
	 * @param w Width of the cube
	 * @param h Height of the cube
	 * @param d Depth of the cube
	 */
	public Cube(int x, int y, int z, int w, int h, int d, GL2 gl) {
		coords = new Coord<Integer>(x,y,z);
		this.w = w;
		this.h = h;
		this.d = d;
		initBuffers(gl);
	}
	
	public void render(GL2 gl, Coord<Float> playerPos) {
		
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		for (int i = 0; i < 6; i++) {
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, faceHandles[i]);
			gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0l);
			
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, colorHandles[i]);
			gl.glColorPointer(3, GL2.GL_FLOAT, 0, 0l);
			
			gl.glDrawArrays(GL2.GL_QUADS, 0, 4);
		}
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		
//		rquad -= 0.01;
	}
	
	private void initBuffers(GL2 gl) {
		LinkedList<FloatBuffer> vertexBuffers = new LinkedList<>();
		ArrayList<FloatBuffer> colorBuffers = new ArrayList<>();
		
		for (int i = 0; i < 6; i++) {
			vertexBuffers.add(Buffers.newDirectFloatBuffer(3 * 4));
			colorBuffers.add(Buffers.newDirectFloatBuffer(3 * 4));
			for (int v = 0; v < 4; v++) {
				//X, Y, Z of a point
				vertexBuffers.get(i).put(new float[] {verts[(i*4) + v][0] + coords.getX(),verts[(i*4) + v][1] + coords.getY(),verts[(i*4) + v][2] + coords.getZ()});
				colorBuffers.get(i).put(new float[] {1.0f*(i/6f), 1.0f, 1.0f});
			}
			vertexBuffers.get(i).flip();
			colorBuffers.get(i).flip();
			gl.glGenBuffers(1, faceHandles, i);
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, faceHandles[i]);
			
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3, vertexBuffers.get(i), GL2.GL_STATIC_DRAW);
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
			
			gl.glGenBuffers(1, colorHandles, i);
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, colorHandles[i]);
			
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3, colorBuffers.get(i), GL2.GL_STATIC_DRAW);
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		}
	}
}
