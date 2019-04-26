package brave.spring.rabbit;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class HelloWorldProducer {
  private final RabbitTemplate newRabbitTemplate;

  HelloWorldProducer(RabbitTemplate newRabbitTemplate) {
    this.newRabbitTemplate = newRabbitTemplate;
  }

  void send() {
    byte[] messageBody = "hello world".getBytes();
    MessageProperties properties = new MessageProperties();
    properties.setHeader("not-zipkin-header", "fakeValue");
    Message message = MessageBuilder.withBody(messageBody).andProperties(properties).build();
    newRabbitTemplate.send("test.binding", message);
  }
}
