package mcp;

import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GreetingClient {

    private final ManagedChannel managedChannel;

    public GreetingClient(ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    @PostConstruct
    private void initializeClient() {
        greetingServiceBlockingStub = GreetingServiceGrpc.newBlockingStub(managedChannel);
    }

    Logger log = LoggerFactory.getLogger(GreetingClient.class);

    private GreetingServiceGrpc.GreetingServiceBlockingStub
            greetingServiceBlockingStub;

    public Greeting hi() {
        return greetingServiceBlockingStub.sayHi(Empty.newBuilder().build());
    }

    public Greeting greeting(String name) {

        Greet greeting = Greet
                .newBuilder()
                .setName(name)
                .build();

        return greetingServiceBlockingStub.greeting(greeting);
    }
}
