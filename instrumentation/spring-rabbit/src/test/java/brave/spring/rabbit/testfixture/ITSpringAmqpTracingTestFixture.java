package brave.spring.rabbit.testfixture;

import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import zipkin2.Span;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ITSpringAmqpTracingTestFixture {
  private ApplicationContext producerContext;
  private ApplicationContext consumerContext;
  private BlockingQueue<Span> producerSpans;
  private BlockingQueue<Span> consumerSpans;

  public ITSpringAmqpTracingTestFixture() {
    producerContext = producerSpringContext();
    consumerContext = consumerSpringContext();
    producerSpans = (BlockingQueue<Span>) producerContext.getBean("producerSpans");
    consumerSpans = (BlockingQueue<Span>) consumerContext.getBean("consumerSpans");
  }

  public void reset() {
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

  public void produceMessage() {
    HelloWorldProducer rabbitProducer =
        producerContext.getBean("tracingRabbitProducer_new", HelloWorldProducer.class);
    rabbitProducer.send();
  }

  public void produceMessageFromDefault() {
    HelloWorldProducer rabbitProducer =
        producerContext.getBean("tracingRabbitProducer_decorate", HelloWorldProducer.class);
    rabbitProducer.send();
  }

  public void awaitMessageConsumed() throws InterruptedException {
    HelloWorldConsumer consumer = consumerContext.getBean(HelloWorldConsumer.class);
    consumer.getCountDownLatch().await();
  }

  public Message capturedMessage() {
    HelloWorldConsumer consumer = consumerContext.getBean(HelloWorldConsumer.class);
    return consumer.capturedMessage;
  }

  public Span nextProducerSpan(int timeoutInSeconds) throws InterruptedException {
    return producerSpans.poll(timeoutInSeconds, TimeUnit.SECONDS);
  }

  public Span nextConsumerSpan(int timeoutInSeconds) throws InterruptedException {
    return consumerSpans.poll(timeoutInSeconds, TimeUnit.SECONDS);
  }
}
