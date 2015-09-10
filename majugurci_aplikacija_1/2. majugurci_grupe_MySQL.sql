CREATE TABLE majugurci_grupe (
	id_grupa INT NOT NULL auto_increment,
	opis VARCHAR(32) NOT NULL UNIQUE,
	primary KEY (id_grupa)
);

INSERT INTO majugurci_grupe (opis) 
	VALUES ('admin');
	INSERT INTO majugurci_grupe (opis) 
	VALUES ('korisnik');