# MarkDown Note System Use Cases (Detailed Storyboard)

## 1. User Management

### 1.1 Login Flow (`UC-U-01`)
**Actor**: User
1.  **User** visits `/login`.
2.  **System** displays Login Form (Logo, Username field, Password field, Login Button).
3.  **User** enters `username` and `password`.
4.  **User** clicks "Login".
5.  **System** validates credentials via API `POST /auth/login`.
    *   *If valid*: System stores JWT token, redirects to `/`.
    *   *If invalid*: System shows error message "Invalid credentials".

### 1.2 Profile Management (`UC-U-02`)
**Actor**: User
1.  **User** clicks on "Avatar/Name" in top-right header.
2.  **System** shows dropdown menu: [Profile, Logout].
3.  **User** clicks "Profile".
4.  **System** displays Profile Modal/Page:
    *   Read-only: Username, Role, Department.
    *   Editable: Name, Email.
    *   Action: "Change Password" button.
5.  **User** updates Email and clicks "Save".
6.  **System** updates data via API `PUT /users/me`.

## 2. Document Management

### 2.1 View Document List (Home) (`UC-D-01`)
**Actor**: User
1.  **User** logs in or visits `/`.
2.  **System** displays **Document List**:
    *   Left Sidebar: Category Tree.
    *   Main Area: List of documents.
    *   **Filter Rule**: Only "PUBLISHED" documents are shown in this View mode.
3.  **User** can filter by Status (All/Draft/Published/etc.) if allowed or search by keyword.
4.  **User** clicks on a document to Read.

### 2.2 Create / Edit Document (`UC-D-02`)
**Actor**: Author (or User with Write Permission)
1.  **Author** clicks "New Document" or "Edit" on an existing document.
2.  **System** opens **Document Edit View** (`/documents/:id/edit`).
3.  **Author** edits Title and Content (Markdown).
4.  **Author** manages **Metadata** (Top Bar & Side Panel):
    *   **Category**: Selects parent category.
    *   **Tags**: Adds/Removes tags.
    *   **Status**: Change lifecycle (Draft -> Review -> Published).
5.  **Author** manages **Permissions**:
    *   **Group Access**: Toggle Read/Write for their department.
    *   **Public Access**: Toggle Read/Write for all users.
6.  **Author** manages **Settings**:
    *   **Allow Comments**: Toggles whether comments are enabled for this doc.
7.  **Author** saves via "Save" button.

### 2.3 Read Document (Detail View) (`UC-D-03`)
**Actor**: User
1.  **User** clicks on a document from the list.
2.  **System** opens **Document Detail View** (`/documents/:id`).
    *   Read-only Markdown content.
    *   Header: Title, Tags.
    *   Footer: Author info, Permission badges.
3.  **System** displays **Comments Section** (if "Allow Comments" is ON).
    *   User can read existing comments.
    *   User can add a new comment (if logged in).
4.  **System** shows "Edit" button ONLY if user has write permission (Owner, Admin, or Group/Public Write).

### 2.4 Search Documents (`UC-D-04`)
**Actor**: User
1.  **User** types "Roadmap" in Global Search Bar (Header).
2.  **System** suggests results as user types (optional) or waits for Enter.
3.  **User** presses Enter.
4.  **System** navigates to **Search Results Page**.
    *   List of matching documents (Icon, Title, Snippet, Author, Date).
    *   Filters on left: Date Range, Author, Tag.
5.  **User** clicks a result to view.

### 2.5 File Attachment (`UC-D-05`)
**Actor**: Author
1.  **Author** clicks "Attach File" (paperclip icon) in the Document Edit View.
2.  **System** opens file picker.
3.  **Author** selects a file (e.g., `image.png`, `pdf`).
4.  **System** uploads file via API `POST /api/attachments/upload`.
5.  **System** returns metadata (ID, Original Name, Size) and displays it in the "Attachments" list.
6.  **Author** saves the document, and the system links the attachment to the document via `doc_id`.
7.  **System** allows users with Read permission to download the file via `GET /api/attachments/:id`.

## 3. System Administration

### 3.1 User Management (`UC-A-01`)
**Actor**: Admin
1.  **Admin** navigates to User Management Page.
2.  **System** fetches user list via `GET /api/admin/users`.
3.  **Admin** can search users by username or filter by department.
4.  **Admin** changes a user's role (USER -> ADMIN) or status (ACTIVE -> INACTIVE) via `PUT /api/admin/users/{id}/status`.
5.  **Admin** creates a new user via `POST /api/admin/users`.
6.  **System** persists the user and triggers an email/notification (if configured).

### 3.2 Category Management (`UC-A-03`)
**Actor**: Admin
1.  **Admin** opens Category Management View.
2.  **System** displays the full hierarchy.
3.  **Admin** adds a new system-level category via `POST /api/categories`.
4.  **System** refreshes the tree for all users.

### 3.3 Dashboard Metrics (`UC-A-05`)
**Actor**: Admin
1.  **Admin** views the System Overview dashboard.
2.  **System** displays statistics: Total Users, Documents, Storage used.
