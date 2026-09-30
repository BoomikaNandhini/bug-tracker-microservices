# Phase 8 - Project assignment notifications (Kafka disabled)

Project Service now uses synchronous OpenFeign calls to Notification Service.

When a project is created, every unique assigned developer and tester receives one notification.
Kafka is not used in this phase.

Flow:
Project Service -> User Service (get user details) -> Notification Service -> notification_db
