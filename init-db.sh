#!/bin/bash
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE vsms_auth_db;
    CREATE DATABASE vsms_vehicle_db;
    CREATE DATABASE vsms_service_db;
    CREATE DATABASE vsms_inventory_db;
EOSQL
