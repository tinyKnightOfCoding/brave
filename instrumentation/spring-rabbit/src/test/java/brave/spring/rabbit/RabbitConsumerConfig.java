package brave.spring.rabbit;

import brave.Tracing;
import brave.propagation.StrictScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.Span;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@EnableRabbit
@Configuration
class RabbitConsumerConfig {
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
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      SpringRabbitTracing springRabbitTracing
  ) {
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory =
        new SimpleRabbitListenerContainerFactory();
    rabbitListenerContainerFactory.setConnectionFactory(connectionFactory);
    return springRabbitTracing.decorateSimpleRabbitListenerContainerFactory(
        rabbitListenerContainerFactory
    );
  }

  @Bean
  public HelloWorldConsumer helloWorldRabbitConsumer() {
    return new HelloWorldConsumer();
  }
}
