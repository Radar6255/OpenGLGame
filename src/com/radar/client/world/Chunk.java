package com.radar.client.world;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.radar.client.Player;
import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Block.Block;
import com.radar.client.world.Block.Cube;
import com.radar.client.world.Block.FaceUpdateData;
import com.radar.client.world.Block.Fluid;
import com.radar.client.world.Block.Updateable;
import com.radar.client.world.generation.WorldGen;
import com.radar.common.PointConversion;

/**
 * @author radar
 * A class to represent a chunk of cubes,
 * used to make generating, loading, and unloading the world easier.
 */
public class Chunk implements Comparable<Chunk>{
	/**
	 * List of all cubes in this chunk
	 */
	private HashMap<Coord<Integer>, Cube> cubes;
	
	private HashMap<Coord<Integer>, Fluid> fluids;
	
	private HashSet<Integer> facesToRemove;
	
	private LinkedList<Cube> cubesToModify;
	
	private LinkedList<FaceUpdateData> facesToAdd;
	
	private static final int EXTRA_FACES = 30;
	
	private FloatBuffer vertexBuffer;
	
	private FloatBuffer normalBuffer;
	
	/**
	 * Used to do some buffer creation on this chunks first render call
	 */
	private boolean first = true;
	
	/**
	 * Used to regenerate the chunks faces if there was a block change in this chunk or an adjacent chunk
	 */
	private boolean update = false;
	
	/**
	 * The x, z position of the chunk in the world
	 */
	private int x, z;
	
	/**
	 * Array to hold all the data for all the faces in this chunk
	 */
	private float[] faceVerts;
	
	/**
	 * Array to hold all the data for all the face normals in this chunk
	 */
	private float[] normals;
	
	/**
	 * Holds the handle to the verticies for all the cubes in this chunk
	 */
	private int[] vertexHandle = new int[1];
	
	/**
	 * Holds the handle to the normals for all the verticies in this chunk
	 */
	private int[] normalHandle = new int[1];
	
	
	/**
	 * Holds the number of faces visible in this chunk
	 */
	private volatile int numFaces = 0;
	
	private WindowUpdates window;
	
	LinkedList<Integer> availableSpace;
	
	private int maxFaces;
	
	/**
	 * Indicies of textures for each face
	 */
	private static short[][] faceTextures = new short[][] {{1, 1, 1, 1, 3, 2}, {4, 4, 4, 4, 4, 4}, {2, 2, 2, 2, 2, 2}, {5, 5, 5, 5, 5, 5}, {6, 6, 6, 6, 7, 7}, {11, 11, 11, 11, 11, 11}};
	
	/**
	 * Creates a chunk at the specified x, z
	 * @param x The x chunk position of this chunk
	 * @param z The z chunk position of this chunk
	 */
	public Chunk(int x, int z, WorldGen gen, WindowUpdates window) {
		this.x = x;
		this.z = z;
		this.window = window;
		
		availableSpace = new LinkedList<>();
		
		facesToRemove = new HashSet<>();
		facesToAdd = new LinkedList<>();
		cubesToModify = new LinkedList<>();
		fluids = new HashMap<>();

		load(gen);
	}
	
	/**
	 * Adds the cube to the window update so that it will be drawn
	 * @param pos The position  of the cube to update
	 */
	public void updateCube(Coord<Integer> pos) {
		if(cubes.containsKey(new Coord<Integer>(pos.getX()+16*x, pos.getY(), pos.getZ()+16*z))) {
			window.addUpdate((Updateable) cubes.get(new Coord<Integer>(pos.getX()+16*x, pos.getY(), pos.getZ()+16*z)));
		}
	}

	public void removeCubeFaces(Cube removing) {
		int[] temp = removing.remove();
		boolean isVisible = true;
		
//		System.out.println(removing.getPosition().toString());
		for(int i = 0; i < facesToAdd.size(); i++) {
			if(facesToAdd.get(i).getCubePos().equals(removing.getPosition())) {
				facesToAdd.remove(i);
				cubesToModify.remove(i);
				i--;
				isVisible = false;
			}
		}
		
		if(isVisible) {
			for (int i = 0; i < temp.length; i++) {
				if(temp[i] > 0) {
					facesToRemove.add(temp[i]);
				}
			}
		}
		removing.facesNotVisible();
	}
	
