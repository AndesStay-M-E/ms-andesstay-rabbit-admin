package cl.duoc.andesstay.rabbitadmin.dto;

public record ExchangeResponse(
        String name,
        String type,
        boolean durable
) {
}
