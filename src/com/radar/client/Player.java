package com.radar.client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.radar.client.world.Coord;

public class Player implements KeyListener{
	
	
	/**
	 * Position of the player
	 */
	private Coord<Float> pos;
	
	/**
	 * Rotation of the players view
	 */
	float xRot, yRot;
	
	boolean w, a, s, d, space, shift;
	
	private float movementSpeed = 0.2f;
	
	public Player(float x, float y, float z, float xRot, float yRot) {
		pos = new Coord<Float>(x,y,z);
		
		this.xRot = xRot;
		this.yRot = yRot;
		
		w = false; a = false; s = false; d = false; space = false; shift = false;
	}
	
	public void tick() {
		if (w) {
			pos.setZ(movementSpeed);
		}else if (s) {
			pos.setZ(-movementSpeed);
		}else pos.setZ(0f);
		
		if (a) {
			pos.setX(movementSpeed);
		}else if (d) {
			pos.setX(-movementSpeed);
		}else pos.setX(0f);
	}
	
	public Coord<Float> getPos() {
		return pos;
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
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
