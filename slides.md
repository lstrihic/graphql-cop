---
theme: default
background: https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1920
class: text-center
highlighter: shiki
lineNumbers: true
info: |
  ## GraphQL with Spring Boot
  Solving the N+1 Problem with @BatchMapping
drawings:
  persist: false
transition: slide-left
title: GraphQL with Spring Boot
mdc: true
---

# GraphQL with Spring Boot

Solving the N+1 Problem with @BatchMapping

---
transition: fade-out
---

# What is GraphQL?

A query language for your API

<v-clicks>

- **Query exactly what you need** - No over-fetching or under-fetching
- **Single endpoint** - `/graphql` handles all operations
- **Strongly typed schema** - Self-documenting API
- **Nested queries** - Fetch related data in one request
- **Real-time subscriptions** - Live data updates

</v-clicks>

<div v-click class="mt-8 p-4 bg-blue-500 bg-opacity-10 rounded">
Instead of multiple REST endpoints, GraphQL provides a flexible query interface
</div>

---
transition: slide-up
---

# REST vs GraphQL

The fundamental difference

<div grid="~ cols-2 gap-4" class="mt-4">

<div>

## REST API
```http
GET /disclosures
GET /disclosures/1/author
GET /disclosures/1/comments
GET /comments/1/author
GET /comments/2/author
```

**Problems:**
- Multiple round trips
- Over-fetching data
- Under-fetching (need more requests)

</div>

<div>

## GraphQL API
```graphql
query {
  disclosure(id: "1") {
    title
    author { name }
    comments {
      content
      author { name }
    }
  }
}
```

**Benefits:**
- Single request
- Exact data needed
- Strongly typed

</div>

</div>

---
layout: two-cols
---

# GraphQL Schema

Define your API contract

```graphql
type Disclosure {
  id: ID!
  title: String!
  description: String!
  severity: Severity!
  author: Author!
  comments: [Comment!]!
  commentsCount: Int!
  isRecent: Boolean!
}

type Author {
  id: ID!
  name: String!
  email: String!
  disclosures: [Disclosure!]!
}
```

::right::

<div class="ml-4">

## Schema Features

<v-clicks>

- **Types** define data structure
- **!** means required (non-null)
- `[Type!]!` is required list of required items
- **Relationships** between types
- **Custom scalars** (ID, String, Int, etc.)

</v-clicks>

<div v-click class="mt-8 p-4 bg-green-500 bg-opacity-10 rounded">
Schema = Single source of truth for your API
</div>

</div>

---
transition: slide-up
---

# The N+1 Problem

The biggest performance issue in GraphQL

```graphql
query {
  disclosures { author { name } }
}
```

<v-clicks>

<div class="mt-3 p-3 bg-red-500 bg-opacity-20 rounded text-xs">

**Problem:** 1 query for disclosures + N queries for authors = **1 + N**

Example: 100 disclosures = **101 database calls!**

</div>

<div class="mt-3 p-3 bg-yellow-500 bg-opacity-20 rounded text-xs">

**Without batching:**
```txt
DisclosureService.findAll called
AuthorService.findById: A1  ← N+1!
AuthorService.findById: A2
...
```

</div>

</v-clicks>

---
transition: slide-up
layout: center
class: text-center
---

# Solution: @BatchMapping

Spring GraphQL's automatic batching

---
transition: fade-out
---

# @BatchMapping Annotation

Automatic N+1 problem solver

```java {all|1-2|3-7|9-10|12-14|16-21|all}
@BatchMapping(typeName = "Disclosure", field = "author")
public Map<Disclosure, Author> author(List<Disclosure> disclosures) {
    // 1. Extract all author IDs (deduplicated)
    List<String> authorIds = disclosures.stream()
            .map(Disclosure::getAuthorId)
            .distinct()
            .toList();

    // 2. Single batched database call
    List<Author> authors = authorService.findByIds(authorIds);

    // 3. Create lookup map
    Map<String, Author> authorMap = authors.stream()
            .collect(Collectors.toMap(Author::getId, a -> a));

    // 4. Map disclosures to their authors
    return disclosures.stream()
            .collect(Collectors.toMap(
                disclosure -> disclosure,
                disclosure -> authorMap.get(disclosure.getAuthorId())
            ));
}
```

---
transition: slide-up
---

# How @BatchMapping Works

Spring GraphQL collects and batches requests automatically

<div class="mt-8">

