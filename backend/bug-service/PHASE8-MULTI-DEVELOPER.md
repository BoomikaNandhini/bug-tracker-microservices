# Phase 8 - Multi-developer bug assignment (Kafka disabled)

When a bug is created, all developers assigned to the project are copied into
BugReport.assignedDeveloperIds and all receive a notification through
Notification Service using OpenFeign.

assignedToId is retained as the primary/legacy assignee for backward compatibility.
Kafka is not used in this phase.
