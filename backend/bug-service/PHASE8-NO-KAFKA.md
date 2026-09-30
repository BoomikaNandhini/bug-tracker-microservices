# Bug Service - Phase 8 (Kafka Disabled)

Kafka is intentionally disabled for the current migration stage.

## Current notification flow

Bug Service -> Eureka/OpenFeign -> Notification Service -> notification_db -> optional email

When a bug is created, Bug Service sends a synchronous `POST /internal/notifications` request to `notification-service` for:
- the assigned developer
- the project creator

If Notification Service is unavailable, the bug remains successfully created and a warning is logged.

## Kafka status

Kafka classes/configuration are retained for the later migration, but they are disabled with:

```yaml
notification:
  kafka:
    enabled: false
```

The Kafka publisher/listener/topic configuration is guarded by `@ConditionalOnProperty`, so Kafka does not need to be running now.

## Later Kafka migration

Set the property to `true`, configure Kafka, and replace the synchronous notification call with the existing event/publisher flow as part of the Kafka phase.

## Run order

1. Eureka Server - 8761
2. User Service - 8081
3. Project Service - 8082
4. Notification Service - 8084
5. Bug Service - 8083
6. API Gateway - 8080
7. Angular frontend

## Test

Create a bug assigned to a developer in a project. The notification service should receive the request and persist notifications. Then call:

- `GET /api/notifications` with the current `X-User-Id` header (temporary until Phase 9)
- `GET /api/notifications/unread`
- `PUT /api/notifications/{id}/read`
- `PUT /api/notifications/read-all`

No Kafka broker is required for this stage.
