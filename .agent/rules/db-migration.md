# Database Schema Changes, Migrations, and Data Preservation Rule

This rule governs all database schema modifications, migrations, and data safety protocols within this repository.

## 1. Mandatory Migration Script Generation
- Whenever the database schema is altered (e.g., adding, modifying, renaming, or dropping JPA entity models, tables, columns, constraints, foreign keys, or indexes):
  - The agent MUST generate an executable migration script in the git-ignored root folder `migrate/`.
  - The script must be formatted and designed to be executed directly from inside the database container (e.g., PostgreSQL in `ticketing-postgres`).
  - Example location: `migrate/migration.sh` or `migrate/migrate_<timestamp>_<description>.sh`.

## 2. In-Container Script Capabilities & Required Modes
The generated script inside `migrate/` MUST support at least two operation modes via command-line arguments:
1. `--mode=backup`
   - Dumps or exports the database data safely (e.g., using `pg_dump` targeting the application database `ticketing_db`, or `$DB_NAME` / `$POSTGRES_DB` with user `$DB_USER` / `$POSTGRES_USER`).
   - Produces a backup archive or SQL dump file (e.g., at a specified location or `/tmp/backup_<timestamp>.sql`).
2. `--mode=restore --backup=<file>`
   - Restores the database from the specified backup file (e.g., using `psql` or `pg_restore`).
   - Validates that the backup file exists and aborts safely if invalid.
   - Cleans or restores tables to maintain data integrity in case of failed migrations or corruption.

> **Scope Note:** Copying the script inside the container and copying the backup file outside/inside the container is NOT the responsibility of this script. The script only handles the execution logic inside the container.

## 3. Mandatory User Clarification for New Columns Without Defaults
- For any new field or column introduced to an entity/schema:
  - If a default value is **not explicitly defined** in the schema, entity annotation, or database column definition:
  - The agent **MUST prompt and ask the user** (e.g., using the `ask_question` tool or conversational prompt) to specify the default value or the backfill strategy for existing rows before modifying the code or applying the schema change.
  - Never assume an arbitrary default (or leave it nullable without explicit user confirmation) when backfilling existing records might lead to corrupt or incomplete states.

## 4. Deployment Safety and Recovery Protocol
- If the agent is deploying a schema change where container startup is less likely (e.g., non-trivial DDL, column type changes, non-null constraint additions, or high-risk schema refactors):
  - The agent MUST trigger/perform a database backup prior to deployment.
  - If the database/container is accessible, the agent must ensure that if the container fails to start up or health check fails after migration, a restore operation is performed from the backup to return the database to a healthy state.
