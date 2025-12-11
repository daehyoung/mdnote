# UI/UX Design

## Design Concept
- **Theme**: Clean, Modern, Professional. Light/Dark mode support.
- **Framework**: Vuetify (Material Design).
- **Layout**: 
    - **Sidebar**: Organization Tree / Category Tree Navigation.
    - **Header**: Search bar, User Profile, Notifications.
    - **Main Content**: Document View / Edit area.

## Key Screens

### 1. Login Page
- Simple centered card with Logo.
- ID / Password input.
- "Forgot Password" link.

### 2. Main Dashboard (Home)
- **Left Panel**: 
    - Toggle between "Organization View" and "Category View".
    - Tree component.
- **Main Area**:
    - Recent Documents.
    - User's Drafts.
    - Pinned Documents.

### 3. Document Viewer
- Title, Breadcrumbs (Category path).
- Metadata (Author, Date, Status, Tags).
- **Body**: Markdown rendered content.
- **Right Panel** (Collapsible):
    - Table of Contents.
    - Comments.
    - Attachments.

### 4. Document Editor
- **Toolbar**: WYSIWYG controls, formatting.
- **Edit Area**: Split view (Markdown / Preview) or Rich Text.
- **Settings Panel**:
    - Category selector.
    - Tag input.
    - Permission settings.
    - Lifecycle actions (Save Draft, Request Approval).

### 5. Admin Console
- **User Management**: Table view of users with Add/Edit/Delete actions.
- **System Settings**: Form for global configs.