```mermaid
%%{init: {'theme':'dark'}}%%
graph LR
    A[GraphQL Query] --> B[Fetch Disclosures]
    B --> C[Spring Collects<br/>Author Requests]
    C --> D[BatchMapping<br/>Called Once]
    D --> E[Single DB Query<br/>for All Authors]
    E --> F[Map Results Back]
    F --> G[Return Response]

    style C fill:#86efac,stroke:#22c55e,stroke-width:3px,color:#000
    style D fill:#4ade80,stroke:#16a34a,stroke-width:3px,color:#000
    style E fill:#22c55e,stroke:#15803d,stroke-width:3px,color:#fff
```

</div>

<v-clicks>

<div class="mt-8 grid grid-cols-2 gap-4">

<div class="p-4 bg-red-500 bg-opacity-20 rounded">

**Without @BatchMapping:**
- 1 query for disclosures
- 100 queries for authors
- Total: **101 queries**
- Time: ~10 seconds

</div>

<div class="p-4 bg-green-500 bg-opacity-20 rounded">

**With @BatchMapping:**
- 1 query for disclosures
- 1 batched query for authors
- Total: **2 queries**
- Time: ~200ms

</div>

</div>

</v-clicks>

---
transition: fade-out
---

# Service Layer Implementation

Batched methods for efficiency

<div grid="~ cols-2 gap-4">

<div>

## Without Batching ❌
```java
public Optional<Author> findById(String id) {
    logger.info("findById: {}", id);
    simulateLatency(); // 100ms
    return Optional.ofNullable(
        authors.get(id)
    );
}
```

**Result:** Called N times
```
AuthorService.findById: A1
AuthorService.findById: A2
AuthorService.findById: A1  ← Duplicate!
```

</div>

<div>

## With Batching ✅
```java
public List<Author> findByIds(List<String> ids) {
    logger.info("findByIds: {} (BATCHED)",
        ids.size());
    simulateLatency(); // 100ms once
    return ids.stream()
        .map(authors::get)
        .filter(Objects::nonNull)
        .toList();
}
```

**Result:** Called once
```
AuthorService.findByIds: 2 (BATCHED)
```

</div>

</div>

---
transition: slide-up
---

# Nested Batching

@BatchMapping works at every level

```java {all|1-2|3-5|7-9|11-15|all}
@BatchMapping(typeName = "Disclosure", field = "comments")
public Map<Disclosure, List<Comment>> comments(List<Disclosure> disclosures) {
    List<String> disclosureIds = disclosures.stream()
            .map(Disclosure::getId)
            .toList();

    // Single batched call for all comments
    Map<String, List<Comment>> commentsMap =
        commentService.findByDisclosureIds(disclosureIds);

    return disclosures.stream()
            .collect(Collectors.toMap(
                disclosure -> disclosure,
                disclosure -> commentsMap.getOrDefault(disclosure.getId(), List.of())
            ));
}

@BatchMapping(typeName = "Comment", field = "author")
public Map<Comment, Author> author(List<Comment> comments) {
    // Batches author fetches for comments too!
    List<String> authorIds = comments.stream()...
}
```

---
transition: fade-out
zoom: 0.7
---

# Console Output: N+1 Solved

Real application logs showing batching

<div class="mt-2">

```log {all|1|2|3|4|all}
DisclosureService.findAll called
AuthorService.findByIds called for 2 authors (BATCHED)  ← Single call!
CommentService.findByDisclosureIds called for 3 disclosures (BATCHED)
AuthorService.findByIds called for 3 authors (BATCHED)  ← Nested batching!
```

</div>

<div class="mt-3 p-4 bg-green-500 bg-opacity-20 rounded">

**Total: 4 database calls** for a complex nested query with 3 disclosures, their authors, comments, and comment authors!

</div>

<v-click>

<div class="mt-3 text-xs">

## Performance Impact

| Scenario | Without Batching | With @BatchMapping |
|----------|------------------|-------------------|
| 3 disclosures + authors | 4 calls, 400ms | 2 calls, 200ms |
| 3 disclosures + authors + comments | 8 calls, 800ms | 4 calls, 400ms |
| 100 disclosures + authors | 101 calls, 10s | 2 calls, 200ms |
| 100 disclosures + 10 comments each | 1,201 calls, 2min | 4 calls, 400ms |


<div class="text-xs mt-3">

**Without batching:**

- **Row 1:** 1 (disclosures) + 3 (authors, 1 per disclosure) = 4 calls
- **Row 2:** 1 + 3 (authors) + 3 (comments) + 1 (comment authors batched) = 8 calls
- **Row 3:** 1 + 100 (authors, 1 per disclosure) = 101 calls
- **Row 4:** 1 + 100 + 100 + 1000 (comment authors, 1 per comment) = 1,201 calls

