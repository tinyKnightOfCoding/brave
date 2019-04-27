package brave.spring.rabbit;

import brave.spring.rabbit.testfixture.DirectRabbitListenerContainerFactoryConfig;
import brave.spring.rabbit.testfixture.ITSpringAmqpTracingTestFixture;
import org.junit.AfterClass;

public class ITSpringRabbitTracingWithDirectRabbitListenerContainerFactory extends ITSpringRabbitTracingBase {

  public static final ITSpringAmqpTracingTestFixture TEST_FIXTURE = new ITSpringAmqpTracingTestFixture(DirectRabbitListenerContainerFactoryConfig.class);

  @AfterClass
  public static void tearDown() {
    TEST_FIXTURE.close();
  }

  public ITSpringRabbitTracingWithDirectRabbitListenerContainerFactory() {
    super(TEST_FIXTURE);
  }
}
