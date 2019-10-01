package com.radar.client.world.Block;

import java.util.ArrayList;

import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Coord;
import com.radar.client.world.WorldGen;

public class Fluid extends Cube implements Updateable{
	
	/**
	 * Verticies of the cube
	 */
	private float[][] verts = new float[][] {
		{0.5f, 0.5f, 0.5f, 1, 1}, {0.5f, -0.5f, 0.5f, 1, 0}, {-0.5f, -0.5f, 0.5f, 0, 0}, {-0.5f, 0.5f, 0.5f, 0, 1},
		{0.5f, 0.5f, -0.5f, 1, 1}, {0.5f, -0.5f, -0.5f, 1, 0}, {-0.5f, -0.5f, -0.5f, 0, 0}, {-0.5f, 0.5f, -0.5f, 0, 1},
		{0.5f, 0.5f, 0.5f, 1, 1}, {0.5f, -0.5f, 0.5f, 1, 0}, {0.5f, -0.5f, -0.5f, 0, 0}, {0.5f, 0.5f, -0.5f, 0, 1},
		{-0.5f, 0.5f, 0.5f, 1, 1}, {-0.5f, -0.5f, 0.5f, 1, 0}, {-0.5f, -0.5f, -0.5f, 0, 0}, {-0.5f, 0.5f, -0.5f, 0, 1},
		{0.5f, 0.5f, 0.5f, 0, 0}, {-0.5f, 0.5f, 0.5f, 0, 1}, {-0.5f, 0.5f, -0.5f, 1, 1}, {0.5f, 0.5f, -0.5f, 1, 0},
		{0.5f, -0.5f, 0.5f, 0, 0}, {-0.5f, -0.5f, 0.5f, 0, 1}, {-0.5f, -0.5f, -0.5f, 1, 1}, {0.5f, -0.5f, -0.5f, 1, 0}
	};
	
	private float height = 1;
	
	/**
	 * Creates a fluid with specified height at x, y, z location
	 * 
	 * @param x The x position of the fluid
	 * @param y The y position of the fluid
	 * @param z The z position of the fluid
	 * @param faceTextures The textures on all of the faces
	 * @param height The height of the fluid at this point
	 * @param gen The world generation, used to get chunk data for liquid spread
	 */
	public Fluid(int x, int y, int z, short[] faceTextures, float height, WorldGen gen) {
		super(x, y, z, faceTextures, gen);
		this.height = height;
		changeHeight(height);
		super.setVerticies(verts);
		
	}
	public Fluid(int x, int y, int z, short[] faceTextures, WorldGen gen) {
		super(x, y, z, faceTextures, gen);
		super.setVerticies(verts);
	}
	
	/**
	 * Sets the fluid's height
	 * @param height The height to set it to
	 */
	public void changeHeight(float height) {
		for (int p = 0; p < 24; p++) {
			verts[p][1] = ((verts[p][1] + 0.5f)*height)-0.5f;
		}
	}
	
	public void update(WindowUpdates window) {
		byte spaceSpread = 1;
		float waterSum = 0;
		int chunkX, chunkZ;
		
		chunkX = (int) Math.floor(coords.getX()/16.0);
		chunkZ = (int) Math.floor(coords.getZ()/16.0);
		
		ArrayList<ArrayList<ArrayList<Short>>> currentChunk = gen.getChunk(chunkX, chunkZ);
		ArrayList<ArrayList<ArrayList<Short>>> adjacentChunk;
		
		//Finding this cubes position relative to the corner of the chunk it is in
		byte relX, relZ;
		if (coords.getX() < 0) {
			relX = (byte) (15 - (Math.abs(coords.getX()+1) % 16));
		}else {
			relX = (byte) (Math.abs(coords.getX()) % 16);
		}
		if (coords.getZ() < 0) {
			relZ = (byte) (15 - (Math.abs(coords.getZ()+1) % 16));
		}else {
			relZ = (byte) (Math.abs(coords.getZ()) % 16);
		}
		
		if (!gen.liquids.containsKey(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()))) {
			gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()), height);
		}
		
		if (coords.getY() > 1 && currentChunk.get(relX).get(relZ).get(coords.getY()-1) == 0) {
			window.getChunk(chunkX, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ()));
			gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ()), height);
			return;
		}
		
		
		if (relX-1 >= 0) {
			if (currentChunk.get(relX-1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 0) {
					spaceSpread++;
				}if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 6) {
					waterSum += gen.liquids.get(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()));
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX-1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(15).get(relZ).size() > coords.getY()) {
					if (adjacentChunk.get(15).get(relZ).get(coords.getY()) == 0) {
						spaceSpread++;
					}if (adjacentChunk.get(15).get(relZ).get(coords.getY()) == 6) {
						waterSum += gen.liquids.get(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()));
					}
				}
			}
		}
		if (relZ-1 >= 0) {
			if (currentChunk.get(relX).get(relZ-1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ-1).get(coords.getY()) == 0) {
					spaceSpread++;
				}if (currentChunk.get(relX).get(relZ-1).get(coords.getY()) == 6) {
					waterSum += gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1));
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ-1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(15).size() > coords.getY()) {
					if (adjacentChunk.get(relX).get(15).get(coords.getY()) == 0) {
						spaceSpread++;
					}if (adjacentChunk.get(relX).get(15).get(coords.getY()) == 6) {
						waterSum += gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1));
					}
				}
			}
		}
		if (relX+1 < 16) {
			if (currentChunk.get(relX+1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX+1).get(relZ).get(coords.getY()) == 0) {
					spaceSpread++;
				}if (currentChunk.get(relX+1).get(relZ).get(coords.getY()) == 6) {
					waterSum += gen.liquids.get(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()));
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX+1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(0).get(relZ).size() > coords.getY()) {
					if (adjacentChunk.get(0).get(relZ).get(coords.getY()) == 0) {
						spaceSpread++;
					}if (adjacentChunk.get(0).get(relZ).get(coords.getY()) == 6) {
						waterSum += gen.liquids.get(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()));
					}
				}
			}
		}
		if (relZ+1 < 16) {
			if (currentChunk.get(relX).get(relZ+1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ+1).get(coords.getY()) == 0) {
					spaceSpread++;
				}if (currentChunk.get(relX).get(relZ+1).get(coords.getY()) == 6) {
					waterSum += gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1));
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ+1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(0).size() > coords.getY()) {
					if (adjacentChunk.get(relX).get(0).get(coords.getY()) == 0) {
						spaceSpread++;
					}if (adjacentChunk.get(relX).get(0).get(coords.getY()) == 6) {
						waterSum += gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1));
					}
				}
			}
		}
