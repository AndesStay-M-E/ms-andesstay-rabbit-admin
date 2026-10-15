package cl.duoc.andesstay.rabbitadmin.exception;

/** La cola o recurso pedido no existe en RabbitMQ (HTTP 404). */
public class RabbitResourceNotFoundException extends RuntimeException {

    public RabbitResourceNotFoundException(String message) {
        super(message);
    }
}
