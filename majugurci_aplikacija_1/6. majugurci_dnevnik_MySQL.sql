CREATE TABLE majugurci_dnevnik (
	id_dnevnik INT NOT NULL auto_increment,
	korisnik INT NOT NULL,
	komanda MEDIUMTEXT NOT NULL,
	odgovor MEDIUMTEXT NOT NULL,
	ipadresa varchar(25) NOT NULL,
	vrijeme DATETIME,
	trajanje INT NOT NULL,
	primary KEY (id_dnevnik),
	CONSTRAINT majugurci_dnevnik_FK1 FOREIGN KEY (korisnik) REFERENCES majugurci_korisnici (id_korisnik)
);