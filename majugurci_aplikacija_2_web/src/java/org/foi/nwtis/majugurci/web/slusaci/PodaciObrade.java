/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.slusaci;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Mario
 * 
 * Klasa koja predstavlja strukturu podataka za JMS poruku o statističkim
 * podacima obrade dretve koja provjerava email poruke.
 */
public class PodaciObrade implements Serializable {
    
    private long id;
    private Date pocetakObrade;
    private Date krajObrade;
    private int brojProcitanihPoruka;
    private int brojNWTiSPoruka;
    private List<String> dodaniKorisnici;
    private List<String> neuspjesniKorisnici;

    public PodaciObrade() {
    }

    public PodaciObrade(long id, Date pocetakObrade, Date krajObrade, int brojProcitanihPoruka, int brojNWTiSPoruka, List<String> dodaniKorisnici, List<String> neuspjesniKorisnici) {
        this.id = id;
        this.pocetakObrade = pocetakObrade;
        this.krajObrade = krajObrade;
        this.brojProcitanihPoruka = brojProcitanihPoruka;
        this.brojNWTiSPoruka = brojNWTiSPoruka;
        this.dodaniKorisnici = dodaniKorisnici;
        this.neuspjesniKorisnici = neuspjesniKorisnici;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getNeuspjesniKorisnici() {
        return neuspjesniKorisnici;
    }

    public void setNeuspjesniKorisnici(List<String> neuspjesniKorisnici) {
        this.neuspjesniKorisnici = neuspjesniKorisnici;
    }

    public List<String> getDodaniKorisnici() {
        return dodaniKorisnici;
    }

    public void setDodaniKorisnici(List<String> dodaniKorisnici) {
        this.dodaniKorisnici = dodaniKorisnici;
    }

    public Date getPocetakObrade() {
        return pocetakObrade;
    }

    public void setPocetakObrade(Date pocetakObrade) {
        this.pocetakObrade = pocetakObrade;
    }

    public Date getKrajObrade() {
        return krajObrade;
    }

    public void setKrajObrade(Date krajObrade) {
        this.krajObrade = krajObrade;
    }

    public int getBrojProcitanihPoruka() {
        return brojProcitanihPoruka;
    }

    public void setBrojProcitanihPoruka(int brojProcitanihPoruka) {
        this.brojProcitanihPoruka = brojProcitanihPoruka;
    }

    public int getBrojNWTiSPoruka() {
        return brojNWTiSPoruka;
    }

    public void setBrojNWTiSPoruka(int brojNWTiSPoruka) {
        this.brojNWTiSPoruka = brojNWTiSPoruka;
    }
    
    
}
