# GraphQL Demo Application

Spring Boot GraphQL workshop with slides and live demo.

Presenataion link: https://lstrihic.github.io/graphql-cop

## Prerequisites

- Java 17
- Maven 3.6+
- Node.js 14+

## Run Slides

```bash
npm install
npm run dev
```

Slides will be available at http://localhost:3030

## Run Application

```bash
./mvnw spring-boot:run
```

Application runs at http://localhost:8080

GraphQL endpoint: http://localhost:8080/graphql
GraphiQL UI: http://localhost:8080/graphiql

## Build

**Slides:**
```bash
npm run build
npm run export
```

**Application:**
```bash
./mvnw clean package
java -jar target/graphql-0.0.1-SNAPSHOT.jar
```
