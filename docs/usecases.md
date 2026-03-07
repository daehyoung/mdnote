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

## 3. System Administration

### 3.1 User Management (`UC-A-01`)
**Actor**: Admin
1.  **Admin** navigates to `/admin/users`.
2.  **System** displays **User List** table.
    *   Columns: Username, Name, Department, Role, Status, Actions.
3.  **Admin** clicks "Add User".
4.  **System** shows **Create User Modal**.
    *   Fields: Username, Password, Name, Email, Department (Tree Select), Role (Select).
5.  **Admin** fills form and saves.
6.  **System** creates user `POST /users` (Admin API).
7.  **System** refreshes list.

### 3.2 System Settings (`UC-A-02`)
**Actor**: Admin
1.  **Admin** navigates to `/admin/settings`.
2.  **System** displays configuration form.
    *   Site Name.
    *   Allow Registration (Yes/No).
    *   Default Language.
3.  **Admin** changes settings and clicks "Save".
