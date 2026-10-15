package cl.duoc.andesstay.rabbitadmin.service;

import cl.duoc.andesstay.rabbitadmin.config.RabbitAdminProperties;
import cl.duoc.andesstay.rabbitadmin.dto.BindingDestination;
import cl.duoc.andesstay.rabbitadmin.dto.BindingRequest;
import cl.duoc.andesstay.rabbitadmin.dto.ExchangeKind;
import cl.duoc.andesstay.rabbitadmin.dto.ExchangeRequest;
import cl.duoc.andesstay.rabbitadmin.dto.ExchangeResponse;
import cl.duoc.andesstay.rabbitadmin.dto.QueueRequest;
import cl.duoc.andesstay.rabbitadmin.exception.InvalidRabbitRequestException;
import cl.duoc.andesstay.rabbitadmin.exception.ProtectedResourceException;
import cl.duoc.andesstay.rabbitadmin.exception.RabbitResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RabbitAdminServiceTest {

    private AmqpAdmin amqpAdmin;
    private RabbitAdminService service;

    @BeforeEach
    void setUp() {
        amqpAdmin = mock(AmqpAdmin.class);
        service = new RabbitAdminService(
                amqpAdmin,
                new RabbitAdminProperties(List.of("andesstay.commands", "notify.email.q"))
        );
    }

    @Test
    void creaColaDurableConDeadLetter() {
        service.createQueue(new QueueRequest(
                "demo.q", true, "andesstay.dlx", "demo.dlq", 60000, 100L));

        ArgumentCaptor<Queue> captor = ArgumentCaptor.forClass(Queue.class);
        verify(amqpAdmin).declareQueue(captor.capture());

        Queue queue = captor.getValue();
        assertThat(queue.getName()).isEqualTo("demo.q");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", "andesstay.dlx")
                .containsEntry("x-dead-letter-routing-key", "demo.dlq");
    }

    @Test
    void creaExchangeDelTipoIndicado() {
        ExchangeResponse response = service.createExchange(
                new ExchangeRequest("demo.topic", ExchangeKind.TOPIC, true));

        assertThat(response.type()).isEqualTo("topic");
        verify(amqpAdmin).declareExchange(any());
    }

    @Test
    void noPermiteEliminarRecursosProtegidos() {
        assertThatThrownBy(() -> service.deleteQueue("notify.email.q", false, false))
                .isInstanceOf(ProtectedResourceException.class);

        assertThatThrownBy(() -> service.deleteExchange("andesstay.commands"))
                .isInstanceOf(ProtectedResourceException.class);

        verify(amqpAdmin, never()).deleteQueue(anyString());
        verify(amqpAdmin, never()).deleteExchange(anyString());
    }

    @Test
    void eliminarColaInexistenteDevuelveNotFound() {
        when(amqpAdmin.getQueueInfo("demo.q")).thenReturn(null);

        assertThatThrownBy(() -> service.deleteQueue("demo.q", false, false))
                .isInstanceOf(RabbitResourceNotFoundException.class);
    }

    @Test
    void noEliminaColaConMensajesSiSePideIfEmpty() {
        when(amqpAdmin.getQueueInfo("demo.q"))
                .thenReturn(new QueueInformation("demo.q", 5, 0));

        assertThatThrownBy(() -> service.deleteQueue("demo.q", false, true))
                .isInstanceOf(InvalidRabbitRequestException.class);

        verify(amqpAdmin, never()).deleteQueue(anyString());
    }

    @Test
    void rechazaNombresReservadosOVacios() {
        assertThatThrownBy(() -> service.deleteExchange("amq.direct"))
                .isInstanceOf(InvalidRabbitRequestException.class);

        assertThatThrownBy(() -> service.getQueue(" "))
                .isInstanceOf(InvalidRabbitRequestException.class);
    }

    @Test
    void creaBindingSoloSiLaColaDestinoExiste() {
        BindingRequest request = new BindingRequest(
                "demo.direct", "demo.q", BindingDestination.QUEUE, "demo.key");

        when(amqpAdmin.getQueueInfo("demo.q")).thenReturn(null);

        assertThatThrownBy(() -> service.createBinding(request))
                .isInstanceOf(RabbitResourceNotFoundException.class);

        when(amqpAdmin.getQueueInfo("demo.q"))
                .thenReturn(new QueueInformation("demo.q", 0, 0));

        service.createBinding(request);

        ArgumentCaptor<Binding> captor = ArgumentCaptor.forClass(Binding.class);
        verify(amqpAdmin).declareBinding(captor.capture());
        assertThat(captor.getValue().getExchange()).isEqualTo("demo.direct");
        assertThat(captor.getValue().getRoutingKey()).isEqualTo("demo.key");
    }
}
