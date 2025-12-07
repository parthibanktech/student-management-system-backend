# Service Startup Order Guide

To avoid dependency errors and connection refusals, please start the services in the following order.

## Phase 1: Infrastructure (Must be running first)
These services provide the foundation (Database, Messaging, Service Discovery).

1.  **Docker Infrastructure** (PostgreSQL, Zookeeper, Kafka)
    *   *Command:* `docker-compose up -d postgres zookeeper kafka`
    *   *Wait:* Until `docker ps` shows them as healthy (approx. 30 seconds).

2.  **Discovery Service** (Eureka)
    *   *Port:* `4007`
    *   *Why:* All other services need to register here. If this isn't up, they will fail to connect.
    *   *Wait:* Until logs say "Started DiscoveryServiceApplication".

3.  **API Gateway**
    *   *Port:* `4000`
    *   *Why:* This is the entry point for all external requests. It fetches routes from the Discovery Service.

## Phase 2: Core Business Services (Independent)
These services depend on Phase 1 but not on each other. You can start them in parallel.

4.  **Student Service**
    *   *Port:* `4001`
    *   *Dependencies:* Postgres, Kafka, Discovery Service.

5.  **Course Service**
    *   *Port:* `4002`
    *   *Dependencies:* Postgres, Discovery Service.

6.  **Payment Service**
    *   *Port:* `4004`
    *   *Dependencies:* Postgres, Kafka, Discovery Service.

7.  **Library Service**
    *   *Port:* `4005`
    *   *Dependencies:* Postgres, Discovery Service.

## Phase 3: Dependent Business Services
These services rely on other business services to be fully functional.

8.  **Enrollment Service**
    *   *Port:* `4003`
    *   *Dependencies:* Student Service, Course Service (for validation).
    *   *Note:* It will start without them, but enrollment requests will fail if Student/Course services are down.

9.  **Notification Service**
    *   *Port:* `4006`
    *   *Dependencies:* Kafka (listens for events from Student/Payment/Enrollment).

--- 

## Quick Reference: Port Map
| Service | Port |
| :--- | :--- |
| API Gateway | 4000 |
| Student Service | 4001 |
| Course Service | 4002 |
| Enrollment Service | 4003 |
| Payment Service | 4004 |
| Library Service | 4005 |6667674R87IR
| Notification Service | 4006 |
| Discovery Service | 4007 |
