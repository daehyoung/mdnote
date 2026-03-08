# Issue Tracking

| Issue ID | Description | Status | Priority | Notes |
| :--- | :--- | :--- | :--- | :--- |
| ISS-01 | `DocumentRepository.searchDocuments` lacks paging. | Resolved | High | Backend updated to Pageable, builds optimized. |
| ISS-02 | User Management (`/api/admin/users`) lacks server-side paging. | Resolved | Medium | Controller and Frontend updated for paged API. |
| ISS-03 | Build Failure (Error 137) during Docker build. | Resolved | High | Maven memory limited to 512m in Dockerfile. |
| ISS-04 | Frontend/Backend Search Parity. | Resolved | Medium | Ensure all frontend search/filter uses the paged server endpoints. |
| ISS-05 | Frontend `npm install` failure (ERESOLVE). | Resolved | High | Downgraded `@pinia/testing` and added `--legacy-peer-deps`. |
| ISS-06 | `deploy.sh` continues on failure. | Resolved | Medium | Added `set -e` to exit immediately on errors. |
| ISS-07 | Layout centering bug in `MainLayout.vue`. | Resolved | Medium | Removed centering classes from `v-main` to fix UI collapsing. |
| ISS-08 | State loss on refresh after login. | Resolved | High | User profile now hydrated globally in router guard. |
| ISS-09 | Broken layout due to CSS stubbing in `vite.config.js`. | Resolved | Critical | Removed `stub-css` plugin that emptied all styles in production. |

## Wrongly Implemented Items (Found)
1. **[Backend]** `DocumentRepository.searchDocuments` returned `List` instead of `Page`. (Fixing)
2. **[Frontend]** Potentially client-side paging in management views. (Investigating)
