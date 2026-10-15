package cl.duoc.andesstay.rabbitadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuración del administrador (prefijo andesstay.rabbit-admin).
 *
 * protectedNames: colas y exchanges del sistema que no se pueden eliminar.
 */
@ConfigurationProperties(prefix = "andesstay.rabbit-admin")
public record RabbitAdminProperties(
        List<String> protectedNames
) {

    public RabbitAdminProperties {
        protectedNames = protectedNames == null ? List.of() : List.copyOf(protectedNames);
    }

    public boolean isProtected(String name) {
        return protectedNames.contains(name);
    }
}
