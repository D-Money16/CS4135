package elib.elib_gateway;

public class ServiceInstanceUnavailableException extends RuntimeException {

    public ServiceInstanceUnavailableException(String serviceId) {
        super("No instance available for service: " + serviceId);
    }
}