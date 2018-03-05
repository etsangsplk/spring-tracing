package mcp.config;

import brave.Tracing;
import brave.context.slf4j.MDCCurrentTraceContext;
import brave.http.HttpTracing;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.sampler.Sampler;
import brave.spring.web.TracingClientHttpRequestInterceptor;
import brave.spring.webmvc.TracingHandlerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.amqp.RabbitMQSender;

import java.io.IOException;

@Profile("rabbit")
@Configuration
public class TracingShipToRabbitConfiguration {
    /**
     * Configuration for sending spans to RabbitMQ
     */
    @Bean
    Sender sender(@Value("${mcp.rabbit.url}") String rabbitmqHostUrl,
                  @Value("${mcp.rabbit.queue}") String zipkinQueue) throws IOException {
        RabbitMQSender sender;

        sender = RabbitMQSender.newBuilder()
                .queue(zipkinQueue)
                .addresses(rabbitmqHostUrl).build();

        return sender;
    }

    /**
     * Configuration for how to buffer spans into messages for Zipkin
     */
    @Bean
    AsyncReporter<Span> spanReporter(Sender sender) {

        return AsyncReporter.create(sender);
    }

    @Bean
    Tracing tracing(@Value("${mcp:spring-tracing}") String serviceName,
                    AsyncReporter<Span> spanReporter) {
        return Tracing
                .newBuilder()
                .sampler(Sampler.ALWAYS_SAMPLE)
                .localServiceName(serviceName)
                .propagationFactory(ExtraFieldPropagation
                        .newFactory(B3Propagation.FACTORY, "client-id"))
                .currentTraceContext(MDCCurrentTraceContext.create())
                .spanReporter(spanReporter)
                .build();
    }

    @Bean
    HttpTracing httpTracing(Tracing tracing) {
        return HttpTracing.create(tracing);
    }
}