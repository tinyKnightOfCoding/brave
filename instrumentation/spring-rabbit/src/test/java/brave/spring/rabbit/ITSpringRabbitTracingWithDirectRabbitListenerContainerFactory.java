package brave.spring.rabbit;

import brave.spring.rabbit.testfixture.ITSpringAmqpTracingTestFixture;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;

public class ITSpringRabbitTracingWithDirectRabbitListenerContainerFactory extends ITSpringRabbitTracingBase {

  public ITSpringRabbitTracingWithDirectRabbitListenerContainerFactory() {
    super(new ITSpringAmqpTracingTestFixture(DirectRabbitListenerContainerFactory.class));
  }
}
