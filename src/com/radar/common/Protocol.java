package com.radar.common;

public interface Protocol {
	/**
	 * Sent for a block update message formated as updateBlock x y z blockNum
	 */
	public static String BLOCK_UPDATE = "updateBlock";
	
	public static String SEED = "seed";
}
