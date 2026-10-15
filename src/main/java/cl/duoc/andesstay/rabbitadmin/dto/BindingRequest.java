package cl.duoc.andesstay.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Datos de un binding (crear o eliminar).
 *
 * Ejemplo:
 * {
 *   "exchange": "demo.direct",
 *   "destination": "demo.q",
 *   "destinationType": "QUEUE",
 *   "routingKey": "demo.key"
 * }
 */
public record BindingRequest(

        @NotBlank(message = "El exchange de origen es obligatorio")
        @Pattern(regexp = RabbitNameRules.NAME_REGEX, message = RabbitNameRules.NAME_MESSAGE)
        String exchange,

        @NotBlank(message = "El destino es obligatorio")
        @Pattern(regexp = RabbitNameRules.NAME_REGEX, message = RabbitNameRules.NAME_MESSAGE)
        String destination,

        @NotNull(message = "El tipo de destino es obligatorio: QUEUE o EXCHANGE")
        BindingDestination destinationType,

        @NotNull(message = "La routing key es obligatoria (puede ser vacía para fanout)")
        @Pattern(regexp = RabbitNameRules.ROUTING_KEY_REGEX, message = RabbitNameRules.ROUTING_KEY_MESSAGE)
        String routingKey

) {
}
