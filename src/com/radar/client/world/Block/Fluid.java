package com.radar.client.world.Block;

import java.util.ArrayList;
import java.util.LinkedList;

import com.jogamp.opengl.GL2;
import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Coord;
import com.radar.client.world.Coord2D;
import com.radar.client.world.generation.WorldGen;
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
	
	private float[][] faceVerts;
	
	private float[][] faceNorms;
	
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
		super(x, y, z, faceTextures, gen, false);
		setHeight(height);
		adjacentFaceCull();
		super.setVerticies(verts);
		
	}
	public Fluid(int x, int y, int z, short[] faceTextures, WorldGen gen) {
		super(x, y, z, faceTextures, gen, false);
		adjacentFaceCull();
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
	
	public float[][] getFaceVerts(int startingFaceID){
		faceVerts = super.getFaceVerts(startingFaceID);
		return faceVerts;
	}
	
	//TODO Causing my problems most likely
	public LinkedList<FaceUpdateData> renderUpdate() {
		super.renderUpdate();
		getFaceVerts(0);
		getNormals();
		return null;
	}
	
	public float[][] getNormals() {
		faceNorms = super.getNormals();
		return faceNorms;
	}
	
	//TODO Speed up and sort
	public void render(GL2 gl) {
		int t = 0;
		if(faceVerts == null || faceNorms == null) {
			return;
		}
		gl.glFlush();
		gl.glBegin(GL2.GL_QUADS);
		for(int i = 0; i < 6; i++) {
			if(visibleFaces[i]) {
				gl.glTexCoord2fv(faceVerts[4*t], 3);
				gl.glNormal3fv(faceNorms[4*t], 0);
				gl.glVertex3fv(faceVerts[4*t], 0);

				gl.glTexCoord2fv(faceVerts[4*t+1], 3);
				gl.glNormal3fv(faceNorms[4*t+1], 0);
				gl.glVertex3fv(faceVerts[4*t+1], 0);

				gl.glTexCoord2fv(faceVerts[4*t+2], 3);
				gl.glNormal3fv(faceNorms[4*t+2], 0);
				gl.glVertex3fv(faceVerts[4*t+2], 0);

				gl.glTexCoord2fv(faceVerts[4*t+3], 3);
				gl.glNormal3fv(faceNorms[4*t+3], 0);
				gl.glVertex3fv(faceVerts[4*t+3], 0);
				t++;
			}
		}
		gl.glEnd();
	}
	
	public void update(WindowUpdates window) {
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
		}else if(gen.liquids.get(coords) != height) {
			if (Math.pow(gen.liquids.get(coords) - height, 2) > 0.001) {
				stable = 0;
			}
			setHeight(gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ())));
		}
		
		if (height < 0.05) {
			currentChunk.get(relX).get(relZ).set(coords.getY(), (short) 0);
			window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ, gen);
			gen.liquids.remove(coords);
			return;
		}

		LinkedList<Float> waterLevels = adjacentWaterLevels(relX, relZ, chunkX, chunkZ, currentChunk);
		byte spaceSpread = (byte) (waterLevels.get(1).byteValue() + 1);
		byte waterAdjacent = (byte) waterLevels.get(2).byteValue();
		float waterSum = waterLevels.get(0);
		
		if (coords.getY() > 1) {
			if(currentChunk.get(relX).get(relZ).get(coords.getY()-1) == 0) {
				if(!(height == 1 || spaceSpread == 1)) {
					float pullHeight = (waterSum + height - 1) / waterAdjacent;
					if(pullHeight < 0) {
						pullWater(relX, relZ, 0, chunkX, chunkZ, currentChunk, window);
						height = waterSum + height;
					}else {
						pullWater(relX, relZ, pullHeight, chunkX, chunkZ, currentChunk, window);
						height = 1;
					}
				}
				gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ()), height);
				gen.liquids.remove(coords);
			
				currentChunk.get(relX).get(relZ).set(coords.getY(), (short) 0);
				window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ, gen);
				window.removeCubeUpdate(this);
			
				currentChunk.get(relX).get(relZ).set(coords.getY()-1, (short) 6);
				window.getChunk(chunkX, chunkZ).load(relX, coords.getY()-1, relZ, gen);
				window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY()-1, relZ));
				return;
			}
			if(currentChunk.get(relX).get(relZ).get(coords.getY()-1) == 6 && gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ())) != 1) {
				if(gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ())) + height >= 1){
					float newHeight = height + gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ())) - 1.0f;
					
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ()), 1.0f);
					gen.liquids.put(coords, newHeight);
					setHeight(newHeight);
					
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY()-1, relZ));
					window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ));
					return;
				}
