CREATE TABLE majugurci_meteo (
  id_meteo_podaci INT NOT NULL auto_increment,
  adresa INT NOT NULL,
  sunrise TIMESTAMP NULL,
  sunset TIMESTAMP NULL,
  temperature_value FLOAT DEFAULT NULL,
  temperature_min FLOAT DEFAULT NULL,
  temperature_max FLOAT DEFAULT NULL,
  temperature_unit VARCHAR(255) DEFAULT '',
  humidity_value FLOAT DEFAULT NULL,
  humidity_unit VARCHAR(255) DEFAULT '',
  pressure_value FLOAT DEFAULT NULL,
  pressure_unit VARCHAR(255) DEFAULT '',
  wind_speed_value FLOAT DEFAULT NULL,
  wind_speed_name VARCHAR(255) DEFAULT '',
  wind_direction_value FLOAT DEFAULT NULL,
  wind_direction_code VARCHAR(255) DEFAULT '',
  wind_direction_name VARCHAR(255) DEFAULT '',
  clouds_value INT DEFAULT NULL,
  clouds_name VARCHAR(255) DEFAULT '',
  visibility VARCHAR(255) DEFAULT '',
  precipitation_value FLOAT DEFAULT NULL,
  precipitation_mode VARCHAR(255) DEFAULT '',
  precipitation_unit VARCHAR(255) DEFAULT '',
  weather_number INT DEFAULT NULL,
  weather_value VARCHAR(255) DEFAULT '',
  weather_icon VARCHAR(255) DEFAULT '',
  last_update TIMESTAMP NULL,
  vrijeme_spremanja DATETIME,
  primary KEY (id_meteo_podaci),
  CONSTRAINT majugurci_meteo_FK1 FOREIGN KEY (adresa) REFERENCES majugurci_adrese (id_adresa)
);