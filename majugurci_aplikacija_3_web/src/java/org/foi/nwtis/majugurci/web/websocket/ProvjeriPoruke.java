/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.websocket;

import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.foi.nwtis.majugurci.ejb.SpremisteJMSPoruka;
import org.foi.nwtis.majugurci.podaci.PodaciOmotac;

/**
 *
 * @author Mario
 * 
 * Dretva koja svaku sekundu provjerava jesu li pristigle nove JMS poruke.
 * Ako jesu poziva endpoint WebSocketa da obavijseti klijente.
 */
public class ProvjeriPoruke extends Thread {
    
    SpremisteJMSPoruka spremisteJMSPoruka = lookupSpremisteJMSPorukaBean();

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        while (true) {
            PodaciOmotac po = spremisteJMSPoruka.dajNovePoruke();
            if (po == null) {
                //nema novih poruka
            } else {
                AdresaEndpoint.obavijestiPromjenu("Stigle su nove poruke, osvježavam stranicu.");
            }

            if (this.isInterrupted()) {
                break;
            }

            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                //Logger.getLogger(AdresaEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
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
