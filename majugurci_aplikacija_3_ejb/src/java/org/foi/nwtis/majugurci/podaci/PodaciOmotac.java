/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.podaci;

import java.io.Serializable;
import java.util.List;
import org.foi.nwtis.majugurci.web.slusaci.PodaciObrade;
import org.foi.nwtis.majugurci.web.podaci.PodaciDodanaAdresa;

/**
 *
 * @author Mario
 * 
 * Klasa koja sadrži strukture podataka za JMS poruke.
 */
public class PodaciOmotac implements Serializable {
    
    private List<PodaciDodanaAdresa> adrese;
    private List<PodaciObrade> mailPoruke;
    
    public void PodaciOmotac() {
        
    }
    
    public void PodaciOmotac(List adrese, List mailPoruke) {
        this.adrese = adrese;
        this.mailPoruke = mailPoruke;
    }

    public List<PodaciDodanaAdresa> getAdrese() {
        return adrese;
    }

    public void setAdrese(List<PodaciDodanaAdresa> adrese) {
        this.adrese = adrese;
    }

    public List<PodaciObrade> getMailPoruke() {
        return mailPoruke;
    }

    public void setMailPoruke(List<PodaciObrade> mailPoruke) {
        this.mailPoruke = mailPoruke;
    }
    
    
}
