package cl.duoc.andesstay.rabbitadmin.dto;

public record PurgeResponse(
        String queue,
        int purgedMessages
) {
}
