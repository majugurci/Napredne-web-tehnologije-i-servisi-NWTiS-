/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.kontrole;

import java.util.Date;

/**
 *
 * @author Mario
 */
public class Dnevnik {
    
    private String komanda;
    private String odgovor;
    private String ipAdresa;
    private Date datum;
    private int trajanje;

    public Dnevnik() {
    }

    public Dnevnik(String komanda, String odgovor, String ipAdresa, Date datum, int trajanje) {
        this.komanda = komanda;
        this.odgovor = odgovor;
        this.ipAdresa = ipAdresa;
        this.datum = datum;
        this.trajanje = trajanje;
    }

    public String getKomanda() {
        return komanda;
    }

    public void setKomanda(String komanda) {
        this.komanda = komanda;
    }

    public String getOdgovor() {
        return odgovor;
    }

    public void setOdgovor(String odgovor) {
        this.odgovor = odgovor;
    }

    public String getIpAdresa() {
        return ipAdresa;
    }

    public void setIpAdresa(String ipAdresa) {
        this.ipAdresa = ipAdresa;
    }

    public Date getDatum() {
        return datum;
    }

    public void setDatum(Date datum) {
        this.datum = datum;
    }

    public int getTrajanje() {
        return trajanje;
    }

    public void setTrajanje(int trajanje) {
        this.trajanje = trajanje;
    }
    
    
    
}
