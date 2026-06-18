-- V1__init_core_schema.sql
-- Core Monolith — initial PostgreSQL schema
-- Creates schema placeholders for all Core modules.
-- Each module owns its own schema; no cross-schema foreign keys.
--
-- Modules: userprofile (MongoDB — no schema here),
--          event, registration, venue, schedule (added by Wessel/Johan)

-- Create module schemas upfront so Flyway migrations from each module
-- can be applied independently without schema creation conflicts.
CREATE SCHEMA IF NOT EXISTS event;
CREATE SCHEMA IF NOT EXISTS registration;
CREATE SCHEMA IF NOT EXISTS venue;
CREATE SCHEMA IF NOT EXISTS schedule;

-- Note: userprofile module uses MongoDB (separate database).
-- These PostgreSQL schemas are placeholders for Wessel's and Johan's modules.
