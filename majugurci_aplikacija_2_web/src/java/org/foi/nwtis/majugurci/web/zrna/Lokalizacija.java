/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.zrna;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author Mario
 *
 * Zrno u koje se sprema korisnički izbor jezika aplikacije.
 */
@ManagedBean
@SessionScoped
public class Lokalizacija implements Serializable {

    private static final Map<String, Object> jezici;
    private String odabraniJezik = "hr";
    private Locale vazecaLokalizacija = new Locale("hr");

    private String referrer;

    static {
        jezici = new HashMap<>();
        jezici.put("Hrvatski", new Locale("hr"));
        jezici.put("English", Locale.ENGLISH);
    }

    /**
     * Creates a new instance of Lokalizacija
     */
    public Lokalizacija() {
    }

    public void init() {
        referrer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("referer");
    }

    public String getOdabraniJezik() {
        return odabraniJezik;
    }

    public void setOdabraniJezik(String odabraniJezik) {
        this.odabraniJezik = odabraniJezik;
    }

    public Locale getVazecaLokalizacija() {
        return vazecaLokalizacija;
    }

    public void setVazecaLokalizacija(Locale vazecaLokalizacija) {
        this.vazecaLokalizacija = vazecaLokalizacija;
    }

    public Map<String, Object> getJezici() {
        return jezici;
    }

    /**
     * Postavlja jezika aplikacije na onaj kojeg je korisnik odabrao
     *
     * @return
     */
    public Object odaberiJezik() {
        Iterator i = jezici.keySet().iterator();
        while (i.hasNext()) {
            String kljuc = (String) i.next();
            Locale l = (Locale) jezici.get(kljuc);
            if (odabraniJezik.equals(l.getLanguage())) {
                FacesContext.getCurrentInstance().getViewRoot().setLocale(l);
                vazecaLokalizacija = l;

                if (referrer != null) {
                    String[] zahtjev = referrer.split("/");
                    int duzina = zahtjev.length;
                    //ako se sa indeksa vratimo na indeks zbog pogreske preusmjeri korinika dalje
                    if (zahtjev[duzina - 1].equals("index.xhtml")) {
                        return "OK";
                    }
                    try {
                        FacesContext.getCurrentInstance().getExternalContext().redirect(referrer);
                    } catch (IOException ex) {
                        Logger.getLogger(Lokalizacija.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    return "OK";
                }
            }
        }
        return "ERROR";
    }
}
