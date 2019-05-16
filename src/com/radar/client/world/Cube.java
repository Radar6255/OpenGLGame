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
	private final static float[][] verts = new float[][] {
		{0.5f, 0.5f, 0.5f}, {0.5f, -0.5f, 0.5f}, {-0.5f, -0.5f, 0.5f}, {-0.5f, 0.5f, 0.5f},
		{0.5f, 0.5f, -0.5f}, {0.5f, -0.5f, -0.5f}, {-0.5f, -0.5f, -0.5f}, {-0.5f, 0.5f, -0.5f},
		{0.5f, 0.5f, 0.5f}, {0.5f, -0.5f, 0.5f}, {0.5f, -0.5f, -0.5f}, {0.5f, 0.5f, -0.5f},
		{-0.5f, 0.5f, 0.5f}, {-0.5f, -0.5f, 0.5f}, {-0.5f, -0.5f, -0.5f}, {-0.5f, 0.5f, -0.5f},
		{0.5f, 0.5f, 0.5f}, {-0.5f, 0.5f, 0.5f}, {-0.5f, 0.5f, -0.5f}, {0.5f, 0.5f, -0.5f},
		{0.5f, -0.5f, 0.5f}, {-0.5f, -0.5f, 0.5f}, {-0.5f, -0.5f, -0.5f}, {0.5f, -0.5f, -0.5f}
	};
	/**
	 * Colors stored for each face
	 */
	private float[][] faceColors = new float[][] {
		//0 Green face
		{0f,1f,0f},
		//1 Magenta face
		{1f,0f,1f},
		//2 Red face
		{1f,0f,0f},
		//3 Blue face
		{0f,0f,1f},
		//4 Light blue, top face
		{0f,1f,1f},
		//5 Yellow, bottom face
		{1f,1f,0f}
	};
	
	/**
	 * Holds which faces are visible to the player or not used to cull faces between cubes
	 */
	private boolean[] visibleFaces = new boolean[] {true, true, true, true, true, true};
	
	/**
	 * Holds the handles for the VBOs of a face
	 */
	private int[] faceHandles = new int[6];
	/**
	 * Holds the handles for the color values of the faces
	 */
	private int[] colorHandles = new int[6];
	
	
	/**
	 * Holds x,y,z coordinates of the cube
	 */
	private Coord<Integer> coords;
	
	/**
	 * Holds width, height, and depth of the cube repsepecively
	 */
	private int w, h, d;
	
	private boolean first = true;
	
	private WorldGen gen;
	
	/**
	 * Constructor to set up OpenGL buffers
	 * @param x X Position of the cube
	 * @param y Y Position of the cube
	 * @param z Z Position of the cube
	 * @param w Width of the cube
	 * @param h Height of the cube
	 * @param d Depth of the cube
	 */
	public Cube(int x, int y, int z, int w, int h, int d, WorldGen gen) {
		coords = new Coord<Integer>(x,y,z);
		this.w = w;
		this.h = h;
		this.d = d;
		this.gen = gen;
	}
	
	/**
	 * Draws this cube's faces
	 * @param gl The GL2 reference used to draw the cube
	 */
	public void render(GL2 gl) {
		
		if (first) {
			//Needed to move the buffer initialization to the render call because is needs a new copy of gl
			initBuffers(gl);
			first = false;
		}
		
		
//		gl.glTranslated(coords.getX(), coords.getY(), coords.getZ());
		for (int i = 0; i < 6; i++) {
			if (visibleFaces[i]) {
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, faceHandles[i]);
				gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0l);
			
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, colorHandles[i]);
				gl.glColorPointer(3, GL2.GL_FLOAT, 0, 0l);
			
				gl.glDrawArrays(GL2.GL_QUADS, 0, 4);
			}
		}
	}
	
	
	/**
	 * Function to set up the buffers for all the cube faces
	 * @param gl The GL2 reference to get access to OpenGL buffers
	 */
	private void initBuffers(GL2 gl) {
		LinkedList<FloatBuffer> vertexBuffers = new LinkedList<>();
		LinkedList<FloatBuffer> colorBuffers = new LinkedList<>();
		
		adjacentFaceCull();
		
		for (int i = 0; i < 6; i++) {
			//Only adding visible faces to the GPU
			if (visibleFaces[i]) {
				vertexBuffers.add(Buffers.newDirectFloatBuffer(3 * 4));
				colorBuffers.add(Buffers.newDirectFloatBuffer(3 * 4));
				for (int v = 0; v < 4; v++) {
					//X, Y, Z of a point
					
					vertexBuffers.getLast().put(new float[] {verts[(i*4) + v][0] + coords.getX(),verts[(i*4) + v][1] + coords.getY(),verts[(i*4) + v][2] + coords.getZ()});
//					colorBuffers.get(i).put(new float[] {1.0f*(i/6f), 1.0f, 1.0f});
					colorBuffers.getLast().put(faceColors[i]);
				}
				vertexBuffers.getLast().flip();
				colorBuffers.getLast().flip();
				gl.glGenBuffers(1, faceHandles, i);
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, faceHandles[i]);
			
				gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3, vertexBuffers.getLast(), GL2.GL_STATIC_DRAW);
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
			
				gl.glGenBuffers(1, colorHandles, i);
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, colorHandles[i]);
			
				gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3, colorBuffers.getLast(), GL2.GL_STATIC_DRAW);
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
			}
		}
	}
	
	private void adjacentFaceCull() {
		int chunkX, chunkZ;
		
		chunkX = (int) Math.floor(coords.getX()/16.0);
		chunkZ = (int) Math.floor(coords.getZ()/16.0);
		
		
		ArrayList<ArrayList<ArrayList<Integer>>> currentChunk = gen.getChunk(chunkX, chunkZ);
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
		if (currentChunk.get(relX).get(relZ).size() <= coords.getY() || currentChunk.get(relX).get(relZ).get(coords.getY()) != 1) {
			System.out.print("Look: ");
			System.out.println(coords.getX()+" "+relX+" "+ chunkX +" "+coords.getZ()+" "+relZ+" "+chunkZ);
		}
		
		//TODO Check for faces on the border of chunks
		if (relX-1 >= 0) {
			if (currentChunk.get(relX-1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) != 0) {
//					System.out.println("Test");
					visibleFaces[3] = false;
				}
			}
		}
		if (relZ-1 >= 0) {
			if (currentChunk.get(relX).get(relZ-1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ-1).get(coords.getY()) != 0) {
//					System.out.println("Test");
					visibleFaces[1] = false;
				}
			}
		}
		if (relX+1 < 16) {
			if (currentChunk.get(relX+1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX+1).get(relZ).get(coords.getY()) != 0) {
					visibleFaces[2] = false;
				}
			}
		}
		if (relZ+1 < 16) {
			if (currentChunk.get(relX).get(relZ+1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ+1).get(coords.getY()) != 0) {
					visibleFaces[0] = false;
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
