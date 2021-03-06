/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.slusaci;

import java.io.File;
import java.util.ArrayList;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.majugurci.konfiguracije.bp.BP_Konfiguracija;

/**
 * Web application lifecycle listener.
 *
 * @author Mario
 * 
 * Slušač aplikacije koji učitava konfiguracijske datoteke za bazu podataka i 
 * općenite konfiguracijske podatke, pokreće pozadinsku dretvu koja pregledava
 * email poruke te inicijalizira listu aktivnih korisnika u sustavu.
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {
    
    private ObradaEmailPoruka oep;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String direktorij = sce.getServletContext().
                getRealPath("/WEB-INF") + File.separator;
        String datotekaBaza = direktorij + sce.getServletContext().
                getInitParameter("konfiguracijaBaza");

        BP_Konfiguracija konfigBP = null;

        try {
            konfigBP = new BP_Konfiguracija(datotekaBaza);
        } catch (Exception ex) {
            System.out.println("Ne mogu učitati datoteku postavki za BP");
            return;
        }

        sce.getServletContext().setAttribute("konfigBP", konfigBP);

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
        
        ArrayList ak = new ArrayList();
        sce.getServletContext().setAttribute("AktivniKorisnici", ak);

        
        oep = new ObradaEmailPoruka(konfigRazno, konfigBP);
        oep.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute("konfigBP");
        sce.getServletContext().removeAttribute("konfigRazno");
        sce.getServletContext().removeAttribute("AktivniKorisnici");

        if(oep != null && !oep.isInterrupted()){
            oep.interrupt();
        }
    }
}
