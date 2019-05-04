package brave.spring.rabbit.testfixture;

import brave.spring.rabbit.SpringDirectRabbitListenerContainerFactoryTracing;
import brave.spring.rabbit.SpringRabbitTracing;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class DirectRabbitListenerContainerFactoryConfig {

  @Bean
  public SpringDirectRabbitListenerContainerFactoryTracing springDirectRabbitListenerContainerFactoryTracing(
      SpringRabbitTracing springRabbitTracing) {
    return new SpringDirectRabbitListenerContainerFactoryTracing(springRabbitTracing);
  }

  @Bean
  public DirectRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      SpringDirectRabbitListenerContainerFactoryTracing springDirectRabbitListenerContainerFactoryTracing
  ) {
    DirectRabbitListenerContainerFactory rabbitListenerContainerFactory =
        new DirectRabbitListenerContainerFactory();
    rabbitListenerContainerFactory.setConnectionFactory(connectionFactory);
    return springDirectRabbitListenerContainerFactoryTracing.decorateDirectRabbitListenerContainerFactory(
        rabbitListenerContainerFactory
    );
  }
}
