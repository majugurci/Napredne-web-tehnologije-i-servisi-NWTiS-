/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.slusaci;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.majugurci.ejb.SpremisteJMSPoruka;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.majugurci.web.websocket.ProvjeriPoruke;

/**
 * Web application lifecycle listener.
 *
 * @author Mario
 * 
 * Slušač aplikacije koji ima nekoliko zadaća.
 * (1) Učitava konfiguracijske datoteke
 * (2) Inicijalizira spremište poruka i poziva metode za serijalizaciju
 * i deserijalizaciju prilikom pokretanja/zaustavljanja aplikacije
 * (3) Poziva dretvu koja u kratkim vremenskim razmacima provjerava JMS poruke.
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {
    SpremisteJMSPoruka spremisteJMSPoruka = lookupSpremisteJMSPorukaBean();  
    
    ProvjeriPoruke pp;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String direktorij = sce.getServletContext().
                getRealPath("/WEB-INF") + File.separator;

        String datotekaRazno = direktorij + sce.getServletContext().
                getInitParameter("konfiguracijaRazno");

        Konfiguracija konfigRazno = null;

        try {
            konfigRazno = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datotekaRazno);
        } catch (Exception ex) {
            System.out.println("Ne mogu učitati datoteku postavki za aplikaciju");
            return;
        }
        
        sce.getServletContext().setAttribute("konfigRazno", konfigRazno);

        String server = konfigRazno.dajPostavku("server");
        int port = Integer.parseInt(konfigRazno.dajPostavku("port"));
        String datSerijalizacije = konfigRazno.dajPostavku("datotekaSerijalizacije");
        
        spremisteJMSPoruka.init(server, port, datSerijalizacije);
        spremisteJMSPoruka.ucitajSerijalizaciju();
        
        pp = new ProvjeriPoruke();
        pp.start();
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute("konfigRazno");
        spremisteJMSPoruka.serijalizirajEvideniciju();
        if(pp != null && !pp.isInterrupted()){
            pp.interrupt();
        }
    }

    private SpremisteJMSPoruka lookupSpremisteJMSPorukaBean() {
        try {
            Context c = new InitialContext();
            return (SpremisteJMSPoruka) c.lookup("java:global/majugurci_aplikacija_3/majugurci_aplikacija_3_ejb/SpremisteJMSPoruka!org.foi.nwtis.majugurci.ejb.SpremisteJMSPoruka");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }
}
