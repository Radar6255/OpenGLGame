package com.radar.client.world;

import java.util.LinkedList;

import com.radar.client.window.WindowUpdates;
import com.radar.client.world.Block.Cube;
import com.radar.client.world.Block.Fluid;
import com.radar.client.world.Block.Updateable;
import com.radar.common.PointConversion;

public class BlockUpdateHandler {
	public static int priority = 0;
	
	private LinkedList<Updateable> blocksToUpdate = new LinkedList<Updateable>();
	
	public void update(WindowUpdates window) {		
		@SuppressWarnings("unchecked")
		LinkedList<Updateable> tempUpdates = (LinkedList<Updateable>) blocksToUpdate.clone();
		blocksToUpdate.clear();
		
		tempUpdates.sort(null);
		for (Updateable cube: tempUpdates) {
			cube.update(window);

			if(cube instanceof Fluid) {
				Fluid fluid = (Fluid) cube;
				Coord<Integer> pos = fluid.getPos();
				Coord2D<Integer> chunkPos = PointConversion.findChunk(pos);
				
				//Wasn't needed, probably from when fluids faces were in chunks
//				window.getChunk(chunkPos).removeCubeFaces(fluid);
				
				Coord2D<Integer> rel = PointConversion.absoluteToRelative(pos);
				window.getChunk(chunkPos).renderUpdateCube(rel.getX(), pos.getY(), rel.getZ(), 6);
			}
		}
	}
	
	public void addUpdate(Updateable update) {
		for(Updateable test: blocksToUpdate) {
			if(test.equals(update)) {
				return;
			}
		}
		blocksToUpdate.add(update);
	}
	
	public void removeUpdates() {
		blocksToUpdate.clear();
	}
	
	public void removeCubeUpdate(Cube remove) {
		int i = 0;
		for(Updateable update: blocksToUpdate) {
			if(update instanceof Cube) {
				Cube updateCube = (Cube) update;
				if(updateCube.equals(remove)) {
					break;
				}
			}
			i++;
		}
		if(i < blocksToUpdate.size()) {
			blocksToUpdate.remove(i);
		}
	}
}
