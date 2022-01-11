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
		GCP_PUB_SUB, WITHOUT_IMPL;
	}
	
	/** Specific adapter name implementation to instance. */
	private MessagingImplTypes adapterImplType = MessagingImplTypes.WITHOUT_IMPL;
	
	
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
	
	public void setAdapterImplType(String adapterImplType) {
		this.adapterImplType = MessagingImplTypes.valueOf(adapterImplType);
	}
	
}
