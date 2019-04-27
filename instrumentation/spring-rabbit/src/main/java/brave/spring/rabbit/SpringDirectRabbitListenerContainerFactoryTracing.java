package brave.spring.rabbit;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

/**
 * Factory for Brave instrumented {@linkplain DirectRabbitListenerContainerFactory}.
 */
public class SpringDirectRabbitListenerContainerFactoryTracing {

  private final SpringRabbitTracing rabbitTracing;

  public SpringDirectRabbitListenerContainerFactoryTracing(SpringRabbitTracing rabbitTracing) {
    this.rabbitTracing = rabbitTracing;
  }

  /** Instruments an existing {@linkplain DirectRabbitListenerContainerFactory} */
  public DirectRabbitListenerContainerFactory decorateDirectRabbitListenerContainerFactory(
      DirectRabbitListenerContainerFactory factory
  ) {
    Advice[] chain = factory.getAdviceChain();
    if (chain != null) {
      factory.setAdviceChain(rabbitTracing.padWithTracingAdviceIfNotPresentYet(chain));
    } else {
      factory.setAdviceChain(new TracingRabbitListenerAdvice(rabbitTracing));
    }
    return factory;
  }

  /** Creates an instrumented {@linkplain DirectRabbitListenerContainerFactory} */
  public DirectRabbitListenerContainerFactory newDirectRabbitListenerContainerFactory(
      ConnectionFactory connectionFactory
  ) {
    DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setAdviceChain(new TracingRabbitListenerAdvice(rabbitTracing));
    return factory;
  }
}
