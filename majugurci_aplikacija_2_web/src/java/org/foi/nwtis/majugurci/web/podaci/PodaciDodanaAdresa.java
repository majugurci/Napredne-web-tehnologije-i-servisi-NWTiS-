/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.podaci;

import java.io.Serializable;

/**
 *
 * @author Mario
 * 
 * Klasa koja služi kao struktura podataka za slanje JMS poruka o 
 * dodanim adresama.
 */
public class PodaciDodanaAdresa implements Serializable {
    private long id;
    private String korisnik;
    private String lozinka;
    private String adresa;

    public PodaciDodanaAdresa() {
    }

    public PodaciDodanaAdresa(long id, String korisnik, String lozinka, String adresa) {
        this.id = id;
        this.korisnik = korisnik;
        this.lozinka = lozinka;
        this.adresa = adresa;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(String korisnik) {
        this.korisnik = korisnik;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }
    
    
}
