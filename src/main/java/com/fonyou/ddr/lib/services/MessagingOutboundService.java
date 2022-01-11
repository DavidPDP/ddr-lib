package com.fonyou.ddr.lib.services;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import com.fonyou.ddr.lib.autoconf.MessagingAutoConfiguration.MessagingOutboundGateway;
import com.fonyou.ddr.lib.entities.FonyouDdr;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
public class MessagingOutboundService {
	
	/** Messaging outbound port. */
	private MessagingOutboundGateway gateway;
	
	/** Base table name where will store the DDRs. */
	private String destBaseTableName;
	
	// Inner classes to wrap FonyouDdr in FonyouDdrMessage 
	@Value
	private class DigestConfiguration {
		
		private String tableName;
		
		public DigestConfiguration(String baseTableName) {
			String currentDate = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy_MM"));
			this.tableName = baseTableName + "_" + currentDate;
		}
		
	}
	
	@Value
	public class FonyouDdrMessage {
		
		private FonyouDdr content;
		
		private DigestConfiguration digestConfiguration = 
			new DigestConfiguration(destBaseTableName);
		
	}
	// ------------------------------------------
	
	/**
	 * Starts the FonyouDdr publish flow at FonyouDdrMessage 
	 * format in the PubSub component.
	 * @param ddr DDR instance with the required fields to store.
	 */
	public void publishDdr(FonyouDdr ddr) {
		FonyouDdrMessage pubSubMessage = new FonyouDdrMessage(ddr);
		gateway.sendToPubSub(pubSubMessage);
	}
	
}
