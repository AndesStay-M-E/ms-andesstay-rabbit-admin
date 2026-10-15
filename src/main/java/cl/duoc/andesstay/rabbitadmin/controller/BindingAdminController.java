package cl.duoc.andesstay.rabbitadmin.controller;

import cl.duoc.andesstay.rabbitadmin.dto.BindingRequest;
import cl.duoc.andesstay.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST   /api/rabbit/bindings   crear binding (JSON en el cuerpo)
 * DELETE /api/rabbit/bindings?exchange=..&destination=..&destinationType=QUEUE&routingKey=..
 *
 * El DELETE usa parámetros de consulta porque un binding no tiene id propio:
 * se identifica por la combinación exchange + destino + routing key.
 */
@RestController
@RequestMapping("/api/rabbit/bindings")
public class BindingAdminController {

    private final RabbitAdminService rabbitAdminService;

    public BindingAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @PostMapping
    public ResponseEntity<BindingRequest> create(
            @Valid @RequestBody BindingRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(rabbitAdminService.createBinding(request));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @Valid @ModelAttribute BindingRequest request
    ) {
        rabbitAdminService.deleteBinding(request);
        return ResponseEntity.noContent().build();
    }
}
