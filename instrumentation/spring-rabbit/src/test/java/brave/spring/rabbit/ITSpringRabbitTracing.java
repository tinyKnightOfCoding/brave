package brave.spring.rabbit;

import brave.spring.rabbit.testfixture.ITSpringAmqpTracingTestFixture;

public class ITSpringRabbitTracing extends ITSpringRabbitTracingBase {

  public ITSpringRabbitTracing() {
    super(new ITSpringAmqpTracingTestFixture());
  }
}
