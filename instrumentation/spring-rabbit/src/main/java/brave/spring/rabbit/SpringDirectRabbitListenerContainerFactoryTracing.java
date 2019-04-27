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

    TracingRabbitListenerAdvice tracingAdvice = new TracingRabbitListenerAdvice(rabbitTracing);
    // If there are no existing advice, return only the tracing one
    if (chain == null) {
      factory.setAdviceChain(tracingAdvice);
      return factory;
    }

    // If there is an existing tracing advice return
    for (Advice advice : chain) {
      if (advice instanceof TracingRabbitListenerAdvice) {
        return factory;
      }
    }

    // Otherwise, add ours and return
    factory.setAdviceChain(padChain(chain, tracingAdvice));
    return factory;
  }

  private Advice[] padChain(Advice[] chain, TracingRabbitListenerAdvice tracingAdvice) {
    Advice[] newChain = new Advice[chain.length + 1];
    System.arraycopy(chain, 0, newChain, 0, chain.length);
    newChain[chain.length] = tracingAdvice;
    return newChain;
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
