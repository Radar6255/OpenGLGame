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

import com.radar.client.world.Coord;

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
	boolean w, a, s, d, space, shift;
	
	boolean pause = false;
	
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
		
		w = false; a = false; s = false; d = false; space = false; shift = false;
	}
	
	/**
	 * Function to run things that should be ticked for the player
	 */
	public void tick() {
		//Getting mouse details
		PointerInfo mouseLoc = MouseInfo.getPointerInfo();
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
			pos.setX(-movementSpeed*sin + pos.getX());
			pos.setZ(movementSpeed*cos + pos.getZ());
		}else if (s) {
			pos.setX(movementSpeed*sin + pos.getX());
			pos.setZ(-movementSpeed*cos + pos.getZ());
		}
		
		//Left right movement
		if (a) {
			pos.setX(movementSpeed*cos + pos.getX());
			pos.setZ(movementSpeed*sin + pos.getZ());
		}else if (d) {
			pos.setX(-movementSpeed*cos + pos.getX());
			pos.setZ(-movementSpeed*sin + pos.getZ());
		}
		
		//Up down movement
		if(space) {
			pos.setY(-movementSpeed + pos.getY());
		}else if (shift) {
			pos.setY(movementSpeed + pos.getY());
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
