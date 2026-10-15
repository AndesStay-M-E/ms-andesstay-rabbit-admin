package cl.duoc.andesstay.rabbitadmin.exception;

/** Se intentó eliminar un recurso del sistema protegido (HTTP 409). */
public class ProtectedResourceException extends RuntimeException {

    public ProtectedResourceException(String message) {
        super(message);
    }
}
