# AGENTS.md

# MinLish AI Engineering Constitution

Version: 1.0
Project: MinLish

This file defines the REQUIRED engineering rules, architecture constraints, business invariants, and implementation laws for ALL AI agents and human contributors working on the MinLish project.

ALL AGENTS MUST READ THIS FILE BEFORE MAKING ANY CHANGE.

This file is the single source of truth for implementation behavior.

---

# 1. PROJECT OVERVIEW

MinLish is an Android vocabulary learning application focused on TOEIC and IELTS learning.

Core technologies:

- Android Kotlin
- Jetpack Compose
- MVVM
- NestJS Backend
- MySQL
- Prisma ORM
- REST API
- JWT Authentication
- SM-2 Spaced Repetition

System architecture:

```text
Android App
(Kotlin + Compose)
        ↓ REST API
NestJS Backend
        ↓
MySQL Database
```

The backend is ALWAYS the source of truth.

---

# 2. ABSOLUTE CORE PRINCIPLES

ALL AGENTS MUST FOLLOW THESE PRINCIPLES.

## REQUIRED

1. Preserve architecture consistency.
2. Make the smallest safe change possible.
3. Respect existing business rules.
4. Keep code readable and maintainable.
5. Follow modern Android and backend best practices.
6. Avoid unnecessary complexity.
7. Avoid unnecessary dependencies.
8. Preserve compatibility with MVP scope.
9. Keep backend logic in backend.
10. Keep UI logic in UI.

---

## STRICTLY FORBIDDEN

AGENTS MUST NOT:

1. Rewrite large parts of the project unnecessarily.
2. Change architecture without explicit request.
3. Introduce unrelated refactors.
4. Break existing API contracts.
5. Introduce business logic regressions.
6. Add random libraries.
7. Hardcode credentials or secrets.
8. Change database structure casually.
9. Replace existing patterns inconsistently.
10. Modify unrelated modules.

---

# 3. CRITICAL PRODUCT INVARIANTS

THESE RULES ARE ABSOLUTE.

BREAKING THESE RULES IS CONSIDERED A CRITICAL ERROR.

---

## 3.1 Favorites Rules

1. Favorites is NOT a separate table.
2. Favorites is a default USER deck.
3. Favorites deck is automatically created when a user registers.
4. Favorites deck MUST have:

```text
name = Favorites
is_default = true
deck_type = USER
```

5. DO NOT create:

```text
user_favorite_vocabularies
favorite_vocabularies
user_favorites
```

6. Favorites is implemented by copying vocabulary into the Favorites deck.
7. Copied vocabulary MUST store:

```text
source_vocabulary_id
```

8. Favorites duplicate checking MUST use:

```text
source_vocabulary_id
```

9. Favorites deck MUST NOT allow direct CSV import.
10. Favorites deck MUST NOT allow manual vocabulary insertion in MVP.
11. Favorites deck MUST NOT be deletable.

---

## 3.2 Vocabulary Rules

1. Vocabulary belongs to EXACTLY ONE deck.
2. DO NOT create many-to-many deck-vocabulary relationships.
3. DO NOT create:

```text
deck_vocabularies
vocabulary_decks
```

4. Exact duplicate vocabulary is defined by:

```text
deck_id + normalized_word + normalized_meaning
```

5. Same word with different meaning IS ALLOWED.
6. Learning and review are based on vocabulary entry, NOT word.
7. Vocabulary identity is ALWAYS:

```text
vocabulary_id
```

---

## 3.3 ReviewCard Rules

1. ReviewCard MUST be based on:

```text
user_id + vocabulary_id
```

2. DO NOT add:

```text
deck_id
```

into:

```text
review_cards
```

3. Deck review is implemented by filtering:

```text
vocabularies.deck_id
```

4. SM-2 state MUST remain global per vocabulary.

---

## 3.4 SM-2 Rules

1. Backend computes SM-2.
2. Android MUST NOT compute SM-2 intervals.
3. Android only sends:

```text
AGAIN
HARD
GOOD
EASY
```

4. Backend computes:

- repetition
- interval_days
- ease_factor
- due_at

5. SM-2 logic MUST remain centralized.
6. SM-2 logic MUST NOT be duplicated in frontend.

---

## 3.5 Import Rules

1. CSV import MUST support partial success.
2. Duplicate rows MUST be skipped.
3. Invalid rows MUST NOT fail the entire file.
4. Import MUST return detailed report.
5. Import MUST use batch validation.
6. Import MUST use batch DB operations.
7. Import MUST NOT query DB row-by-row.

