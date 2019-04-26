package brave.spring.rabbit;

import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import zipkin2.Span;

import java.util.concurrent.BlockingQueue;

class ITSpringAmqpTracingTestFixture {
  ApplicationContext producerContext;
  ApplicationContext consumerContext;
  BlockingQueue<Span> producerSpans;
  BlockingQueue<Span> consumerSpans;

  ITSpringAmqpTracingTestFixture() {
    producerContext = producerSpringContext();
    consumerContext = consumerSpringContext();
    producerSpans = (BlockingQueue<Span>) producerContext.getBean("producerSpans");
    consumerSpans = (BlockingQueue<Span>) consumerContext.getBean("consumerSpans");
  }

  void reset() {
    HelloWorldConsumer consumer = consumerContext.getBean(HelloWorldConsumer.class);
    consumer.reset();
    producerSpans.clear();
    consumerSpans.clear();
  }

  private ApplicationContext producerSpringContext() {
    return createContext(CommonRabbitConfig.class, RabbitProducerConfig.class);
  }

  private ApplicationContext createContext(Class... configurationClasses) {
    AnnotationConfigApplicationContext producerContext = new AnnotationConfigApplicationContext();
    producerContext.register(configurationClasses);
    producerContext.refresh();
    return producerContext;
  }

  private ApplicationContext consumerSpringContext() {
    return createContext(CommonRabbitConfig.class, RabbitConsumerConfig.class);
  }

  void produceMessage() {
    HelloWorldProducer rabbitProducer =
        producerContext.getBean("tracingRabbitProducer_new", HelloWorldProducer.class);
    rabbitProducer.send();
  }

  void produceMessageFromDefault() {
    HelloWorldProducer rabbitProducer =
        producerContext.getBean("tracingRabbitProducer_decorate", HelloWorldProducer.class);
    rabbitProducer.send();
  }

  void awaitMessageConsumed() throws InterruptedException {
    HelloWorldConsumer consumer = consumerContext.getBean(HelloWorldConsumer.class);
    consumer.getCountDownLatch().await();
  }

  Message capturedMessage() {
    HelloWorldConsumer consumer = consumerContext.getBean(HelloWorldConsumer.class);
    return consumer.capturedMessage;
  }
}
