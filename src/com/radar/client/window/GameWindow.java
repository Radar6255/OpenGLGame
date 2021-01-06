package com.radar.client.window;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.jogamp.opengl.awt.GLCanvas;

public class GameWindow {

	/**
	 * @param width Desired width of the window
	 * @param height Desired height of the window
	 * @param title Title of the window
	 * @param game The instance of this game
	 */
	Frame frame;
	public GameWindow(int width, int height, String title, GLCanvas canvas) {
		frame = new Frame(title);
		Dimension size = new Dimension(width, height);
		canvas.setPreferredSize(size);
//		frame.setMaximumSize(size);
//		frame.setMinimumSize(size);
		//TODO Change eventually
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);
		//Code to stop the program from running
		frame.addWindowListener(new WindowAdapter(){  
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                canvas.destroy();
            	System.out.println("Window closed");
            }
        });
	}
	
	public void changeTitle(String title) {
		frame.setTitle(title);
	}

}
