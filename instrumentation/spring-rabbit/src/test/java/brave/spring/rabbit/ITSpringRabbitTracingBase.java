package brave.spring.rabbit;

import brave.spring.rabbit.testfixture.ITSpringAmqpTracingTestFixture;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.junit.BrokerRunning;
import zipkin2.DependencyLink;
import zipkin2.Span;
import zipkin2.internal.DependencyLinker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.groups.Tuple.tuple;
import static zipkin2.Span.Kind.CONSUMER;
import static zipkin2.Span.Kind.PRODUCER;

public abstract class ITSpringRabbitTracingBase {

  @ClassRule
  public static BrokerRunning brokerRunning = BrokerRunning.isRunning();

  @Rule
  public TestName testName = new TestName();

  private final ITSpringAmqpTracingTestFixture testFixture;

  ITSpringRabbitTracingBase(ITSpringAmqpTracingTestFixture testFixture) {
    this.testFixture = testFixture;
  }

  @Before
  public void reset() {
    testFixture.reset();
  }

  @Test
  public void propagates_trace_info_across_amqp_from_producer() throws Exception {
    testFixture.produceMessage();
    testFixture.awaitMessageConsumed();

    List<Span> allSpans = new ArrayList<>();
    allSpans.add(takeProducerSpan());
    allSpans.add(takeConsumerSpan());
    allSpans.add(takeConsumerSpan());

    String originatingTraceId = allSpans.get(0).traceId();
    String consumerSpanId = allSpans.get(1).id();

    assertThat(allSpans)
        .extracting(Span::kind, Span::traceId, Span::parentId)
        .containsExactly(
            tuple(PRODUCER, originatingTraceId, null),
            tuple(CONSUMER, originatingTraceId, originatingTraceId),
            tuple(null, originatingTraceId, consumerSpanId)
        );
  }

  @Test
  public void clears_message_headers_after_propagation() throws Exception {
    testFixture.produceMessage();
    testFixture.awaitMessageConsumed();

    Message capturedMessage = testFixture.capturedMessage();
    Map<String, Object> headers = capturedMessage.getMessageProperties().getHeaders();
    assertThat(headers.keySet()).containsExactly("not-zipkin-header");
  }

  @Test
  public void tags_spans_with_exchange_and_routing_key() throws Exception {
    testFixture.produceMessage();
    testFixture.awaitMessageConsumed();

    List<Span> consumerSpans = new ArrayList<>();
    consumerSpans.add(takeConsumerSpan());
    consumerSpans.add(takeConsumerSpan());

    assertThat(consumerSpans)
        .filteredOn(s -> s.kind() == CONSUMER)
        .flatExtracting(s -> s.tags().entrySet())
        .containsOnly(
            entry("rabbit.exchange", "test-exchange"),
            entry("rabbit.routing_key", "test.binding"),
            entry("rabbit.queue", "test-queue")
        );

    assertThat(consumerSpans)
        .filteredOn(s -> s.kind() != CONSUMER)
        .flatExtracting(s -> s.tags().entrySet())
        .isEmpty();
  }

  /**
   * Technical implementation of clock sharing might imply a race. This ensures happens-after
   */
  @Test
  public void listenerSpanHappensAfterConsumerSpan() throws Exception {
    testFixture.produceMessage();
    testFixture.awaitMessageConsumed();

    Span span1 = takeConsumerSpan(), span2 = takeConsumerSpan();
    Span consumerSpan = span1.kind() == Span.Kind.CONSUMER ? span1 : span2;
    Span listenerSpan = consumerSpan == span1 ? span2 : span1;

    assertThat(consumerSpan.timestampAsLong() + consumerSpan.durationAsLong())
        .isLessThanOrEqualTo(listenerSpan.timestampAsLong());
  }

  @Test
  public void creates_dependency_links() throws Exception {
    testFixture.produceMessage();
    testFixture.awaitMessageConsumed();

    List<Span> allSpans = new ArrayList<>();
    allSpans.add(takeProducerSpan());
    allSpans.add(takeConsumerSpan());
    allSpans.add(takeConsumerSpan());

    List<DependencyLink> links = new DependencyLinker().putTrace(allSpans).link();
    assertThat(links).extracting("parent", "child").containsExactly(
        tuple("spring-amqp-producer", "rabbitmq"),
        tuple("rabbitmq", "spring-amqp-consumer")
    );
  }

  @Test
  public void tags_spans_with_exchange_and_routing_key_from_default() throws Exception {
    testFixture.produceMessageFromDefault();
    testFixture.awaitMessageConsumed();

    List<Span> consumerSpans = new ArrayList<>();
    consumerSpans.add(takeProducerSpan());
    consumerSpans.add(takeConsumerSpan());

    assertThat(consumerSpans)
        .filteredOn(s -> s.kind() == CONSUMER)
        .flatExtracting(s -> s.tags().entrySet())
        .containsOnly(
            entry("rabbit.exchange", "test-exchange"),
            entry("rabbit.routing_key", "test.binding"),
            entry("rabbit.queue", "test-queue")
        );

    assertThat(consumerSpans)
        .filteredOn(s -> s.kind() != CONSUMER)
        .flatExtracting(s -> s.tags().entrySet())
        .isEmpty();
  }

  // We will revisit this eventually, but these names mostly match the method names
  @Test
  public void method_names_as_span_names() throws Exception {
    testFixture.produceMessage();
    testFixture.awaitMessageConsumed();

    List<Span> allSpans = new ArrayList<>();
    allSpans.add(takeProducerSpan());
    allSpans.add(takeConsumerSpan());
    allSpans.add(takeConsumerSpan());

    assertThat(allSpans)
        .extracting(Span::name)
        .containsExactly("publish", "next-message", "on-message");
  }

  /**
   * Call this to block until a span was reported
   */
  Span takeProducerSpan() throws InterruptedException {
    Span result = testFixture.nextProducerSpan(3);
    assertThat(result)
        .withFailMessage("Producer span was not reported")
        .isNotNull();
    // ensure the span finished
    assertThat(result.durationAsLong()).isPositive();
    return result;
  }

  /**
   * Call this to block until a span was reported
   */
  Span takeConsumerSpan() throws InterruptedException {
    Span result = testFixture.nextConsumerSpan(3);
    assertThat(result)
        .withFailMessage("Consumer span was not reported")
        .isNotNull();
    // ensure the span finished
    assertThat(result.durationAsLong()).isPositive();
    return result;
  }
}
