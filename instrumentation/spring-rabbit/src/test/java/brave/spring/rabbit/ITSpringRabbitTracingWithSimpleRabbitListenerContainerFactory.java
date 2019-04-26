package brave.spring.rabbit;

import brave.spring.rabbit.testfixture.ITSpringAmqpTracingTestFixture;
import brave.spring.rabbit.testfixture.SimpleRabbitListenerContainerFactoryConfig;

public class ITSpringRabbitTracingWithSimpleRabbitListenerContainerFactory extends ITSpringRabbitTracingBase {

  public ITSpringRabbitTracingWithSimpleRabbitListenerContainerFactory() {
    super(new ITSpringAmqpTracingTestFixture(SimpleRabbitListenerContainerFactoryConfig.class));
  }
}