	/**
	 * Called by the worldGen thread, used to do any intense processes,
	 * before first render call
	 */
	public void load(WorldGen gen) {
		//TODO Find a more precise way of finding number of blocks in a chunk or get a better guess
		cubes = new HashMap<>(100);
		
		ArrayList<ArrayList<ArrayList<Short>>> chunk = gen.getChunk(x, z);
		
		for (int tx = 0; tx < 16; tx++) {
			for (int tz = 0; tz < 16; tz++) {
				for (int ty = 0; ty < chunk.get(tx).get(tz).size(); ty++) {
					if (chunk.get(tx).get(tz).get(ty) != 0) {
						Coord<Integer> currentPos = new Coord<Integer>(x*16 + tx, ty, z*16 + tz);
						if (chunk.get(tx).get(tz).get(ty) != 6) {
							addCube(currentPos, new Block(x*16 + tx, ty, z*16 + tz, faceTextures[chunk.get(tx).get(tz).get(ty)-1], gen));
						}else {
							Fluid temp;
							
							try {
								temp = new Fluid(x*16 + tx, ty, z*16 + tz, faceTextures[chunk.get(tx).get(tz).get(ty)-1], gen.liquids.get(new Coord<Integer>(x*16 + tx, ty, z*16 + tz)), gen);
							}catch(Exception e){
								temp = new Fluid(x*16 + tx, ty, z*16 + tz, faceTextures[chunk.get(tx).get(tz).get(ty)-1], 1, gen);
							}
							fluids.put(currentPos, temp);
							addCube(currentPos, temp);
						}
					}
				}
			}
		}
		generateBufferArray();
		update = true;
	}
	
	/**
	 * Updates the visuals of the block in the chunk from a change in one position
	 * used for block placement, removal, and some updates
	 * @param x The x position of the block changed
	 * @param y The y position of the block changed
	 * @param z The z position of the block changed
	 * @param gen The world generation used to find what changed
	 */
	public void load(int x, int y, int z, WorldGen gen) {
		ArrayList<ArrayList<ArrayList<Short>>> chunk = gen.getChunk(this.x, this.z);
		Coord2D<Integer> rel = PointConversion.absoluteToRelative(new Coord2D<Integer>(x, z));
		
		if(chunk.get(rel.getX()).get(rel.getZ()).size() <= y) {
			System.out.println("Out of bounds load");
			return;
		}
		int mostRecentLoad = chunk.get(rel.getX()).get(rel.getZ()).get(y);
		if (mostRecentLoad == 0) {
			if(!cubes.containsKey(new Coord<Integer>(x+16*this.x,y,z+16*this.z))) {
				return;
			}
			if(fluids.containsKey(new Coord<Integer>(x+16*this.x,y,z+16*this.z))) {
				fluids.remove(new Coord<Integer>(x+16*this.x,y,z+16*this.z));
			}
			removeCubeFaces(cubes.get(new Coord<Integer>(x+16*this.x,y,z+16*this.z)));
			
			cubes.remove(new Coord<Integer>(x+16*this.x,y,z+16*this.z));
		}else {
			if (mostRecentLoad != 6) {
				Block block = new Block(x+16*this.x,y,z+16*this.z, faceTextures[chunk.get(x).get(z).get(y)-1], gen);
				cubes.put(new Coord<Integer>(x+16*this.x,y,z+16*this.z), block);
				block.facesNotVisible();
			}else {
				Fluid temp;
				if (gen.liquids.containsKey(new Coord<Integer>(x+16*this.x,y,z+16*this.z))) {
					temp = new Fluid(x+16*this.x,y,z+16*this.z, faceTextures[chunk.get(x).get(z).get(y)-1], gen.liquids.get(new Coord<Integer>(x+16*this.x,y,z+16*this.z)), gen);
				}else {
					temp = new Fluid(x+16*this.x,y,z+16*this.z, faceTextures[chunk.get(x).get(z).get(y)-1], 1, gen);
					gen.liquids.put(new Coord<Integer>(x+16*this.x,y,z+16*this.z), 1f);
				}
				temp.setPriority(BlockUpdateHandler.priority);
				BlockUpdateHandler.priority++;
				temp.facesNotVisible();
				addCube(new Coord<Integer>(x+16*this.x,y,z+16*this.z), temp);
				fluids.put(new Coord<Integer>(x+16*this.x, y, z+16*this.z), temp);
			}
			renderUpdateCube(rel.getX(),y,rel.getZ(), mostRecentLoad);
		}
		if (cubes.containsKey(new Coord<Integer>(x+1+16*this.x,y,z+16*this.z))) {
			renderUpdateCube(x+1, y, z, mostRecentLoad);
			
		}if (cubes.containsKey(new Coord<Integer>(x-1+16*this.x,y,z+16*this.z))) {
			renderUpdateCube(x-1, y, z, mostRecentLoad);
			
		}if (cubes.containsKey(new Coord<Integer>(x+16*this.x,y+1,z+16*this.z))) {
			renderUpdateCube(x, y+1, z, mostRecentLoad);
			
		}if (cubes.containsKey(new Coord<Integer>(x+16*this.x,y-1,z+16*this.z))) {
			renderUpdateCube(x, y-1, z, mostRecentLoad);
			
		}if (cubes.containsKey(new Coord<Integer>(x+16*this.x,y,z+1+16*this.z))) {
			renderUpdateCube(x, y, z+1, mostRecentLoad);
			
		}if (cubes.containsKey(new Coord<Integer>(x+16*this.x,y,z-1+16*this.z))) {
			renderUpdateCube(x, y, z-1, mostRecentLoad);	
		}
	}
	/**
	 * @param x The x position of the cube to update
	 * @param y The y position of the cube to update
	 * @param z The z position of the cube to update
	 * @param loadedFrom
	 */
	public void renderUpdateCube(int x, int y, int z, int loadedFrom) {
		Cube update = cubes.get(new Coord<Integer>(x + this.x*16,y,z + this.z*16));
		if(update == null) {
			return;
		}
		LinkedList<FaceUpdateData> in = update.renderUpdate();
		
		if(loadedFrom != 6 && update instanceof Fluid) {
			Fluid fluid = (Fluid) update;
			Coord2D<Integer> rel = PointConversion.absoluteToRelative(fluid.getPosition());
			updateCube(new Coord<Integer>(rel.getX(), fluid.getPosition().getY(), rel.getZ()));
		}
		if(!(update instanceof Fluid)) {
			for (FaceUpdateData curr: in) {
				if (curr.getAction() == 1) {
					cubesToModify.add(update);
					facesToAdd.add(curr);
				}else if (curr.getAction() == -1) {
					facesToRemove.add(curr.getFaceID());
					update.replaceId(curr.getFaceID());
				}
			}
		}
	}
	
