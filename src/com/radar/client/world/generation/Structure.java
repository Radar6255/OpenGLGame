package com.radar.client.world.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import com.radar.client.world.Coord;
import com.radar.client.world.Coord2D;
import com.radar.client.world.Dimension;
import com.radar.common.PointConversion;
import com.radar.tools.StructGen;

public class Structure {
	/**
	 * The width of the structure, x axis
	 */
	int width;
	
	/**
	 * The height of the structure, y axis
	 */
	int height;
	
	/**
	 * The depth of the structure, z axis
	 */
	int depth;
	
	/**
	 * The structure data
	 */
	ArrayList<ArrayList<ArrayList<Short>>> data;
	
	public Structure(int w, int h, int d) {
		this.width = w;
		this.height = h;
		this.depth = d;
	}
	
	public void setData(ArrayList<ArrayList<ArrayList<Short>>> data) {
		this.data = data;
	}
	
	public static Structure loadStructure(String filename) {
		System.out.println("Loading structure from file: "+filename+"...");
		Scanner in;
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			System.out.println("Input file not found, world not saved");
			return null;
		}
		
		try {
//			HashMap<Coord2D<Integer>, ArrayList<ArrayList<ArrayList<Short>>>> out = new HashMap<>();
			if (!in.hasNextLine()) {
				System.out.println("Failed to load structure");
				in.close();
				return null;
			}

			String chunkData = in.nextLine();
			int maxHeight = 0;

			ArrayList<ArrayList<ArrayList<Short>>> creating = new ArrayList<ArrayList<ArrayList<Short>>>();
			String[] xArrays = chunkData.split(":");
			for (int x = 1; x < xArrays.length; x++) {
				creating.add(new ArrayList<ArrayList<Short>>());
				String[] zArrays = xArrays[x].split(";");
				for (int z = 1; z < zArrays.length; z++) {
					creating.get(x-1).add(new ArrayList<Short>());
					int y = 0;
					for (String block: zArrays[z].split(",")) {
						if(!block.equals("")) {
							creating.get(x-1).get(z-1).add(Short.parseShort(block));
						}
						y++;
					}
					
					if(y > maxHeight) {
						maxHeight = y;
					}
				}
			}
			

			Structure out = new Structure(creating.size(), maxHeight, creating.get(0).size());
			out.setData(creating);
			System.out.println(creating);
			
			in.close();
			System.out.println("Finished loading structure from file!");
			return out;
		}catch(Exception e) {
			System.out.println("Error loading file!!!");
			e.printStackTrace();
			return null;
		}
	}
	
	public static void saveStructure(StructGen gen, String filename) {
		Coord<Integer> bottom = gen.getBottomCorner();
		Coord<Integer> top = gen.getTopCorner();
		
		if(bottom == null) {
			return;
		}
		
		System.out.println("Saving structure as "+filename+".dat");
		PrintStream out;
		try {
			File saveFile = new File(filename + ".dat");
			out = new PrintStream(saveFile);
		} catch (FileNotFoundException e) {
			System.out.println("Output file not found, structure not saved");
			return;
		}
		
		//TODO Make it so it supports structures larger than one chunk
		ArrayList<ArrayList<ArrayList<Short>>> current = gen.getChunk(0, 0);
		
		System.out.println(bottom.toString() + ", top: "+top.toString());
		
		for (int x = bottom.getX(); x < top.getX()+1; x++) {
			out.print(":");
			for (int z = bottom.getZ(); z < top.getZ()+1; z++) {
				out.print(";");
				
				int y = 0;
				for (Short i: current.get(x).get(z)) {
					if(y > top.getY()) {
						break;
					}
					if(y >= bottom.getY()) {
						out.print(i+",");
					}
					y++;
				}
			}
		}
		
		out.close();
		System.out.println("Structure saved!");
	}
	
	/**
	 * Places a structure inside a world at the specified location.
	 * Doesn't replace blocks or air
	 * 
	 * @param pos Location of where the bottom left corner of the structure should go
	 * @param gen The world generation to put the structure into
	 * @param dim
	 */
	public void placeStructure(Coord<Integer> pos, Generation gen, Dimension dim) {
		
		
		for(int x = 0; x < width; x++) {
			for(int z = 0; z < depth; z++) {
				Coord2D<Integer> chunkPos = PointConversion.findChunk(pos);
				ArrayList<ArrayList<ArrayList<Short>>> chunk = gen.getChunk(chunkPos);
				
				
			}
		}
	}
}
