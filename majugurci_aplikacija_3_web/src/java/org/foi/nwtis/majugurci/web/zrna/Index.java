/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.zrna;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.foi.nwtis.majugurci.ejb.SpremisteJMSPoruka;
import org.foi.nwtis.majugurci.ejb.mdb.AdreseMDB;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.web.podaci.PodaciDodanaAdresa;
import org.foi.nwtis.majugurci.web.slusaci.PodaciObrade;

/**
 *
 * @author Mario
 * 
 * Prikaz JSM poruka i mogućnosti njihovog pregleda i brisanja.
 */
@ManagedBean
@ViewScoped
public class Index implements Serializable {
    @EJB
    private SpremisteJMSPoruka spremisteJMSPoruka;
    
    private List<PodaciDodanaAdresa> adrese;
    private List<PodaciObrade> mailPoruke;
    
    private List<PodaciDodanaAdresa> odabraneAdrese = new ArrayList<>();
    private List<PodaciObrade> odabraneEmailPoruke = new ArrayList<>();
    
    private PodaciObrade odabranaMailPorukaPregled;
    private PodaciObrade odabranaMailPorukaPregledNeuspjeh;
    
    private String korisnickoIme;
    private String lozinka;
    
    private String server;
    private int port;

    /**
     * Creates a new instance of index
     */
    public Index() {
    }
    
    @PostConstruct
    public void init() {
        adrese = spremisteJMSPoruka.getAdrese();
        mailPoruke = spremisteJMSPoruka.getMailPoruke();
        
        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        Konfiguracija konfigRazno = (Konfiguracija) sc.getAttribute("konfigRazno");
        
        this.server = konfigRazno.dajPostavku("server");
        this.port = Integer.parseInt(konfigRazno.dajPostavku("port"));
    }
    
    public void obirisiAdresePoruke() {
        if (odabraneAdrese.isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:obrisiPorukeAdrese", new FacesMessage("Niste odabrali poruke za obrisati"));
        } else {
            spremisteJMSPoruka.obrisiAdresePoruke(odabraneAdrese);
        }
    }
    
    public void obirisiSveAdresePoruke() {
        if (adrese.isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:obrisiSvePorukeAdrese", new FacesMessage("Nema poruka za obrisati"));
        } else {
            spremisteJMSPoruka.obrisiAdresePoruke(adrese);
        }
    }
    
    public void obirisiMailPoruke() {
        if (odabraneEmailPoruke.isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:obrisiPorukeMail", new FacesMessage("Niste odabrali poruke za obrisati"));
        } else {
            spremisteJMSPoruka.obrisiMailPoruke(odabraneEmailPoruke);
        }
    }
    
    public void obirisiSveMailPoruke() {
        if (mailPoruke.isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:obrisiSvePorukeMail", new FacesMessage("Nema poruka za obrisati"));
        } else {
            spremisteJMSPoruka.obrisiMailPoruke(mailPoruke);
        }
    }
    
    public void dodajNovogKorisnika() {
        if (lozinka.trim().equals("") || korisnickoIme.trim().equals("")) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("forma:zaGreskuDodajKorisnika", new FacesMessage("Morate unijeti korisničko ime i lozinku"));
        } else {
            String komanda = "ADD " + korisnickoIme + "; PASSWD " + lozinka + ";";
            String odgovorServera = posaljiZahtjev(komanda);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:zaOdgovorServera", new FacesMessage("Odgovor slanja zahtjeva: " + odgovorServera));
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

    public PodaciObrade getOdabranaMailPorukaPregledNeuspjeh() {
        return odabranaMailPorukaPregledNeuspjeh;
    }

    public void setOdabranaMailPorukaPregledNeuspjeh(PodaciObrade odabranaMailPorukaPregledNeuspjeh) {
        this.odabranaMailPorukaPregledNeuspjeh = odabranaMailPorukaPregledNeuspjeh;
    }

    public PodaciObrade getOdabranaMailPorukaPregled() {
        return odabranaMailPorukaPregled;
    }

    public void setOdabranaMailPorukaPregled(PodaciObrade odabranaMailPorukaPregled) {
        this.odabranaMailPorukaPregled = odabranaMailPorukaPregled;
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

    public List<PodaciDodanaAdresa> getOdabraneAdrese() {
        return odabraneAdrese;
    }

    public void setOdabraneAdrese(List<PodaciDodanaAdresa> odabraneAdrese) {
        this.odabraneAdrese = odabraneAdrese;
    }

    public List<PodaciObrade> getOdabraneEmailPoruke() {
        return odabraneEmailPoruke;
    }

    public void setOdabraneEmailPoruke(List<PodaciObrade> odabraneEmailPoruke) {
        this.odabraneEmailPoruke = odabraneEmailPoruke;
    }
    
    /**
     * Otvara vezu prema serveru te mu šalje zahtjev
     *
     * @param komanda zahtjev koji se šalje serveru
     * @return odgovor od servera (String)
     */
    private String posaljiZahtjev(String komanda) {

        String porukaServera = null;

        InputStream is = null;
        OutputStream os = null;
        Socket socket = null;
        StringBuilder sb = null;

        try {
            socket = new Socket(server, port);
        } catch (IOException ex) {
            //Logger.getLogger(AdministratorSustava.class.getName()).log(Level.SEVERE, null, ex);
            return "Problem u spajanju na socket server";
        }

        try {
            os = socket.getOutputStream();
            is = socket.getInputStream();

            os.write(komanda.getBytes("ISO-8859-2"));
            os.flush();

            socket.shutdownOutput();

            sb = new StringBuilder();

            // Klijent ceka do 5 sekundi na odgovor servera
            for (int i = 0; i < 100; i++) {
                if (is.available() > 0) {
                    while (is.available() > 0) {
                        int znak = is.read();
                        if (znak == -1) {
                            break;
                        }
                        sb.append((char) znak);
                    }
                    break;
                } else {
                    sleep(50);
                }

            }

            while (is.available() > 0) {
                int znak = is.read();
                if (znak == -1) {
                    break;
                }
                sb.append((char) znak);
            }

            porukaServera = sb.toString();

        } catch (IOException ex) {
            Logger.getLogger(AdreseMDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(AdreseMDB.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(AdreseMDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (os != null) {
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(AdreseMDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(AdreseMDB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return porukaServera;

    }
    
}