//				if(gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ())) + height != 1) {
					float pullHeight = (waterSum + height + gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ())) - 1) / waterAdjacent;
					
					if(pullHeight < 0) {
						pullWater(relX, relZ, 0, chunkX, chunkZ, currentChunk, window);
						height = waterSum + height;
					}else {
						pullWater(relX, relZ, pullHeight, chunkX, chunkZ, currentChunk, window);
						height = 1 - gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ()));
					}
//				}
				currentChunk.get(relX).get(relZ).set(coords.getY(), (short) 0);
				window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ, gen);
				window.removeCubeUpdate(this);
				
				gen.liquids.remove(coords);
				gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ()), height + gen.liquids.get(new Coord<Integer>(coords.getX(), coords.getY()-1, coords.getZ())));
				window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY()-1, relZ));
				return;
			}
			
		}
		
		
		if (spaceSpread == 1) {
			return;
		}
		
		float spreadHeight = roundFloat((waterSum + height) / spaceSpread);
		
		if (Math.pow(spreadHeight - height, 2) > 0.0005) {
			gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()), spreadHeight);
			setHeight(spreadHeight);
			stable = 0;
		}else {
			stable++;
		}
		
		if(stable > 3) {
			return;
		}
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
				}
			}else {
				while (currentChunk.get(relX-1).get(relZ).size() < coords.getY()) {
					currentChunk.get(relX-1).get(relZ).add((short) 0);
				}currentChunk.get(relX-1).get(relZ).add((short) 6);
				gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
				window.getChunk(chunkX, chunkZ).load(relX-1, coords.getY(), relZ, gen);
				window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX-1, coords.getY(), relZ));
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
					}
				}else {
					while (adjacentChunk.get(15).get(relZ).size() < coords.getY()) {
						adjacentChunk.get(15).get(relZ).add((short) 0);
					}adjacentChunk.get(15).get(relZ).add((short) 6);
					gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
					window.getChunk(chunkX-1, chunkZ).load(15, coords.getY(), relZ, gen);
					window.getChunk(chunkX-1, chunkZ).updateCube(new Coord<Integer>(15, coords.getY(), relZ));
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
				}
			}else {
				while (currentChunk.get(relX).get(relZ-1).size() < coords.getY()) {
					currentChunk.get(relX).get(relZ-1).add((short) 0);
				}currentChunk.get(relX).get(relZ-1).add((short) 6);
				gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
				window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ-1, gen);
				window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ-1));
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
					}
				}else {
					while (adjacentChunk.get(relX).get(15).size() < coords.getY()) {
						adjacentChunk.get(relX).get(15).add((short) 0);
					}adjacentChunk.get(relX).get(15).add((short) 6);
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
					window.getChunk(chunkX, chunkZ-1).load(relX, coords.getY(), 15, gen);
					window.getChunk(chunkX, chunkZ-1).updateCube(new Coord<Integer>(relX, coords.getY(), 15));
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
				}
			}else {
				while (currentChunk.get(relX+1).get(relZ).size() < coords.getY()) {
					currentChunk.get(relX+1).get(relZ).add((short) 0);
				}currentChunk.get(relX+1).get(relZ).add((short) 6);
				gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
				window.getChunk(chunkX, chunkZ).load(relX+1, coords.getY(), relZ, gen);
				window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX+1, coords.getY(), relZ));
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
					}
				}else {
					while (adjacentChunk.get(0).get(relZ).size() < coords.getY()) {
						adjacentChunk.get(0).get(relZ).add((short) 0);
					}adjacentChunk.get(0).get(relZ).add((short) 6);
					gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
					window.getChunk(chunkX+1, chunkZ).load(0, coords.getY(), relZ, gen);
					window.getChunk(chunkX+1, chunkZ).updateCube(new Coord<Integer>(0, coords.getY(), relZ));
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
				}
			}else {
				while (currentChunk.get(relX).get(relZ+1).size() < coords.getY()) {
					currentChunk.get(relX).get(relZ+1).add((short) 0);
				}currentChunk.get(relX).get(relZ+1).add((short) 6);
				gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
				window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ+1, gen);
				window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ+1));
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
					}
				}else {
					while (adjacentChunk.get(relX).get(0).size() < coords.getY()) {
						adjacentChunk.get(relX).get(0).add((short) 0);
					}adjacentChunk.get(relX).get(0).add((short) 6);
					gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
					window.getChunk(chunkX, chunkZ+1).load(relX, coords.getY(), 0, gen);
					window.getChunk(chunkX, chunkZ+1).updateCube(new Coord<Integer>(relX, coords.getY(), 0));
				}
			}
		}
		
