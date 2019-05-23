package com.radar.client;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Coord;
import com.radar.client.world.WorldGen;

public class Player implements KeyListener, MouseListener{
	
	
	/**
	 * Position of the player
	 */
	private Coord<Float> pos;
	
	/**
	 * Rotation of the players view
	 */
	float xRot = 0, yRot = 0;
	
	/**
	 * Booleans to keep track of what buttons are currently pressed
	 */
	boolean w, a, s, d, space, shift, place;
	
	boolean pause = false;
	
	private WorldGen worldGen;
	
	/**
	 * The players movement speed amount
	 */
	private float movementSpeed = PlayerSettings.defaultMovementSpeed;
	
	/**
	 * Center coordinates of the players screen
	 */
	int centerX = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
	int centerY = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
	
	public Player(float x, float y, float z, float xRot, float yRot) {
		pos = new Coord<Float>(x,y,z);
		
		this.xRot = xRot;
		this.yRot = yRot;
		
		w = false; a = false; s = false; d = false; space = false; shift = false; place = false;
	}
	
	/**
	 * Function to run things that should be ticked for the player
	 */
	public void tick(WindowUpdates window) {
		//Getting mouse details
		PointerInfo mouseLoc = MouseInfo.getPointerInfo();
		//TODO Sometimes throws errors when alt-tabbing
		Point tempPoint = mouseLoc.getLocation();
		
		float x = (float) tempPoint.getX();
		float y = (float) tempPoint.getY();

		if (!pause) {
			//Finding the mouse movement and changing rotation
			xRot = xRot + (x-centerX)/PlayerSettings.mouseSensitivity;
			yRot = yRot + (y-centerY)/PlayerSettings.mouseSensitivity;
		
			try {
				new Robot().mouseMove(centerX, centerY);
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
		
		//Finding the sine and cosine for the horizontal rotation
		float sin = (float) Math.sin(Math.toRadians(xRot));
		float cos = (float) Math.cos(Math.toRadians(xRot));
		
		//Foward backward movement
		if (w) {
			pos.setX(movementSpeed*sin + pos.getX());
			pos.setZ(-movementSpeed*cos + pos.getZ());
		}else if (s) {
			pos.setX(-movementSpeed*sin + pos.getX());
			pos.setZ(movementSpeed*cos + pos.getZ());
		}
		
		//Left right movement
		if (a) {
			pos.setX(-movementSpeed*cos + pos.getX());
			pos.setZ(-movementSpeed*sin + pos.getZ());
		}else if (d) {
			pos.setX(movementSpeed*cos + pos.getX());
			pos.setZ(movementSpeed*sin + pos.getZ());
		}
		
		//Up down movement
		if(space) {
			pos.setY(movementSpeed + pos.getY());
		}else if (shift) {
			pos.setY(-movementSpeed + pos.getY());
		}
		
		if (place) {
			placeBlock(window);
		}
		
	}
	
	public void render(GL2 gl) {
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(-0.01f, 0.0f,-1.1f);
		gl.glVertex3f(0.01f, 0.0f,-1.1f);
		gl.glVertex3f(0.0f, 0.01f,-1.1f);
		gl.glVertex3f(0.0f, -0.01f,-1.1f);
		gl.glEnd();
	}
	
	public void addGen(WorldGen worldGen) {
		this.worldGen = worldGen;
	}
	
	private void placeBlock(WindowUpdates window) {
		float xVec = (float) Math.sin(Math.toRadians(xRot));
		float yVec = (float) -Math.sin(Math.toRadians(yRot));
		float zVec = (float) -Math.cos(Math.toRadians(xRot));
		
		float i = 0;
		int chunkX = (int) Math.floor((pos.getX()+(i*xVec))/16);
		int chunkZ = (int) Math.floor((pos.getZ()+(i*zVec))/16);
		ArrayList<ArrayList<ArrayList<Integer>>> current = worldGen.getChunk(chunkX, chunkZ);
		
		while (current != null && !current.isEmpty()) {
			float relX, relY, relZ;
			if (pos.getX()+(i*xVec) < 0) {
				relX = (15 - (Math.abs(pos.getX()+i*xVec+1) % 16));
			}else {
				relX = Math.abs(pos.getX()+i*xVec) % 16;
			}
			if (pos.getZ()+(i*zVec) < 0) {
				relZ =  (15 - (Math.abs(pos.getZ()+i*zVec+1) % 16));
			}else {
				relZ = Math.abs(pos.getZ()+i*zVec) % 16;
			}
			relY = (pos.getY()+yVec*i);
			
//			int relX, relY, relZ;
//			if (pos.getX()+(i*xVec) < 0) {
//				relX = (int) (15 - (Math.abs(Math.round(pos.getX()+i*xVec)+1) % 16));
//			}else {
//				relX = (int) Math.round(Math.abs(pos.getX()+i*xVec) % 16);
//			}
//			if (pos.getZ()+(i*zVec) < 0) {
//				relZ = (int) (15 - (Math.abs(Math.round(pos.getZ()+i*zVec)+1) % 16));
//			}else {
//				relZ = (int) Math.round(Math.abs(pos.getZ()+i*zVec) % 16);
//			}
//			relY = (int) (pos.getY()+yVec*i);
			
			if (current.get((int) Math.floor(relX)).get((int) Math.floor(relZ)).size() > relY && relY >= 0) {
				if (current.get((int) Math.floor(relX)).get((int) Math.floor(relZ)).get((int) relY) != 0) {
					current.get((int) Math.floor(relX)).get((int) Math.floor(relZ)).set((int) relY, 0);
					try {
						window.getChunk(chunkX, chunkZ).update(worldGen);
					}catch(Exception e) {
						System.out.println("Placed block out of viewing range");
					}
					break;
				}
			}if (current.get((int) Math.ceil(relX)).get((int) Math.floor(relZ)).size() > relY && relY >= 0) {
				if (current.get((int) Math.ceil(relX)).get((int) Math.floor(relZ)).get((int) relY) != 0) {
					current.get((int) Math.ceil(relX)).get((int) Math.floor(relZ)).set((int) relY, 0);
					try {
						window.getChunk(chunkX, chunkZ).update(worldGen);
					}catch(Exception e) {
						System.out.println("Placed block out of viewing range");
					}
					break;
				}
			}if (current.get((int) Math.floor(relX)).get((int) Math.ceil(relZ)).size() > relY && relY >= 0) {
				if (current.get((int) Math.floor(relX)).get((int) Math.ceil(relZ)).get((int) relY) != 0) {
					current.get((int) Math.floor(relX)).get((int) Math.ceil(relZ)).set((int) relY, 0);
					try {
						window.getChunk(chunkX, chunkZ).update(worldGen);
					}catch(Exception e) {
						System.out.println("Placed block out of viewing range");
					}
					break;
				}
			}if (current.get((int) Math.ceil(relX)).get((int) Math.ceil(relZ)).size() > relY && relY >= 0) {
				if (current.get((int) Math.ceil(relX)).get((int) Math.ceil(relZ)).get((int) relY) != 0) {
					current.get((int) Math.ceil(relX)).get((int) Math.ceil(relZ)).set((int) relY, 0);
					try {
						window.getChunk(chunkX, chunkZ).update(worldGen);
					}catch(Exception e) {
						System.out.println("Placed block out of viewing range");
					}
					break;
				}
			}
			
//			if (current.get(relX).get(relZ).size() > relY && relY >= 0) {
//				if (current.get(relX).get(relZ).get(relY) != 0) {
//					current.get(relX).get(relZ).set(relY, 0);
//					try {
//						window.getChunk(chunkX, chunkZ).update(worldGen);
//					}catch(Exception e) {
//						System.out.println("Placed block out of viewing range");
//					}
//					break;
//				}
//				
//			}
			
			i+=0.1f;
			chunkX = (int) Math.floor((pos.getX()+(i*xVec))/16);
			chunkZ = (int) Math.floor((pos.getZ()+(i*zVec))/16);
			
			current = worldGen.getChunk(chunkX, chunkZ);
		}
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
		case KeyEvent.VK_P:
			place = true;
			break;
		case KeyEvent.VK_SPACE:
			space = true;
			break;
		case KeyEvent.VK_SHIFT:
			shift = true;
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
		case KeyEvent.VK_P:
			place = false;
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
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
