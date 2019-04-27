package brave.spring.rabbit.testfixture;

import brave.Tracing;
import brave.propagation.StrictScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.spring.rabbit.SpringRabbitTracing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.Span;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class RabbitConsumerConfig {
  @Bean
  public Tracing tracing(BlockingQueue<Span> consumerSpans) {
    return Tracing.newBuilder()
        .localServiceName("spring-amqp-consumer")
        .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
            .addScopeDecorator(StrictScopeDecorator.create())
            .build())
        .spanReporter(consumerSpans::add)
        .build();
  }

  @Bean
  public SpringRabbitTracing springRabbitTracing(Tracing tracing) {
    return SpringRabbitTracing.create(tracing);
  }

  @Bean
  public BlockingQueue<Span> consumerSpans() {
    return new LinkedBlockingQueue<>();
  }

  @Bean
  public HelloWorldConsumer helloWorldRabbitConsumer() {
    return new HelloWorldConsumer();
  }
}
