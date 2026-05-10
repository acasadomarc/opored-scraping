# opored-scraping

OpoRed Scraping is a Spring Boot microservice designed to periodically scrape public announcements from various official Spanish governmental bulletins. It filters these announcements for civil service examinations and related personnel news, then publishes the structured data to Apache Kafka topics for downstream consumption.

## Core Functionality

*   **Scheduled Scraping**: The service uses scheduled tasks to automatically fetch the latest publications from each source daily.
*   **Data Sources**: It integrates with the open data APIs of the following official gazettes:
    *   **BOE** (Boletín Oficial del Estado)
    *   **BOCYL** (Boletín Oficial de Castilla y León)
    *   **BOR** (Boletín Oficial de La Rioja)
*   **Data Processing**: For each source, the service parses the response (XML for BOE/BOR, JSON for BOCYL), filters for relevant sections such as "Oposiciones y Concursos" or "Autoridades y Personal", and extracts key details like the title, publication date, and URLs for HTML and PDF documents.
*   **Kafka Integration**: Processed announcements are structured into DTOs and sent to dedicated Kafka topics:
    *   `announcementsBOE-topic`
    *   `announcementsBOCYL-topic`
    *   `announcementsBOR-topic`
*   **Resilience**: The service is built with resilience in mind, featuring:
    *   A reactive `WebClient` for non-blocking I/O.
    *   Automatic retries for transient server-side errors (5xx).
    *   Graceful handling of client-side errors, particularly for dates when no bulletin is published (404 Not Found).
    *   Asynchronous processing to prevent blocking the main service threads.

## Architecture

The application's logic is modularized into several service components:

*   **`*ApiCallerService`**: (e.g., `BoeApiCallerService`) These services contain scheduled methods that initiate the scraping process for a specific data source at fixed times.
*   **`WebClientService`**: A central, reactive HTTP client that performs the API requests. It handles response statuses, retry logic, and delegates the raw response data to the appropriate parser.
*   **`*ExtractDataService`**: (e.g., `BoeExtractDataService`) A set of services implementing a common interface, each containing the specific logic to parse the data format (XML/JSON) from one source.
*   **`KafkaProducer`**: A service responsible for serializing the final data objects and sending them to the correct Kafka topic.

## Getting Started

### Prerequisites

*   Java 21 (JDK)
*   Maven 3.x
*   A running instance of Apache Kafka
*   Docker (optional, for containerization)

### Configuration

Before running the application, configure your Kafka connection in `src/main/resources/application.properties`:

```properties
# Kafka config
spring.kafka.bootstrap-servers=your-kafka-host:9092
```

### Running the Application

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/acasadomarc/opored-scraping.git
    cd opored-scraping
    ```

2.  **Build and run the application using Maven:**
    ```sh
    mvn spring-boot:run
    ```

The service will start, and the scheduled jobs will trigger automatically at their configured times (e.g., 10:00 and 16:00 for BOE).

## Running with Docker

The repository includes a `Dockerfile` to containerize the application.

1.  **Build the Docker image:**
    ```sh
    docker build -t opored-scraping .
    ```

2.  **Run the container:**
    You must provide the Kafka broker address as an environment variable.
    ```sh
    docker run -p 18082:18082 --name opored-scraper \
      -e SPRING_KAFKA_BOOTSTRAP_SERVERS="your-kafka-host:9092" \
      opored-scraping
    ```

## Continuous Integration

This project uses GitHub Actions for its CI/CD pipeline, defined in `.github/workflows/ci.yml`. The workflow automates the following steps on every push to the `main` branch:

1.  **Run Tests**: Executes the full suite of unit tests.
2.  **Code Analysis**: Performs a static analysis of the code with SonarQube.
3.  **Build a JAR**: Packages the application into an executable JAR file.
4.  **Build and Push Docker Image**: Builds a Docker image and pushes it to the GitHub Container Registry (`ghcr.io`).
