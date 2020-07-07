# stacks-java
Java Web API Application Scaffolding for Amido Stacks

## Run Locally

> ./mvnw spring-boot:run

## Build Docker Image

> docker build -t image-tag .

## Swagger/OAS

Automatically generated. Go to: http://localhost:9000/swagger/index.html

## Health check

Available at: http://localhost:9000/health
(This can also be configured to run on another port)

## CONFIGURATION

The following environment variables are required:

- AZURE_COSMOSDB_KEY
- AZURE_APPLICATION_INSIGHTS_INSTRUMENTATION_KEY

