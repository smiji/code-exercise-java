# URL Shortener System Design

## 1. Core Workflows

### 1.1 Short URL Generation (No Alias Provided)

When a user requests a shortened URL without specifying a custom alias, the system handles the creation automatically:

- **Client Request:** The user submits a request containing the destination `long_url`.
- **Code Generation:** The system automatically computes a unique, 7-character alphanumeric string.
- **URL Construction:** This 7-character string is appended to the base domain to create the final short URL.
- **Persistence:** The mapping of the generated key, `long_url`, and metadata is saved to the SQLite database.

### 1.2 Short URL Generation (Custom Alias Provided)

When a user wants a personalized or branded link, the application performs an explicit verification:

- **Availability Check:** The application queries the SQLite database to see if the requested alias string already exists.
- **Handling Duplicates:** If the alias is already allocated, the system rejects the request and throws a conflict error.
- **Successful Assignment:** If available, the application reserves the alias, links it to the target `long_url`, and commits the record to the database.

---

## 2. Alphanumeric Code Generation Strategy

The generation of the unique 7-character string relies on a two-step translation process:

### Step 1: Unique Numeric ID Generation

To ensure sequential distribution and eliminate collisions, the application uses a lightweight variant of the **Twitter Snowflake ID** algorithm.

- It generates a **64-bit unsigned integer** that guarantees global uniqueness without requiring expensive database lookups.

### Step 2: Base62 Alphanumeric Encoding

The unique 64-bit integer is converted into a compact, URL-safe string format:

- **Character Set:** A pre-defined character array containing 62 characters: numbers (`0-9`), lowercase letters (`a-z`), and uppercase letters (`A-Z`).
- **Conversion Math:** The system loops exactly 7 times to enforce a strict **7-character fixed size** string. In each iteration, a modulo 62 operation (`id % 62`) determines the remainder.
- **Array Indexing:** This remainder acts as the exact array index to extract the character, after which the numeric ID is divided by 62 (`id / 62`) for the subsequent iteration step.

---

## 3. Cross-Cutting Concerns

### 3.1 Global Exception Handling

- An application-wide exception middleware interceptor captures runtime errors.
- It formats errors gracefully into standardized JSON payloads (e.g., handling validation errors for taken aliases or unexpected database connection losses).

### 3.2 In-Memory Caching (Dual LRU Strategy)

To optimize data retrieval times and minimize raw database reads, the system uses two separate **Least Recently Used (LRU)** caches:

- **Short-to-Long Cache:** Bypasses database lookups during incoming user redirections by mapping `short_url` -> `long_url`.
- **Long-to-Short Cache:** Avoids duplicating work during write operations by immediately resolving `long_url` -> `short_url` if the link was recently processed.

---

## 4. Database Schema

### Table: `url_details`

| Column Name    | Data Type | Constraints               | Notes                                          |
| :------------- | :-------- | :------------------------ | :--------------------------------------------- |
| `id`           | INTEGER   | PRIMARY KEY               | Unique system ID                               |
| `actual_url`   | TEXT      | UNIQUE, NOT NULL          | The destination web address                    |
| `shortend_url` | TEXT      | UNIQUE, NOT NULL          | The generated 7-character code or custom alias |
| `created_at`   | DATETIME  | DEFAULT CURRENT_TIMESTAMP | Automatically set on creation                  |
| `updated_at`   | DATETIME  | DEFAULT CURRENT_TIMESTAMP | Automatically modified on update               |
