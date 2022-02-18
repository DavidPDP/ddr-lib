package com.fonyou.ddr.lib.entities;

/**
 * Functional interface to intercept the method that triggers the DDR publish.
 */
@FunctionalInterface
public interface FonyouDdr {
		
	/**
	 * Indicates the end of the system flow associated with
	 * the DDR for its subsequent publication in the messaging
	 * component.
	 */
	void endTransaction();
	
}
