# Personal Ticketing System

An enterprise-grade, high-performance Ticketing System with PostgreSQL persistence, Keycloak JWT on-demand user synchronization, configurable APIGW host prefix routing, and an interactive Kanban dashboard.

---

## 🏛️ Architecture & Key Concepts

```
                  ┌───────────────────────────────┐
                  │    API Gateway / Ingress      │
                  │   (Keycloak Auth & Routing)   │
                  └───────────────┬───────────────┘
                                  │ Relays JWT in Authorization header
                                  │ Routes /ticketing/* (configurable)
                                  ▼
      ┌───────────────────────────────────────────────────────┐
      │         Spring Boot Application (Port 8080)           │
      │                                                       │
      │  ┌───────────────────────┐   ┌─────────────────────┐  │
      │  │  React 18 + Tailwind  │   │  Spring Boot REST   │  │
      │  │  Kanban SPA (static)  │   │  API Controllers    │  │
      │  └───────────────────────┘   └──────────┬──────────┘  │
      │                                         │             │
      │                  JwtAuthFilter          │             │
      │         (On-demand user auto-creation)  │             │
      │                                         ▼             │
      │                              Service & Domain Model   │
      └─────────────────────────────────────────┬─────────────┘
                                                │
                                                ▼
                                   ┌─────────────────────────┐
                                   │  PostgreSQL Persistence │
                                   └─────────────────────────┘
```

### 1. Project Hierarchy & Sequential Ticket IDs
- **Top Layer:** `Project` (Primary key is a **3-character alphanumeric code**, e.g. `ADH`, `PR1`).
- **Default Project:** `Adhocs` (`ADH`) is auto-seeded on startup.
- **Ticket ID Format:** `<project-code>-<5-digit sequential number>` (e.g. `ADH-00001`, `PR1-00001`), generated with pessimistic database locking.

### 2. Scopes and Workflow Lifecycle

```
             ┌────────────────────────────────────────────────────────┐
             │                      PLAN SCOPE                        │
             │   - Locked Phase: PLAN                                 │
             │   - Exploratory Ideas (Active / Inactive)              │
             └───────────────┬────────────────────────┬───────────────┘
                             │                        │
                      Promote│                  Cancel│
                             ▼                        ▼
    ┌───────────────────────────────────┐    ┌─────────────────────────┐
    │            LIVE SCOPE             │    │         CLOSED          │
    │  - Ideas sealed for modification  │    │  (completed = false)    │
    │  - Active ideas -> Checkpoints    │    │  [RESERVED RED CARD]    │
    └────────────────┬──────────────────┘    └─────────────────────────┘
                     │ Starts in PLANNED
                     ▼
             ┌───────────────────────────────────┐
             │          PLANNED PHASE            │
             │  - Checkpoints: IMMUTABLE         │
             │  - Assignee: IMMUTABLE (None)     │
             └───────────────┬───────────────────┘
                             │ Transition to EXECUTION
                             │ (Mandatory Assignee Prompt!)
                             ▼
             ┌───────────────────────────────────┐
             │         EXECUTION PHASE           │
             │  - Checkpoints: MUTABLE (toggle)  │
             │  - Assignee: Reassignable         │
             │    (Cannot be de-assigned)        │
             └───────────────┬───────────────────┘
                             │
            ┌────────────────┴────────────────┐
      Normal Close                       Cancel Live
            ▼                                 ▼
┌─────────────────────────┐       ┌─────────────────────────┐
│         CLOSED          │       │         CLOSED          │
│   (completed = true)    │       │   (completed = false)   │
│   [EMERALD / GREEN]     │       │   [RESERVED RED CARD]   │
└─────────────────────────┘       └─────────────────────────┘
```

- **Comments:** Mutable and appendable across **all** phases (Plan, Planned, Execution, Closed).
- **Related Tickets:** Bidirectional/unidirectional references linking any ticket by ID without sharing internal data.
- **Attachments:** Configurable size limit (`MAX_ATTACHMENT_SIZE_MB`) and count limit (`MAX_ATTACHMENT_COUNT`).

---

## 🎨 UI & Kanban Dashboard Features
- **Poppable / Collapsible Left Gutter:**
  - Persists across all views.
  - Project dropdown to switch context or trigger creation.
  - Scope switcher buttons (**Live** and **Plan**). Live scope is open by default.
  - Bottom icons for **Settings** (Create Project, Create Ticket modals) and **Profile** (Theme & Avatar management).
- **Overview Metrics Dashboard:**
  - Total tickets, live/plan ratios, phase distributions, completion and cancellation rates.
  - Quick-jump project cards to open any Kanban board immediately.
- **Kanban Board:**
  - **Live Scope:** Exactly 3 lanes (**Planned**, **Execution**, **Closed**).
  - **Plan Scope:** Clean card grid without lanes, displaying ideas, promotion, and cancellation controls.
  - **Color-Coding:** Distinct phase color cards. **Red color is strictly reserved for cancelled/incomplete tickets.**
  - **Completed Filter:** Only completed tickets up to 1 week old are shown by default.
  - **Cap on Filtered View:** When filters are applied, at most 5 or 6 tickets are displayed per lane.
