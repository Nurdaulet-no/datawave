
---

# Datawave: High-Frequency Time-Series Aggregator

A Spring Boot service designed as a hardcore learning playground for ingesting high-volume tick events. It bypasses classic relational database bottlenecks by offloading concurrent writes and updates to in-memory ring buffers, writing to PostgreSQL partitions via dedicated background workers.

## Architecture (The "Survive 300 VUs" Edition)

* **REST API:** `TickController` handles tick writes (`POST`), range reads (`GET`), and range volume updates (`PATCH`).
* **Asynchronous Queues:** To prevent HikariCP connection pool exhaustion and PostgreSQL deadlocks under heavy concurrent load, **both** inserts and updates are offloaded to separate `ArrayBlockingQueue` buffers. The API returns `200 OK` immediately.
* **Workers:** Dedicated background daemon threads drain the queues.
* Inserts are batched (flushed every 50ms or 6,000 items) using JDBC `reWriteBatchedInserts=true`.
* Updates are processed strictly sequentially to guarantee zero database deadlocks.


* **Storage:** Raw SQL via `JdbcTemplate` targeting time-partitioned PostgreSQL tables (`ticks_YYYY_MM_DD`) with BRIN indexes on `created_at`.
* **Maintenance:** `AdminService` trims old partitions nightly at 03:00 via a `@Scheduled` job (because executing `TRUNCATE` under user load is a death sentence for the database).

## Configuration

Key settings required in `src/main/resources/application.yaml`:

* `spring.datasource.url`: JDBC URL (MUST include `reWriteBatchedInserts=true` for batching to work)
* `spring.datasource.hikari.maximum-pool-size`: 50 (Do not use the default 10 under high load)
* `spring.datasource.hikari.connection-timeout`: 10000 (Fail fast rather than hanging threads)

## API Reference

Base path: `/api/v1/ticks`

* `GET /?from=ISO_INSTANT&to=ISO_INSTANT&limit=N` — Fetch ticks in range (ordered desc).
* `POST /` — Async ingestion of a single tick.
* `PATCH /volume?delta=INT&from=ISO_INSTANT&to=ISO_INSTANT` — Async volume adjustment over a time range.

---

## Performance & Load Testing Results

This architecture was stress-tested using `k6` to find the breaking point of a synchronous relational database setup, and subsequently optimized with async queues.

### Test Configuration (`load-tests/mix.js`)

* **Hardware:** Local machine (Acer Aspire A515-45G)
* **Virtual Users (VUs):** 300 constant concurrent users
* **Duration:** 60 seconds
* **Traffic Profile:** 70% Ingest (`POST`), 20% Read (`GET`), 10% Update (`PATCH`)

### Exact k6 Output Metrics

> **Total Requests Processed:** 364,556
> **Throughput:** 6,071 RPS (Requests Per Second)
> **Failure Rate:** 0.00% (0 errors)

### Latency (HTTP Request Duration)

* **Median:** 18.09 ms
* **Average:** 48.61 ms
* **p(90):** 216.81 ms
* **p(95):** 250.23 ms
* **Max:** 1.50 s

## Honest Limitations & Troubleshooting

This is a learning project mutating towards high-load readiness. Be aware of the current architectural flaws:

1. **The Async Illusion (Queue Saturation):** The 6,000+ RPS is heavily reliant on the Tomcat threads returning immediately. The single background thread processing `PATCH` requests *will* fall behind if the 10% update traffic continuously exceeds PostgreSQL's sequential write speed. Over time, the 200,000-item queue will saturate, eventually blocking Tomcat and bringing the server down.
2. **Volatile Memory:** If the JVM crashes or the container restarts, any data sitting in the `ArrayBlockingQueue` is permanently lost. Production systems require a persistent broker like Apache Kafka.
3. **Tail Latency:** The `p(95)` of 250ms and `Max` of 1.5s is the trade-off for heavy JVM Garbage Collection and PostgreSQL struggling with concurrent read/write locks under extreme pressure.
4. **Strict Partitioning:** You *must* pre-create daily partitions (`ticks_YYYY_MM_DD`) with BRIN indexes. Ingesting data for a date without a corresponding partition will cause catastrophic SQL exceptions.

---