package com.radar.tools;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.radar.client.Player;
import com.radar.client.window.GameWindow;
import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Chunk;
import com.radar.client.world.Dimension;
import com.radar.client.world.TextureMap;
import com.radar.client.world.generation.WorldGen;

/**
 * @author radar
 * Class to handle any window updates that happen
 */
public class StructWindow extends WindowUpdates {
	
	/**
	 * Used to change the perspective of the window
	 */
	private GLU glu = new GLU();
	
	/**
	 * Holds the texture map which also needs to be closed
	 */
	TextureMap textures;
	
	private long lastTick = 0;
	
	/**
	 * The player this window is rendering for
	 */
	private Player player;
	
	/**
	 * The window of the game, used to update the fps counter in the title
	 */
	private GameWindow window;
	
	long renderTimeMax = 0;
	
	private Chunk world;
	
	StructGen gen;
	
	/**
	 * Creates a controller for the windows updates
	 * @param player The player this window updater is for
	 * @param window The window that needs to be updated
	 */
	public StructWindow(Player player, GameWindow window) {
		super(player, window);
		this.player = player;
		this.window = window;
		
		player.setFly(true);
	}
	
	//Contains any draw calls
	@Override
	public void display(GLAutoDrawable drawable) {
		long start = System.currentTimeMillis();
		if (start - lastTick > 500) {
//			new Thread(() -> {
//				for (Chunk chunk: tChunks) {
//					chunk.update(gen, this);
//				}
//			}).start();
			lastTick = System.currentTimeMillis();
		}
		
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

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {0, 0f, 0f, 1.0f}, 0);
		
		player.render(gl);
	    //Angle, x, y, z
	  	//Angle, verticle, horizontal
		gl.glRotatef(player.getYRot(), 1f, 0f, 0f);
		gl.glRotatef(player.getXRot(), 0f, 1f, 0f);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {-1f, 1f, 1f, 0.0f}, 0);

		
		//Moving the world around the players coordinates
		switch(player.currentDimension) {
		case NORMAL:
			gl.glTranslatef(-player.getPos().getX(), -player.getPos().getY(), -player.getPos().getZ());
			break;
		case TIME:
			gl.glTranslatef(-player.getPos().getX() * WorldGen.timeWorldUpscale, -player.getPos().getY(), -player.getPos().getZ() * WorldGen.timeWorldUpscale);
			break;
		default:
			gl.glTranslatef(-player.getPos().getX(), -player.getPos().getY(), -player.getPos().getZ());
			break;
		}
		
		//Drawing all of the visible chunks
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		
		//Setting material properties for all the cubes
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, new float[] {0.5f, 0.5f, 0.5f, 1.0f}, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0.1f, 0.1f, 0.1f, 1.0f}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, new float[] {0.5f, 0.5f, 0.5f, 1.0f}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, 0);
//        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 100.0f);

		//TODO Call chunk render here
		world.render(gl);
		
		gl.glFlush();
		
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_LIGHT0);
//		player.currentBlockVisual(gl);
		
//		gl.glPushMatrix();
//		gl.glLoadIdentity();
//
//		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {(float) Math.abs(0.5), 0, (float) Math.abs(0.5), 0.0f}, 0);
//		
//		gl.glPopMatrix();

		//Slowing down chunk loading so that it doesnt fps drop when loading chunks
		
		if (System.currentTimeMillis()-start != 0) {
			long temp = System.currentTimeMillis()-start;
			if (temp > renderTimeMax) {
				renderTimeMax = temp;
			}
			window.changeTitle("Render time: "+temp+"ms"+" FPS: "+1000/(System.currentTimeMillis()-start)+" Render time max: "+renderTimeMax);
			
			
		}
	}

	/**
	 * Gets the visible chunk at a location
	 * @param chunkX The x position of the chunk relative to other chunks
	 * @param chunkZ The z position of the chunk relative to other chunks
	 * @return The chunk object at that location
	 */
	@Override
	public Chunk getChunk(int chunkX, int chunkZ) {
		return world;
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable) {
		gen.stop();
		
		GL2 gl = drawable.getGL().getGL2();
		
		//TODO Delete the single chunk
		world.delete(gl);
		
		System.out.println("Deleted remaining chunks");
		
		textures.close(gl);
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		textures = new TextureMap(gl);
		
		// TODO Initialize the chunk

		gen = new StructGen(player, this);
		world = gen.genChunk(0, 0);
		
		player.addGen(Dimension.NORMAL, gen);
		
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 0f);
	    gl.glClearDepth(1.0f);
	    
		gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);
	    gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
	    
	    gl.glMatrixMode(GL2.GL_MODELVIEW);
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
	    glu.gluPerspective( 45.0f * 1.1f, h * 1.1f, 0.10f, 600.0 );
	    gl.glMatrixMode( GL2.GL_MODELVIEW );
	    gl.glLoadIdentity();
	}
	
	public Player getPlayer() {
		return player;
	}
}
