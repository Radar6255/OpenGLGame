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
		
		Player player1 = new Player(0, 0, 0, 0, 0);
		this.addKeyListener(player1);
		
		animator = new FPSAnimator(this, 120);
		this.addGLEventListener(new WindowUpdates(player1));
		animator.start();
	}
	
	
	/**
	 * Function to stop the program
	 */
	public void stop() {
		animator.stop();
		System.out.println("Stopped animator");
	}
	
	public static void main(String[] args) {
		new Game();
	}
}
