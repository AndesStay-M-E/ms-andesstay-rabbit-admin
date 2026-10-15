package cl.duoc.andesstay.rabbitadmin.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce los errores a respuestas HTTP claras (RFC 7807 / ProblemDetail).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errores de @Valid en el cuerpo JSON. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBodyValidation(MethodArgumentNotValidException exception) {
        return validationProblem(exception.getBindingResult());
    }

    /** Errores de @Valid en parámetros de consulta (@ModelAttribute). */
    @ExceptionHandler(BindException.class)
    public ProblemDetail handleParamsValidation(BindException exception) {
        return validationProblem(exception.getBindingResult());
    }

    /** JSON mal formado o enum desconocido (ej: type = "XYZ"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                "El cuerpo de la solicitud no es un JSON válido o contiene un valor no permitido",
                "bad-request"
        );
    }

    @ExceptionHandler(InvalidRabbitRequestException.class)
    public ProblemDetail handleInvalid(InvalidRabbitRequestException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Solicitud inválida", exception.getMessage(), "bad-request");
    }

    @ExceptionHandler(RabbitResourceNotFoundException.class)
    public ProblemDetail handleNotFound(RabbitResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", exception.getMessage(), "not-found");
    }

    @ExceptionHandler(ProtectedResourceException.class)
    public ProblemDetail handleProtected(ProtectedResourceException exception) {
        return problem(HttpStatus.CONFLICT, "Recurso protegido", exception.getMessage(), "protected");
    }

    /** RabbitMQ no está disponible. */
    @ExceptionHandler(AmqpConnectException.class)
    public ProblemDetail handleConnection(AmqpConnectException exception) {
        log.error("RabbitMQ no disponible: {}", exception.getMessage());
        return problem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "RabbitMQ no disponible",
                "No fue posible conectarse a RabbitMQ",
                "broker-unavailable"
        );
    }

    /**
     * RabbitMQ rechazó la operación. Ejemplos: la cola ya existe con otros
     * argumentos (PRECONDITION_FAILED) o el exchange no existe (NOT_FOUND).
     */
    @ExceptionHandler(AmqpException.class)
    public ProblemDetail handleAmqp(AmqpException exception) {
        String cause = rootCauseMessage(exception);
        log.error("RabbitMQ rechazó la operación: {}", cause);
        return problem(
                HttpStatus.CONFLICT,
                "Operación rechazada por RabbitMQ",
                "RabbitMQ rechazó la operación: " + cause,
                "broker-conflict"
        );
    }

    /** Mensaje de la causa más profunda (RabbitMQ envuelve el error real). */
    private String rootCauseMessage(Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : exception.getMessage();
    }

    private ProblemDetail validationProblem(BindingResult result) {
        Map<String, String> errors = new LinkedHashMap<>();

        result.getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        result.getGlobalErrors().forEach(error ->
                errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST,
                "Error de validación",
                "Existen datos inválidos en la solicitud",
                "validation"
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, String type) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://andesstay.cl/errors/" + type));
        return problem;
    }
}
