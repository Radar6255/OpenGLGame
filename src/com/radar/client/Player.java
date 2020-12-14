package com.radar.client;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.jogamp.opengl.GL2;
import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Coord;
import com.radar.client.world.Coord2D;
import com.radar.client.world.Dimension;
import com.radar.client.world.WorldGen;
import com.radar.common.Protocol;

public class Player implements KeyListener, MouseListener, Protocol{
	
	private final static float[][] verts = new float[][] {
		{0.5f, 0.5f, 0.5f, 1, 1}, {0.5f, -0.5f, 0.5f, 1, 0}, {-0.5f, -0.5f, 0.5f, 0, 0}, {-0.5f, 0.5f, 0.5f, 0, 1},
		{0.5f, 0.5f, -0.5f, 1, 1}, {0.5f, -0.5f, -0.5f, 1, 0}, {-0.5f, -0.5f, -0.5f, 0, 0}, {-0.5f, 0.5f, -0.5f, 0, 1},
		{0.5f, 0.5f, 0.5f, 1, 1}, {0.5f, -0.5f, 0.5f, 1, 0}, {0.5f, -0.5f, -0.5f, 0, 0}, {0.5f, 0.5f, -0.5f, 0, 1},
		{-0.5f, 0.5f, 0.5f, 1, 1}, {-0.5f, -0.5f, 0.5f, 1, 0}, {-0.5f, -0.5f, -0.5f, 0, 0}, {-0.5f, 0.5f, -0.5f, 0, 1},
		{0.5f, 0.5f, 0.5f, 0, 0}, {-0.5f, 0.5f, 0.5f, 0, 1}, {-0.5f, 0.5f, -0.5f, 1, 1}, {0.5f, 0.5f, -0.5f, 1, 0},
		{0.5f, -0.5f, 0.5f, 0, 0}, {-0.5f, -0.5f, 0.5f, 0, 1}, {-0.5f, -0.5f, -0.5f, 1, 1}, {0.5f, -0.5f, -0.5f, 1, 0}
	};
	/**
	 * Position of the player
	 */
	private Coord<Float> pos;
	
	/**
	 * Block ID of the block to place
	 */
	private short currentlyPlacing = 1;
	
	public Dimension currentDimesnion;
	
	/**
	 * Velocity of the player
	 */
	private Coord<Float> velocity;
	
	/**
	 * Acceleration of the player
	 */
	private Coord<Float> acceleration;
	
	/**
	 * Booleans for which keys
	 */
	boolean forward, sideways;
	
	/**
	 * Boolean used to see if the player can jump at the moment
	 */
	boolean jump;
	
	/**
	 * The size of the players hitbox
	 */
	static final float playerSize = 0.75f;
	
	/**
	 * Rotation of the players view
	 */
	float xRot = 0, yRot = 0;
	
	/**
	 * Booleans to keep track of what buttons are currently pressed
	 */
	boolean w, a, s, d, space, shift, breakB, place, c;
	
	/**
	 * Boolean to control whether the game is paused or not,
	 * used to free mouse from the first person camera
	 */
	boolean pause = false;
	
	/**
	 * The current world generation, used to grab chunks to check and modify
	 */
	private HashMap<Dimension, WorldGen> worldGen;
	
	/**
	 * The players movement speed amount
	 */
	private float movementSpeed = PlayerSettings.defaultMovementSpeed;
	
	private Scanner in;
	
	private PrintStream out;
	
	/**
	 * Center coordinates of the players screen
	 */
	int centerX = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
	int centerY = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
	
	public Player(float x, float y, float z, float xRot, float yRot) {
		pos = new Coord<Float>(x,y,z);
		velocity = new Coord<Float>(0f,0f,0f);
		acceleration = new Coord<Float>(0f,-0.01f,0f);
		worldGen = new HashMap<Dimension, WorldGen>();
		
		this.xRot = xRot;
		this.yRot = yRot;
		
		w = false; a = false; s = false; d = false; space = false; shift = false; breakB = false; place = false;
		jump = false;
		
		this.currentDimesnion = Dimension.NORMAL;
	}
	
