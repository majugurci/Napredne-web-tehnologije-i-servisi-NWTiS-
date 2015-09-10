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
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.majugurci.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.majugurci.socketServer.MeteoPodaciDretva;
import org.foi.nwtis.majugurci.socketServer.SocketServer;

/**
 * Web application lifecycle listener.
 *
 * @author Mario
 * 
 * Slušač aplikacije koji pri pokretanju aplikacije učitava konfiguracijske 
 * datoteke, pokreće rad socket servera, pokreće pozadinsku dretvu koja prikuplja
 * meteo podatke, te inicijalizira listu aktivnih korisnika
 */
public class SlusacAplikacije implements ServletContextListener {

    MeteoPodaciDretva mpd;

    SocketServer ss;

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

        mpd = new MeteoPodaciDretva(konfigBP, konfigRazno);
        mpd.start();
        
        String cjenik = direktorij + sce.getServletContext().
                getInitParameter("cjenik");
        
        sce.getServletContext().setAttribute("konfigCjenikPutanja", cjenik);

        ss = new SocketServer(mpd, konfigBP, konfigRazno, cjenik);
        ss.start();
        
        ArrayList ak = new ArrayList();
        sce.getServletContext().setAttribute("AktivniKorisnici", ak);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        System.out.println("Pokrecem contextDestroyed");

        sce.getServletContext().removeAttribute("konfigBP");
        sce.getServletContext().removeAttribute("konfigRazno");
        sce.getServletContext().removeAttribute("konfigCjenikPutanja");
        sce.getServletContext().removeAttribute("AktivniKorisnici");

        if (mpd != null && !mpd.isInterrupted()) {
            mpd.interrupt();
        }

        if (!ss.isZaustaviServer()) {
            ss.setZaustaviServer(true);
        }
    }
}
