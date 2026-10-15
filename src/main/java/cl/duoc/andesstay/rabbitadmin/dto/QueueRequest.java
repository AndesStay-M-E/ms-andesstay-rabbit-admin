package cl.duoc.andesstay.rabbitadmin.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos para crear una cola.
 *
 * Ejemplo:
 * {
 *   "name": "demo.q",
 *   "durable": true,
 *   "deadLetterExchange": "andesstay.dlx",
 *   "deadLetterRoutingKey": "demo.dlq",
 *   "messageTtlMs": 60000,
 *   "maxLength": 1000
 * }
 */
public record QueueRequest(

        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Pattern(regexp = RabbitNameRules.NAME_REGEX, message = RabbitNameRules.NAME_MESSAGE)
        String name,

        Boolean durable,

        @Pattern(regexp = RabbitNameRules.NAME_REGEX, message = RabbitNameRules.NAME_MESSAGE)
        String deadLetterExchange,

        @Pattern(regexp = RabbitNameRules.ROUTING_KEY_REGEX, message = RabbitNameRules.ROUTING_KEY_MESSAGE)
        String deadLetterRoutingKey,

        @Min(value = 1000, message = "El TTL mínimo es 1000 ms")
        @Max(value = 604800000, message = "El TTL máximo es 7 días (604800000 ms)")
        Integer messageTtlMs,

        @Min(value = 1, message = "El largo máximo debe ser al menos 1")
        @Max(value = 1000000, message = "El largo máximo no puede superar 1.000.000 mensajes")
        Long maxLength

) {

    /** Por defecto las colas son durables. */
    public boolean durableOrDefault() {
        return durable == null || durable;
    }

    /** No tiene sentido una routing key de DLQ sin exchange de DLQ. */
    @AssertTrue(message = "deadLetterRoutingKey requiere indicar deadLetterExchange")
    public boolean isDeadLetterConfigValid() {
        return deadLetterRoutingKey == null
                || deadLetterRoutingKey.isBlank()
                || (deadLetterExchange != null && !deadLetterExchange.isBlank());
    }
}
