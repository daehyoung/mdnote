# Database Schema Design

## ER Diagram (Conceptual)

### Users & Organization
- **User**
    - id (PK), username, password_hash, email, name, role, status
    - dept_id (FK to Department)
- **Department** (Organization)
    - id (PK), name, parent_id (Self-FK)

### Documents
- **Category**
    - id (PK), name, parent_id (Self-FK)
- **Document**
    - id (PK), title, content (Text), category_id (FK), author_id (FK), status (DRAFT, REVIEW, PUBLISHED, etc.)
    - created_at, updated_at
- **Tag**
    - id (PK), name
- **DocumentTag**
    - doc_id (FK), tag_id (FK)
- **Attachment**
    - id (PK), doc_id (FK), file_path, file_name, file_size
- **Comment**
    - id (PK), doc_id (FK), user_id (FK), content, created_at

## Schema SQL (PostgreSQL)

```sql
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id INTEGER REFERENCES department(id)
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    name VARCHAR(100),
    role VARCHAR(20), -- ADMIN, USER
    status VARCHAR(20), -- ACTIVE, INACTIVE
    dept_id INTEGER REFERENCES department(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id INTEGER REFERENCES category(id)
);

CREATE TABLE document (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    category_id INTEGER REFERENCES category(id),
    author_id INTEGER REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tag (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE document_tag (
    doc_id INTEGER REFERENCES document(id),
    tag_id INTEGER REFERENCES tag(id),
    PRIMARY KEY (doc_id, tag_id)
);
```
