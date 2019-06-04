package com.radar.client.window;

import java.util.HashMap;
import java.util.LinkedList;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.radar.client.Player;
import com.radar.client.world.Chunk;
import com.radar.client.world.Coord2D;
import com.radar.client.world.TextureMap;
import com.radar.client.world.WorldGen;

/**
 * @author radar
 * Class to handle any window updates that happen
 */
public class WindowUpdates implements GLEventListener {
	
	/**
	 * List of all chunks rendering for this player
	 */
	private HashMap<Coord2D<Integer>,Chunk> chunks;
	
	/**
	 * List of chunks waiting to be rendered
	 */
	private LinkedList<Chunk> chunkQueue;
	
	/**
	 * Used to change the perspective of the window
	 */
	private GLU glu = new GLU();
	
	/**
	 * The world generation, kept to close when the game ends
	 */
	WorldGen gen;
	
	/**
	 * Holds the texture map which also needs to be closed
	 */
	TextureMap textures;
	
	/**
	 * Set to true when adding a chunk to the current rendering chunks
	 */
	private volatile boolean adding = false;
	
	/**
	 * Set to true when clearing the queue of chunks to be rendered
	 */
	private volatile boolean clearing = false;
	
	/**
	 * The player this window is rendering for
	 */
	private Player player;
	
	/**
	 * The window of the game, used to update the fps counter in the title
	 */
	private GameWindow window;
	
	private int seed = -1;
	
	/**
	 * Creates a controller for the windows updates
	 * @param player The player this window updater is for
	 * @param window The window that needs to be updated
	 */
	public WindowUpdates(Player player, GameWindow window) {
		this.player = player;
		this.window = window;
		chunks = new HashMap<Coord2D<Integer>,Chunk>();
		chunkQueue = new LinkedList<Chunk>();
	}
	
	//Contains any draw calls
	@Override
	public void display(GLAutoDrawable drawable) {
		long start = System.currentTimeMillis();
		//Drawing background
		GL2 gl = drawable.getGL().getGL2();
	    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
	    
	    gl.glMatrixMode( GL2.GL_MODELVIEW );
	    gl.glLoadIdentity();
		
	    player.tick(this);


		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, new float[] {1.0f}, 0);
		
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[] {0.2f, 0.2f, 0.2f, 0.0f}, 0);
		gl.glLightf(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, 0.03f);
//		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {(float) Math.abs(0.5), (float) Math.abs(0.5), 0.0f, 0.0f}, 0);

//		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {0, 0, 1, 0.0f}, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {0, 0f, 0f, 1.0f}, 0);
		
		player.render(gl);
	    //Angle, x, y, z
	  	//Angle, verticle, horizontal
		gl.glRotatef(player.getYRot(), 1f, 0f, 0f);
		gl.glRotatef(player.getXRot(), 0f, 1f, 0f);
		
		
		//Moving the world around the players coordinates
		gl.glTranslatef(-player.getPos().getX(), -player.getPos().getY(), -player.getPos().getZ());
		
		//Drawing all of the visible chunks
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		
		//Setting material properties for all the cubes
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, new float[] {0.8f, 0.8f, 0.8f, 1.0f}, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0.1f, 0.1f, 0.1f, 1.0f}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, new float[] {0.1f, 0.1f, 0.1f, 1.0f}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0.04f, 0.04f, 0.04f, 0.0f}, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.0f);

		for (Chunk chunk: chunks.values()) {
			chunk.render(gl);
		}
		gl.glFlush();
		
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_LIGHT0);
		
		
//		gl.glPushMatrix();
//		gl.glLoadIdentity();
//
//		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {(float) Math.abs(0.5), 0, (float) Math.abs(0.5), 0.0f}, 0);
//		
//		gl.glPopMatrix();
		
//		if (System.currentTimeMillis()-start > 5) {
//			System.out.println("Render time: "+(System.currentTimeMillis()-start)+"ms");
//		}
		
		//Old system may bring back
//		if (!adding && !chunkQueue.isEmpty()) {
//			clearing = true;
//			chunks.addAll(chunkQueue);
//			chunkQueue.clear();
//			clearing = false;
//		}
		//Slowing down chunk loading so that it doesnt fps drop when loading chunks
		if (!adding && !chunkQueue.isEmpty()) {
			clearing = true;
			Chunk temp = chunkQueue.pop();
			chunks.put(new Coord2D<Integer>(temp.getX(), temp.getZ()), temp);
			clearing = false;
		}
		
		for (Chunk chunk: chunks.values()) {
			if (chunk.distance(player.getPos().getX(), player.getPos().getZ()) > VideoSettings.renderDistance) {
				gen.removeChunk(chunk.getX(), chunk.getZ());
				chunk.delete(gl);
				chunks.remove(new Coord2D<Integer>(chunk.getX(),chunk.getZ()));
				break;
			}
		}
		
		if (System.currentTimeMillis()-start != 0) {
			window.changeTitle("Render time: "+(System.currentTimeMillis()-start)+"ms"+" FPS: "+1000/(System.currentTimeMillis()-start));
		}
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		gen.stop();
		GL2 gl = drawable.getGL().getGL2();
		
		for (Chunk chunk: chunks.values()) {
			chunk.delete(gl);
		}
		System.out.println("Deleted remaining chunks");
		
		textures.close(gl);
	}
	
	public void addSeed(int seed) {
		this.seed = seed;
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		
		// TODO Auto-generated method stub
		GL2 gl = drawable.getGL().getGL2();
		textures = new TextureMap(gl);
		gen = new WorldGen(player, seed, this);
		
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 0f);
	    gl.glClearDepth(1.0f);
	    
		gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);
	    gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
	    
	    gl.glMatrixMode(GL2.GL_MODELVIEW);
	    
//        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_DONT_CARE);
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
	    glu.gluPerspective( 45.0f, h, 0.10f, 600.0 );
	    gl.glMatrixMode( GL2.GL_MODELVIEW );
	    gl.glLoadIdentity();
	}
	
	/**
	 * Gets the visible chunk at a location
	 * @param chunkX The x position of the chunk relative to other chunks
	 * @param chunkZ The z position of the chunk relative to other chunks
	 * @return The chunk object at that location
	 */
	public Chunk getChunk(int chunkX, int chunkZ) {
		return chunks.get(new Coord2D<Integer>(chunkX, chunkZ));
	}
	
	/**
	 * Adds a chunk to be rendered to this window
	 * @param chunk The chunk to add to the render
	 */
	public void addChunk(Chunk chunk) {
		while (clearing) {}
		if (!clearing) {
			adding = true;
			chunkQueue.add(chunk);
			adding = false;
		}
	}
}
