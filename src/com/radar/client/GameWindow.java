package com.radar.client;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow {

	/**
	 * @param width Desired width of the window
	 * @param height Desired height of the window
	 * @param title Title of the window
	 * @param game The instance of this game
	 */
	public GameWindow(int width, int height, String title, Game game) {
		Frame frame = new Frame(title);
		Dimension size = new Dimension(width, height);
		game.setPreferredSize(size);
//		frame.setMaximumSize(size);
//		frame.setMinimumSize(size);
		//TODO Change eventually
		frame.add(game);
		frame.pack();
		frame.setVisible(true);
		//Code to stop the program from running
		frame.addWindowListener(new WindowAdapter(){  
            public void windowClosing(WindowEvent e) {
            	System.out.println("Closing window...");
                frame.dispose();
            }
        });
//		game.setDefaultCloseOperation(game.getDefaultCloseOperation());
	}

}
