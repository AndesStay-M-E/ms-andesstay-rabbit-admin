package cl.duoc.andesstay.rabbitadmin.controller;

import cl.duoc.andesstay.rabbitadmin.dto.PurgeResponse;
import cl.duoc.andesstay.rabbitadmin.dto.QueueRequest;
import cl.duoc.andesstay.rabbitadmin.dto.QueueResponse;
import cl.duoc.andesstay.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * POST   /api/rabbit/queues                      crear cola
 * GET    /api/rabbit/queues/{name}               ver mensajes y consumidores
 * DELETE /api/rabbit/queues/{name}               eliminar cola
 * DELETE /api/rabbit/queues/{name}/messages      vaciar (purgar) cola
 */
@RestController
@RequestMapping("/api/rabbit/queues")
public class QueueAdminController {

    private final RabbitAdminService rabbitAdminService;

    public QueueAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @PostMapping
    public ResponseEntity<QueueResponse> create(
            @Valid @RequestBody QueueRequest request
    ) {
        QueueResponse response = rabbitAdminService.createQueue(request);

        return ResponseEntity
                .created(URI.create("/api/rabbit/queues/" + response.name()))
                .body(response);
    }

    @GetMapping("/{name}")
    public ResponseEntity<QueueResponse> get(
            @PathVariable String name
    ) {
        return ResponseEntity.ok(rabbitAdminService.getQueue(name));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(
            @PathVariable String name,
            @RequestParam(defaultValue = "false") boolean ifUnused,
            @RequestParam(defaultValue = "false") boolean ifEmpty
    ) {
        rabbitAdminService.deleteQueue(name, ifUnused, ifEmpty);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{name}/messages")
    public ResponseEntity<PurgeResponse> purge(
            @PathVariable String name
    ) {
        return ResponseEntity.ok(rabbitAdminService.purgeQueue(name));
    }
}
