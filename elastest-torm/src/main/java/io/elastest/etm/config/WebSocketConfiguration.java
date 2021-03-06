package io.elastest.etm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import io.elastest.etm.utils.UtilTools;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration {
	
	@Autowired
	public UtilTools utilTools;
	
	@Value("${et.etm.rabbit.host}")
	public String rabbitMqHost;
	
	@Value("${et.etm.rabbit.user}")
	public String rabbitMqUser;
	
	@Value("${et.etm.rabbit.pass}")
	public String rabbitMqPass;
	
	@Value("${et.etm.rabbit.vhost}")
	public String rabbitMqvhost;

	@Configuration
	public class WebSocketMessageBrokerConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

		@Override
		public void configureMessageBroker(MessageBrokerRegistry config) {
			config.setApplicationDestinationPrefixes("/app");
			config.enableStompBrokerRelay("/queue", "/topic", "/exchange")
					.setAutoStartup(true)
					.setClientLogin(rabbitMqUser)
					.setClientPasscode(rabbitMqPass)
					.setSystemLogin(rabbitMqUser)
					.setSystemPasscode(rabbitMqPass)
					.setRelayHost(rabbitMqHost)
					.setSystemHeartbeatReceiveInterval(5000)
					.setSystemHeartbeatSendInterval(5000)
					.setRelayPort(61613)
					.setVirtualHost(rabbitMqvhost);
		}

		@Override
		public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
			stompEndpointRegistry.addEndpoint("/rabbitMq").setHandshakeHandler(new DefaultHandshakeHandler())
			.setAllowedOrigins("*").addInterceptors(new HttpSessionHandshakeInterceptor());
		}
	}
}