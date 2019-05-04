package brave.spring.rabbit;

import brave.Tracing;
import brave.propagation.ThreadLocalCurrentTraceContext;
import org.junit.After;
import org.junit.Test;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import zipkin2.reporter.Reporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SpringDirectRabbitListenerContainerFactoryTracingTest {

  Tracing tracing = Tracing.newBuilder()
      .currentTraceContext(ThreadLocalCurrentTraceContext.create())
      .spanReporter(Reporter.NOOP)
      .build();
  private final SpringRabbitTracing springRabbitTracing = SpringRabbitTracing.create(tracing);
  private final SpringDirectRabbitListenerContainerFactoryTracing testee =
      new SpringDirectRabbitListenerContainerFactoryTracing(springRabbitTracing);

  @After
  public void close() {
    Tracing.current().close();
  }

  @Test
  public void decorateDirectRabbitListenerContainerFactory_adds_by_default() {
    DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();

    assertThat(testee.decorateDirectRabbitListenerContainerFactory(factory).getAdviceChain())
        .allMatch(advice -> advice instanceof TracingRabbitListenerAdvice);
  }

  @Test public void decorateDirectRabbitListenerContainerFactory_skips_when_present() {
    DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();
    factory.setAdviceChain(new TracingRabbitListenerAdvice(springRabbitTracing));

    assertThat(testee.decorateDirectRabbitListenerContainerFactory(factory).getAdviceChain())
        .hasSize(1);
  }

  @Test public void newDirectRabbitListenerContainerFactory_has_advice() {
    DirectRabbitListenerContainerFactory factory =
        testee.newDirectRabbitListenerContainerFactory(mock(ConnectionFactory.class));
    assertThat(factory.getAdviceChain())
        .hasSize(1)
        .hasOnlyElementsOfType(TracingRabbitListenerAdvice.class);
  }
}
