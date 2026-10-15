package cl.duoc.andesstay.rabbitadmin.service;

import cl.duoc.andesstay.rabbitadmin.config.RabbitAdminProperties;
import cl.duoc.andesstay.rabbitadmin.dto.BindingDestination;
import cl.duoc.andesstay.rabbitadmin.dto.BindingRequest;
import cl.duoc.andesstay.rabbitadmin.dto.ExchangeRequest;
import cl.duoc.andesstay.rabbitadmin.dto.ExchangeResponse;
import cl.duoc.andesstay.rabbitadmin.dto.PurgeResponse;
import cl.duoc.andesstay.rabbitadmin.dto.QueueRequest;
import cl.duoc.andesstay.rabbitadmin.dto.QueueResponse;
import cl.duoc.andesstay.rabbitadmin.dto.RabbitNameRules;
import cl.duoc.andesstay.rabbitadmin.exception.InvalidRabbitRequestException;
import cl.duoc.andesstay.rabbitadmin.exception.ProtectedResourceException;
import cl.duoc.andesstay.rabbitadmin.exception.RabbitResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.stereotype.Service;

/**
 * Toda la administración de RabbitMQ pasa por aquí.
 *
 * Los controladores llaman a estos métodos de alto nivel y no conocen
 * ninguna clase de RabbitMQ (AmqpAdmin, Queue, Exchange, Binding).
 */
@Service
public class RabbitAdminService {

    private static final Logger log =
            LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin amqpAdmin;
    private final RabbitAdminProperties properties;

    public RabbitAdminService(
            AmqpAdmin amqpAdmin,
            RabbitAdminProperties properties
    ) {
        this.amqpAdmin = amqpAdmin;
        this.properties = properties;
    }

    /* =========================== COLAS =========================== */

    public QueueResponse createQueue(QueueRequest request) {
        QueueBuilder builder = request.durableOrDefault()
                ? QueueBuilder.durable(request.name())
                : QueueBuilder.nonDurable(request.name());

        if (hasText(request.deadLetterExchange())) {
            builder.deadLetterExchange(request.deadLetterExchange());
        }

        if (hasText(request.deadLetterRoutingKey())) {
            builder.deadLetterRoutingKey(request.deadLetterRoutingKey());
        }

        if (request.messageTtlMs() != null) {
            builder.ttl(request.messageTtlMs());
        }

        if (request.maxLength() != null) {
            builder.maxLength(request.maxLength());
        }

        Queue queue = builder.build();
        amqpAdmin.declareQueue(queue);

        log.info("Cola creada nombre={} durable={} dlx={}",
                queue.getName(), queue.isDurable(), request.deadLetterExchange());

        return new QueueResponse(
                queue.getName(),
                queue.isDurable(),
                request.deadLetterExchange(),
                request.deadLetterRoutingKey(),
                null,
                null
        );
    }

    public QueueResponse getQueue(String name) {
        requireValidName(name);

        QueueInformation info = amqpAdmin.getQueueInfo(name);

        if (info == null) {
            throw new RabbitResourceNotFoundException("La cola '" + name + "' no existe");
        }

        return new QueueResponse(
                info.getName(),
                null,
                null,
                null,
                info.getMessageCount(),
                info.getConsumerCount()
        );
    }

    /**
     * Elimina una cola.
     *
     * @param onlyIfUnused true: solo si no tiene consumidores.
     * @param onlyIfEmpty  true: solo si no tiene mensajes.
     */
    public void deleteQueue(String name, boolean onlyIfUnused, boolean onlyIfEmpty) {
        requireValidName(name);
        requireNotProtected(name);

        QueueInformation info = amqpAdmin.getQueueInfo(name);

        if (info == null) {
            throw new RabbitResourceNotFoundException("La cola '" + name + "' no existe");
        }

        if (onlyIfUnused && info.getConsumerCount() > 0) {
            throw new InvalidRabbitRequestException(
                    "La cola '" + name + "' tiene " + info.getConsumerCount() + " consumidores activos");
        }

        if (onlyIfEmpty && info.getMessageCount() > 0) {
            throw new InvalidRabbitRequestException(
                    "La cola '" + name + "' tiene " + info.getMessageCount() + " mensajes pendientes");
        }

        amqpAdmin.deleteQueue(name);
        log.warn("Cola eliminada nombre={}", name);
    }

    public PurgeResponse purgeQueue(String name) {
        requireValidName(name);

        if (amqpAdmin.getQueueInfo(name) == null) {
            throw new RabbitResourceNotFoundException("La cola '" + name + "' no existe");
        }

        int purged = amqpAdmin.purgeQueue(name);
        log.warn("Cola vaciada nombre={} mensajesEliminados={}", name, purged);

        return new PurgeResponse(name, purged);
    }

    /* ========================= EXCHANGES ========================= */

    public ExchangeResponse createExchange(ExchangeRequest request) {
        Exchange exchange = new ExchangeBuilder(request.name(), request.type().amqpType())
                .durable(request.durableOrDefault())
                .build();

        amqpAdmin.declareExchange(exchange);

        log.info("Exchange creado nombre={} tipo={} durable={}",
                exchange.getName(), exchange.getType(), exchange.isDurable());

        return new ExchangeResponse(
                exchange.getName(),
                exchange.getType(),
                exchange.isDurable()
        );
    }

    public void deleteExchange(String name) {
        requireValidName(name);
        requireNotProtected(name);

        amqpAdmin.deleteExchange(name);
        log.warn("Exchange eliminado nombre={}", name);
    }

    /* ========================== BINDINGS ========================= */

    public BindingRequest createBinding(BindingRequest request) {
        requireDestinationExists(request);

        amqpAdmin.declareBinding(toBinding(request));

        log.info("Binding creado {} -[{}]-> {} ({})",
                request.exchange(), request.routingKey(),
                request.destination(), request.destinationType());

        return request;
    }

    public void deleteBinding(BindingRequest request) {
        amqpAdmin.removeBinding(toBinding(request));

        log.warn("Binding eliminado {} -[{}]-> {} ({})",
                request.exchange(), request.routingKey(),
                request.destination(), request.destinationType());
    }

    /* ====================== MÉTODOS PRIVADOS ===================== */

    private Binding toBinding(BindingRequest request) {
        Binding.DestinationType type =
                request.destinationType() == BindingDestination.QUEUE
                        ? Binding.DestinationType.QUEUE
                        : Binding.DestinationType.EXCHANGE;

        return new Binding(
                request.destination(),
                type,
                request.exchange(),
                request.routingKey(),
                null
        );
    }

    /** Si el destino es una cola, debe existir antes de enlazarla. */
    private void requireDestinationExists(BindingRequest request) {
        if (request.destinationType() == BindingDestination.QUEUE
                && amqpAdmin.getQueueInfo(request.destination()) == null) {
            throw new RabbitResourceNotFoundException(
                    "La cola destino '" + request.destination() + "' no existe");
        }
    }

    private void requireValidName(String name) {
        if (!RabbitNameRules.isValidName(name)) {
            throw new InvalidRabbitRequestException(RabbitNameRules.NAME_MESSAGE);
        }
    }

    private void requireNotProtected(String name) {
        if (properties.isProtected(name)) {
            throw new ProtectedResourceException(
                    "'" + name + "' es un recurso del sistema AndesStay y no se puede eliminar");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
