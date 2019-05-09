package com.radar.client;

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

public class Game extends GLCanvas{
	private static final long serialVersionUID = 1L;
	
	/**
	 * OpenGL's animator, allows me to draw using GPU
	 */
	FPSAnimator animator;
	
	public Game() {
		new GameWindow(1200,800,"OpenGL Tests",this);
		
		animator = new FPSAnimator(this, 120);
		this.addGLEventListener(new WindowUpdates());
		animator.start();
	}
	
	
	/**
	 * Function to stop the program
	 */
	public void stop() {
		
	}
	
	public static void main(String[] args) {
		new Game();
	}
}
