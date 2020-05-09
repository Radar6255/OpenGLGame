package com.radar.client.world.Block;

import java.util.ArrayList;

import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Coord;
import com.radar.client.world.Coord2D;
import com.radar.client.world.WorldGen;
import com.radar.common.PointConversion;

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
	
	private int priority = 0;
	
	private int stable = 0;
	
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
		setHeight(height);
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
	public void changeHeight() {
		verts = new float[][] {
			{0.5f, 0.5f, 0.5f, 1, 1}, {0.5f, -0.5f, 0.5f, 1, 0}, {-0.5f, -0.5f, 0.5f, 0, 0}, {-0.5f, 0.5f, 0.5f, 0, 1},
			{0.5f, 0.5f, -0.5f, 1, 1}, {0.5f, -0.5f, -0.5f, 1, 0}, {-0.5f, -0.5f, -0.5f, 0, 0}, {-0.5f, 0.5f, -0.5f, 0, 1},
			{0.5f, 0.5f, 0.5f, 1, 1}, {0.5f, -0.5f, 0.5f, 1, 0}, {0.5f, -0.5f, -0.5f, 0, 0}, {0.5f, 0.5f, -0.5f, 0, 1},
			{-0.5f, 0.5f, 0.5f, 1, 1}, {-0.5f, -0.5f, 0.5f, 1, 0}, {-0.5f, -0.5f, -0.5f, 0, 0}, {-0.5f, 0.5f, -0.5f, 0, 1},
			{0.5f, 0.5f, 0.5f, 0, 0}, {-0.5f, 0.5f, 0.5f, 0, 1}, {-0.5f, 0.5f, -0.5f, 1, 1}, {0.5f, 0.5f, -0.5f, 1, 0},
			{0.5f, -0.5f, 0.5f, 0, 0}, {-0.5f, -0.5f, 0.5f, 0, 1}, {-0.5f, -0.5f, -0.5f, 1, 1}, {0.5f, -0.5f, -0.5f, 1, 0}
		};
		for (int p = 0; p < 24; p++) {
			verts[p][1] = ((verts[p][1] + 0.5f)*height)-0.5f;
		}
		super.setVerticies(verts);
	}
	
	public Coord<Integer> getPos(){
		return this.coords;
	}
	
	public void setHeight(float height) {
		this.height = roundFloat(height);
		changeHeight();
	}
	
	public void update(WindowUpdates window) {
		if(stable > 3) {
			return;
		}
		byte spaceSpread = 1;
		float waterSum = 0;
		int chunkX, chunkZ;

		chunkX = (int) Math.floor(coords.getX()/16.0);
		chunkZ = (int) Math.floor(coords.getZ()/16.0);
		
		ArrayList<ArrayList<ArrayList<Short>>> currentChunk = gen.getChunk(chunkX, chunkZ);
		ArrayList<ArrayList<ArrayList<Short>>> adjacentChunk;
		
		//Finding this cubes position relative to the corner of the chunk it is in
		Coord2D<Integer> relatives = PointConversion.absoluteToRelative(coords);
		int relX = relatives.getX();
		int relZ = relatives.getZ();
		
		if (!gen.liquids.containsKey(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()))) {
			gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()), height);
		}else if(gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ())) != height) {
			setHeight(gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ())));
		}
		
		if (height < 0.03) {
			currentChunk.get(relX).get(relZ).set(coords.getY(), (short) 0);
			window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ, gen);
			return;
		}
		
		if (coords.getY() > 1) {
			if(currentChunk.get(relX).get(relZ).get(coords.getY()-1) == 0) {
				gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ()), height);
				gen.liquids.remove(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()));
				
				currentChunk.get(relX).get(relZ).set(coords.getY(), (short) 0);
				window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ, gen);
				window.removeCubeUpdate(this);
				
				currentChunk.get(relX).get(relZ).set(coords.getY()-1, (short) 6);
				window.getChunk(chunkX, chunkZ).load(relX, coords.getY()-1, relZ, gen);
				window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY()-1, relZ));
				return;
			}else if(currentChunk.get(relX).get(relZ).get(coords.getY()-1) == 6) {
				if(gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ())) + height >= 1){
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ()), 1.0f);
					
					float newHeight = height + gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ())) - 1;
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()), newHeight);
					setHeight(newHeight);
					
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY()-1, relZ));
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ));
					System.out.println("Partial fall "+height);
					return;
				}else {
					currentChunk.get(relX).get(relZ).set(coords.getY(), (short) 0);
					window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ, gen);
					window.removeCubeUpdate(this);
					
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ()), height + gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ())));
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY()-1, relZ));
					return;
				}
			}
		}
		
		if (relX-1 >= 0) {
			if (currentChunk.get(relX-1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 0) {
					spaceSpread++;
				}if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 6) {
					waterSum += gen.liquids.get(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()));
					spaceSpread++;
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
						spaceSpread++;
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
					spaceSpread++;
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
						spaceSpread++;
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
					spaceSpread++;
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
						spaceSpread++;
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
					spaceSpread++;
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
						spaceSpread++;
					}
				}
			}
		}
		
		if (spaceSpread == 1) {
			return;
		}
		float spreadHeight = roundFloat((waterSum + height) / spaceSpread);
		
		//Changing empty blocks
		if (relX-1 >= 0) {
			if (currentChunk.get(relX-1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 0) {
					gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
					currentChunk.get(relX-1).get(relZ).set(coords.getY(), (short) 6);
					window.getChunk(chunkX, chunkZ).load(relX-1, coords.getY(), relZ, gen);
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX-1, coords.getY(), relZ));
				}else if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 6) {
					gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX-1, coords.getY(), relZ));
