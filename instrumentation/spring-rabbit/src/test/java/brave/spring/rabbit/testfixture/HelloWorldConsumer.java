package brave.spring.rabbit.testfixture;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.util.concurrent.CountDownLatch;

class HelloWorldConsumer {
  private CountDownLatch countDownLatch;
  Message capturedMessage;

  HelloWorldConsumer() {
    this.countDownLatch = new CountDownLatch(1);
  }

  @RabbitListener(queues = "test-queue")
  public void testReceiveRabbit(Message message) {
    this.capturedMessage = message;
    this.countDownLatch.countDown();
  }

  public void reset() {
    this.countDownLatch = new CountDownLatch(1);
    this.capturedMessage = null;
  }

  public CountDownLatch getCountDownLatch() {
    return countDownLatch;
  }
}
