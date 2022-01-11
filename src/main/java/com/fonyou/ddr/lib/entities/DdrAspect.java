package com.fonyou.ddr.lib.entities;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

/**
 * Aspect that routes the DDR methods that trigger the end of system 
 * flows for later publication.
 */
@Aspect
public class DdrAspect {

	@AfterReturning("execution(public void com.fonyou.ddr.lib.entities.FonyouDdr.endTransaction())")
	public void interceptEndTransaction(JoinPoint joinPoint) {
		System.out.println("Entra al Aspect");
	}
	
}
