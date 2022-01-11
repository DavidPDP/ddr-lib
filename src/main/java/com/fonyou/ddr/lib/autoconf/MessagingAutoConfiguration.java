package com.fonyou.ddr.lib.autoconf;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.EnableLoadTimeWeaving.AspectJWeaving;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fonyou.ddr.lib.services.MessagingOutboundService;
import com.fonyou.ddr.lib.services.MessagingOutboundService.FonyouDdrMessage;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.outbound.PubSubMessageHandler;
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter;

/**
 * Starter for DDR projects. Provides a {@link MessagingOutboundService} 
 * via auto-config. Binds properties from {@link FonyouDdrProperties}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "fonyou.ddr.lib.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FonyouDdrProperties.class)
@EnableLoadTimeWeaving(aspectjWeaving=AspectJWeaving.AUTODETECT)
@ComponentScan(basePackages = {"com.fonyou.ddr.lib.*","com.google.*"})
public class MessagingAutoConfiguration {
		
	private final FonyouDdrProperties fyDdrProperties;
	
	public MessagingAutoConfiguration(FonyouDdrProperties fyDdrProperties) {
		this.fyDdrProperties = fyDdrProperties;
	}
	
	// Instantiation of GCP adapter -------------
	@Bean
	public DirectChannel pubSubOutputChannel() {
		return new DirectChannel();
	}
	
	@Bean
	@ServiceActivator(inputChannel = "pubSubOutputChannel")
	public MessageHandler messageSenderImpl(PubSubTemplate pubSubTemplate) {
		return new PubSubMessageHandler(pubSubTemplate, fyDdrProperties.getPubSubTopicName());
	}
	// ------------------------------------------
	
	// Definition of messaging port -------------
	@MessagingGateway(defaultRequestChannel = "pubSubOutputChannel")
	public interface MessagingOutboundGateway {
		void sendToPubSub(FonyouDdrMessage message);
	}
	// ------------------------------------------
	
	// Instantiation of messaging port ----------
	@Bean
	public MessagingOutboundService messagingOutboundService(
			MessagingOutboundGateway gateway) {
		
		switch(fyDdrProperties.getAdapterImplType()) {
			case GCP_PUB_SUB:
				return new MessagingOutboundService(
					gateway, fyDdrProperties.getDestBaseTableName()
				);
			default:
				throw new BeanInstantiationException(
					MessagingOutboundService.class, 
					"The adapter could not be instantiated."
				);
		}
		
	}
	
	@Bean
	public JacksonPubSubMessageConverter jacksonPubSubMessageConverter(ObjectMapper objectMapper) {
		return new JacksonPubSubMessageConverter(objectMapper);
	}
	// ------------------------------------------

}