	/**
	 * A function to add a cube to this chunk
	 * @param The absolute position of the cube
	 * @param cube The cube to add to the chunk
	 */
	public void addCube(Coord<Integer> pos, Cube cube) {
		cubes.put(pos, cube);
	}
	
	/**
	 * Function to draw all the cubes in this chunk
	 * @param gl The GL2 reference for drawing the cuebs
	 */
	public void render(GL2 gl) {
		//On the first render run it checks which cubes are actually visible
		//It then stores the visible cubes and gets the number of faces that are visible
		//This is to create the right size buffer
		if (first) {
			//Causes a little lag, somethings may be able to be moved around
			initBuffers(gl);
			first = false;
			update = false;
		}else {
			if (update) {
				delete(gl);
				initBuffers(gl);
				update = false;
			}
			modifyBuffer(gl);
			
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 5*Buffers.SIZEOF_FLOAT, 3*Buffers.SIZEOF_FLOAT);
			gl.glVertexPointer(3, GL2.GL_FLOAT, 5*Buffers.SIZEOF_FLOAT, 0l);
			
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalHandle[0]);
			gl.glNormalPointer(GL2.GL_FLOAT, 3*Buffers.SIZEOF_FLOAT, 0l);
			
			gl.glDrawArrays(GL2.GL_QUADS, 0, numFaces * 4);
			
