# ADR 0003: REST for queries, RabbitMQ for workflow events

Gateway-to-service requests and simple reads use REST. The order saga and notifications use RabbitMQ so checkout does not synchronously depend on downstream completion.
