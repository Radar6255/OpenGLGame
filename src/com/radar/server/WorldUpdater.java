package com.radar.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author radar
 * Class to hold the world, used server side to control updates of the world
 */
public class WorldUpdater {
	
	public static ArrayList<Player> players;
	public static ServerSocket server;
	
	/**
	 * Starts a server for players to join on the port specified in the args
	 * @param args The server arguments formated as "port playerNum"
	 */
	public static void main (String[] args) {
		int seed = new Random().nextInt(2000);
		players = new ArrayList<>();
		WorldUpdater updater = new WorldUpdater();
		
		if (args.length < 2) {
			System.out.println("Please give arguments: port playerNum");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		
		System.out.println("Starting server on port "+port);
		
		try {
			server = new ServerSocket(port);
			
			for (int i = 0; i < Integer.parseInt(args[1]); i++) {
				Socket player = server.accept();
				players.add(new Player(player, updater, seed, i));
				new Thread(players.get(i)).start();
				System.out.println("Player joined");
			}
		} catch (IOException e) {
			System.out.println("IOException when creating server socket");
		}
	}
	
	/**
	 * Sends the block update to all players but the one who sent the update
	 * @param x The x position of the update
	 * @param y The y position of the update
	 * @param z The z position of the update
	 * @param blockID The new blockID for that position
	 * @param playerID The playerID of who sent the update
	 */
	public void updateBlock(int x, int y, int z, int blockID, int playerID) {
		for (int i = 0; i < players.size(); i++) {
			if (i != playerID) {
				players.get(i).sendUpdate(x, y, z, blockID);
			}
		}
	}
	
}
