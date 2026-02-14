# High-Frequency Time-Series Aggregator

## Overview

The High-Frequency Time-Series Aggregator efficiently processes time-series data for high-load and fintech applications. It ingests thousands of events per second and facilitates rapid querying of aggregates.

## Features

- **High Throughput**: Ingests thousands of events per second.
- **Efficient Querying**: Supports fast aggregation (1-minute, 5-minute, 1-hour).
- **PostgreSQL Partitioning**: Utilizes time-based partitioning (daily, weekly).
- **BRIN Indexes**: Implements Block Range Indexes for optimized query performance.
- **Java Ring Buffer**: Collects events and flushes them to the database using raw JDBC with `reWriteBatchedInserts=true`.

## Technology Stack

- **Database**: PostgreSQL
- **Programming Language**: Java
- **Libraries**: JDBC

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/high-frequency-time-series-aggregator.git
   ```
2. Navigate to the project directory:
   ```bash
   cd high-frequency-time-series-aggregator
   ```
3. Configure PostgreSQL settings in the application properties file.

4. Build and run the application.

## Usage

- Start ingesting time-series data by sending events to the API endpoint.
- Query aggregates efficiently using SQL on partitioned tables.

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for improvements.

## License

This project is licensed under the MIT License. See the LICENSE file for details.