---

# 4. REQUIRED ARCHITECTURE

## Android Architecture

Required:

```text
UI Layer
↓
ViewModel
↓
Repository
↓
Remote/Data Source
```

Business logic MUST NOT exist directly inside composables.

---

## Backend Architecture

Required:

```text
Controller
↓
Service
↓
Repository / Prisma
↓
Database
```

Controllers MUST remain thin.

Business logic MUST exist inside services.

---

# 5. ANDROID RULES

## Jetpack Compose Rules

ALL COMPOSABLES MUST:

1. Be small and reusable.
2. Support Modifier parameter.
3. Avoid unnecessary recomposition.
4. Use state hoisting when appropriate.
5. Separate UI and business logic.
6. Use immutable UI state when possible.

---

## Compose Restrictions

DO NOT:

1. Put networking inside composables.
2. Put database access inside composables.
3. Put business logic inside composables.
4. Create giant composable files.
5. Use global mutable state carelessly.

---

## State Management

Preferred:

- ViewModel
- StateFlow
- collectAsState()
- remember
- immutable UI state

---

# 6. BACKEND RULES

## Backend is Source of Truth

Backend MUST own:

- authentication
- authorization
- validation
- duplicate checking
- SM-2 computation
- import processing
- review scheduling
- business invariants

Android MUST NOT bypass backend logic.

---

## Validation Rules

ALL INPUTS MUST BE VALIDATED.

Required:

- DTO validation
- request validation
- ownership validation
- duplicate validation
- authorization validation

---

## Ownership Rules

Users MUST ONLY modify their own:

- USER decks
- USER vocabularies
- USER settings
- USER progress

Users MUST NOT modify:

- SYSTEM decks
- SYSTEM vocabularies
- Other users' data

---

## Entity & Swagger Decorator Rules

1. **Multi-Line for Multi-Option Decorators:**
   Any `@ApiProperty` or `@ApiPropertyOptional` decorator containing **more than one key** in its options object **must** be written in multi-line format with a trailing comma.

   Example (Correct):

   ```ts
   @ApiProperty({
     example: 4,
     description: 'Quality 0–5 tương ứng AGAIN/HARD/GOOD/EASY',
   })
   @Expose()
   quality: number;
   ```

   Example (Incorrect - STRICTLY FORBIDDEN):

   ```ts
   @ApiProperty({ example: 4, description: 'Quality 0–5 tương ứng...' })
   ```

2. **Single-Option Exception:**
   Decorators may be written on a single line only if they contain a single property (e.g. `@ApiProperty({ example: 'uuid-v4' })`).

3. **Response Serialization:**
   Always decorate entity fields with `@Expose()` from `class-transformer` to control response serialization. Do not expose sensitive fields such as `passwordHash` in response entities.

4. **Circular Dependency Prevention:**
   DO NOT use the barrel index `src/entities/index.ts` to import entities inside their own or closely related modules. Doing so can cause Circular Dependency warnings or errors in NestJS. Use direct relative imports instead.

---

# 7. DATABASE RULES

## Database Design Rules

1. Use UUID IDs.
2. Use soft delete where defined.
3. Preserve foreign keys.
4. Preserve indexes.
5. Preserve constraints.
6. Preserve generated columns.
7. Preserve normalization logic.

---

## Forbidden Database Changes

DO NOT:

1. Remove soft delete.
2. Remove constraints casually.
3. Add unnecessary many-to-many tables.
4. Duplicate business state.
5. Store derived data inconsistently.
6. Change review card identity rules.

---

## Prisma 7.x Connection Rules

1. **No Hardcoded Connection URL in Schema:**
   The `datasource db` block in `prisma/schema.prisma` **must not** contain a direct `url` property. The URL is passed dynamically at runtime.
2. **Database Client Adapter Required:**
   Do not instantiate `PrismaClient` with default constructors. You **must** configure and pass the `PrismaMariaDb` adapter:

   ```ts
   import { PrismaClient } from "@prisma/client";
   import { PrismaMariaDb } from "@prisma/adapter-mariadb";

   const adapter = new PrismaMariaDb(connectionOptions);
   const prisma = new PrismaClient({ adapter });
   ```

3. **Database Migration & Seeding:**
   Seed scripts are configured under `migrations.seed` inside `prisma.config.ts` rather than `package.json`.
