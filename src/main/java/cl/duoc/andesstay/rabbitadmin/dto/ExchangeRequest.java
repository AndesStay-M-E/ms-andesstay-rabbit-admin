package cl.duoc.andesstay.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Datos para crear un exchange.
 *
 * Ejemplo: { "name": "demo.direct", "type": "DIRECT", "durable": true }
 */
public record ExchangeRequest(

        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Pattern(regexp = RabbitNameRules.NAME_REGEX, message = RabbitNameRules.NAME_MESSAGE)
        String name,

        @NotNull(message = "El tipo es obligatorio: DIRECT, TOPIC, FANOUT o HEADERS")
        ExchangeKind type,

        Boolean durable

) {

    public boolean durableOrDefault() {
        return durable == null || durable;
    }
}