</div>

</div>

</v-click>

---
transition: slide-up
---

# Queries in Spring GraphQL

Simple and powerful

<div grid="~ cols-2 gap-4">

<div>

## Simple Query
```java
@QueryMapping
public Disclosure disclosure(@Argument String id) {
    return disclosureService
        .findById(id)
        .orElse(null);
}

@QueryMapping
public List<Disclosure> disclosures() {
    return disclosureService.findAll();
}
```

</div>

<div>

## Usage
```graphql
query {
  disclosure(id: "1") {
    id
    title
    severity
  }

  disclosures {
    id
    title
  }
}
```

</div>

</div>

---
transition: fade-out
zoom: 0.9
---

# Computed Fields

Server-side calculated values

```java
@SchemaMapping(typeName = "Disclosure", field = "isRecent")
public boolean isRecent(Disclosure disclosure) {
    long daysSince = ChronoUnit.DAYS.between(
        disclosure.getPublishedDate(), LocalDate.now());
    return daysSince <= 30;
}

@SchemaMapping(typeName = "Disclosure", field = "commentsCount")
public int commentsCount(Disclosure disclosure) {
    return commentService
        .findByDisclosureId(disclosure.getId())
        .size();
}
```

<v-click>

<div class="mt-4">

```graphql
query {
  disclosures {
    title
    isRecent        # Computed server-side
    commentsCount   # Computed server-side
  }
}
```

</div>

</v-click>

---
transition: slide-up
zoom: 0.8
---

# Mutations

Modifying data with type safety

<div grid="~ cols-2 gap-4">

<div class="text-xs">

#### Input Class
```java
public class CreateDisclosureInput {
    private String title;
    private String description;
    private Severity severity;
    private String authorId;

    // getters and setters
}
```

#### Mutation Resolver
```java
@MutationMapping
public Disclosure createDisclosure(
    @Argument CreateDisclosureInput input) {

    return disclosureService.create(
        input.getTitle(),
        input.getDescription(),
        input.getSeverity(),
        input.getAuthorId()
    );
}
```

</div>

<div>

#### GraphQL Schema
```graphql
input CreateDisclosureInput {
  title: String!
  description: String!
  severity: Severity!
  authorId: String!
}

type Mutation {
  createDisclosure(
    input: CreateDisclosureInput!
  ): Disclosure!
}
```

#### Usage
```graphql
mutation {
  createDisclosure(input: {
    title: "New Vulnerability"
    description: "Critical issue"
    severity: HIGH
    authorId: "A1"
  }) {
    id
    title
  }
}
```

</div>

</div>

---
transition: fade-out
layout: two-cols
zoom: 0.8
---

# Spring GraphQL Annotations

Complete toolkit

<v-clicks>

## Query & Mutation
- `@QueryMapping` - Query resolvers
- `@MutationMapping` - Mutation resolvers
- `@Argument` - Type-safe arguments

## Field Resolution
- `@SchemaMapping` - Computed fields
- `@BatchMapping` - N+1 solution

## Input
- Input classes for type safety
- No `Map<String, Object>`!

</v-clicks>

::right::

<!-- <div class="ml-4">

<v-clicks>

## Benefits

- ✅ Type-safe at compile time
- ✅ No manual DataLoader config
- ✅ Automatic batching
- ✅ Automatic deduplication
- ✅ Works at every nesting level
- ✅ IDE autocomplete support
- ✅ Clean, readable code

</v-clicks>

<div v-click class="mt-8 p-4 bg-blue-500 bg-opacity-10 rounded">
Spring GraphQL handles the complexity - you write clean business logic
</div>

</div> -->

---
transition: slide-up
layout: center
class: text-center
---

# Live Demo

Let's see it in action

http://localhost:8080/graphiql

---
transition: fade-out
---

# Demo: N+1 Problem vs Solution

Side-by-side comparison

<div grid="~ cols-2 gap-4" class="mt-4 text-xs">

<div>

## ❌ Without @BatchMapping

```graphql
query {
  disclosuresNPlusOne {
    id
    title
    author { name }
  }
}
```

**Console Output:**
```log
DisclosureService.findAll
AuthorService.findById: A1
AuthorService.findById: A2
AuthorService.findById: A1
```

<div class="p-2 bg-red-500 bg-opacity-20 rounded mt-2">
4 calls, ~400ms, duplicates!
</div>

</div>

<div>

## ✅ With @BatchMapping

```graphql
query {
  disclosures {
    id
    title
    author { name }
  }
}
```

**Console Output:**
```log
DisclosureService.findAll
AuthorService.findByIds: 2 (BATCHED)
```

