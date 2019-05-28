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
	boolean w, a, s, d, space, shift, breakB, place;
	
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
		
		w = false; a = false; s = false; d = false; space = false; shift = false; breakB = false; place = false;
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
		
		float xChange = 0; float zChange = 0;
		
		//Finding the sine and cosine for the horizontal rotation
		float sin = (float) Math.sin(Math.toRadians(xRot));
		float cos = (float) Math.cos(Math.toRadians(xRot));
		
		//Foward backward movement
		if (w) {
			pos.setX(movementSpeed*sin + pos.getX());
			xChange += movementSpeed*sin;
			pos.setZ(-movementSpeed*cos + pos.getZ());
			zChange += -movementSpeed*cos;
		}else if (s) {
			pos.setX(-movementSpeed*sin + pos.getX());
			xChange += -movementSpeed*sin;
			pos.setZ(movementSpeed*cos + pos.getZ());
			zChange += movementSpeed*cos;
		}
		
		//Left right movement
		if (a) {
			pos.setX(-movementSpeed*cos + pos.getX());
			xChange += -movementSpeed*cos;
			pos.setZ(-movementSpeed*sin + pos.getZ());
			zChange += -movementSpeed*sin;
		}else if (d) {
			pos.setX(movementSpeed*cos + pos.getX());
			xChange += movementSpeed*cos;
			pos.setZ(movementSpeed*sin + pos.getZ());
			zChange += movementSpeed*sin;
		}
		
		//Up down movement
		if(space) {
			pos.setY(movementSpeed + pos.getY());
		}else if (shift) {
			pos.setY(-movementSpeed + pos.getY());
		}
		
		pos.setY(pos.getY() - 0.1f);
		
		collision(xChange, zChange);
		
		if (breakB) {
			breakBlock(window);
		}
		if (place) {
			placeBlock(window);
		}
		
	}
	
	public void render(GL2 gl) {
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(-0.003f, 0.0f,-0.3f);
		gl.glVertex3f(0.003f, 0.0f,-0.3f);
		gl.glVertex3f(0.0f, 0.003f,-0.3f);
		gl.glVertex3f(0.0f, -0.003f,-0.3f);
		gl.glEnd();
	}
	
	public void addGen(WorldGen worldGen) {
		this.worldGen = worldGen;
	}
	
	private void collision(float xChange, float zChange) {
		float xCorrection = 0;
		float zCorrection = 0;
		int xCollision = 0;
		int zCollision = 0;
		//Collision using a 2*2
		for (int i = 0; i < 4; i++) {
			float xOff = 0.0f;
			float zOff = 0.0f;
			
			if (i == 1 || i == 3) {
				xOff += 1;
			}if (i >= 2) {
				zOff += 1;
			}
			
			int chunkX = (int) Math.floor(Math.floor(pos.getX()+xOff)/16.0);
			int chunkZ = (int) Math.floor(Math.floor(pos.getZ()+zOff)/16.0);
			
			ArrayList<ArrayList<ArrayList<Integer>>> current = worldGen.getChunk(chunkX, chunkZ);
			
			if (current == null) {
				return;
			}
			
			int relX;
			int relZ;
			
			if (pos.getX()+xOff < 0) {
				relX = (int) (15 - (Math.abs(1+Math.floor(pos.getX()+xOff)) % 16));
			}else {
				relX = (int) (Math.floor(pos.getX()+xOff) % 16);
			}
			if (pos.getZ()+zOff < 0) {
				relZ = (int) (15 - (Math.abs(1+Math.floor(pos.getZ()+zOff)) % 16));
			}else {
				relZ = (int) (Math.floor(pos.getZ()+zOff) % 16);
			}
			if (relX > 15 || relX < 0 || relZ > 15 || relZ < 0) {
				System.out.println("Horizontal "+relX+" "+relZ);
			}
//			if (current.get(relX).get(relZ).size() > Math.ceil(pos.getY()-1) && current.get(relX).get(relZ).get((int) Math.ceil(pos.getY()-1)) != 0) {
			if (current.get(relX).get(relZ).size() > Math.round(pos.getY()-1) && current.get(relX).get(relZ).get((int) Math.round(pos.getY()-1)) != 0) {
//				if ((pos.getX() > Math.floor(pos.getX()+xOff)-1 || pos.getX() < (Math.floor(pos.getX()+xOff))+1) && (pos.getZ() > Math.floor(pos.getZ()+zOff)-1 || pos.getZ() < Math.floor(pos.getZ()+zOff)+1)) {
				if (pos.getX() > Math.floor(pos.getX()+xOff)-1 && roundFloat(pos.getX()-xChange) <= (float) Math.floor(pos.getX()+xOff)-1) {
					xCollision++;
					xCorrection = (float) Math.floor(pos.getX()+xOff)-1;
				}
				
				if (pos.getX() < Math.floor(pos.getX()+xOff)+1 && roundFloat(pos.getX()-xChange) >= (float) Math.floor(pos.getX()+xOff)+1) {
					xCollision++;
					xCorrection = (float) Math.floor(pos.getX()+xOff)+1;
				}

				if (pos.getZ() > Math.floor(pos.getZ()+zOff)-1 && roundFloat(pos.getZ()-zChange) <= (float) Math.floor(pos.getZ()+zOff)-1) {
					zCollision++;
					zCorrection = (float) Math.floor(pos.getZ()+zOff)-1;
				}
				
				if (pos.getZ() < Math.floor(pos.getZ()+zOff)+1 && roundFloat(pos.getZ()-zChange) >= (float) Math.floor(pos.getZ()+zOff)+1) {
					zCollision++;
					zCorrection = (float) Math.floor(pos.getZ()+zOff)+1;
				}
			}
		}
		
		if (zCollision > xCollision) {
			pos.setZ(zCorrection);
		}else if (xCollision > zCollision) {
			pos.setX(xCorrection);
		}else if (zCollision != 0 && xCollision != 0){
			pos.setX(xCorrection);
			pos.setZ(zCorrection);
		}
		
		//Collision with the floor
		for (int i = 0; i < 4; i++) {
			float xOff = 0.5f;
			float zOff = 0.5f;
			
			if (i == 1 || i == 3) {
				xOff += 1;
			}if (i >= 2) {
				zOff += 1;
			}
			int chunkX = (int) Math.floor(Math.floor(pos.getX()+xOff)/16.0);
			int chunkZ = (int) Math.floor(Math.floor(pos.getZ()+zOff)/16.0);
			
			ArrayList<ArrayList<ArrayList<Integer>>> current = worldGen.getChunk(chunkX, chunkZ);
			
			if (current == null) {
				return;
			}
			
			int relX;
			int relZ;
			
			if (pos.getX()+xOff < 0) {
				relX = (int) (15 - (Math.abs(1+Math.floor(pos.getX()+xOff)) % 16));
			}else {
				relX = (int) (Math.floor(pos.getX()+xOff) % 16);
			}
			if (pos.getZ()+zOff < 0) {
				relZ = (int) (15 - (Math.abs(1+Math.floor(pos.getZ()+zOff)) % 16));
			}else {
				relZ = (int) (Math.floor(pos.getZ()+zOff) % 16);
			}
			if (current.get(relX).get(relZ).size() > Math.ceil(pos.getY()-2) && current.get(relX).get(relZ).get((int) Math.ceil(pos.getY()-2)) != 0) {
//				pos.setY((float) Math.ceil(pos.getY()));
				pos.setY((float) Math.round(pos.getY()));
				break;
			}
		}
	}
	
	public float roundFloat(float in) {
		return (float) (Math.round(in*1000)/1000.0);
	}
	
	/**
	 * Function to break blocks, currently breaks them at the moment
	 * @param window Window to break the block in
	 */
	private void breakBlock(WindowUpdates window) {
		float axesOff = (float) Math.PI/12.0f;
		
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
//				System.out.println("Z round "+cz+" "+Math.round(cz));
				cz = (float) Math.round(cz);
			}
			
			if (xVec < -axesOff) {
				cx = (float) Math.ceil(cx);
			}else if (xVec > axesOff) {
				cx = (float) Math.floor(cx);
			}else {
//				System.out.println("X round "+cx+" "+Math.round(cx));
				cx = (float) Math.round(cx);
			}
			if (yVec < -axesOff) {
				cy = (float) Math.ceil(cy);
			}else if (yVec > axesOff) {
				cy = (float) Math.floor(cy);
			}else {
//				System.out.println("Y round "+yVec+" "+Math.round(cy));
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
			
				if (current.get(relX).get(relZ).size() > Math.floor(cy) && cy > 0 && current.get(relX).get(relZ).get((int) Math.floor(cy)) != 0){
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
			i += 0.004f;
		}breakB = false;
	}
	
	
	/**
	 * Function to place a block in the world where the player is looking
	 * @param window The window to place the block in
	 */
	private void placeBlock(WindowUpdates window) {
		float axesOff = (float) Math.PI/12.0f;
		
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
					i -= 0.004f * 1;
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
					
					chunkX = (int) Math.floor(cx/16.0);
					chunkZ = (int) Math.floor(cz/16.0);
					
					relX = (int) cx % 16;
					relZ = (int) cz % 16;
					
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
					
					current = worldGen.getChunk(chunkX, chunkZ);
					while (current.get(relX).get(relZ).size() <= Math.floor(cy)) {
						current.get(relX).get(relZ).add(0);
					}
					current.get(relX).get(relZ).set((int) Math.floor(cy), 1);
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
