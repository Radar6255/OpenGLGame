package com.radar.client;

import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

/**
 * @author radar
 * Class to handle any window updates that happen
 */
public class WindowUpdates implements GLEventListener {
	
	/**
	 * Stores the location of the backgrounds buffered vertex data
	 */
	private int[] backgroundVertHandle = new int[1];
	/**
	 * Stores the location of the backgrounds buffered color data
	 */
	private int[] backgroundColorHandle = new int[1];
	
	@Override
	public void display(GLAutoDrawable drawable) {
		//Contains any draw calls
		
		//Drawing background
		GL2 gl = drawable.getGL().getGL2();
		renderBackground(gl);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		GL2 gl = drawable.getGL().getGL2();
		createInitialVBOs(gl);

	}

	@Override
	public void reshape(GLAutoDrawable drawable, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub

	}
	
	public void createInitialVBOs(GL2 gl) {
		/**
		 * Holds the backgrounds vertex buffer data
		 */
		FloatBuffer backgroundVertex;
		/**
		 * Holds the backgrounds color buffer data
		 */
		FloatBuffer backgroundColor;
		// 2 for X,Y
		// 4 for 4 verticies
		backgroundVertex = Buffers.newDirectFloatBuffer(2 * 4);
		backgroundVertex.put(new float[] {-1,-1});
		backgroundVertex.put(new float[] {-1,1});
		backgroundVertex.put(new float[] {1,1});
		backgroundVertex.put(new float[] {1,-1});
		backgroundVertex.flip();
		
		//3 for r,g,b colors
		//4 for 4 verticies
		backgroundColor = Buffers.newDirectFloatBuffer(3 * 4);
		backgroundColor.put(new float[] {1f, 1f, 1f});
		backgroundColor.put(new float[] {1f, 1f, 0.5f});
		backgroundColor.put(new float[] {1f, 1f, 1f});
		backgroundColor.put(new float[] {1f, 1f, 1f});
		backgroundColor.flip();
		
		gl.glGenBuffers(1, backgroundVertHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, backgroundVertHandle[0]);
		
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 2, backgroundVertex, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		
		
		gl.glGenBuffers(1, backgroundColorHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, backgroundColorHandle[0]);
		
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3, backgroundColor, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}
	
	private void renderBackground(GL2 gl) {
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, backgroundVertHandle[0]);
		gl.glVertexPointer(2, GL2.GL_FLOAT, 0, 0l);
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, backgroundColorHandle[0]);
		gl.glColorPointer(3, GL2.GL_FLOAT, 0, 0l);
		
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		
		gl.glDrawArrays(GL2.GL_QUADS, 0, 4);
		
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
	}

}
