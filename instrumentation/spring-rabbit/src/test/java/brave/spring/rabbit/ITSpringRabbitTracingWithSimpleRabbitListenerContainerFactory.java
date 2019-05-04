package brave.spring.rabbit;

import brave.spring.rabbit.testfixture.ITSpringAmqpTracingTestFixture;
import brave.spring.rabbit.testfixture.SimpleRabbitListenerContainerFactoryConfig;
import org.junit.AfterClass;

public class ITSpringRabbitTracingWithSimpleRabbitListenerContainerFactory
    extends ITSpringRabbitTracingBase {

  private static final ITSpringAmqpTracingTestFixture TEST_FIXTURE =
      new ITSpringAmqpTracingTestFixture(SimpleRabbitListenerContainerFactoryConfig.class);

  @AfterClass
  public static void tearDown() {
    TEST_FIXTURE.close();
  }

  public ITSpringRabbitTracingWithSimpleRabbitListenerContainerFactory() {
    super(TEST_FIXTURE);
  }
}
