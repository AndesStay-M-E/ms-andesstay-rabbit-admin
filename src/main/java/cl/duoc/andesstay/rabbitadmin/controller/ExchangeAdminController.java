package cl.duoc.andesstay.rabbitadmin.controller;

import cl.duoc.andesstay.rabbitadmin.dto.ExchangeRequest;
import cl.duoc.andesstay.rabbitadmin.dto.ExchangeResponse;
import cl.duoc.andesstay.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST   /api/rabbit/exchanges          crear exchange
 * DELETE /api/rabbit/exchanges/{name}   eliminar exchange
 */
@RestController
@RequestMapping("/api/rabbit/exchanges")
public class ExchangeAdminController {

    private final RabbitAdminService rabbitAdminService;

    public ExchangeAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @PostMapping
    public ResponseEntity<ExchangeResponse> create(
            @Valid @RequestBody ExchangeRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(rabbitAdminService.createExchange(request));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(
            @PathVariable String name
    ) {
        rabbitAdminService.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }
}