- **Search & Filter Bar:**
  - Searchable dropdowns for Assignee, Phase, Tags, and Completion Date Range.
  - Colorful badges on top differentiating the flavor of each applied filter.
- **Profile & Theming:**
  - Themes: `Dark Slate`, `Clean Light`, `Midnight Slate`, `Arctic Nord`, `Deep Indigo`.
  - Contrast-tuned to ensure high readability.
  - Persisted in user profile in PostgreSQL across sessions.
  - Basic photos + custom photo uploads available to all users.

---

## ⚙️ Configuration & Environment Variables

| Variable | Default | Description |
| :--- | :--- | :--- |
| `HOST_PREFIX` | `/ticketing` | Context path when routed behind APIGW |
| `PORT` | `8080` | Port for the application server |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `ticketing_db` | PostgreSQL database name |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JPA_DDL_AUTO` | `update` | Hibernate DDL behavior (`update`, `validate`) |
| `JWT_HEADER` | `Authorization` | Header containing JWT token from APIGW |
| `JWT_PREFIX` | `Bearer ` | Token prefix |
| `DEFAULT_DEV_USER` | `admin` | Fallback user for dev / unauthenticated requests |
| `MAX_ATTACHMENT_COUNT`| `10` | Max attachments per ticket |
| `MAX_ATTACHMENT_SIZE_MB` | `20` | Max single attachment size in MB |
| `ATTACHMENT_STORAGE_DIR` | `./data/attachments` | Local directory for uploaded files |
| `AVATAR_STORAGE_DIR` | `./data/avatars` | Local directory for avatar & project photos |

---

## 🚀 Running the System

### Option A: Docker Compose (Recommended for Production)

Run PostgreSQL and the Ticketing System container together:

```bash
docker compose up -d --build
```

The system will be accessible at:
- Web UI & API: `http://localhost:8080/ticketing/`
- Health check: `http://localhost:8080/ticketing/api/metrics`

### Option B: Local Development

1. **Start PostgreSQL:**
   ```bash
   docker run --name pg-ticket -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=ticketing_db -p 5432:5432 -d postgres:16-alpine
   ```

2. **Build Frontend:**
   ```bash
   cd frontend
   npm.cmd install
   npm.cmd run build
   cd ..
   ```

3. **Run Spring Boot Backend:**
   ```bash
   mvn spring-boot:run
   ```

4. **Run Automated Test Suite:**
   ```bash
   mvn test
   ```

---

## 📡 REST API Reference

All endpoints are prefixed by `${HOST_PREFIX}` (default `/ticketing`).

### Projects
- `GET /api/projects`: List all projects
- `GET /api/projects/{code}`: Get project details
- `POST /api/projects`: Create project (3-char code)

### Tickets
- `GET /api/projects/{code}/tickets?scope=LIVE&includeAllCompleted=false`: Get project tickets
- `GET /api/tickets/{id}`: Get full ticket details (checkpoints, ideas, comments, attachments, relations)
- `POST /api/tickets`: Create ticket (starts in Plan scope)
- `PUT /api/tickets/{id}`: Update title, description, priority, tags
- `POST /api/tickets/{id}/promote`: Promote Plan ticket to Live scope (seals ideas, converts to checkpoints)
- `POST /api/tickets/{id}/cancel-plan`: Cancel Plan ticket
- `POST /api/tickets/{id}/transition`: Transition Live phase (`PLANNED`, `EXECUTION`, `CLOSED`)
- `POST /api/tickets/{id}/reassign`: Reassign assignee in Execution phase

### Ideas & Checkpoints
- `POST /api/tickets/{id}/ideas`: Add idea (Plan scope)
- `PUT /api/tickets/{id}/ideas/{ideaId}`: Toggle active/inactive or edit idea text
- `DELETE /api/tickets/{id}/ideas/{ideaId}`: Delete idea
- `PUT /api/tickets/{id}/checkpoints/{checkpointId}`: Toggle checkpoint completion (Execution phase)

### Comments, Relations & Attachments
- `POST /api/tickets/{id}/comments`: Add comment (allowed in all phases)
- `POST /api/tickets/{id}/related`: Link related ticket by ID
- `DELETE /api/tickets/{id}/related/{relatedId}`: Unlink related ticket
- `POST /api/tickets/{id}/attachments`: Upload attachment (multipart)
- `GET /api/attachments/{id}/download`: Download attachment file
- `DELETE /api/attachments/{id}`: Delete attachment

### Profile & Photos
- `GET /api/profile`: Get current authenticated user profile
- `PUT /api/profile/theme`: Persist user theme preference (`dark`, `light`, `slate`, `nord`, `indigo`)
- `PUT /api/profile/avatar`: Update user avatar URL
- `GET /api/users`: List users for assignment
- `GET /api/photos`: List available avatars & badges
- `POST /api/photos/upload`: Upload custom photo (available to all users & projects)
- `GET /api/metrics`: Dashboard system metrics
