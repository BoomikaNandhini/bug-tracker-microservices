# Notification Service - Phase 8 (Kafka Disabled)

Kafka is intentionally disabled for the current migration stage.

## Current notification flow

Bug Service -> Eureka/OpenFeign -> Notification Service -> notification_db -> optional email

The internal endpoint is:

`POST /internal/notifications`

It accepts `CreateNotificationRequest` and stores an in-app notification. Email remains optional and is controlled by `notification.email.enabled`.

## Kafka status

The Kafka consumer/topic configuration and event classes are retained for the later Kafka phase, but the consumer/configuration beans are guarded by `@ConditionalOnProperty`.

Current setting:

```yaml
notification:
  kafka:
    enabled: false
```

Kafka does not need to be installed or running for the service to work in this stage.

## User notification APIs

- `GET /api/notifications`
- `GET /api/notifications/unread`
- `PUT /api/notifications/{id}/read`
- `PUT /api/notifications/read-all`

The current temporary identity mechanism is `X-User-Id`. Phase 9 will replace this with centralized JWT identity propagation.
