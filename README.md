# ms-andesstay-rabbit-admin

Microservicio administrador de RabbitMQ de AndesStay. Expone una API REST para crear y eliminar colas, exchanges y bindings.

- Puerto: **8086**
- Seguridad: JWT de Azure AD con rol **ADMIN** (claim `roles`).
- Toda la lógica de RabbitMQ está en `RabbitAdminService`; los controladores no conocen clases de RabbitMQ.
- Las entradas se validan con DTO + Bean Validation (`@NotBlank`, `@Pattern`, `@Min`, `@Max`, `@AssertTrue`).
- Los recursos del sistema (`andesstay.commands`, `notify.email.q`, etc.) están protegidos y no se pueden eliminar. La lista está en `application.yaml` (`andesstay.rabbit-admin.protected-names`).

## Endpoints

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| POST | `/api/rabbit/queues` | Crear cola | 201 |
| GET | `/api/rabbit/queues/{name}` | Ver cantidad de mensajes y consumidores | 200 / 404 |
| DELETE | `/api/rabbit/queues/{name}?ifUnused=false&ifEmpty=false` | Eliminar cola | 204 / 404 / 409 |
| DELETE | `/api/rabbit/queues/{name}/messages` | Vaciar (purgar) cola | 200 |
| POST | `/api/rabbit/exchanges` | Crear exchange | 201 |
| DELETE | `/api/rabbit/exchanges/{name}` | Eliminar exchange | 204 / 409 |
| POST | `/api/rabbit/bindings` | Crear binding | 201 / 404 |
| DELETE | `/api/rabbit/bindings?exchange=&destination=&destinationType=QUEUE&routingKey=` | Eliminar binding | 204 |

### Ejemplos

```http
POST /api/rabbit/exchanges
Authorization: Bearer <token>
Content-Type: application/json

{ "name": "demo.direct", "type": "DIRECT", "durable": true }
```

```http
POST /api/rabbit/queues
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "demo.q",
  "durable": true,
  "deadLetterExchange": "andesstay.dlx",
  "deadLetterRoutingKey": "demo.dlq",
  "messageTtlMs": 60000
}
```

```http
POST /api/rabbit/bindings
Authorization: Bearer <token>
Content-Type: application/json

{ "exchange": "demo.direct", "destination": "demo.q", "destinationType": "QUEUE", "routingKey": "demo.key" }
```

### Errores

| Caso | HTTP |
|---|---|
| Nombre vacío, con caracteres inválidos o que empieza con `amq.` | 400 |
| Tipo de exchange desconocido | 400 |
| `deadLetterRoutingKey` sin `deadLetterExchange` | 400 |
| La cola no existe | 404 |
| Recurso protegido del sistema | 409 |
| RabbitMQ rechaza la operación (ej. cola existente con otros argumentos) | 409 |
| RabbitMQ apagado | 503 |

## Ejecutar

1. Levantar RabbitMQ con el repositorio `infra-andesstay-messaging` (`docker compose up -d`).
2. `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
   Con el tenant de Eduardo: `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=eduardo`.

## Pruebas

`./mvnw test`
