package cl.duoc.andesstay.rabbitadmin.exception;

/** Parámetro inválido detectado en el servicio (HTTP 400). */
public class InvalidRabbitRequestException extends RuntimeException {

    public InvalidRabbitRequestException(String message) {
        super(message);
    }
}
