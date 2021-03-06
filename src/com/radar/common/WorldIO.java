package com.radar.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.radar.client.world.Coord;
import com.radar.client.world.Coord2D;
import com.radar.client.world.Dimension;
import com.radar.client.world.generation.WorldGen;

/**
 * @author radar
 * Class to save and load the world from file
 */
public class WorldIO {
	
	/**
	 * Used to hold the seed so that the generation can know what seed the world generated with
	 */
	private int seed = -1;
	
	public WorldIO() {
		
	}
	
	/**
	 * @return The seed for the world if it was loaded
	 */
	public int getSeed() {
		return seed;
	}
	
	/**
	 * Function to save the world to a file
	 * @param world The world in its' current state (may remove, currently not used)
	 * @param editedChunks A set of all chunks that have been modified from default generation
	 * @param gen WorldGen object, used to get the chunks
	 * @param genSeed The seed this world was generated with
	 */
	public void save(HashMap<Coord2D<Integer>, ArrayList<ArrayList<ArrayList<Short>>>> world, HashSet<Coord2D<Integer>> editedChunks, WorldGen gen, int genSeed, Dimension dim) {
		System.out.println("Saving world...");
		PrintStream out;
		try {
			File saveFile = new File("dimension" + dim + ".dat");
			out = new PrintStream(saveFile);
		} catch (FileNotFoundException e) {
			System.out.println("Output file not found, world not saved");
			return;
		}
		// Saving generation seed
		out.println(genSeed);
		
		// Saving edited chunks
		for (Coord2D<Integer> pos: editedChunks) {
			out.println(pos.getX()+" "+pos.getZ());
			ArrayList<ArrayList<ArrayList<Short>>> current = gen.getChunk(pos.getX(), pos.getZ());
			
			for (int x = 0; x < 16; x++) {
				out.print(":");
				for (int z = 0; z < 16; z++) {
					out.print(";");
					for (Short i: current.get(x).get(z)) {
						out.print(i+",");
					}
				}
			}out.println();
		}
		// Saving fluid levels
		out.println();
		for (Coord<Integer> position: gen.liquids.keySet()) {
			out.println(position.getX()+" "+position.getY()+" "+position.getZ()+" "+gen.liquids.get(position));
		}
		
		out.close();
		System.out.println("World saved!");
	}
	
	/**
	 * Function to load a world from file
	 * @param filename The filename of the file to load
	 * @return A hashmap with keys as chunk locations and values as the chunk data
	 */
	public HashMap<Coord2D<Integer>, ArrayList<ArrayList<ArrayList<Short>>>> load(String filename, WorldGen gen){
		System.out.println("Loading world from file: "+filename+"...");
		Scanner in;
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			System.out.println("Input file not found, world not saved");
			return null;
		}
		try {
			HashMap<Coord2D<Integer>, ArrayList<ArrayList<ArrayList<Short>>>> out = new HashMap<>();
			if (in.hasNextLine()) {
				String seedS = in.nextLine();
				this.seed = Integer.parseInt(seedS);
			}else {
				System.out.println("Failed to load file");
				in.close();
				return out;
			}
			
			while (in.hasNextLine()) {
				String line = in.nextLine();
				
				if(line.equals("")) {
					break;
				}
				String[] coords = line.split(" ");
				
				if (coords.length < 1) {
					break;
				}
				//Creating the key for the hash map to use later
				Coord2D<Integer> loadPos = new Coord2D<Integer>(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
				
				String chunkData = in.nextLine();
				
				ArrayList<ArrayList<ArrayList<Short>>> creating = new ArrayList<ArrayList<ArrayList<Short>>>();
				String[] xArrays = chunkData.split(":");
				for (int x = 1; x < 17; x++) {
					creating.add(new ArrayList<ArrayList<Short>>());
					String[] zArrays = xArrays[x].split(";");
					for (int z = 1; z < 17; z++) {
						creating.get(x-1).add(new ArrayList<Short>());
						for (String block: zArrays[z].split(",")) {
							creating.get(x-1).get(z-1).add(Short.parseShort(block));
						}
					}
				}
				out.put(loadPos, creating);
			}
			
			while(in.hasNextLine()) {
				String line = in.nextLine();
				String[] fluidValues = line.split(" ");
				Coord<Integer> fluidPos = new Coord<>(Integer.parseInt(fluidValues[0]), Integer.parseInt(fluidValues[1]), Integer.parseInt(fluidValues[2]));
				gen.liquids.put(fluidPos, Float.parseFloat(fluidValues[3]));
			}
		
			in.close();
			System.out.println("Finished loading world from file!");
			return out;
		}catch(Exception e) {
			System.out.println("Error loading file!!!");
			return null;
		}
	}
}