//		if (stable < 1) {
//			window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ));
//		}
		
	}
	
	private void pullWater(int relX, int relZ, float spreadHeight, int chunkX, int chunkZ, ArrayList<ArrayList<ArrayList<Short>>> currentChunk, WindowUpdates window) {
		ArrayList<ArrayList<ArrayList<Short>>> adjacentChunk;
		//Changing empty blocks
		if (relX-1 >= 0) {
			if (currentChunk.get(relX-1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 6) {
					if(spreadHeight == 0) {
						currentChunk.get(relX-1).get(relZ).set(coords.getY(), (short) 0);
						window.getChunk(chunkX, chunkZ).load(relX-1, coords.getY(), relZ, gen);
						gen.liquids.remove(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()));
					} else {
						gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
						window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX-1, coords.getY(), relZ));
					}
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX-1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(15).get(relZ).size() > coords.getY()) {
					if (adjacentChunk.get(15).get(relZ).get(coords.getY()) == 6) {
						if(spreadHeight == 0) {
							adjacentChunk.get(15).get(relZ).set(coords.getY(), (short) 0);
							window.getChunk(chunkX-1, chunkZ).load(15, coords.getY(), relZ, gen);
							gen.liquids.remove(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()));
						} else {
							gen.liquids.put(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()), spreadHeight);
							window.getChunk(chunkX-1, chunkZ).updateCube(new Coord<Integer>(15, coords.getY(), relZ));
						}
					}
				}
			}
		}
		if (relZ-1 >= 0) {
			if (currentChunk.get(relX).get(relZ-1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ-1).get(coords.getY()) == 6) {
					if(spreadHeight == 0) {
						currentChunk.get(relX).get(relZ-1).set(coords.getY(), (short) 0);
						window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ-1, gen);
						gen.liquids.remove(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1));
					} else {
						gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
						window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ-1));
					}
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ-1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(15).size() > coords.getY()) {
					if (adjacentChunk.get(relX).get(15).get(coords.getY()) == 6) {
						if(spreadHeight == 0) {
							adjacentChunk.get(relX).get(15).set(coords.getY(), (short) 0);
							window.getChunk(chunkX, chunkZ-1).load(relX, coords.getY(), 15, gen);
							gen.liquids.remove(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1));
						} else {
							gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1), spreadHeight);
							window.getChunk(chunkX, chunkZ-1).updateCube(new Coord<Integer>(relX, coords.getY(), 15));
						}
					}
				}
			}
		}
		if (relX+1 < 16) {
			if (currentChunk.get(relX+1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX+1).get(relZ).get(coords.getY()) == 6) {
					if(spreadHeight == 0) {
						currentChunk.get(relX+1).get(relZ).set(coords.getY(), (short) 0);
						window.getChunk(chunkX, chunkZ).load(relX+1, coords.getY(), relZ, gen);
						gen.liquids.remove(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()));
					} else {
						gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
						window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX+1, coords.getY(), relZ));
					}
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX+1, chunkZ);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(0).get(relZ).size() > coords.getY()) {
					if (adjacentChunk.get(0).get(relZ).get(coords.getY()) == 6) {
						if(spreadHeight == 0) {
							adjacentChunk.get(0).get(relZ).set(coords.getY(), (short) 0);
							window.getChunk(chunkX+1, chunkZ).load(0, coords.getY(), relZ, gen);
							gen.liquids.remove(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()));
						} else {
							gen.liquids.put(new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ()), spreadHeight);
							window.getChunk(chunkX+1, chunkZ).updateCube(new Coord<Integer>(0, coords.getY(), relZ));
						}
					}
				}
			}
		}
		if (relZ+1 < 16) {
			if (currentChunk.get(relX).get(relZ+1).size() > coords.getY()) {
				if (currentChunk.get(relX).get(relZ+1).get(coords.getY()) == 6) {
					if(spreadHeight == 0) {
						currentChunk.get(relX).get(relZ+1).set(coords.getY(), (short) 0);
						window.getChunk(chunkX, chunkZ).load(relX, coords.getY(), relZ+1, gen);
						gen.liquids.remove(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1));
					} else {
						gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
						window.getChunk(chunkX, chunkZ).updateCube(new Coord<Integer>(relX, coords.getY(), relZ+1));
					}
				}
			}
		}else {
			adjacentChunk = gen.getChunk(chunkX, chunkZ+1);
			if (adjacentChunk.size() != 0) {
				if (adjacentChunk.get(relX).get(0).size() > coords.getY()) {
					if (adjacentChunk.get(relX).get(0).get(coords.getY()) == 6) {
						if(spreadHeight == 0) {
							adjacentChunk.get(relX).get(0).set(coords.getY(), (short) 0);
							window.getChunk(chunkX, chunkZ+1).load(relX, coords.getY(), 0, gen);
							gen.liquids.remove(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1));
						} else {
							gen.liquids.put(new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1), spreadHeight);
							window.getChunk(chunkX, chunkZ+1).updateCube(new Coord<Integer>(relX, coords.getY(), 0));
						}
					}
				}
			}
		}
	}
	
	private LinkedList<Float> adjacentWaterLevels(int relX, int relZ, int chunkX, int chunkZ, ArrayList<ArrayList<ArrayList<Short>>> currentChunk){
		ArrayList<ArrayList<ArrayList<Short>>> adjacentChunk;
		float spaceSpread = 0;
		float waterAdjacent = 0;
		float waterSum = 0;
		
		if (relX-1 >= 0) {
			if (currentChunk.get(relX-1).get(relZ).size() > coords.getY()) {
				if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 0) {
					spaceSpread++;
				}if (currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 6) {
					waterSum += gen.liquids.get(new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ()));
					spaceSpread++;
					waterAdjacent++;
				}
			}else {
				spaceSpread++;
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
						waterAdjacent++;
					}
				}else {
					spaceSpread++;
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
					waterAdjacent++;
				}
			}else {
				spaceSpread++;
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
						waterAdjacent++;
					}
				}else {
					spaceSpread++;
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
					waterAdjacent++;
				}
			}else {
				spaceSpread++;
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
						waterAdjacent++;
					}
				}else {
					spaceSpread++;
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
					waterAdjacent++;
				}
			}else {
				spaceSpread++;
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
						waterAdjacent++;
					}
				}else {
					spaceSpread++;
				}
			}
		}
		LinkedList<Float> out = new LinkedList<>();
		out.add(waterSum);
		out.add(spaceSpread);
		out.add(waterAdjacent);
		return out;
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
		return (float) (Math.floor(in*10000)/10000.0);
	}
	
	@Override
	public int getPriority() {
		return priority;
	}
	@Override
	public int compareTo(Updateable o) {
		return -o.getPriority() + this.priority;
	}
	
	/**
	 * Used to get rid of faces between cubes that won't be seen
	 */
