#!/bin/bash
set -e

# POSTGRES_USER, TOPIC_NAME are set in docker/Dockerfile

psql -v ON_ERROR_STOP=1 -v --username "$POSTGRES_USER" <<-EOSQL
  CREATE TABLE game (
      game_id serial PRIMARY KEY,
      game_name text,
      complexity integer
  );

EOSQL