	/**
	 * Function to run things that should be ticked for the player
	 */
	public void tick(WindowUpdates window) {
		forward = false;
		sideways = false;
		//Getting mouse details
		PointerInfo mouseLoc = MouseInfo.getPointerInfo();
		//TODO Sometimes throws errors when alt-tabbing
		Point tempPoint;
		try {
			tempPoint = mouseLoc.getLocation();
		}catch(Exception E) {
			tempPoint = new Point(centerX, centerY);
		}
		float x = (float) tempPoint.getX();
		float y = (float) tempPoint.getY();

		if (!pause) {
			//Finding the mouse movement and changing rotation
			xRot = xRot + (x-centerX)*PlayerSettings.mouseSensitivity;
			yRot = yRot + (y-centerY)*PlayerSettings.mouseSensitivity;
		
			if (yRot < -90) {
				yRot = -90;
			}if (yRot > 90) {
				yRot = 90;
			}
			
			try {
				new Robot().mouseMove(centerX, centerY);
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
		
		float xChange = 0; float yChange = 0; float zChange = 0;
		
		//Finding the sine and cosine for the horizontal rotation
		float sin = (float) Math.sin(Math.toRadians(xRot));
		float cos = (float) Math.cos(Math.toRadians(xRot));
		
		acceleration.setX(0f);
//		acceleration.setY(-0.01f);
		acceleration.setY(0f);
		acceleration.setZ(0f);
		
		if (c) {
			if (this.currentDimesnion == Dimension.NORMAL) {
				this.currentDimesnion = Dimension.TIME;
			}else {
				this.currentDimesnion = Dimension.NORMAL;
			}
			c = false;
		}
		
		//Foward backward movement
		if (w) {
			acceleration.setX(movementSpeed*sin + acceleration.getX());
			acceleration.setZ(-movementSpeed*cos + acceleration.getZ());
			forward = true;
		}if (s) {
			acceleration.setX(-movementSpeed*sin + acceleration.getX());
			acceleration.setZ(movementSpeed*cos + acceleration.getZ());
			forward = true;
		}
		
		//Left right movement
		if (a) {
			acceleration.setX(-movementSpeed*cos + acceleration.getX());
			acceleration.setZ(-movementSpeed*sin + acceleration.getZ());
			sideways = true;
		}if (d) {
			acceleration.setX(movementSpeed*cos + acceleration.getX());
			acceleration.setZ(movementSpeed*sin + acceleration.getZ());
			sideways = true;
		}
		
		if (sideways && forward) {
			float standardX = acceleration.getX()/((float) Math.sqrt(Math.pow(acceleration.getX(),2)+Math.pow(acceleration.getZ(),2)));
			float standardZ = acceleration.getZ()/((float) Math.sqrt(Math.pow(acceleration.getX(),2)+Math.pow(acceleration.getZ(),2)));
			acceleration.setX(standardX*movementSpeed);
			acceleration.setZ(standardZ*movementSpeed);
		}
		
		//Up down movement
		if(space && jump) {
			pos.setY(movementSpeed + pos.getY());
			acceleration.setY(0.14f);
			space = false;
			jump = false;
		}else if (shift) {
			pos.setY(-movementSpeed + pos.getY());
		}
		
		velocity.setX(velocity.getX() + 0.34f*acceleration.getX());
		velocity.setY(velocity.getY() + acceleration.getY());
		velocity.setZ(velocity.getZ() + 0.34f*acceleration.getZ());
		
		if (velocity.getX() > 0.2) {
			velocity.setX(0.2f);
		}else if (velocity.getX() < -0.2) {
			velocity.setX(-0.2f);
		}
		if (velocity.getY() > 0.4) {
			velocity.setY(0.4f);
		}else if (velocity.getY() < -0.4) {
			velocity.setY(-0.4f);
		}
		if (velocity.getZ() > 0.2) {
			velocity.setZ(0.2f);
		}else if (velocity.getZ() < -0.2) {
			velocity.setZ(-0.2f);
		}
		
		pos.setX(pos.getX() + velocity.getX());
		pos.setY(pos.getY() + velocity.getY());
		pos.setZ(pos.getZ() + velocity.getZ());

		xChange = velocity.getX();
		yChange = velocity.getY();
		zChange = velocity.getZ();
		
		//X Friction
		if (Math.abs(velocity.getX()) > 0) {
			velocity.setX(velocity.getX()-0.5f*velocity.getX());
		}
		
		//Z Friction
		if (Math.abs(velocity.getZ()) > 0) {
			velocity.setZ(velocity.getZ()-0.5f*velocity.getZ());
		}
		
		collision(xChange, yChange, zChange, window);
		
		if (breakB) {
			breakBlock(window);
		}
		if (place) {
			placeBlock(window);
		}
	}
	
	public void currentBlockVisual(GL2 gl) {
		double yVec = -Math.sin(Math.toRadians(yRot));
		double xzComponent = Math.cos(Math.toRadians(yRot));
		
		double xVec = Math.sin(Math.toRadians(xRot)) * xzComponent;
		double zVec = -Math.cos(Math.toRadians(xRot)) * xzComponent;
		
		float i = 0;
		
		//Current x, y, z coords of the ray
		float cx, cy, cz;
		
		while (i < 50) {
			cx = pos.getX() + (float) (i*xVec);
			cy = pos.getY() + (float) (i*yVec);
			cz = pos.getZ() + (float) (i*zVec);
			
			cx = (float) Math.ceil(cx - 0.5);
			cy = (float) Math.ceil(cy - 0.5);
			cz = (float) Math.ceil(cz - 0.5);
			
			int chunkX = (int) Math.floor(cx/16.0);
			int chunkZ = (int) Math.floor(cz/16.0);
			
			ArrayList<ArrayList<ArrayList<Short>>> current = worldGen.get(currentDimesnion).getChunk(chunkX, chunkZ);
//			Coord<Integer> collision = pointCollision(cx, cy, cz, current);
			if (current != null) {
				int relX = (int) cx % 16;
				int relZ = (int) cz % 16;
				
				if (cx < 0) {
					relX = (int) (15 - (Math.abs(1+cx) % 16));
				}else {
					relX = (int) cx % 16;
				}
				if (cz < 0) {
					relZ = (int) (15 - (Math.abs(1+cz) % 16));
				}else {
					relZ = (int) cz % 16;
				}
				//TODO Fix random out of bounds errors
				try {
					if (current.get(relX).get(relZ).size() > Math.floor(cy) && cy > 0 && current.get(relX).get(relZ).get((int) Math.floor(cy)) != 0){
						gl.glBegin(GL2.GL_LINES);
						gl.glColor3f(0.0f, 0.0f, 0.0f);
						gl.glVertex3f(pos.getX(), pos.getY()+1, pos.getZ());
						gl.glVertex3d(pos.getX() + (i*xVec), pos.getY() + (i*yVec) - 0f, pos.getZ()+(i*zVec));
						
						gl.glColor3f(1f, 1f, 1f);
						gl.glVertex3f(pos.getX(), pos.getY()-1, pos.getZ());
						gl.glVertex3f(pos.getX() + (float) (i*xVec), pos.getY() + (float) (i*yVec) - 0f, pos.getZ()+(float) (i*zVec));
						
//						gl.glVertex3f(pos.getX(), pos.getY(), pos.getZ()+1);
//						gl.glVertex3f(pos.getX() + (float) (i*xVec), pos.getY() + (float) (i*yVec), pos.getZ()+(float) (i*zVec));
//						gl.glVertex3f(pos.getX(), pos.getY(), pos.getZ()-1);
//						gl.glVertex3f(pos.getX() + (float) (i*xVec), pos.getY() + (float) (i*yVec), pos.getZ()+(float) (i*zVec));
						gl.glEnd();
						
						gl.glColor3f(0.0f, 0.0f, 0.0f);
						gl.glBegin(GL2.GL_LINE_LOOP);
						gl.glLineWidth(3f);
						for (int f = 0; f < verts.length; f++) {
							
							
//							gl.glBegin(GL2.GL_POLYGON);
//							gl.glBegin(GL2.GL_LINES);
//							gl.glVertex3fv(Cube.verts[f], 0);
							gl.glVertex3f(verts[f][0]+(int) cx, verts[f][1]+((float) Math.floor(cy)), verts[f][2]+(int) cz);

							
//						for (int p = 0; p < 3; p++) {
//							gl.glVertex
//						}
						}
						gl.glEnd();
						return;
					}
				}catch(Exception e) {
					System.out.println("Caught random out of bounds");
				}
			}
//			i += 0.004f;
			i += 0.1f;
		}gl.glEnd();
	}
	
	public void update(String message, WindowUpdates window) {
		String[] command = message.split(" ");
		switch(command[0]) {
		case(BLOCK_UPDATE):{
			int x = Integer.parseInt(command[1]);
			int y = Integer.parseInt(command[2]);
			int z = Integer.parseInt(command[3]);
			short blockID = Short.parseShort(command[4]);
			int chunkX = (int) Math.floor(x/16.0);
			int chunkZ = (int) Math.floor(z/16.0);
			
//			ArrayList<ArrayList<ArrayList<Integer>>> current = worldGen.getChunk(chunkX, chunkZ);
//			if (current == null || current.isEmpty()) {
//				worldGen.loadChunk(chunkX, chunkZ);
//			}
//			int relX = (int) x % 16;
//			int relZ = (int) z % 16;
//			
//			if (x < 0) {
//				relX = (int) (15 - (Math.abs(1+x) % 16));
//			}else {
//				relX = (int) x % 16;
//			}
//			if (z < 0) {
//				relZ = (int) (15 - (Math.abs(1+z) % 16));
//			}else {
//				relZ = (int) z % 16;
//			}
//			try {
//				current.get(relX).get(relZ).set(y, blockID);
//			}catch(Exception e) {
//				System.out.println("Error updating block "+x+" "+y+" "+z);
//			}
			worldGen.get(currentDimesnion).placeBlock(x, y, z, chunkX, chunkZ, blockID);
			
			break;
		}
		}
	}
	
	public int addSocket(Socket server, WindowUpdates window) {
		try {
			in = new Scanner(server.getInputStream());
			out = new PrintStream(server.getOutputStream());
		} catch (IOException e) {
			System.out.println("Problem creating input or output streams");
		}
		
		int seed = Integer.parseInt(in.nextLine().split(" ")[1]);
		new Thread(()-> {
			while (in.hasNextLine()) {
				System.out.println("Got message");
				update(in.nextLine(), window);
			}
		}).start();
		return seed;
	}
	
	public void render(GL2 gl) {
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(-0.001f, 0.0f,-0.1f);
		gl.glVertex3f(0.001f, 0.0f,-0.1f);
		gl.glVertex3f(0.0f, 0.001f,-0.1f);
		gl.glVertex3f(0.0f, -0.001f,-0.1f);
		gl.glEnd();
	}
	
	/**
	 * Function to give this player a world to base collisions and block placement on
	 * @param worldGen The world generation to give this player
	 */
	public void addGen(Dimension dim, WorldGen worldGen) {
		this.worldGen.put(dim, worldGen);
	}
	
	/**
	 * Function to handle player collisions with the world
	 * @param xChange The change in the players x coord from last tick
	 * @param yChange The change in the players y coord from last tick
	 * @param zChange The change in the players z coord from last tick
	 * @param window The window used to update the world for debug purposes
	 */
	private void collision(float xChange, float yChange, float zChange, WindowUpdates window) {
		Coord<Float> pos;
		if (this.currentDimesnion == Dimension.TIME) {
			pos = new Coord<Float>(this.pos.getX() * WorldGen.timeWorldUpscale, this.pos.getY(), this.pos.getZ() * WorldGen.timeWorldUpscale);
		}else {
			pos = this.pos;
		}
		float xCorrection = 0;
		float zCorrection = 0;
		int xCollision = 0;
		int zCollision = 0;
		//Only do horizontal collision checks when you have moved horizontally
		if (xChange != 0 || zChange != 0) {
			//Horizontal collisions using a 2*2
			for (int i = 0; i < 4; i++) {
				float xOff = 0.0f;
				float zOff = 0.0f;
				
				if (i == 1 || i == 3) {
					xOff += 1;
				}if (i >= 2) {
					zOff += 1;
				}
				
	//			ArrayList<ArrayList<ArrayList<Integer>>> current = worldGen.getChunk(chunkX, chunkZ);
				//TODO Causes random out of bounds errors even after checking that it would be in bounds
				if ((worldGen.get(currentDimesnion).getBlock(pos.getX()+xOff, (float) Math.floor(roundFloat(pos.getY()-1-yChange)), pos.getZ()+zOff) != 0 && worldGen.get(currentDimesnion).getBlock(pos.getX()+xOff, (float) Math.floor(roundFloat(pos.getY()-1-yChange)), pos.getZ()+zOff) != -1)
						|| (worldGen.get(currentDimesnion).getBlock(pos.getX()+xOff, (float) Math.ceil(pos.getY()-yChange), pos.getZ()+zOff) != 0 && worldGen.get(currentDimesnion).getBlock(pos.getX()+xOff, (float) Math.ceil(pos.getY()-yChange), pos.getZ()+zOff) != -1)
						|| (worldGen.get(currentDimesnion).getBlock(pos.getX()+xOff, Math.round(pos.getY()-0.5-yChange), pos.getZ()+zOff) != 0 && worldGen.get(currentDimesnion).getBlock(pos.getX()+xOff, Math.round(pos.getY()-0.5-yChange), pos.getZ()+zOff) != -1)) {
					//Check if there is a collision
					if ((pos.getX()-(1-playerSize) > Math.floor(pos.getX()+xOff)-1 && pos.getX()+(1-playerSize) < Math.floor(pos.getX()+xOff)+1) && (pos.getZ()-(1-playerSize) > Math.floor(pos.getZ()+zOff)-1 && pos.getZ()+(1-playerSize) < Math.floor(pos.getZ()+zOff)+1)) {
						if (pos.getX()-(1-playerSize) > Math.floor(pos.getX()+xOff)-1 && roundFloat(pos.getX()-xChange-(1-playerSize)) <= (float) Math.floor(pos.getX()+xOff)-1) {
							xCollision++;
							xCorrection = (float) Math.floor(pos.getX()+xOff)-1 + (1-playerSize);
						}
						
						if (pos.getX()+(1-playerSize) < Math.floor(pos.getX()+xOff)+1 && roundFloat(pos.getX()-xChange+(1-playerSize)) >= (float) Math.floor(pos.getX()+xOff)+1) {
							xCollision++;
							xCorrection = (float) Math.floor(pos.getX()+xOff)+1 - (1-playerSize);
						}
		
						if (pos.getZ()-(1-playerSize) > Math.floor(pos.getZ()+zOff)-1 && roundFloat(pos.getZ()-zChange-(1-playerSize)) <= (float) Math.floor(pos.getZ()+zOff)-1) {
							zCollision++;
							zCorrection = (float) Math.floor(pos.getZ()+zOff)-1 + (1-playerSize);
						}
						
						if (pos.getZ()+(1-playerSize) < Math.floor(pos.getZ()+zOff)+1 && roundFloat(pos.getZ()-zChange+(1-playerSize)) >= (float) Math.floor(pos.getZ()+zOff)+1) {
							zCollision++;
							zCorrection = (float) Math.floor(pos.getZ()+zOff)+1 - (1-playerSize);
						}
					}
				}
			}
			
			if (zCollision > xCollision) {
				pos.setZ(zCorrection);
				velocity.setZ(0f);
			}else if (xCollision > zCollision) {
				pos.setX(xCorrection);
				velocity.setX(0f);
			}else if (zCollision != 0 && xCollision != 0){
				pos.setX(xCorrection);
				pos.setZ(zCorrection);
				velocity.setX(0f);
				velocity.setZ(0f);
			}
		}
		
		//Collision with the floor
		for (int i = 0; i < 4; i++) {
			float xOff = 0.0f;
			float zOff = 0.0f;
		
			if (i == 1 || i == 3) {
				xOff += playerSize;
			}if (i >= 2) {
				zOff += playerSize;
			}
//			int chunkX = (int) Math.floor(Math.floor(pos.getX()+xOff)/16.0);
//			int chunkZ = (int) Math.floor(Math.floor(pos.getZ()+zOff)/16.0);
			
//			ArrayList<ArrayList<ArrayList<Short>>> current = worldGen.getChunk(chunkX, chunkZ);
			
			if (worldGen.get(currentDimesnion).getBlock(pos.getX()+xOff, (float) Math.ceil(pos.getY()-2), pos.getZ()+zOff) != 0 && worldGen.get(currentDimesnion).getBlock(pos.getX()+xOff, (float) Math.ceil(pos.getY()-2), pos.getZ()+zOff) != -1) {
//			if (current.get(relX).get(relZ).size() > Math.ceil(pos.getY()-2) && Math.ceil(pos.getY()-2) >= 0 && current.get(relX).get(relZ).get((int) Math.ceil(pos.getY()-2)) != 0) {
				//For debugging, shows where collision checks are made
//				Coord2D<Integer> rel = PointConversion.absoluteToRelative(new Coord2D<Integer>((int) Math.floor(pos.getX()+xOff), (int) Math.floor(pos.getZ()+zOff)));
//				if (current.get(rel.getX()).get(rel.getZ()).get((int) Math.ceil(pos.getY()-2)) != 4) {
//					current.get(rel.getX()).get(rel.getZ()).set((int) Math.ceil(pos.getY()-2), (short) 4);
//					window.getChunk(chunkX, chunkZ).load(rel.getX(), (int) Math.ceil(pos.getY()-2), rel.getZ(), worldGen);
//				}
				if (pos.getX()-(1-playerSize) > Math.floor(pos.getX()+xOff)-1 && pos.getX()+(1-playerSize) < Math.floor(pos.getX()+xOff)+1
						&& pos.getZ()-(1-playerSize) > Math.floor(pos.getZ()+zOff)-1 && pos.getZ()+(1-playerSize) < Math.floor(pos.getZ()+zOff)+1) {
					if (Math.ceil(pos.getY()-2) <= roundFloat(pos.getY()-2-yChange)) {
						velocity.setY(0f);
						pos.setY((float) Math.ceil(pos.getY()));
						jump = true;
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Function to round a number, used to deal with precision loss in float conversions
	 * @param in Number to round
	 * @return The number rounded to 3 digits
	 */
	public float roundFloat(float in) {
		return (float) (Math.round(in*1000)/1000.0);
	}
	
	/**
	 * Function to find if a point is inside a block
	 * @param x X position to check for collision
	 * @param y Y position to check for collision
	 * @param z Z position to check for collision
	 * @param currentChunk The chunk to check for the collision
	 * @return The point relative to the chunk where the collision happened or null if it didn't collide
	 */
	public Coord<Integer> pointCollision(float x, float y, float z, ArrayList<ArrayList<ArrayList<Short>>> currentChunk) {
		float cx = (float) Math.ceil(x - 0.5);
		float cy = (float) Math.ceil(y - 0.5);
		float cz = (float) Math.ceil(z - 0.5);
		
		if (currentChunk != null) {
			int relX = (int) cx % 16;
			int relZ = (int) cz % 16;
			
			if (cx < 0) {
				relX = (int) (15 - (Math.abs(1+cx) % 16));
			}else {
				relX = (int) cx % 16;
			}
			if (cz < 0) {
				relZ = (int) (15 - (Math.abs(1+cz) % 16));
			}else {
				relZ = (int) cz % 16;
			}
			
			if (currentChunk.get(relX).get(relZ).size() > Math.floor(cy) && cy > 0 && currentChunk.get(relX).get(relZ).get((int) Math.floor(cy)) != 0){
				return new Coord<Integer>(relX, (int) Math.floor(cy), relZ);
			}
		}
		
		return null;
	}
	
	/**
	 * Function to break blocks
	 * @param window Window to break the block in
	 */
	private void breakBlock(WindowUpdates window) {
		
		float yVec = (float) -Math.sin(Math.toRadians(yRot));
		float xzComponent = (float) Math.cos(Math.toRadians(yRot));
		
		float xVec = (float) Math.sin(Math.toRadians(xRot)) * xzComponent;
		float zVec = (float) -Math.cos(Math.toRadians(xRot)) * xzComponent;
		
		float i = 0;
		
		//Current x, y, z coords of the ray
		float cx, cy, cz;
		while (i < 50) {
			cx = pos.getX() + i*xVec;
			cy = pos.getY() + i*yVec;
			cz = pos.getZ() + i*zVec;
			
			int chunkX = (int) Math.floor(cx/16.0);
			int chunkZ = (int) Math.floor(cz/16.0);
			
			
			ArrayList<ArrayList<ArrayList<Short>>> current = worldGen.get(currentDimesnion).getChunk(chunkX, chunkZ);
			Coord<Integer> collisionPoint = pointCollision(cx, cy, cz, current);
			if (collisionPoint != null) {
				if (current.get(collisionPoint.getX()).get(collisionPoint.getZ()).size() > collisionPoint.getY() && collisionPoint.getY() > 0 && current.get(collisionPoint.getX()).get(collisionPoint.getZ()).get(collisionPoint.getY()) != 0){
//					if(current.get(index))
					current.get(collisionPoint.getX()).get(collisionPoint.getZ()).set(collisionPoint.getY(), (short) 0);

					cy = collisionPoint.getY();
					if (Game.MULTIPLAYER) {
						//TODO Fix, inaccurate due to changed cx, cy, cz
						out.println(BLOCK_UPDATE+" "+(int) cx+" "+(int) cy+" "+(int) cz+" 0");
					}
					worldGen.get(currentDimesnion).editedChunks.add(new Coord2D<Integer>(chunkX, chunkZ));
					try {
//						if(worldGen.liquids.containsKey(new Coord<Integer>(collisionPoint.getX()+16*chunkX,collisionPoint.getY(),collisionPoint.getZ()+16*chunkZ))) {
//							System.out.println("Removed "+worldGen.liquids.get(new Coord<Integer>(collisionPoint.getX()+16*chunkX,collisionPoint.getY(),collisionPoint.getZ()+16*chunkZ)));
//						}
						worldGen.get(currentDimesnion).liquids.remove(new Coord<Integer>(collisionPoint.getX()+16*chunkX,collisionPoint.getY(),collisionPoint.getZ()+16*chunkZ));
						window.getChunk(chunkX, chunkZ).load(collisionPoint.getX(), collisionPoint.getY(), collisionPoint.getZ(), worldGen.get(currentDimesnion));
						// World has been modified so it must save a file
						worldGen.get(currentDimesnion).write = true;
						
						//Updating adjacent chunks if neccessary
						if ((int) Math.floor(collisionPoint.getX()) == 15) {
							window.getChunk(chunkX+1, chunkZ).renderUpdateCube(0, (int) cy, collisionPoint.getZ(), 0);
						}else if ((int) Math.floor(collisionPoint.getX()) == 0) {
							window.getChunk(chunkX-1, chunkZ).renderUpdateCube(15, (int) cy, collisionPoint.getZ(), 0);
						}
						
						if ((int) Math.floor(collisionPoint.getZ()) == 15) {
							window.getChunk(chunkX, chunkZ+1).renderUpdateCube(collisionPoint.getX(), (int) cy, 0, 0);
						}else if ((int) Math.floor(collisionPoint.getZ()) == 0) {
							window.getChunk(chunkX, chunkZ-1).renderUpdateCube(collisionPoint.getX(), (int) cy, 15, 0);
						}
						
					}catch(Exception e) {
						e.printStackTrace();
					}
					breakB = false;
					break;
				}
			}
			i += 0.004f;
		}breakB = false;
	}
	
	
	/**
	 * Function to place a block in the world where the player is looking
	 * @param window The window to place the block in
	 */
	private void placeBlock(WindowUpdates window) {
		
		float yVec = (float) -Math.sin(Math.toRadians(yRot));
		float xzComponent = (float) Math.cos(Math.toRadians(yRot));
		
		float xVec = (float) Math.sin(Math.toRadians(xRot)) * xzComponent;
		float zVec = (float) -Math.cos(Math.toRadians(xRot)) * xzComponent;
		
		float i = 0;
		
		//Current x, y, z coords of the ray
		float cx, cy, cz;
		while (i < 50) {
			cx = pos.getX() + i*xVec;
			cy = pos.getY() + i*yVec;
			cz = pos.getZ() + i*zVec;
			
			int chunkX = (int) Math.floor(cx/16.0);
			int chunkZ = (int) Math.floor(cz/16.0);
			
			ArrayList<ArrayList<ArrayList<Short>>> current = worldGen.get(currentDimesnion).getChunk(chunkX, chunkZ);
			
			Coord<Integer> collisionPoint = pointCollision(cx, cy, cz, current);
			if (collisionPoint != null) {
				if (current.get(collisionPoint.getX()).get(collisionPoint.getZ()).size() > collisionPoint.getY() && collisionPoint.getY() > 0 && current.get(collisionPoint.getX()).get(collisionPoint.getZ()).get(collisionPoint.getY()) != 0){

					i -= 0.004f * 1;
					cx = pos.getX() + i*xVec;
					cy = pos.getY() + i*yVec;
					cz = pos.getZ() + i*zVec;
					
					cx = (float) Math.ceil(cx - 0.5);
					cy = (float) Math.ceil(cy - 0.5);
					cz = (float) Math.ceil(cz - 0.5);
					
					chunkX = (int) Math.floor(cx/16.0);
					chunkZ = (int) Math.floor(cz/16.0);
					
					int relX = (int) cx % 16;
					int relZ = (int) cz % 16;
					
					if (cx < 0) {
						relX = (int) (15 - (Math.abs(1+cx) % 16));
					}else {
						relX = (int) cx % 16;
					}
					if (cz < 0) {
						relZ = (int) (15 - (Math.abs(1+cz) % 16));
					}else {
						relZ = (int) cz % 16;
					}
					
					current = worldGen.get(currentDimesnion).getChunk(chunkX, chunkZ);
					collisionPoint = new Coord<Integer>(relX, (int) cy, relZ);
					
					while (current.get(collisionPoint.getX()).get(collisionPoint.getZ()).size() <= collisionPoint.getY()) {
						current.get(collisionPoint.getX()).get(collisionPoint.getZ()).add((short) 0);
					}
					current.get(collisionPoint.getX()).get(collisionPoint.getZ()).set((int) collisionPoint.getY(), currentlyPlacing);
					
					if (Game.MULTIPLAYER) {
						out.println(BLOCK_UPDATE+" "+(int) cx+" "+(int) cy+" "+(int) cz+" 1");
					}
					worldGen.get(currentDimesnion).editedChunks.add(new Coord2D<Integer>(chunkX, chunkZ));
					try {
						cy = collisionPoint.getY();
						window.getChunk(chunkX, chunkZ).load(relX, (int) collisionPoint.getY(), relZ, worldGen.get(currentDimesnion));
						// World has been modified so it must save a file
						worldGen.get(currentDimesnion).write = true;
						
						if(currentlyPlacing == 6)
							window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(collisionPoint.getX(),collisionPoint.getY(),collisionPoint.getZ()));
//						window.getChunk(chunkX, chunkZ).load(relX, (int) cy, relZ, worldGen);
						//Updating adjacent chunks if neccessary
						if ((int) Math.floor(collisionPoint.getX()) == 15) {
							window.getChunk(chunkX+1, chunkZ).renderUpdateCube(0, (int) cy, collisionPoint.getZ(), currentlyPlacing);
						}if ((int) Math.floor(collisionPoint.getZ()) == 15) {
							window.getChunk(chunkX, chunkZ+1).renderUpdateCube(collisionPoint.getX(), (int) cy, 0, currentlyPlacing);
						}
						if ((int) Math.floor(collisionPoint.getX()) == 0) {
							window.getChunk(chunkX-1, chunkZ).renderUpdateCube(15, (int) cy, collisionPoint.getZ(), currentlyPlacing);
						}if ((int) Math.floor(collisionPoint.getZ()) == 0) {
							window.getChunk(chunkX, chunkZ-1).renderUpdateCube(collisionPoint.getX(), (int) cy, 15, currentlyPlacing);
						}
					}catch(Exception e) {
						System.out.println("Bah "+e.getMessage());
					}
					place = false;
					break;
				}
			}
			i += 0.004f;
		}place = false;
	}
	
	/**
	 * Function to get the position of the player
	 * @return A Coord object with this player's position in the world
	 */
	public Coord<Float> getPos() {
		return pos;
	}
	
	/**
	 * Getter for the players current x rotation
	 * @return The players x rotation in degrees
	 */
	public float getXRot() {
		return xRot;
	}
	
	/**
	 * Getter for the players current y rotation
	 * @return The players y rotation in degrees
	 */
	public float getYRot() {
		return yRot;
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		switch (arg0.getKeyCode()){
		case KeyEvent.VK_W:
			w = true;
			break;
		case KeyEvent.VK_S:
			s = true;
			break;
		case KeyEvent.VK_A:
			a = true;
			break;
		case KeyEvent.VK_D:
			d = true;
			break;
		case KeyEvent.VK_SPACE:
			space = true;
			break;
		case KeyEvent.VK_SHIFT:
			shift = true;
			break;
		case KeyEvent.VK_C:
			c = true;
			break;
		case KeyEvent.VK_1:
			currentlyPlacing = 1;
			break;
		case KeyEvent.VK_2:
			currentlyPlacing = 2;
			break;
		case KeyEvent.VK_3:
			currentlyPlacing = 3;
			break;
		case KeyEvent.VK_4:
			currentlyPlacing = 4;
			break;
		case KeyEvent.VK_5:
			currentlyPlacing = 5;
			break;
		case KeyEvent.VK_6:
			currentlyPlacing = 6;
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		switch (arg0.getKeyCode()){
		case KeyEvent.VK_W:
			w = false;
			break;
		case KeyEvent.VK_S:
			s = false;
			break;
		case KeyEvent.VK_A:
			a = false;
			break;
		case KeyEvent.VK_D:
			d = false;
			break;
		case KeyEvent.VK_C:
			c = false;
			break;
		case KeyEvent.VK_SPACE:
			space = false;
			break;
		case KeyEvent.VK_SHIFT:
			shift = false;
			break;
		case KeyEvent.VK_ESCAPE:
			if (pause) {
				pause = false;
			}else {
				pause = true;
			}
			break;
		case KeyEvent.VK_P:
			System.out.println("Player Position: "+pos.getX()+" "+pos.getY()+" "+pos.getZ());
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//Moved here from mouseClicked to make the clicks more consistant
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			breakB = true;
			break;
		case MouseEvent.BUTTON3:
			place = true;
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

}