//	protected void adjacentFaceCull() {
//		numVisibleFaces = 6;
//		int chunkX, chunkZ;
//		
//		chunkX = (int) Math.floor(coords.getX()/16.0);
//		chunkZ = (int) Math.floor(coords.getZ()/16.0);
//		
//		
//		ArrayList<ArrayList<ArrayList<Short>>> currentChunk = gen.getChunk(chunkX, chunkZ);
//		ArrayList<ArrayList<ArrayList<Short>>> adjacentChunk;
//		
//		
//		//Finding this cubes position relative to the corner of the chunk it is in
//		byte relX, relZ;
//		if (coords.getX() < 0) {
//			relX = (byte) (15 - (Math.abs(coords.getX()+1) % 16));
//		}else {
//			relX = (byte) (Math.abs(coords.getX()) % 16);
//		}
//		if (coords.getZ() < 0) {
//			relZ = (byte) (15 - (Math.abs(coords.getZ()+1) % 16));
//		}else {
//			relZ = (byte) (Math.abs(coords.getZ()) % 16);
//		}
//		
//		if (relX-1 >= 0) {
//			if (currentChunk.get(relX-1).get(relZ).size() > coords.getY()) {
//				Coord<Integer> fluidCheck = new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ());
//				if (!transparentBlockIDs.contains(currentChunk.get(relX-1).get(relZ).get(coords.getY()))) {
//					visibleFaces[3] = false;
//					numVisibleFaces--;
//				}else if(currentChunk.get(relX-1).get(relZ).get(coords.getY()) == 6 && gen.liquids.containsKey(fluidCheck) && gen.liquids.get(fluidCheck) >= height) {
//					visibleFaces[3] = false;
//					numVisibleFaces--;
//				}
//			}
//		}else {
//			adjacentChunk = gen.getChunk(chunkX-1, chunkZ);
//			if (adjacentChunk.size() != 0) {
//				if (adjacentChunk.get(15).get(relZ).size() > coords.getY()) {
//					Coord<Integer> fluidCheck = new Coord<Integer>(coords.getX()-1, coords.getY(), coords.getZ());
//					if (!transparentBlockIDs.contains(adjacentChunk.get(15).get(relZ).get(coords.getY()))) {
//						visibleFaces[3] = false;
//						numVisibleFaces--;
//					}else if(adjacentChunk.get(15).get(relZ).get(coords.getY()) == 6 && gen.liquids.containsKey(fluidCheck) && gen.liquids.get(fluidCheck) >= height) {
//						visibleFaces[3] = false;
//						numVisibleFaces--;
//					}
//				}
//			}
//		}
//		if (relZ-1 >= 0) {
//			if (currentChunk.get(relX).get(relZ-1).size() > coords.getY()) {
//				Coord<Integer> fluidCheck = new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1);
//				if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ-1).get(coords.getY()))) {
//					visibleFaces[1] = false;
//					numVisibleFaces--;
//				}else if(currentChunk.get(relX).get(relZ-1).get(coords.getY()) == 6 && gen.liquids.containsKey(fluidCheck) && gen.liquids.get(fluidCheck) >= height) {
//					visibleFaces[1] = false;
//					numVisibleFaces--;
//				}
//			}
//		}else {
//			adjacentChunk = gen.getChunk(chunkX, chunkZ-1);
//			if (adjacentChunk.size() != 0) {
//				if (adjacentChunk.get(relX).get(15).size() > coords.getY()) {
//					Coord<Integer> fluidCheck = new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()-1);
//					if (!transparentBlockIDs.contains(adjacentChunk.get(relX).get(15).get(coords.getY()))) {
//						visibleFaces[1] = false;
//						numVisibleFaces--;
//					}else if(adjacentChunk.get(relX).get(15).get(coords.getY()) == 6 && gen.liquids.containsKey(fluidCheck) && gen.liquids.get(fluidCheck) >= height) {
//						visibleFaces[1] = false;
//						numVisibleFaces--;
//					}
//				}
//			}
//		}
//		if (relX+1 < 16) {
//			if (currentChunk.get(relX+1).get(relZ).size() > coords.getY()) {
//				Coord<Integer> fluidCheck = new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ());
//				if (!transparentBlockIDs.contains(currentChunk.get(relX+1).get(relZ).get(coords.getY()))) {
//					visibleFaces[2] = false;
//					numVisibleFaces--;
//				}else if(currentChunk.get(relX+1).get(relZ).get(coords.getY()) == 6 && gen.liquids.containsKey(fluidCheck) && gen.liquids.get(fluidCheck) >= height) {
//					visibleFaces[2] = false;
//					numVisibleFaces--;
//				}
//			}
//		}else {
//			adjacentChunk = gen.getChunk(chunkX+1, chunkZ);
//			if (adjacentChunk.size() != 0) {
//				if (adjacentChunk.get(0).get(relZ).size() > coords.getY()) {
//					Coord<Integer> fluidCheck = new Coord<Integer>(coords.getX()+1, coords.getY(), coords.getZ());
//					if (!transparentBlockIDs.contains(adjacentChunk.get(0).get(relZ).get(coords.getY()))) {
//						visibleFaces[2] = false;
//						numVisibleFaces--;
//					}else if(adjacentChunk.get(0).get(relZ).get(coords.getY()) == 6 && gen.liquids.containsKey(fluidCheck) && gen.liquids.get(fluidCheck) >= height) {
//						visibleFaces[2] = false;
//						numVisibleFaces--;
//					}
//				}
//			}
//		}
//		if (relZ+1 < 16) {
//			if (currentChunk.get(relX).get(relZ+1).size() > coords.getY()) {
//				Coord<Integer> fluidCheck = new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1);
//				if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ+1).get(coords.getY()))) {
//					visibleFaces[0] = false;
//					numVisibleFaces--;
//				}else if(currentChunk.get(relX).get(relZ+1).get(coords.getY()) == 6 && gen.liquids.containsKey(fluidCheck) && gen.liquids.get(fluidCheck) >= height) {
//					visibleFaces[0] = false;
//					numVisibleFaces--;
//				}
//			}
//		}else {
//			adjacentChunk = gen.getChunk(chunkX, chunkZ+1);
//			if (adjacentChunk.size() != 0) {
//				if (adjacentChunk.get(relX).get(0).size() > coords.getY()) {
//					Coord<Integer> fluidCheck = new Coord<Integer>(coords.getX(), coords.getY(), coords.getZ()+1);
//					if (!transparentBlockIDs.contains(adjacentChunk.get(relX).get(0).get(coords.getY()))) {
//						visibleFaces[0] = false;
//						numVisibleFaces--;
//					}else if(adjacentChunk.get(relX).get(0).get(coords.getY()) == 6 && gen.liquids.containsKey(fluidCheck) && gen.liquids.get(fluidCheck) >= height) {
//						visibleFaces[0] = false;
//						numVisibleFaces--;
//					}
//				}
//			}
//		}
//		if (coords.getY()-1 >= 0) {
//			if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ).get(coords.getY()-1))) {
//				visibleFaces[5] = false;
//				numVisibleFaces--;
//			}
//		}
//		if (coords.getY()+1 < currentChunk.get(relX).get(relZ).size()) {
////			if (!transparentBlockIDs.contains(currentChunk.get(relX).get(relZ).get(coords.getY()+1)) && height == 1) {
//			if(currentChunk.get(relX).get(relZ).get(coords.getY()+1) != 0 && height == 1) {
//				visibleFaces[4] = false;
//				numVisibleFaces--;
//			}
//		}
//	}
}
