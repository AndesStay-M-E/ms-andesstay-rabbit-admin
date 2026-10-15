package cl.duoc.andesstay.rabbitadmin.dto;

/**
 * Reglas de nombres compartidas por los DTO y el servicio.
 *
 *  - Solo letras, números, punto, guion y guion bajo.
 *  - Entre 1 y 200 caracteres.
 *  - No puede empezar con "amq." (prefijo reservado por RabbitMQ).
 */
public final class RabbitNameRules {

    public static final String NAME_REGEX =
            "^(?!amq\\.)[A-Za-z0-9._-]{1,200}$";

    public static final String NAME_MESSAGE =
            "Nombre inválido: use letras, números, '.', '-' o '_' (máx. 200) y no empiece con 'amq.'";

    /** Las routing keys pueden incluir los comodines * y # (exchange topic). */
    public static final String ROUTING_KEY_REGEX =
            "^[A-Za-z0-9._*#-]{0,200}$";

    public static final String ROUTING_KEY_MESSAGE =
            "Routing key inválida: use letras, números, '.', '-', '_', '*' o '#' (máx. 200)";

    private RabbitNameRules() {
    }

    public static boolean isValidName(String name) {
        return name != null && name.matches(NAME_REGEX);
    }
}
