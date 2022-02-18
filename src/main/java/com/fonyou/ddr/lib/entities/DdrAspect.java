package com.fonyou.ddr.lib.entities;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

import com.fonyou.ddr.lib.autoconf.FonyouDdrProperties;
import com.fonyou.ddr.lib.autoconf.MessagingAutoConfiguration.MessagingOutboundGateway;
import com.google.api.core.ApiFuture;

/**
 * Aspect that routes the DDR methods that trigger the end of system flows for
 * later publication.
 */
@Aspect
public class DdrAspect {
	
	private MessagingOutboundGateway gateway;
	private String destinationBaseTableName;
	
	@Autowired
	public void setGateway(MessagingOutboundGateway gateway) {
		this.gateway = gateway;
	}
	
	@Autowired
	public void setDestinationBaseTableName(FonyouDdrProperties fyDdrProperties) {
		this.destinationBaseTableName = fyDdrProperties.getDestBaseTableName();
	}

	// Inner classes to wrap FonyouDdr in FonyouDdrMessage
	public class DigestConfiguration {

		private final String tableName;

		public DigestConfiguration(String baseTableName) {
			String currentDate = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy_MM"));
			this.tableName = baseTableName + "_" + currentDate;
		}
		
		public String getTableName() {
			return tableName;
		}

	}

	public class FonyouDdrMessage {

		private final FonyouDdr content;

		private final DigestConfiguration digestConfiguration = 
			new DigestConfiguration(destinationBaseTableName);

		public FonyouDdrMessage(FonyouDdr content) {
			this.content = content;
		}
		
		public FonyouDdr getContent() {
			return content;
		}
		
		public DigestConfiguration getDigestConfiguration() {
			return digestConfiguration;
		}
		
	}
	// ------------------------------------------

	@AfterReturning("execution(public void com.fonyou.ddr.lib.entities.FonyouDdr.endTransaction())")
	public void interceptEndTransaction(JoinPoint joinPoint) throws InterruptedException, ExecutionException {
		FonyouDdr ddr = (FonyouDdr) joinPoint.getTarget();
		FonyouDdrMessage pubSubMessage = new FonyouDdrMessage(ddr);
		ApiFuture<String> response = gateway.sendToPubSub(pubSubMessage);
		System.out.println(response.get());
	}

}
