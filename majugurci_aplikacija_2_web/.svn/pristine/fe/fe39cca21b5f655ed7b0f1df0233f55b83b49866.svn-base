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
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Mario
 * 
 * Zrno koje obavlja zadaću odjave korisnika iz sustava, 
 * te ukoliko prijavljeni korisnik koji nije administrator pokuša pristupiti
 * administratorskom dijelu aplikacije javlja mu pogrešku.
 */
@ManagedBean
@RequestScoped
public class IndexOdabirAkcije {
    
    ResourceBundle bundle;

    /**
     * Creates a new instance of IndexOdabirAkcije
     */
    public IndexOdabirAkcije() {
    }
    
    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        bundle = context.getApplication().getResourceBundle(context, "m");
        
        String originalURL = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("javax.servlet.forward.request_uri");

        if (originalURL != null) {
            String[] zahtjev = originalURL.split("/");

            //ako je korisnik preusmjeren s neke stranice u /admin direktoriju jer nema ovlasti
            if (zahtjev[2].equals("admin")) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("indexOdabirAkcije_greskaOvlasti"), null));
            }
        }
    }

    public void odjavaKorisnika() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(true);

        if (session.getAttribute("korisnik") != null) {
            session.removeAttribute("korisnik");
            try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                String putanjaRedirekta = ec.getRequestContextPath() + "/indexOdabirAkcije.xhtml";
                ec.redirect(putanjaRedirekta);
            } catch (IOException ex) {
                Logger.getLogger(IndexOdabirAkcije.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //dogodila se greska s odjavom
            //ovo se ne bi trebalo izvrsavati
        }
        //return "OK";
    }
}
