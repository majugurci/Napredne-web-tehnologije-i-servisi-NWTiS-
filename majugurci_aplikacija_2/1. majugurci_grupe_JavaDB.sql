CREATE TABLE majugurci_grupe (
	id_grupa integer NOT NULL 
                PRIMARY KEY GENERATED ALWAYS AS IDENTITY 
                (START WITH 1, INCREMENT BY 1),
	opis VARCHAR(32) NOT NULL UNIQUE
);

INSERT INTO majugurci_grupe (opis) 
	VALUES ('admin');
	INSERT INTO majugurci_grupe (opis) 
	VALUES ('korisnik');