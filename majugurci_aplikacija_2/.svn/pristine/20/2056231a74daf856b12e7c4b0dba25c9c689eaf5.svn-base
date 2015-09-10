CREATE TABLE majugurci_dnevnik (
  id_dnevnik integer NOT NULL 
                PRIMARY KEY GENERATED ALWAYS AS IDENTITY 
                (START WITH 1, INCREMENT BY 1),
  korisnik integer NOT NULL,
  url varchar(255) NOT NULL DEFAULT '',
  ipadresa varchar(25) NOT NULL DEFAULT '',
  vrijeme TIMESTAMP not null DEFAULT CURRENT_TIMESTAMP,
  trajanje int NOT NULL DEFAULT 0,
  status int NOT NULL DEFAULT 0,
  opis varchar(255) NOT NULL DEFAULT '',
  CONSTRAINT majugurci_dnevnik_FK1 FOREIGN KEY (korisnik) REFERENCES majugurci_korisnici (id_korisnik)
);