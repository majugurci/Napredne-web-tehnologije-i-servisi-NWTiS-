CREATE TABLE majugurci_transakcije (
	id_transakcije INT NOT NULL auto_increment,
	korisnik INT NOT NULL,
	iznos_promjena NUMERIC(15,2) DEFAULT 0,
	novo_stanje NUMERIC(15,2) DEFAULT 0,
	datum DATETIME,
	usluga varchar(255) NOT NULL,
	primary KEY (id_transakcije),
	CONSTRAINT majugurci_transakcije_FK1 FOREIGN KEY (korisnik) REFERENCES majugurci_korisnici (id_korisnik)
);