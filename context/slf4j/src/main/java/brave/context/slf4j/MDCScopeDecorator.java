package brave.context.slf4j;

import brave.internal.propagation.CorrelationFieldScopeDecorator;
import brave.propagation.CurrentTraceContext;
import org.slf4j.MDC;

/**
 * Adds {@linkplain MDC} properties "traceId", "parentId", "spanId" and "sampled" when a {@link
 * brave.Tracer#currentSpan() span is current}. "traceId" and "spanId" are used in log correlation.
 * "parentId" is used for scenarios such as log parsing that reconstructs the trace tree. "sampled"
 * is used as a hint that a span found in logs might be in Zipkin.
 *
 * <p>Ex.
 * <pre>{@code
 * tracing = Tracing.newBuilder()
 *                  .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
 *                    .addScopeDecorator(MDCScopeDecorator.create())
 *                    .build()
 *                  )
 *                  ...
 *                  .build();
 * }</pre>
 */
public final class MDCScopeDecorator extends CorrelationFieldScopeDecorator {
  public static CurrentTraceContext.ScopeDecorator create() {
    return new MDCScopeDecorator();
  }

  @Override protected String get(String key) {
    return MDC.get(key);
  }

  @Override protected void put(String key, String value) {
    MDC.put(key, value);
  }

  @Override protected void remove(String key) {
    MDC.remove(key);
  }

  MDCScopeDecorator() {
  }
}
