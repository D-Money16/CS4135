package elib.elib_core;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
