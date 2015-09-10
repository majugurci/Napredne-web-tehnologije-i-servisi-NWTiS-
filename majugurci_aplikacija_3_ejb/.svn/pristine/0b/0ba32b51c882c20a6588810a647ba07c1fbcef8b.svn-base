/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.ejb.mdb;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.foi.nwtis.majugurci.ejb.SpremisteJMSPoruka;
import org.foi.nwtis.majugurci.web.slusaci.PodaciObrade;

/**
 *
 * @author Mario
 * 
 * Obrađuje primanje JMS poruka o obradi email poruka.
 * Sprema poruke u spremište poruka.
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/NWTiS_majugurci_1"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class EmailMDB implements MessageListener {

    @EJB
    private SpremisteJMSPoruka spremisteJMSPoruka;

    public EmailMDB() {
    }

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage om = (ObjectMessage) message;
            PodaciObrade po = (PodaciObrade) om.getObject();
            spremisteJMSPoruka.dodajMailPoruku(po);
            spremisteJMSPoruka.setImaNovihPoruka(true);
        } catch (JMSException ex) {
            Logger.getLogger(EmailMDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
