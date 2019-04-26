package brave.spring.rabbit.testfixture;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CommonRabbitConfig {
  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory result = new CachingConnectionFactory();
    result.setAddresses("127.0.0.1");
    return result;
  }

  @Bean
  public Exchange exchange() {
    return ExchangeBuilder.topicExchange("test-exchange").durable(true).build();
  }

  @Bean
  public Queue queue() {
    return new Queue("test-queue");
  }

  @Bean
  public Binding binding(Exchange exchange, Queue queue) {
    return BindingBuilder.bind(queue).to(exchange).with("test.binding").noargs();
  }

  @Bean
  public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }
}
