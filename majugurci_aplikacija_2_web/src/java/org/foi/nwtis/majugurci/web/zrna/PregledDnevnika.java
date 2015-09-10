/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.zrna;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.foi.nwtis.majugurci.ejb.eb.MajugurciDnevnik;
import org.foi.nwtis.majugurci.ejb.sb.MajugurciDnevnikFacade;

/**
 *
 * @author Mario
 * 
 * Zrno za prikaz dnevničkih zapisa.
 */
@ManagedBean
@ViewScoped
public class PregledDnevnika implements Serializable {

    @EJB
    private MajugurciDnevnikFacade majugurciDnevnikFacade;

    private List<MajugurciDnevnik> zapisiDnevnika;
    private List<MajugurciDnevnik> filtriraniZapisi;
    
    private Date pocetniDatum;
    private Date ztavrsniDatum;

    /**
     * Creates a new instance of PregledDnevnika
     */
    public PregledDnevnika() {
    }

    /**
     * Prilikom instanciranja zrna podaci se dohvaćaju iz baze podataka.
     */
    @PostConstruct
    public void init() {
        zapisiDnevnika = majugurciDnevnikFacade.findAll();
    }

    public List<MajugurciDnevnik> getZapisiDnevnika() {
        return zapisiDnevnika;
    }

    public void setZapisiDnevnika(List<MajugurciDnevnik> zapisiDnevnika) {
        this.zapisiDnevnika = zapisiDnevnika;
    }
    
    /**
     * Metoda za filtriranje po datumskom intervalu. Uzima početni i krajnji 
     * datum preko korisničkog unosa, te daje mogućnost da jedan ili oba datuma
     * mogu biti prazni.
     * @param value
     * @param filter
     * @param locale
     * @return 
     */
    public boolean filterByDate(Object value, Object filter, Locale locale) {

        String filterText = (filter == null) ? null : filter.toString().trim();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }
        if (value == null) {
            return false;
        }

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date filterDate = (Date) value;
        Date dateFrom;
        Date dateTo;
        try {
            String fromPart = filterText.substring(0, filterText.indexOf("-"));
            String toPart = filterText.substring(filterText.indexOf("-") + 1);
            dateFrom = fromPart.isEmpty() ? null : df.parse(fromPart);
            dateTo = toPart.isEmpty() ? null : df.parse(toPart);
        } catch (ParseException pe) {
            //System.out.println("unable to parse date: " + filterText);
            return false;
        }

        return (dateFrom == null || filterDate.after(dateFrom)) && (dateTo == null || filterDate.before(dateTo));
    }

    public Date getPocetniDatum() {
        return pocetniDatum;
    }

    public void setPocetniDatum(Date pocetniDatum) {
        this.pocetniDatum = pocetniDatum;
    }

    public Date getZtavrsniDatum() {
        return ztavrsniDatum;
    }

    public void setZtavrsniDatum(Date ztavrsniDatum) {
        this.ztavrsniDatum = ztavrsniDatum;
    }

    public List<MajugurciDnevnik> getFiltriraniZapisi() {
        return filtriraniZapisi;
    }

    public void setFiltriraniZapisi(List<MajugurciDnevnik> filtriraniZapisi) {
        this.filtriraniZapisi = filtriraniZapisi;
    }
    
    
}