//					window.getChunk(chunkX, chunkZ).load(relX-1, coords.getY(), relZ, gen);
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX-1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(15).get(relZ).size() > coords.getY()) {
					if (adjacentChunk.get(15).get(relZ).get(coords.getY()) == 0) {
						gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
						adjacentChunk.get(15).get(relZ).set(coords.getY(), (short) 6);
						window.getChunk(chunkX-1, chunkZ).load(15, coords.getY(), relZ, gen);
						window.getChunk(chunkX-1, chunkZ).updateCube(new Coord<Integer>(15, coords.getY(), relZ));
					}else if (adjacentChunk.get(15).get(relZ).get(coords.getY()) == 6) {
						gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
						window.getChunk(chunkX-1, chunkZ).updateCube(new Coord<Integer>(15, coords.getY(), relZ));
//						window.getChunk(chunkX-1, chunkZ).load(relX-1, coords.getY(), relZ, gen);
					}
				}
			}
		}
		if (relZ-1 >= 0) {
			if (currentChunk.get(relX).get(relZ-1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ-1).get(coords.getY()) == 0) {
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
					currentChunk.get(relX).get(relZ-1).set(coords.getY(), (short) 6);
					window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ-1, gen);
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ-1));
				}else if (currentChunk.get(relX).get(relZ-1).get(coords.getY()) == 6) {
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ-1));
//					window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ-1, gen);
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ-1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(15).size() > coords.getY()) {
					if (adjacentChunk.get(relX).get(15).get(coords.getY()) == 0) {
						gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
						adjacentChunk.get(relX).get(15).set(coords.getY(), (short) 6);
						window.getChunk(chunkX, chunkZ-1).load(relX, coords.getY(), 15, gen);
						window.getChunk(chunkX, chunkZ-1).updateCube(new Coord<Integer>(relX, coords.getY(), 15));
					}else if (adjacentChunk.get(relX).get(15).get(coords.getY()) == 6) {
						gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
						window.getChunk(chunkX, chunkZ-1).updateCube(new Coord<Integer>(relX, coords.getY(), 15));
//						window.getChunk(chunkX, chunkZ-1).load(relX, coords.getY(), relZ-1, gen);
					}
				}
			}
		}
		if (relX+1 < 16) {
			if (currentChunk.get(relX+1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX+1).get(relZ).get(coords.getY()) == 0) {
					gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
					currentChunk.get(relX+1).get(relZ).set(coords.getY(), (short) 6);
					window.getChunk(chunkX, chunkZ).load(relX+1, coords.getY(), relZ, gen);
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX+1, coords.getY(), relZ));
				}else if (currentChunk.get(relX+1).get(relZ).get(coords.getY()) == 6) {
					gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX+1, coords.getY(), relZ));
//					window.getChunk(chunkX, chunkZ).load(relX+1, coords.getY(), relZ, gen);
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX+1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(0).get(relZ).size() > coords.getY()) {
					if (adjacentChunk.get(0).get(relZ).get(coords.getY()) == 0) {
						gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
						adjacentChunk.get(0).get(relZ).set(coords.getY(), (short) 6);
						window.getChunk(chunkX+1, chunkZ).load(0, coords.getY(), relZ, gen);
						window.getChunk(chunkX+1, chunkZ).updateCube(new Coord<Integer>(0, coords.getY(), relZ));
					}else if (adjacentChunk.get(0).get(relZ).get(coords.getY()) == 6) {
						gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
						window.getChunk(chunkX+1, chunkZ).updateCube(new Coord<Integer>(0, coords.getY(), relZ));
//						window.getChunk(chunkX+1, chunkZ).load(relX+1, coords.getY(), relZ, gen);
					}
				}
			}
		}
		if (relZ+1 < 16) {
			if (currentChunk.get(relX).get(relZ+1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ+1).get(coords.getY()) == 0) {
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
					currentChunk.get(relX).get(relZ+1).set(coords.getY(), (short) 6);
					window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ+1, gen);
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ+1));
				}else if (currentChunk.get(relX).get(relZ+1).get(coords.getY()) == 6) {
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ+1));
//					window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ+1, gen);
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ+1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(0).size() > coords.getY()) {
					if (adjacentChunk.get(relX).get(0).get(coords.getY()) == 0) {
						gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
						adjacentChunk.get(relX).get(0).set(coords.getY(), (short) 6);
						window.getChunk(chunkX, chunkZ+1).load(relX, coords.getY(), 0, gen);
						window.getChunk(chunkX, chunkZ+1).updateCube(new Coord<Integer>(relX, coords.getY(), 0));
					}else if (adjacentChunk.get(relX).get(0).get(coords.getY()) == 6) {
						gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
						window.getChunk(chunkX, chunkZ+1).updateCube(new Coord<Integer>(relX, coords.getY(), 0));
//						window.getChunk(chunkX, chunkZ+1).load(relX, coords.getY(), relZ+1, gen);
					}
				}
			}
		}
//		if (coords.getY()+1 < currentChunk.get(relX).get(relZ).size()) {
//			if (currentChunk.get(relX).get(relZ).get(coords.getY()+1) == 0) {
//				spaceSpread++;
//			}
//		}
		if (Math.pow(spreadHeight - height,2) > 0.001) {
			setHeight(spreadHeight);
			stable = 0;
		}else {
			stable++;
		}
		
//		if (stable < 1) {
//			window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ));
//		}
		
		gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()), spreadHeight);
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	/**
	 * Function to round a number, used to deal with precision loss in float conversions
	 * @param in Number to round
	 * @return The number rounded to 3 digits
	 */
	public float roundFloat(float in) {
		return (float) (Math.round(in*10000)/10000.0);
	}
	
	@Override
	public int getPriority() {
		return priority;
	}
	@Override
	public int compareTo(Updateable o) {
		return -o.getPriority() + this.priority;
	}
}
