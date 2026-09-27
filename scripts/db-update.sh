#!/usr/bin/env bash
# Applies the Liquibase changelogs to the local database from docker-compose.yml.
# Runs as tollgate_owner, the same as the DBA does in test and production.
#
#   scripts/db-update.sh                 reference and demo data (the usual case)
#   scripts/db-update.sh reference       reference data only
set -euo pipefail

cd "$(dirname "$0")/.."

CONTEXTS="${1:-reference,demo}"
DB_URL="${TOLLGATE_DB_URL:-jdbc:postgresql://localhost:5432/tollgate}"

./mvnw -q -B -pl tollgate-db process-resources liquibase:update \
    -Dliquibase.url="$DB_URL" \
    -Dliquibase.username=tollgate_owner \
    -Dliquibase.password=tollgate_owner \
    -Dliquibase.contexts="$CONTEXTS"

echo "Database updated (contexts: $CONTEXTS)"
