package com.radar.tools;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.radar.client.Player;
import com.radar.client.window.GameWindow;
import com.radar.client.window.WindowUpdates;

/**
 * @author radar
 * Class to run a program to save structures so they are easier to build
 */
public class StructureBuilder extends GLCanvas{
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
	public StructureBuilder(String[] args) {
		GameWindow gameWindow = new GameWindow(1200,800,"Structure Builder",this);
		
		Player player1 = new Player(0, 30, 0, 0, 0);
		this.addKeyListener(player1);
		this.addMouseListener(player1);
		
		animator = new FPSAnimator(this, 120);
		window = new StructWindow(player1, gameWindow);
		
		this.addGLEventListener(window);
		animator.start();
		
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		this.setCursor(blankCursor);
	}
	
	
	/**
	 * Function to stop the program
	 */
	@Override
	public void destroy() {
		animator.stop();
		System.out.println("Stopped animator");
	}
	
	/**
	 * Creates an instance of the game
	 * @param args Not used
	 */
	public static void main(String[] args) {
		new StructureBuilder(args);
	}

}
