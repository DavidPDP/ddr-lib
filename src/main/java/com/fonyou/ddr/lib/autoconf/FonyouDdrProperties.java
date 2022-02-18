package com.fonyou.ddr.lib.autoconf;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DDR top-level auto-config settings.
 */
@ConfigurationProperties("fonyou.ddr.lib")
public class FonyouDdrProperties {

	/** Base table name, without date, where the DDR will store. */
	private String destBaseTableName;
	
	/** Pub-Sub topic name where will push messages. */
	private String pubSubTopicName;
	
	/** Available adapters implementations. */
	public enum MessagingImplTypes {
		
		/** Google Cloud Platform Pub-Sub. */
		GCP_PUB_SUB,
		
		/** No implementation provided. */
		DISABLED;
		
	}
	
	/** Specific adapter name implementation to instance. */
	private MessagingImplTypes adapterImplType = MessagingImplTypes.DISABLED;

	
	public String getDestBaseTableName() {
		return destBaseTableName;
	}

	public void setDestBaseTableName(String destBaseTableName) {
		this.destBaseTableName = destBaseTableName;
	}

	public String getPubSubTopicName() {
		return pubSubTopicName;
	}

	public void setPubSubTopicName(String pubSubTopicName) {
		this.pubSubTopicName = pubSubTopicName;
	}

	public MessagingImplTypes getAdapterImplType() {
		return adapterImplType;
	}

	public void setAdapterImplType(MessagingImplTypes adapterImplType) {
		this.adapterImplType = adapterImplType;
	}
	
}
