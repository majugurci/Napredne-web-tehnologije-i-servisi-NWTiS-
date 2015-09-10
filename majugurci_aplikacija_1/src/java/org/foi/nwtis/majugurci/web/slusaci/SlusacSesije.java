/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.slusaci;

import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.foi.nwtis.majugurci.web.kontrole.Korisnik;

/**
 * Web application lifecycle listener.
 *
 * @author Mario
 * 
 * Slušač sesije koji nakon što se korisnik prijavi postavlja njegove podatke
 * u listu svih aktivnih korisnika aplikacije, odnosno kad se odjavi briše
 * ga iz te liste.
 */
public class SlusacSesije implements HttpSessionListener, HttpSessionAttributeListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if (event.getName().equals("korisnik")) {
            Korisnik k = (Korisnik) event.getValue();
            System.out.println("Prijavio se korisnik: " + k.getKorisnickoIme());
            ServletContext sc = event.getSession().getServletContext();
            List ak = (List) sc.getAttribute("AktivniKorisnici");
            ak.add(k);
            sc.setAttribute("AktivniKorisnici", ak);
        }
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        Korisnik k = (Korisnik) event.getValue();    
        ServletContext sc = event.getSession().getServletContext();
        List ak = (List) sc.getAttribute("AktivniKorisnici");
        if (ak.contains(k)) {
            System.out.println("Odjavio se korisnik: " + k.getKorisnickoIme());
            ak.remove(k);
            sc.setAttribute("AktivniKorisnici", ak);
        }

    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
    }
    
}
