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
public class Transakcija {
    
    private float iznosPromjena;
    private float novoStanje;
    private Date datum;
    private String usluga;

    public Transakcija() {
    }

    public Transakcija(float iznosPromjena, float novoStanje, Date datum, String usluga) {
        this.iznosPromjena = iznosPromjena;
        this.novoStanje = novoStanje;
        this.datum = datum;
        this.usluga = usluga;
    }

    public float getIznosPromjena() {
        return iznosPromjena;
    }

    public void setIznosPromjena(float iznosPromjena) {
        this.iznosPromjena = iznosPromjena;
    }

    public float getNovoStanje() {
        return novoStanje;
    }

    public void setNovoStanje(float novoStanje) {
        this.novoStanje = novoStanje;
    }

    public Date getDatum() {
        return datum;
    }

    public void setDatum(Date datum) {
        this.datum = datum;
    }

    public String getUsluga() {
        return usluga;
    }

    public void setUsluga(String usluga) {
        this.usluga = usluga;
    }
    
    
}
