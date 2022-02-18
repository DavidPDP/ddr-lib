package com.fonyou.ddr.lib.autoconf;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.Aspects;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.EnableLoadTimeWeaving.AspectJWeaving;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fonyou.ddr.lib.entities.DdrAspect;
import com.fonyou.ddr.lib.entities.DdrAspect.FonyouDdrMessage;
import com.google.api.core.ApiFuture;
import com.google.cloud.spring.autoconfigure.pubsub.GcpPubSubAutoConfiguration;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.outbound.PubSubMessageHandler;
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter;

/**
 * Starter for DDR projects. Provides a {@link MessagingOutboundService} via
 * auto-config. Binds properties from {@link FonyouDdrProperties}.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(GcpPubSubAutoConfiguration.class)
@EnableConfigurationProperties(FonyouDdrProperties.class)
@EnableLoadTimeWeaving(aspectjWeaving = AspectJWeaving.ENABLED)
@IntegrationComponentScan
public class MessagingAutoConfiguration {

	private static final Log LOGGER = LogFactory.getLog(MessagingAutoConfiguration.class);

	private final FonyouDdrProperties fyDdrProperties;

	public MessagingAutoConfiguration(FonyouDdrProperties fyDdrProperties) {
		this.fyDdrProperties = fyDdrProperties;
	}

	// Instantiation of Messaging adapter -------------
	@MessagingGateway(defaultRequestChannel = "pubSubOutputChannel")
	public interface MessagingOutboundGateway {
		ApiFuture<String> sendToPubSub(FonyouDdrMessage message);
	}
	
	@Bean
	public DirectChannel pubSubOutputChannel() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel = "pubSubOutputChannel")
	public MessageHandler messageSenderImpl(PubSubTemplate pubSubTemplate) {

		LOGGER.info("Messaging Implementation: " + 
				fyDdrProperties.getAdapterImplType().name());
		
		// Instantiates the port.
		switch (fyDdrProperties.getAdapterImplType()) {
			case GCP_PUB_SUB:
				return new PubSubMessageHandler(pubSubTemplate, 
					fyDdrProperties.getPubSubTopicName());
			default:
				throw new BeanInstantiationException(MessageHandler.class,
					"The adapter could not be instantiated.");
		}

		
	}
	// ------------------------------------------

	// Definition of messaging port -------------
	@Bean
	public DdrAspect ddrAspect() {
		// Merge point between the singleton created by AspectJ outside
		// the Spring context and the handling of it as a Bean for the
		// necessary dependency injections.
		DdrAspect ddrAspect = Aspects.aspectOf(DdrAspect.class);
		return ddrAspect;
	}
	// ------------------------------------------
	
	// Message serializer configuration ---------
	@Bean
	public JacksonPubSubMessageConverter jacksonPubSubMessageConverter() throws JsonProcessingException {
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		JavaTimeModule timeModule = new JavaTimeModule();
		
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(format));
		timeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(format));
		
		objectMapper.registerModule(timeModule);
		
		return new JacksonPubSubMessageConverter(objectMapper);
		
	}
	// ------------------------------------------
	
}
