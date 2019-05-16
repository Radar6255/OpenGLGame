package com.radar.client.window;

import java.nio.FloatBuffer;
import java.util.LinkedList;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.radar.client.Player;
import com.radar.client.world.Chunk;
import com.radar.client.world.WorldGen;

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
	
	/**
	 * List of all chunks rendering for this player
	 */
	private LinkedList<Chunk> chunks;
	
	private LinkedList<Chunk> chunkQueue;
	
	private GLU glu = new GLU();
	
	WorldGen gen;
	
	/**
	 * The player this window is rendering for
	 */
	private Player player;
	
	public WindowUpdates(Player player) {
		this.player = player;
		chunks = new LinkedList<Chunk>();
		chunkQueue = new LinkedList<Chunk>();
	}
	
	/**
	 * Used to shutdown threads running for the window
	 */
	public void stop() {
		gen.stop();
	}
	
	//Contains any draw calls
	@Override
	public void display(GLAutoDrawable drawable) {
		//Drawing background
		GL2 gl = drawable.getGL().getGL2();
	    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
	    gl.glLoadIdentity();
//		renderBackground(gl);
	    player.tick();
	    
	    //Angle, x, y, z
	  	//Angle, verticle, horizontal
		gl.glRotatef(player.getYRot(), 1f, 0f, 0f);
		gl.glRotatef(player.getXRot(), 0f, 1f, 0f);
		
		//Moving the world around the players coordinates
		gl.glTranslatef(-player.getPos().getX(), -player.getPos().getY(), -player.getPos().getZ());
		
		//Drawing all of the visible chunks
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		for (Chunk chunk: chunks) {
			chunk.render(gl);
		}
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		if (!chunkQueue.isEmpty()) {
			chunks.addAll(chunkQueue);
			chunkQueue.clear();
		}
		
		for (int i = 0; i < chunks.size(); i++) {
			if (chunks.get(i).distance(player.getPos().getX(), player.getPos().getZ()) > VideoSettings.renderDistance) {
				Chunk removing = chunks.remove(i);
				gen.removeChunk(removing.getX(), removing.getZ());
				i--;
			}
		}
		
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}
	@Override
	public void init(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		GL2 gl = drawable.getGL().getGL2();
		gen = new WorldGen(player, this);
		
		gl.glShadeModel( GL2.GL_SMOOTH );
		gl.glClearColor( 0f, 0f, 0f, 0f );
	    gl.glClearDepth( 1.0f );
	    
		gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);
	    gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST );
//		createInitialVBOs(gl);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		System.out.println("Reshaping");
		GL2 gl = drawable.getGL().getGL2();
		if( height == 0 )
	         height = 1;
				
	    final float h = ( float ) width / ( float ) height;
	      
		gl.glViewport( 0, 0, width, height );
		gl.glMatrixMode( GL2.GL_PROJECTION );
	    gl.glLoadIdentity();
	    //                          Start  End
	    glu.gluPerspective( 45.0f, h, 1.0, 200.0 );
	    gl.glMatrixMode( GL2.GL_MODELVIEW );
	    gl.glLoadIdentity();
	}
	
	public void createInitialVBOs(GL2 gl) {
		System.out.println("Creating VBOs");
		/**
		 * Holds the backgrounds vertex buffer data
		 */
		FloatBuffer backgroundVertex;
		/**
		 * Holds the backgrounds color buffer data
		 */
		FloatBuffer backgroundColor;
		// 3 for X,Y,Z
		// 4 for 4 verticies
		backgroundVertex = Buffers.newDirectFloatBuffer(3 * 4);
		backgroundVertex.put(new float[] {-1,-1,-199f});
		backgroundVertex.put(new float[] {-1,1,-199f});
		backgroundVertex.put(new float[] {1,1,-199f});
		backgroundVertex.put(new float[] {1,-1,-199f});
		backgroundVertex.flip();
		
		//3 for r,g,b colors
		//4 for 4 verticies
		backgroundColor = Buffers.newDirectFloatBuffer(3 * 4);
		backgroundColor.put(new float[] {1f, 1f, 1f});
		backgroundColor.put(new float[] {1f, 1f, 0.5f});
		backgroundColor.put(new float[] {1f, 1f, 1f});
		backgroundColor.put(new float[] {1f, 0.5f, 0.5f});
		backgroundColor.flip();
		
		gl.glGenBuffers(1, backgroundVertHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, backgroundVertHandle[0]);
		
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3, backgroundVertex, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		
		
		gl.glGenBuffers(1, backgroundColorHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, backgroundColorHandle[0]);
		
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3, backgroundColor, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}
	
	private void renderBackground(GL2 gl) {
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, backgroundVertHandle[0]);
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0l);
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, backgroundColorHandle[0]);
		gl.glColorPointer(3, GL2.GL_FLOAT, 0, 0l);
		
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		
		gl.glDrawArrays(GL2.GL_QUADS, 0, 4);
		
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
	}
	
	public void addChunk(Chunk chunk) {
		chunkQueue.add(chunk);
	}
}
