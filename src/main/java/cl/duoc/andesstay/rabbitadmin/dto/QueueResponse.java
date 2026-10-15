package cl.duoc.andesstay.rabbitadmin.dto;

/**
 * Estado de una cola. messageCount y consumerCount son null
 * cuando la respuesta corresponde a una creación.
 */
public record QueueResponse(
        String name,
        Boolean durable,
        String deadLetterExchange,
        String deadLetterRoutingKey,
        Long messageCount,
        Integer consumerCount
) {
}