			for(Fluid fluid: fluids.values()) {
				fluid.render(gl);
			}
		}
	}
	
	private void modifyBuffer (GL2 gl) {
		if(numFaces + facesToAdd.size() - facesToRemove.size() >= maxFaces) {
			maxFaces = maxFaces + EXTRA_FACES;
			FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(5 * 4 * maxFaces);
			FloatBuffer normalBuffer = Buffers.newDirectFloatBuffer(3 * 4 * maxFaces);
			
			normalBuffer.put(normals);
			normalBuffer.flip();
			vertexBuffer.put(faceVerts);
			vertexBuffer.flip();

			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 5 * (maxFaces), vertexBuffer, GL2.GL_DYNAMIC_DRAW);
			
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalHandle[0]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3 * (maxFaces), normalBuffer, GL2.GL_DYNAMIC_DRAW);
			float[] facevertscopy = new float[maxFaces*4*5];
			float[] normalscopy = new float[maxFaces*4*3];
			
			for(int i = 0; i < (maxFaces - EXTRA_FACES)*4*5; i++) {
				facevertscopy[i] = faceVerts[i];
			}
			for(int i = 0; i < (maxFaces - EXTRA_FACES)*4*3; i++) {
				normalscopy[i] = normals[i];
			}
			
			faceVerts = facevertscopy;
			normals = normalscopy;
		}
		Iterator<Integer> facesToRemoveIter = facesToRemove.iterator();
		for (int i = 0; i < facesToAdd.size(); i++) {
//			System.out.println("Added faces");
			FaceUpdateData face = facesToAdd.get(i);
			float faceverts[] = new float[20];
			float normals[] = new float[12];

			float[][] tempFaceVerts = face.getVerts();
			float[][] tempNormals = face.getNorms();

			for (int x = 0; x < tempFaceVerts.length; x++) {
				faceverts[x*5] = tempFaceVerts[x][0];
				faceverts[x*5+1] = tempFaceVerts[x][1];
				faceverts[x*5+2] = tempFaceVerts[x][2];
				faceverts[x*5+3] = tempFaceVerts[x][3];
				faceverts[x*5+4] = tempFaceVerts[x][4];

				normals[x*3] = tempNormals[x][0];
				normals[x*3+1] = tempNormals[x][1];
				normals[x*3+2] = tempNormals[x][2];
			}
			FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(5 * 4);
			FloatBuffer normalBuffer = Buffers.newDirectFloatBuffer(3 * 4);

			int replace = numFaces;
			if(facesToRemoveIter.hasNext()) {
				replace = facesToRemoveIter.next();
				numFaces--;
			}else if(!availableSpace.isEmpty()) {
				replace = availableSpace.remove();
				numFaces--;
			}
			
			cubesToModify.get(i).replaceId(face.getFaceID(), replace);
			vertexBuffer.put(faceverts);
			//vertexBuffer.flip();
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
			vertexBuffer.rewind();
			gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, replace*Buffers.SIZEOF_FLOAT*5*4, 5*4*Buffers.SIZEOF_FLOAT, vertexBuffer);
			
			for(int t = 0; t < 5*4; t++) {
				this.faceVerts[5*4*replace + t] = faceverts[t];
			}

			normalBuffer.put(normals);
			//normalBuffer.flip();
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalHandle[0]);
			normalBuffer.rewind();
			gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, replace*Buffers.SIZEOF_FLOAT*3*4, 3*4*Buffers.SIZEOF_FLOAT, normalBuffer);
			numFaces++;
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

			for(int t = 0; t < 3*4; t++) {
				this.normals[3*4*replace + t] = normals[t];
			}
		}
		cubesToModify.clear();
		facesToAdd.clear();
		
		//Probably fine
		//TODO possibly speed up loop
		while(facesToRemoveIter.hasNext()) {
//			System.out.println("Removed faces");
			int removingIndex = facesToRemoveIter.next();
			float faceverts[] = new float[20];
			float normals[] = new float[12];
			
			for (int t = 0; t < 20; t++) {
				faceverts[t] = 0;
			}for (int t = 0; t < 12; t++) {
				normals[t] = 0;
			}
			
			FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(5 * 4);
			FloatBuffer normalBuffer = Buffers.newDirectFloatBuffer(3 * 4);
			
			availableSpace.add(removingIndex);
			
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
			vertexBuffer.put(faceverts);
			vertexBuffer.flip();
			vertexBuffer.rewind();
			gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, removingIndex*Buffers.SIZEOF_FLOAT*4*5, 5*4*Buffers.SIZEOF_FLOAT, vertexBuffer);

			for(int t = 0; t < 5*4; t++) {
				this.faceVerts[5*4*removingIndex + t] = faceverts[t];
			}
			
			normalBuffer.put(normals);
			normalBuffer.flip();
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalHandle[0]);
			normalBuffer.rewind();
			gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, removingIndex*Buffers.SIZEOF_FLOAT*4*3, 3*4*Buffers.SIZEOF_FLOAT, normalBuffer);
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
			
			for(int t = 0; t < 3*4; t++) {
				this.normals[3*4*removingIndex + t] = normals[t];
			}
		}
		
		facesToRemove.clear();
	}
	
	/**
	 * Used to generate the array to be buffered on the GPU on the first call
	 */
	private void generateBufferArray() {
		LinkedList<Cube> visibleCubes = new LinkedList<>();
		Byte[] cubeFacesNums = new Byte[cubes.size()];
		int t = 0;
		numFaces = 0;
		for (Cube cube: cubes.values()) {
			if (cube.isVisible()) {
				visibleCubes.add(cube);
				numFaces += cube.getNumVisibleFaces();
				cubeFacesNums[t] = cube.getNumVisibleFaces();
				t++;
			}
		}
		
		faceVerts = new float[(numFaces+EXTRA_FACES) * 4 * 5];
		normals = new float[(numFaces+EXTRA_FACES) * 4 * 3];
		int pos = 0;
		
		for (Cube cube: visibleCubes) {
			if(cube instanceof Fluid) {
				cube.getFaceVerts(0);
				cube.getNormals();
				continue;
			}
			float[][] tempFaceVerts = cube.getFaceVerts(pos/4);
			float[][] tempNormals = cube.getNormals();
			
			for (int i = 0; i < tempFaceVerts.length; i++) {
				faceVerts[pos*5] = tempFaceVerts[i][0];
				faceVerts[(pos*5)+1] = tempFaceVerts[i][1];
				faceVerts[(pos*5)+2] = tempFaceVerts[i][2];
				faceVerts[(pos*5)+3] = tempFaceVerts[i][3];
				faceVerts[(pos*5)+4] = tempFaceVerts[i][4];
				
				normals[(pos*3)] = tempNormals[i][0];
				normals[(pos*3)+1] = tempNormals[i][1];
				normals[(pos*3)+2] = tempNormals[i][2];
				pos++;
			}
		}visibleCubes.clear();
		
		vertexBuffer = Buffers.newDirectFloatBuffer(5 * 4 * (numFaces + EXTRA_FACES));
		normalBuffer = Buffers.newDirectFloatBuffer(3 * 4 * (numFaces + EXTRA_FACES));
		maxFaces = numFaces + EXTRA_FACES;
		
		normalBuffer.put(normals);
		normalBuffer.flip();
		vertexBuffer.put(faceVerts);
		vertexBuffer.flip();
	}
	
	/**
	 * Creates the vertex and color buffers for this chunk
	 * @param gl Used to create the buffers for this chunk
	 */
	private void initBuffers(GL2 gl) {
		
		gl.glGenBuffers(1, vertexHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexHandle[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 5 * (numFaces+EXTRA_FACES), vertexBuffer, GL2.GL_DYNAMIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		
		gl.glGenBuffers(1, normalHandle, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalHandle[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 4 * 3 * (numFaces+EXTRA_FACES), normalBuffer, GL2.GL_DYNAMIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * @return This chunks x position relative to other chunks
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * @return This chunks z position relative to other chunks
	 */
	public int getZ() {
		return z;
	}
	
	/**
	 * Deletes all of the cubes buffers
	 * @param gl A new instance of openGL used to delete buffers
	 */
	public void delete(GL2 gl) {
		deleteBuffers(gl);
		cubes.clear();
	}

	public void deleteBuffers(GL2 gl) {
		gl.glDeleteBuffers(1, vertexHandle, 0);
		gl.glDeleteBuffers(1, normalHandle, 0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chunk other = (Chunk) obj;
		if (x != other.x)
			return false;
		if (z != other.z)
			return false;
		return true;
	}

	/**
	 * Gets the distance from this chunk to the point specified
	 * @param tx The x position to get the distance to
	 * @param tz The z position to get the distance to
	 * @return The distance of the corner of this chunk to the point specified
	 */
	public float distance(float tx, float tz) {
		return (float) Math.sqrt(Math.pow(x-(tx/16),2) + Math.pow(z-(tz/16),2));
	}

	@Override
	public int compareTo(Chunk o) {
		Player temp = window.getPlayer();
		float out = distance(temp.getPos().getX(), temp.getPos().getZ()) - o.distance(temp.getPos().getX(), temp.getPos().getZ());
		if(out > 0) {
			return 1;
		}
		if(out < 0) {
			return -1;
		}
		return 0;
	}
}