<div class="p-2 bg-green-500 bg-opacity-20 rounded mt-2">
2 calls, ~200ms, deduplicated!
</div>

</div>

</div>

---
transition: fade-out
zoom: 0.8
---

# Demo: Nested Batching

Complex query with multiple levels

```graphql
query {
  disclosures {
    id
    title
    author { name }
    comments {
      content
      author { name }
    }
  }
}
```

**Console Output:**
```log
DisclosureService.findAll called
AuthorService.findByIds: 2 authors (BATCHED)
CommentService.findByDisclosureIds: 3 disclosures (BATCHED)
AuthorService.findByIds: 3 authors (BATCHED)
```

<div class="mt-3 p-3 bg-green-500 bg-opacity-20 rounded text-sm">

**Only 4 database calls** for entire nested query!

</div>


---
transition: slide-up
---

# Demo: Create Disclosure

Mutation with type-safe input

```graphql
mutation {
  createDisclosure(input: {
    title: "XSS Vulnerability in Comment Section"
    description: "User input not properly sanitized"
    severity: HIGH
    authorId: "A1"
  }) {
    id
    title
    severity
    author {
      name
    }
    isRecent
  }
}
```

---
transition: slide-up
---

# Best Practices Implemented

Production-ready code

<v-clicks>

- ✅ **@BatchMapping** for all relationships - No N+1 problems
- ✅ **Type-safe input classes** - No Map<String, Object>
- ✅ **Automatic deduplication** - Efficient data fetching
- ✅ **Constructor injection** - Immutable dependencies
- ✅ **Proper logging** - Observability for batching
- ✅ **Clean separation** - Controller, Service, Model layers
- ✅ **Computed fields** - Server-side calculations
- ✅ **N+1 demo endpoint** - Compare with and without batching

</v-clicks>

<div v-click class="mt-8 p-4 bg-blue-500 bg-opacity-10 rounded">
This is production-grade GraphQL implementation with Spring Boot
</div>

---
transition: fade-out
---

# Project Structure

Well-organized codebase

```
src/main/java/hr/truenorth/graphql/graphql/
├── controller/
│   ├── DisclosureController.java    # @BatchMapping examples
│   ├── N1DemoController.java        # N+1 problem demo
│   ├── AuthorController.java        # Nested batching
│   └── CommentController.java       # Comment batching
├── input/
│   ├── CreateDisclosureInput.java   # Type-safe mutations
│   ├── UpdateDisclosureInput.java
│   └── CommentInput.java
├── model/
│   ├── Disclosure.java              # Domain models
│   ├── Author.java
│   └── Comment.java
└── service/
    ├── DisclosureService.java       # Business logic
    ├── AuthorService.java           # Batched operations
    └── CommentService.java
```

---
transition: slide-up
---

# Configuration

Simple Spring Boot setup

```properties
spring.application.name=graphql

# GraphQL Configuration
spring.graphql.graphiql.enabled=true
spring.graphql.path=/graphql
spring.graphql.schema.locations=classpath:graphql/
spring.graphql.schema.file-extensions=.graphql,.graphqls
spring.graphql.schema.introspection.enabled=true
spring.graphql.schema.printer.enabled=true

# Logging
logging.level.org.springframework.graphql=DEBUG
```

<div class="mt-8">

## Dependencies
- Spring Boot 3.5.7
- Spring for GraphQL (included)
- That's it! No extra libraries needed

</div>

---
transition: fade-out
zoom: 0.9
---

# Key Takeaways

What you learned today

<v-clicks>

1. **GraphQL vs REST** - Single endpoint, flexible queries, strong typing

2. **N+1 Problem** - The performance killer in GraphQL APIs

3. **@BatchMapping Solution** - Spring GraphQL's automatic batching
   - No manual DataLoader configuration
   - Works at every nesting level
   - Automatic deduplication

4. **Type Safety** - Input classes and POJO objects everywhere

5. **Best Practices** - Production-ready patterns with Spring Boot

</v-clicks>

<div v-click class="mt-8 p-4 bg-green-500 bg-opacity-20 rounded">

**Result:** Fast, maintainable, type-safe GraphQL APIs with Spring Boot

</div>

---
layout: center
---

# Questions?

<div class="mt-8">

**Resources:**
- GraphiQL: http://localhost:8080/graphiql
- Spring GraphQL Docs: https://spring.io/projects/spring-graphql

</div>

<div class="mt-8 text-xl">

Thank you!

</div>

---
layout: end
---

# Thank You

Happy GraphQL Development with Spring Boot! 🚀
