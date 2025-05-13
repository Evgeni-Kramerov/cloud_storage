-- noinspection SqlNoDataSourceInspectionForFile

--liquibase formatted sql

--changeset developer:01
CREATE TABLE IF NOT EXISTS users
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

--rollback DROP TABLE users;