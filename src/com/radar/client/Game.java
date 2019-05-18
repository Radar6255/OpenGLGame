package com.radar.client;


import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.radar.client.window.GameWindow;
import com.radar.client.window.WindowUpdates;
import com.radar.client.world.TextureMap;

/**
 * @author radar
 * A basic 3D game to try out the basics of OpenGL
 */
public class Game extends GLCanvas{
	private static final long serialVersionUID = 1L;
	
	/**
	 * OpenGL's animator, allows me to draw using GPU
	 */
	FPSAnimator animator;
	
	/**
	 * The window where all updates are handled
	 */
	WindowUpdates window;
	
	/**
	 * Used to set up the games pieces
	 */
	public Game() {
		GameWindow gameWindow = new GameWindow(1200,800,"OpenGL Tests",this);
		
		Player player1 = new Player(0, 0, 0, 0, 0);
		this.addKeyListener(player1);
		
		animator = new FPSAnimator(this, 120);
		window = new WindowUpdates(player1, gameWindow);
		
		this.addGLEventListener(window);
		animator.start();
	}
	
	
	/**
	 * Function to stop the program
	 */
	public void stop() {
		animator.stop();
		System.out.println("Stopped animator");
	}
	
	/**
	 * Creates an instance of the game
	 * @param args Not used
	 */
	public static void main(String[] args) {
		new Game();
	}
}