4. **Soft Delete Implementation:**
   Implement soft deletes using `deletedAt: DateTime | null`. Always use `PrismaService.softDelete(delegate, id)` instead of hard-deleting core data.

---

# 8. API RULES

## REST Rules

APIs MUST:

1. Use REST naming consistency.
2. Return consistent response structure.
3. Validate request body.
4. Use JWT authentication.
5. Return proper HTTP status codes.
6. Preserve backward compatibility when possible.

---

## Error Response Rules

Error responses SHOULD include:

```json
{
  "code": "ERROR_CODE",
  "message": "Human readable message"
}
```

---

# 9. IMPORT RULES

## CSV Rules

Required CSV fields:

```text
word
meaning
```

Optional fields:

- pronunciation
- description_en
- example
- collocation
- related_words
- note

---

## Duplicate Handling

Exact duplicate:

```text
deck_id + normalized_word + normalized_meaning
```

Allowed:

```text
Same word + different meaning
```

---

# 10. NORMALIZATION RULES

Backend MUST normalize:

- word
- meaning

Required normalization:

1. trim
2. lowercase
3. collapse multiple spaces

Example:

```ts
function normalizeText(value: string): string {
  return value.trim().toLowerCase().replace(/\s+/g, " ");
}
```

---

# 11. SECURITY RULES

NEVER:

1. Store plain passwords.
2. Expose secrets.
3. Commit credentials.
4. Hardcode API keys.
5. Expose private tokens.

Required:

- bcrypt
- JWT
- validation
- ownership checks
- authorization checks

---

# 12. GIT RULES

## Commit Rules

Commit messages SHOULD use:

```text
feat:
fix:
refactor:
docs:
style:
```

Examples:

```text
feat: add flashcard review API
fix: resolve duplicate vocabulary validation
refactor: extract SM2 service
```

---

## Forbidden Files

DO NOT COMMIT:

```text
.idea/
.gradle/
build/
local.properties
.env
*.keystore
*.jks
```

---

# 13. CODE QUALITY RULES

Code MUST be:

- readable
- maintainable
- modular
- predictable
- consistent

Prefer:

- explicit code
- simple architecture
- reusable components
- small functions

Avoid:

- overengineering
- premature optimization
- magic logic
- giant classes

---

# 14. PERFORMANCE RULES

Required:

1. Avoid unnecessary recomposition.
2. Use pagination for large lists.
3. Use indexed queries.
4. Use batch DB operations.
5. Avoid N+1 query patterns.
6. Avoid unnecessary API calls.

---

# 15. LỜI KHUYÊN CHO DEV ANDROID KHI TRIỂN KHAI

Navigation: Nên có một MainNavGraph để điều hướng. Nếu kiểm tra DataStore thấy có accessToken, tự động mở màn hình chính (HomeScreen), ngược lại thì mở màn hình đăng nhập (LoginScreen).
Dependency Injection: Khuyến nghị sử dụng Hilt (Dagger Hilt) để quản lý việc truyền (inject) AuthRepository, OkHttpClient có Interceptor vào các ViewModels nhằm giữ code sạch sẽ và dễ viết Unit Test.

---

# 16. MVP SCOPE PROTECTION

MVP PRIORITIES:

1. Stable authentication.
2. Stable deck/vocabulary management.
3. Stable flashcard learning.
4. Stable SM-2 review.
5. Stable CSV import.
6. Stable Favorites flow.

DO NOT introduce:

- marketplace systems
- advanced sync systems
- AI generation systems
- complex offline sync
- unnecessary microservices
- unnecessary abstractions

---

# 17. AI AGENT WORKFLOW

ALL AGENTS MUST:

## Step 1

Read:

- AGENTS.md
- relevant requirements
- relevant schema
- related files

---

## Step 2

Understand:

- business rules
- architecture constraints
- affected modules
- ownership rules

---

## Step 3

Implement:

- minimal safe changes
- architecture-consistent logic
- reusable code
- validated behavior

---

## Step 4

Verify:

- business rules preserved
- API compatibility preserved
- no architecture regressions
- no invariant violations

---

## Step 5

Explain:

- what changed
- why it changed
- affected files
- migration steps if needed

---

# 17. FINAL ENGINEERING LAW

The MinLish project values:

> Architecture consistency over personal preference.
> Business correctness over clever implementations.
> Maintainability over complexity.
> Small safe changes over massive rewrites.
> Predictability over abstraction.
> Stable MVP delivery over feature explosion.

ALL AGENTS MUST FOLLOW THESE RULES STRICTLY.
