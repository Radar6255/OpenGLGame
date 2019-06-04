package com.radar.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import com.radar.common.Protocol;

public class Player implements Runnable, Protocol {
	
	/**
	 * Scanner used to get data from the player
	 */
	Scanner in;
	
	/**
	 * PrintStream used to send data to the player
	 */
	PrintStream out;
	
	/**
	 * The servers world updater, used to send block updates to all players
	 */
	WorldUpdater updater;
	
	/**
	 * Index of this player on the server
	 */
	int index;
	
	/**
	 * @param socket The socket to the client
	 * @param updater The world updater for the server
	 * @param seed The seed of the world the server is running
	 * @param index The index of this player on the server
	 */
	public Player (Socket socket, WorldUpdater updater, int seed, int index) {
		this.updater = updater;
		this.index = index;
		try {
			in = new Scanner(socket.getInputStream());
			out = new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Error when creating input or output streams for player "+index);
		}
		
		out.println(SEED+" "+seed);
	}
	
	@Override
	public void run() {
		while (in.hasNextLine()) {
			String[] input = in.nextLine().split(" ");
			switch(input[0]) {
			case(BLOCK_UPDATE):{
				System.out.println("Block update");
				int x = Integer.parseInt(input[1]);
				int y = Integer.parseInt(input[2]);
				int z = Integer.parseInt(input[3]);
				
				int blockID = Integer.parseInt(input[4]);
				
				updater.updateBlock(x, y, z, blockID, index);
				break;
			}
			
			}
		}
	}
	
	/**
	 * @param x The x position of the update
	 * @param y The y position of the update
	 * @param z The z position of the update
	 * @param blockID The new blockID for that position
	 */
	public void sendUpdate(int x, int y, int z, int blockID) {
		out.println(BLOCK_UPDATE+" "+x+" "+y+" "+z+" "+blockID);
	}

}
