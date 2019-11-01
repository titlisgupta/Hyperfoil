package io.hyperfoil.api.processor;

import java.util.function.Function;

import io.hyperfoil.api.config.IncludeBuilders;
import io.hyperfoil.api.connection.HttpRequest;
import io.hyperfoil.api.connection.Request;
import io.hyperfoil.api.session.Action;
import io.hyperfoil.api.session.ResourceUtilizer;
import io.hyperfoil.api.session.Session;
import io.netty.buffer.ByteBuf;

/**
 * Processors for HTTP requests.
 */
@IncludeBuilders({
      @IncludeBuilders.Conversion(from = RequestProcessorBuilder.class, adapter = HttpRequestProcessorBuilder.BuilderConverter.class),
      @IncludeBuilders.Conversion(from = Action.Builder.class, adapter = HttpRequestProcessorBuilder.ActionBuilderConverter.class)
})
public interface HttpRequestProcessorBuilder extends Processor.Builder<HttpRequest, HttpRequestProcessorBuilder> {

   class BuilderConverter implements Function<RequestProcessorBuilder, HttpRequestProcessorBuilder> {
      @Override
      public HttpRequestProcessorBuilder apply(RequestProcessorBuilder processorBuilder) {
         return () -> new RequestProcessorAdapter(processorBuilder.build());
      }
   }

   class RequestProcessorAdapter implements Processor<HttpRequest>, ResourceUtilizer {
      private final Processor<Request> delegate;

      public RequestProcessorAdapter(Processor<Request> delegate) {
         this.delegate = delegate;
      }

      @Override
      public void before(HttpRequest request) {
         delegate.before(request);
      }

      @Override
      public void process(HttpRequest request, ByteBuf data, int offset, int length, boolean isLastPart) {
         delegate.process(request, data, offset, length, isLastPart);
      }

      @Override
      public void after(HttpRequest request) {
         delegate.after(request);
      }

      @Override
      public void reserve(Session session) {
         ResourceUtilizer.reserve(session, delegate);
      }
   }

   class ActionBuilderConverter implements Function<Action.Builder, HttpRequestProcessorBuilder> {
      @Override
      public HttpRequestProcessorBuilder apply(Action.Builder builder) {
         return () -> new Processor.ActionAdapter<>(builder.build());
      }
   }
}