//		if (coords.getY()+1 < currentChunk.get(relX).get(relZ).size()) {
//			if (currentChunk.get(relX).get(relZ).get(coords.getY()+1) == 0) {
//				spaceSpread++;
//			}
//		}
		if (spaceSpread == 1) {
			return;
		}
		float spreadHeight = (waterSum + height) / spaceSpread;
		
		//Changing empty blocks
		if (relX-1 >= 0) {
			if (currentChunk.get(relX-1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 0) {
					gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
					currentChunk.get(relX-1).get(relZ).set(coords.getY(), (short) 6);
					window.getChunk(chunkX, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()));
				}else if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 6) {
					gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
					window.getChunk(chunkX, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()));
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX-1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(15).get(relZ).size() > coords.getY()) {
					if (adjacentChunk.get(15).get(relZ).get(coords.getY()) == 0) {
						gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
						adjacentChunk.get(15).get(relZ).set(coords.getY(), (short) 6);
						window.getChunk(chunkX-1, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()));
					}else if (adjacentChunk.get(15).get(relZ).get(coords.getY()) == 6) {
						gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
						window.getChunk(chunkX-1, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()));
					}
				}
			}
		}
		if (relZ-1 >= 0) {
			if (currentChunk.get(relX).get(relZ-1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ-1).get(coords.getY()) == 0) {
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
					currentChunk.get(relX).get(relZ-1).set(coords.getY(), (short) 6);
					window.getChunk(chunkX, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1));
				}else if (currentChunk.get(relX).get(relZ-1).get(coords.getY()) == 6) {
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
					window.getChunk(chunkX, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1));
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ-1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(15).size() > coords.getY()) {
					if (adjacentChunk.get(relX).get(15).get(coords.getY()) == 0) {
						gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
						adjacentChunk.get(relX).get(15).set(coords.getY(), (short) 6);
						window.getChunk(chunkX, chunkZ-1).blocksToUpdate.add(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1));
					}else if (adjacentChunk.get(relX).get(15).get(coords.getY()) == 6) {
						gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
						window.getChunk(chunkX, chunkZ-1).blocksToUpdate.add(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1));
					}
				}
			}
		}
		if (relX+1 < 16) {
			if (currentChunk.get(relX+1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX+1).get(relZ).get(coords.getY()) == 0) {
					gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
					currentChunk.get(relX+1).get(relZ).set(coords.getY(), (short) 6);
					window.getChunk(chunkX, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()));
				}else if (currentChunk.get(relX+1).get(relZ).get(coords.getY()) == 6) {
					gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
					window.getChunk(chunkX, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()));
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX+1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(0).get(relZ).size() > coords.getY()) {
					if (adjacentChunk.get(0).get(relZ).get(coords.getY()) == 0) {
						gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
						adjacentChunk.get(0).get(relZ).set(coords.getY(), (short) 6);
						window.getChunk(chunkX+1, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()));
					}else if (adjacentChunk.get(0).get(relZ).get(coords.getY()) == 6) {
						gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
						window.getChunk(chunkX+1, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()));
					}
				}
			}
		}
		if (relZ+1 < 16) {
			if (currentChunk.get(relX).get(relZ+1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ+1).get(coords.getY()) == 0) {
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
					currentChunk.get(relX).get(relZ+1).set(coords.getY(), (short) 6);
					window.getChunk(chunkX, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1));
				}else if (currentChunk.get(relX).get(relZ+1).get(coords.getY()) == 6) {
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
					window.getChunk(chunkX, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1));
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ+1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(0).size() > coords.getY()) {
					if (adjacentChunk.get(relX).get(0).get(coords.getY()) == 0) {
						gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
						adjacentChunk.get(relX).get(0).set(coords.getY(), (short) 6);
						window.getChunk(chunkX, chunkZ+1).blocksToUpdate.add(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1));
					}else if (adjacentChunk.get(relX).get(0).get(coords.getY()) == 6) {
						gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
						window.getChunk(chunkX, chunkZ+1).blocksToUpdate.add(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1));
					}
				}
			}
		}
//		if (coords.getY()+1 < currentChunk.get(relX).get(relZ).size()) {
//			if (currentChunk.get(relX).get(relZ).get(coords.getY()+1) == 0) {
//				spaceSpread++;
//			}
//		}
		if (spreadHeight != height) {
			window.getChunk(chunkX, chunkZ).blocksToUpdate.add(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()));
		}
		gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()), spreadHeight);
	}
}
