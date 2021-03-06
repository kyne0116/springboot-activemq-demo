package com.simbest.jms;

import java.text.MessageFormat;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 
 * @author lishuyi
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.simbest")
@EnableAutoConfiguration(exclude = JmxAutoConfiguration.class)
public class ActiveMQWriterApplication {

	@Value("${jms.broker.url}")
	private String broker;

	@Value("${jms.topic.name}")
	private String topicName;

	@Value("${jms.queue.name}")
	private String queueName;

	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveMQWriterApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ActiveMQWriterApplication.class);
	}

	/**
	 * ActiveMQ implementation for connection factory. If you want to use other
	 * messaging engine,you have to implement it here. In this
	 * case,ActiveMQConnectionFactory.
	 * 
	 * @return ConnectionFactory - JMS interface
	 **/
	@Bean
	public ConnectionFactory connectionFactory() {
		LOGGER.debug("<<<<<< Loading connectionFactory");
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL(broker);
		LOGGER.debug(MessageFormat.format("{0} loaded sucesfully >>>>>>>", broker));
		return connectionFactory;
	}
	
	
	 /**
     * Catching connection factory for better performance if big load
     * @return ConnectionFactory - cachingConnection
     **/
	@Bean
	public ConnectionFactory cachingConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setTargetConnectionFactory(connectionFactory());
		connectionFactory.setSessionCacheSize(10);
		return connectionFactory;
	}

	
	/**
     * Sender configuration for topic 
     * @return JmsTemplate
     * @see JmsTemplate
     */
	@Bean(name = "jmsTemplateTopic")
	public JmsTemplate jmsTemplateTopic() {
		LOGGER.debug("<<<<<< Loading jmsTemplateTopic");
		JmsTemplate template = new JmsTemplate();
		template.setConnectionFactory(cachingConnectionFactory());
		template.setDefaultDestinationName(topicName);
		template.setPubSubDomain(true);
		LOGGER.debug("jmsTemplateTopic loaded >>>>>>>");
		return template;
	}

	
	/**
     * Sender configuration for queue 
     * @return JmsTemplate
     * @see JmsTemplate
     */
	@Bean(name = "jmsTemplateQueue")
	public JmsTemplate jmsTemplateQueue() {
		LOGGER.debug("<<<<<< Loading jmsTemplateQueue");
		JmsTemplate template = new JmsTemplate();
		template.setConnectionFactory(cachingConnectionFactory());
		template.setDefaultDestinationName(queueName);
		template.setPubSubDomain(false);
		LOGGER.debug("jmsTemplateQueue loaded >>>>>>>>");
		return template;
	}
	
	/**
	 * ThreadPool for long executions
	 * @return ThreadPoolTaskExecutor
	 * @see ThreadPoolTaskExecutor
	 */
	@Bean
	public ThreadPoolTaskExecutor executor() {
		ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
		ex.setCorePoolSize(5);
		ex.setMaxPoolSize(15);
		return ex;
	}

}