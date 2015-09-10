CREATE TABLE majugurci_adrese (
	id_adresa INT NOT NULL auto_increment,
	korisnik INT NOT NULL,
	adresa varchar(255) NOT NULL UNIQUE,
	latitude varchar(25) NOT NULL,
	longitude varchar(25) NOT NULL,
	primary KEY (id_adresa),
	CONSTRAINT majugurci_adrese_FK1 FOREIGN KEY (korisnik) REFERENCES majugurci_korisnici (id_korisnik)
);

INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Varaždin, Pavlinska 2', '46.307831', '16.338246');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Čakovec, Ulica kralja Tomislava 1', '46.390372', '16.436053');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Zagreb, Trg bana Josipa Jelačića', '45.812864', '15.977506');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Split, Obala Hrvatskog narodnog preporoda', '43.507968', '16.437789');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Rijeka, Trg republike', '45.327327', '14.441318');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Osijek, Europska avenija 9', '45.560445', '18.683863');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Dubrovnik, Ulica don Frana Bulića 4', '42.642571', '18.102730');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Graz, Opernring 14', '47.069241', '15.444432');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'München, Marienplatz', '48.137215', '48.137215');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Wien, Stephansplatz 7', '48.208920', '16.373515');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'London, 3 Abbey Rd', '51.532302', '-0.177628');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Paris, Chemin Lauriston', '48.859219', '2.393768');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Lexington, Kentucky, 1210 University Dr', '38.028500', '-84.504790');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Washington, DC, 1000 Jefferson Dr SW', '38.888953', '-77.026120');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Istanbul, Divan Yolu Cd No:48', '41.008188', '28.974543');
INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) 
	VALUES (1, 'Brasil, São Paulo, Arena Corinthians', '-23.544960', '-46.474648');