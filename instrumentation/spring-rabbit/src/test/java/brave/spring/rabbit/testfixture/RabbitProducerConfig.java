package brave.spring.rabbit.testfixture;

import brave.Tracing;
import brave.propagation.StrictScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.spring.rabbit.SpringRabbitTracing;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.Span;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
class RabbitProducerConfig {
  @Bean
  public Tracing tracing(BlockingQueue<Span> producerSpans) {
    return Tracing.newBuilder()
        .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
            .addScopeDecorator(StrictScopeDecorator.create())
            .build())
        .localServiceName("spring-amqp-producer")
        .spanReporter(producerSpans::add)
        .build();
  }

  @Bean
  public SpringRabbitTracing springRabbitTracing(Tracing tracing) {
    return SpringRabbitTracing.create(tracing);
  }

  @Bean
  public BlockingQueue<Span> producerSpans() {
    return new LinkedBlockingQueue<>();
  }

  @Bean
  public RabbitTemplate newRabbitTemplate(
      ConnectionFactory connectionFactory,
      SpringRabbitTracing springRabbitTracing
  ) {
    RabbitTemplate newRabbitTemplate = springRabbitTracing.newRabbitTemplate(connectionFactory);
    newRabbitTemplate.setExchange("test-exchange");
    return newRabbitTemplate;
  }

  @Bean
  public RabbitTemplate decorateRabbitTemplate(
      ConnectionFactory connectionFactory,
      SpringRabbitTracing springRabbitTracing
  ) {
    RabbitTemplate newRabbitTemplate = new RabbitTemplate(connectionFactory);
    newRabbitTemplate.setExchange("test-exchange");
    return springRabbitTracing.decorateRabbitTemplate(newRabbitTemplate);
  }

  @Bean
  public HelloWorldProducer tracingRabbitProducer_new(
      @Qualifier("newRabbitTemplate") RabbitTemplate newRabbitTemplate
  ) {
    return new HelloWorldProducer(newRabbitTemplate);
  }

  @Bean
  public HelloWorldProducer tracingRabbitProducer_decorate(
      @Qualifier("decorateRabbitTemplate") RabbitTemplate newRabbitTemplate
  ) {
    return new HelloWorldProducer(newRabbitTemplate);
  }
}
