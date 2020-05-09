package com.radar.client.world.Block;

import com.radar.client.window.WindowUpdates;

public interface Updateable extends Comparable<Updateable>{
	public abstract void update(WindowUpdates window);
	public abstract int getPriority();
}
