# Add Search and Sorting Tests to Backend Driver Tests

This plan adds more comprehensive search and sorting verification to the existing `tests/scenario_verifier.py` integration test suite.

## Proposed Changes

### [Backend Tests]

#### [MODIFY] [scenario_verifier.py](file:///Users/daehyoung/ws/md-note/tests/scenario_verifier.py)

-   **Implement `test_search_scenarios()`**:
    -   Create 3 distinct documents:
        -   `doc1`: Title="Alpha Unique Search", Tags=["test-tag-1"]
        -   `doc2`: Title="Beta Unique Search", Tags=["test-tag-2"]
        -   `doc3`: Title="Gamma", Tags=["test-tag-1", "test-tag-3"]
    -   Verify search by title keyword ("Unique Search") returns `doc1` and `doc2`.
    -   Verify search by tag ("test-tag-1") returns `doc1` and `doc3`.
    -   Verify search for non-existent keyword returns 0 results.
-   **Implement `test_sorting_scenarios()`**:
    -   Create 3 documents with predictable titles: "A-Sort", "B-Sort", "C-Sort".
    -   Verify `sort=title,asc` returns them in order [A, B, C].
    -   Verify `sort=title,desc` returns them in order [C, B, A].
    -   Verify `sort=createdAt,desc` returns them in chronological reverse order (C then B then A).
-   **Implement `test_sorting_by_author()`**:
    -   Need a separate admin session.
    -   `test` user creates a doc: "Test-Auth-Doc".
    -   `admin` user creates a doc: "Admin-Auth-Doc".
    -   Verify `sort=author.username,asc` returns "Admin-Auth-Doc" first (assuming 'admin' < 'test').
    -   Verify `sort=author.username,desc` returns "Test-Auth-Doc" first.
-   Update `run()` method with full suite.

-   Update `run()` method with full suite.

### [Backend Paging]

#### [MODIFY] [DocumentRepository.java](file:///Users/daehyoung/ws/md-note/backend/src/main/java/kr/luxsoft/mdnote/repository/DocumentRepository.java)
-   Update `searchDocuments` signature: `Page<Document> searchDocuments(String query, Pageable pageable)`

#### [MODIFY] [DocumentService.java](file:///Users/daehyoung/ws/md-note/backend/src/main/java/kr/luxsoft/mdnote/service/DocumentService.java)
-   Update `searchDocuments` method to accept `Pageable` and return `Page<Document>`.

#### [MODIFY] [DocumentController.java](file:///Users/daehyoung/ws/md-note/backend/src/main/java/kr/luxsoft/mdnote/controller/DocumentController.java)
-   Add `@GetMapping("/search")` endpoint that calls `documentService.searchDocuments`.

#### [MODIFY] [scenario_verifier.py](file:///Users/daehyoung/ws/md-note/tests/scenario_verifier.py)
-   Implement `test_paging_search()`:
    -   Create 5 docs with a common keyword.
    -   Request page 0 with size 2.
    -   Verify result has 2 docs and `totalElements` is 5.
    -   Verify total pages is 3.

### [User Management Paging]

#### [MODIFY] [AdminController.java](file:///Users/daehyoung/ws/md-note/backend/src/main/java/kr/luxsoft/mdnote/controller/AdminController.java)
-   Update `/api/admin/users` to return `Page<User>` instead of `List<User>`.
-   Accept `Pageable` parameter.

#### [MODIFY] [admin.js](file:///Users/daehyoung/ws/md-note/frontend/src/stores/admin.js) (Store)
-   Update `fetchUsers` to handle paged response.
-   Add state for `page`, `size`, `totalPages`, `totalElements`.

#### [MODIFY] [UserManageView.vue](file:///Users/daehyoung/ws/md-note/frontend/src/views/UserManageView.vue)
-   Configure `v-data-table` for server-side paging.
-   Handle `update:options` to fetch paged data.

### [Backend API Documentation]

#### [MODIFY] [OpenAPIConfig.java](file:///Users/daehyoung/ws/md-note/backend/src/main/java/kr/luxsoft/mdnote/config/OpenAPIConfig.java)
- Ensure JWT Bearer Authentication is correctly configured.

#### [MODIFY] Multiple Controllers
Add OpenAPI annotations to improve the Swagger documentation:

- **AuthController**:
    - `@Tag(name = "Authentication")`
    - `@Operation(summary = "User Login")` for `/login`
- **DocumentController**:
    - `@Tag(name = "Documents")`
    - `@Operation` for `getAllDocuments`, `searchDocuments`, `getDocumentById`, `createDocument`, `updateDocument`, `deleteDocument`.
    - Use `@Parameter` for search/filter/paging criteria.
- **AdminController**:
    - `@Tag(name = "Administration")`
    - `@Operation` for user list, status update, department assignment, and creation.
- **CategoryController**:
    - `@Tag(name = "Categories")`
    - `@Operation` for `getCategoryTree`, `create`, `update`, `delete`.
- **CommentController**:
    - `@Tag(name = "Comments")`
    - `@Operation` for `getComments`, `addComment`, `deleteComment`.
- **AttachmentController**:
    - `@Tag(name = "Attachments")`
    - `@Operation` for `uploadFile` (multipart/form-data), `downloadFile` (Resource), `deleteAttachment`.
- **ProfileController**:
    - `@Tag(name = "Profile")`
    - `@Operation` for `getProfile`, `updateProfile`, `updatePassword`.
- **DepartmentController**:
    - `@Tag(name = "Departments")`
    - `@Operation` for `getTree`, `create`, `update`, `delete`.
- **TagController**:
    - `@Tag(name = "Tags")`
    - `@Operation` for `getAllTags`.

### [Build & Infrastructure]

#### [MODIFY] [Dockerfile.test](file:///Users/daehyoung/ws/md-note/backend/Dockerfile.test) (if applicable) or [docker-compose.yml](file:///Users/daehyoung/ws/md-note/docker-compose.yml)
-   Optimize Maven build memory (e.g., `-Xmx512m`) to prevent Error 137 (OOM).

## Verification Plan

### Automated Tests
-   Run the scenario verifier script:
    ```bash
    python3 tests/scenario_verifier.py
    ```
-   The script will output `✅ All Use Case Scenarios PASSED!` if everything is correct.

> [!NOTE]
> This assumes the backend is already running on `localhost:8080`. If not, it should be started via `docker-compose up -d` or `./deploy.sh`.
