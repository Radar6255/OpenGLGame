package com.radar.client;


import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.radar.client.window.GameWindow;
import com.radar.client.window.WindowUpdates;

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
	
	public static boolean MULTIPLAYER = false;
	
	/**
	 * Used to set up the games pieces
	 */
	public Game(String[] args) {
		GameWindow gameWindow = new GameWindow(1200,800,"OpenGL Tests",this);
		
		Player player1 = new Player(0, 200, 0, 0, 0);
		this.addKeyListener(player1);
		this.addMouseListener(player1);
		
		animator = new FPSAnimator(this, 120);
		window = new WindowUpdates(player1, gameWindow);
		
		if (args.length > 1) {
			MULTIPLAYER = true;
			String hostname = args[0];
			int port = Integer.parseInt(args[1]);
			try {
				Socket server = new Socket(hostname, port);
				window.addSeed(player1.addSocket(server, window));
			} catch (IOException e) {
				System.out.println("IOException when connecting to server");
			}
		}
		
		this.addGLEventListener(window);
		animator.start();
		
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		this.setCursor(blankCursor);
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
		new Game(args);
	}
}
