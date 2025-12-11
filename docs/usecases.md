# MarkDown Note System Use Cases (Detailed Storyboard)

## 1. User Management

### 1.1 Login Flow
**Actor**: User
1.  **User** visits `/login`.
2.  **System** displays Login Form (Logo, Username field, Password field, Login Button).
3.  **User** enters `username` and `password`.
4.  **User** clicks "Login".
5.  **System** validates credentials via API `POST /auth/login`.
    *   *If valid*: System stores JWT token, redirects to `/`.
    *   *If invalid*: System shows error message "Invalid credentials".

### 1.2 Profile Management
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

### 2.1 Create Document (Storyboard)
**Actor**: Author
1.  **Author** clicks "New Document" in Sidebar.
2.  **System** opens **Document Editor** (in "Draft" status).
    *   *Title*: Empty.
    *   *Content*: Empty (Edit Mode).
3.  **Author** enters Title: "Q4 Roadmap".
4.  **Author** types Markdown content in Editor pane.
    *   *System* auto-renders Preview in right pane.
5.  **Author** opens "Settings" panel (right sidebar/drawer).
    *   Selects **Category**: "Product Planning" (from Tree).
    *   Adds **Tags**: "Q4", "Roadmap".
6.  **Author** clicks "Attach File" icon in Toolbar.
    *   Selects `diagram.png` from OS dialog.
    *   *System* uploads file `POST /attachments` and returns ID/URL.
    *   *System* inserts `![diagram.png](/files/diagram.png)` into Markdown.
7.  **Author** clicks "Save".
    *   *System* saves document via `POST /documents`.
    *   *System* shows notification "Document Saved".

### 2.2 Document Lifecycle (Approval Flow)
**Actor**: Author, Reviewer
1.  **Author** (on "Q4 Roadmap") changes Status dropdown from "Draft" to "Request Approval".
2.  **System** updates status.
3.  **Reviewer** logs in. sees "Pending approvals" in Dashboard (Future feature) or finds doc via Search.
4.  **Reviewer** opens "Q4 Roadmap".
5.  **Reviewer** reviews content.
6.  **Reviewer** clicks "Add Comment" button at bottom.
    *   Types: "Looks good, but update the dates."
    *   Clicks "Post".
    *   *System* adds comment to list.
7.  **Reviewer** clicks "Approve" button (if satisfied) or "Reject".
    *   Action: "Approve" -> Status becomes "PUBLISHED".
    *   Action: "Reject" -> Status becomes "DRAFT".

### 2.3 Search Documents
**Actor**: User
1.  **User** types "Roadmap" in Global Search Bar (Header).
2.  **System** suggests results as user types (optional) or waits for Enter.
3.  **User** presses Enter.
4.  **System** navigates to **Search Results Page**.
    *   List of matching documents (Icon, Title, Snippet, Author, Date).
    *   Filters on left: Date Range, Author, Tag.
5.  **User** clicks a result to view.

## 3. System Administration

### 3.1 User Management
**Actor**: Admin
1.  **Admin** navigates to `/admin/users`.
2.  **System** displays **User List** table.
    *   Columns: Username, Name, Department, Role, Status, Actions.
3.  **Admin** clicks "Add User".

### 6. User Profile Management
**Story**:
1. User clicks "Profile" in the top bar.
2. User sees their info (Name, Role, Organization).
3. User creates a new password and clicks "Update Password".
4. System confirms update.

### 7. Organization Management (Admin)
**Story**:
1. Admin navigates to "Organization Management".
2. Admin sees a tree of organizations (e.g., Company -> Engineering -> Backend).
3. Admin clicks "Add Child Organization" on a node to create a sub-team.
4. Admin assigns a User to an Organization node via the User List.

### 8. Document Lifecycle & Tags
**Story**:
1. User creates a document. Sets status to "Draft".
2. User adds tags "Java", "Spring".
3. User saves.
4. User changes status to "Review" when ready.
5. Admin/Approver (mock) changes status to "Published".
4.  **System** shows **Create User Modal**.
    *   Fields: Username, Password, Name, Email, Department (Tree Select), Role (Select).
5.  **Admin** fills form and saves.
6.  **System** creates user `POST /users` (Admin API).
7.  **System** refreshes list.

### 3.2 System Settings
**Actor**: Admin
1.  **Admin** navigates to `/admin/settings`.
2.  **System** displays configuration form.
    *   Site Name.
    *   Allow Registration (Yes/No).
    *   Default Language.
3.  **Admin** changes settings and clicks "Save".
