CREATE TABLE USERS (
                       ID BIGSERIAL PRIMARY KEY,
                       USER_NAME VARCHAR NOT NULL UNIQUE,
                       HASH VARCHAR NOT NULL,
                       ROLE VARCHAR NOT NULL DEFAULT 'Customer'
);

CREATE TABLE JWT (
                     ID VARCHAR PRIMARY KEY,
                     JWT VARCHAR NOT NULL,
                     IDENTITY BIGINT NOT NULL REFERENCES USERS (ID) ON DELETE CASCADE,
                     EXPIRY TIMESTAMP NOT NULL,
                     LAST_TOUCHED TIMESTAMP
);

CREATE TABLE TOURS (
                     ID BIGSERIAL PRIMARY KEY,
                     SOURCE VARCHAR NOT NULL,
                     DESTINATION VARCHAR NOT NULL,
                     COST VARCHAR NOT NULL,
                     DESCRIPTION VARCHAR NOT NULL
);