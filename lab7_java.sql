DROP TABLE IF EXISTS city;
DROP TABLE IF EXISTS users;
DROP TYPE IF EXISTS StandartOfLiving;
DROP TYPE IF EXISTS Goverment;
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(80),
                       password VARCHAR(80)
);
CREATE TYPE StandartOfLiving AS ENUM ('ULTRA_HIGH', 'VERY_HIGH', 'LOW', 'ULTRA_LOW','NIGHTMARE');
CREATE TYPE Goverment AS ENUM ('DESPOTISM', 'CORPORATOCRACY', 'TOTALITARIANISM');
CREATE TABLE city (
                     id SERIAL PRIMARY KEY,
                     name VARCHAR(60),
                     x INT,
                     y FLOAT,
                     creationDate TIMESTAMP,
                     area BIGINT,
                     population BIGINT,
	                 metersAboveSeaLevel BIGINT,
                     telephoneCode BIGINT,
                     goverment Goverment,
                     standartOfLiving StandartOfLiving,
                     existGovernor boolean,
                     nameGovernor VARCHAR(60),
                     height INT,
                     age BIGINT,
                     userId INT REFERENCES users(id) ON DELETE CASCADE,
                     indexCollection INT
);