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
	boolean w, a, s, d, space, shift, breakB;
	
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
		
		w = false; a = false; s = false; d = false; space = false; shift = false; breakB = false;
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
		
		if (breakB) {
			breakBlock(window);
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
	
	/**
	 * Function to break blocks, currently breaks them at the moment
	 * @param window Window to place the block in
	 */
	private void breakBlock(WindowUpdates window) {
		float axesOff = 0.1f;
		
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
			
			if (zVec < -axesOff) {
				cz = (float) Math.ceil(cz);
			}else if (zVec > axesOff) {
				cz = (float) Math.floor(cz);
			}else {
				cz = (float) Math.round(cz);
			}
			
			if (xVec < -axesOff) {
				cx = (float) Math.ceil(cx);
			}else if (xVec > axesOff) {
				cx = (float) Math.floor(cx);
			}else {
				cx = (float) Math.round(cx);
			}
			
			if (yVec < -axesOff) {
				cy = (float) Math.ceil(cy);
			}else if (yVec > axesOff) {
				cy = (float) Math.floor(cy);
			}else {
				cy = (float) Math.round(cy);
			}
			
			int chunkX = (int) Math.floor(cx/16.0);
			int chunkZ = (int) Math.floor(cz/16.0);
			
			ArrayList<ArrayList<ArrayList<Integer>>> current = worldGen.getChunk(chunkX, chunkZ);
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
			
				if (current.get(relX).get(relZ).size() > Math.floor(cy) && current.get(relX).get(relZ).get((int) Math.floor(cy)) != 0){
					current.get(relX).get(relZ).set((int) Math.floor(cy), 0);
					try {
						window.getChunk(chunkX, chunkZ).update(worldGen);
						//Updating adjacent chunks if neccessary
						if ((int) Math.floor(relX) == 15) {
							window.getChunk(chunkX+1, chunkZ).update(worldGen);
						}if ((int) Math.floor(relZ) == 15) {
							window.getChunk(chunkX, chunkZ+1).update(worldGen);
						}if ((int) Math.floor(relX) == 0) {
							window.getChunk(chunkX-1, chunkZ).update(worldGen);
						}if ((int) Math.floor(relZ) == 0) {
							window.getChunk(chunkX, chunkZ-1).update(worldGen);
						}
					}catch(Exception e) {
						System.out.println("Placed block out of viewing range");
					}
					breakB = false;
					break;
				}
			}
			i += 0.01f;
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
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			breakB = true;
			break;
		}
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
