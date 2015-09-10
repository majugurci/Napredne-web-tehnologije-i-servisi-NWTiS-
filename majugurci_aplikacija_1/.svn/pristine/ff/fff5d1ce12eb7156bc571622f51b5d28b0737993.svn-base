CREATE TABLE majugurci_korisnici (
	id_korisnik INT NOT NULL auto_increment,
	grupa INT NOT NULL,
	korisnicko_ime VARCHAR(128) NOT NULL UNIQUE,
	lozinka VARCHAR(128) NOT NULL,
	email VARCHAR(256) DEFAULT '',
	sredstva NUMERIC(15,2) DEFAULT 0,
	vrijeme_registracije DATETIME,
	primary KEY (id_korisnik),
	CONSTRAINT majugurci_korisnici_FK1 FOREIGN KEY (grupa) REFERENCES majugurci_grupe (id_grupa)
);

INSERT INTO majugurci_korisnici (grupa, korisnicko_ime, lozinka, email, sredstva, vrijeme_registracije) 
	VALUES ('1', 'admin', '123456', 'admin@foi.hr', '999999', now());
	INSERT INTO majugurci_korisnici (grupa, korisnicko_ime, lozinka, email, sredstva, vrijeme_registracije) 
	VALUES ('2', 'pero', '123456', 'pero@foi.hr', '100', now());