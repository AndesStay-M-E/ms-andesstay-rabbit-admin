package cl.duoc.andesstay.rabbitadmin.dto;

import java.util.Locale;

/**
 * Tipos de exchange permitidos por la API.
 */
public enum ExchangeKind {

    DIRECT,
    TOPIC,
    FANOUT,
    HEADERS;

    /** Nombre que espera RabbitMQ: direct, topic, fanout o headers. */
    public String amqpType() {
        return name().toLowerCase(Locale.ROOT);
    }
}
