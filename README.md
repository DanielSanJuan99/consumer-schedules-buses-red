# Consumer Schedules - Buses RED 🕐📄

Microservicio consumidor que lee horarios y cambios de ruta desde RabbitMQ y los almacena como archivos JSON en disco.

## Tecnologías

- Java 21 (Eclipse Temurin)
- Spring Boot 3.5.7
- Spring AMQP (RabbitMQ)
- Jackson (serialización JSON a archivos)
- Maven 3.9.9
- Docker (multi-stage build)

## Arquitectura

```
┌───────────────────────────────┐
│  RabbitMQ                      │
│  Queue: schedules.queue        │
└────────┬──────────────────────┘
         │ @RabbitListener
         ▼
┌───────────────────────────┐
│  ScheduleConsumerService   │
│  (consume y escribe JSON)  │
└────────┬──────────────────┘
         │ objectMapper.writeValue()
         ▼
┌───────────────────────────┐
│  /app/update_rutas/        │
│  schedule_BUS-003_*.json   │
└───────────────────────────┘

┌─────────────────────────────┐
│  ScheduleConsumerController  │
│  GET /consumer-schedules/*   │
│  (listar y ver archivos)     │
└─────────────────────────────┘
```

## Estructura del Proyecto

```
consumer-schedules-buses-red/
├── src/main/java/com/busesred/consumer/schedules/
│   ├── ConsumerSchedulesApplication.java
│   ├── config/
│   │   └── RabbitMQConfig.java
│   ├── controller/
│   │   └── ScheduleConsumerController.java
│   ├── model/
│   │   └── ScheduleMessage.java
│   └── service/
│       └── ScheduleConsumerService.java
├── src/main/resources/
│   └── application.yml
├── Dockerfile
└── pom.xml
```

## Modelo de Datos (JSON)

Cada mensaje se guarda como archivo JSON con formato español snake_case:

```json
{
  "id_bus": "BUS-003",
  "ruta": "507",
  "nombre_ruta": "Ruta 507 - Centro",
  "hora_salida": "08:30:00",
  "hora_llegada": "09:15:00",
  "tipo_cambio": "SCHEDULE_UPDATE",
  "descripcion": "Actualización de horario matutino",
  "marca_tiempo": "2026-02-15T10:30:00",
  "origen": "Estación Central",
  "destino": "Providencia"
}
```

### Nombre de archivos generados

Formato: `schedule_{busId}_{yyyyMMdd_HHmmss_SSS}.json`

Ejemplo: `schedule_BUS-003_20260215_103000_123.json`

## Variables de Entorno

| Variable | Descripción | Default |
|---|---|---|
| `RABBITMQ_HOST` | Host de RabbitMQ | `rabbitmq` |
| `RABBITMQ_PORT` | Puerto de RabbitMQ | `5672` |
| `RABBITMQ_USERNAME` | Usuario RabbitMQ | *(requerido)* |
| `RABBITMQ_PASSWORD` | Contraseña RabbitMQ | *(requerido)* |

## Almacenamiento de Archivos

Los archivos JSON se guardan en `/app/update_rutas/` dentro del contenedor.

En Docker Compose se usa un volumen persistente:

```yaml
volumes:
  - schedules_data:/app/update_rutas
```

## Configuración RabbitMQ

| Recurso | Nombre |
|---|---|
| Exchange | `schedules.exchange` (TopicExchange) |
| Queue | `schedules.queue` (durable) |
| Routing Key | `schedules.routing.key` |

## Endpoints

### Listar archivos generados
```http
GET /consumer-schedules/files
```

**Respuesta (200 OK):**
```json
{
  "total_archivos": 2,
  "archivos": [
    {
      "nombre": "schedule_BUS-003_20260215_103000_123.json",
      "tamano": 320,
      "ultima_modificacion": "2026-02-15T10:30:00",
      "url_vista": "/api/consumer-schedules/files/schedule_BUS-003_20260215_103000_123.json"
    }
  ]
}
```

### Ver contenido de un archivo
```http
GET /consumer-schedules/files/{fileName}
```

**Respuesta (200 OK):** Contenido JSON del archivo.

### Health Check
```http
GET /consumer-schedules/health
```

## Docker

```bash
docker build --no-cache --platform linux/amd64 -t consumer-schedules-buses-red:latest .

docker run -p 8084:8084 \
  -e RABBITMQ_HOST=rabbitmq \
  -e RABBITMQ_USERNAME=admin \
  -e RABBITMQ_PASSWORD=admin123 \
  -v schedules_data:/app/update_rutas \
  consumer-schedules-buses-red:latest
```

## Puerto

| Servicio | Puerto |
|---|---|
| Consumer Schedules | `8084` |
