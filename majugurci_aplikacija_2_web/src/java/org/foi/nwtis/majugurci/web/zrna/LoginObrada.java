/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.zrna;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.majugurci.ejb.eb.MajugurciKorisnici;
import org.foi.nwtis.majugurci.ejb.sb.MajugurciKorisniciFacade;
import org.foi.nwtis.majugurci.web.podaci.Korisnik;

/**
 *
 * @author Mario
 * 
 * Zrno koje obavlja kontrolu za login formu. Brine se da korisnik unese sve 
 * potrebne podatke te logira korisnika u sustav ako je unio pravilne podatke, 
 * odnosno javlja mu greššku ako nije. 
 * Na ovo zrno se preusmjerava i neprijavljeni korisnik pa stoga u init
 * metodi se provjerava je li to slučaj te ako je ispisuje poruku korisniku.
 */
@ManagedBean
@RequestScoped
public class LoginObrada {

    @EJB
    private MajugurciKorisniciFacade majugurciKorisniciFacade;

    private String korisnickoIme;
    private String lozinka;

    ResourceBundle bundle;

    /**
     * Creates a new instance of LoginObrada
     */
    public LoginObrada() {
    }

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        bundle = context.getApplication().getResourceBundle(context, "m");

        String originalURL = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("javax.servlet.forward.request_uri");

        if (originalURL != null) {
            String[] zahtjev = originalURL.split("/");

            //ako je korisnik preusmjeren sa privatno ili admin dijela jer nije prijavljen
            if (zahtjev[2].equals("privatno") || zahtjev[2].equals("admin")) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("login_greskaOvlasti"), null));
            }
        }

        HttpServletRequest origRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String servletName = origRequest.getServletPath();

        //ako se korisnik registrirao javljamo mu poruku da se može prijaviti
        if (servletName != null) {
            if (servletName.equals("/register.xhtml")) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("login_porukaRegUspjeh"), null));
            }
        }

        //ako korisnik rucno upise link a vec je prijavljen, napravi redirekt na index
        HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
        Korisnik k = (Korisnik) session.getAttribute("korisnik");
        if (k != null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("indexOdabirAkcije.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(LoginObrada.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    /**
     * Metoda obrade prijave koja poziva bazu metodu iz Facade dijela te provjerava
     * je li korisnik dao ispravne podatke ili ne. Ako je korisnik dao ispravne 
     * podatke sprema podatke u sesiju.
     * Također provjerava je li korisnik nadopunio svoje korisničke podatke. 
     * Ako korisnik nije nadopunio podatke nakon prijave ga preusmjerava na 
     * stranicu za dopunu podataka, odnosno kao je sve u redu na index.
     * @return 
     */
    public String obradiPrijavu() {
        FacesContext fc = FacesContext.getCurrentInstance();

        if (korisnickoIme.trim().equals("") || lozinka.trim().equals("")) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("login_greskaIspuniteSvaPolja"), null));
            return "ERROR";
        }

        MajugurciKorisnici k = majugurciKorisniciFacade.authenticateUser(korisnickoIme, lozinka);

        if (k != null) {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
            Korisnik kor = new Korisnik(k.getIdKorisnik(), k.getGrupa().getIdGrupa(),
                    k.getGrupa().getOpis(), k.getKorisnickoIme(), k.getLozinka(), k.getEmail(), k.getVrijemeRegistracije());
            session.setAttribute("korisnik", kor);
        } else {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("login_greskaKriviKorPodaci"), null));
            return "ERROR";
        }

        if (k.getPrvaPrijava() == 1) {
            //korisnik mora dopuniti podatke
            return "OK-DOPUNA";
        }

        return "OK";
    }

}
