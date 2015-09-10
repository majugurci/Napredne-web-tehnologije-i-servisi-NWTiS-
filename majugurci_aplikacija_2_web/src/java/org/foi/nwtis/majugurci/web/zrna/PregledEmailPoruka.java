/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.zrna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.web.podaci.Poruka;

/**
 *
 * @author Mario
 * 
 * Zrno koje se spaja na mail poslužitelj (James) te čita sve poruke za odabranu mapu.
 * Daje okviran popis svih mail poruka u mapi, a postoji i mogućnost uvida 
 * u detalje poruke.
 * Omogućava brisanje odabranih mail poruka.
 */
@ManagedBean
@SessionScoped
public class PregledEmailPoruka {

    private String server;
    private int port;
    private String korisnik;
    private String lozinka;

    private List<Poruka> poruke;
    private Map<String, String> mape;
    private String odabranaMapa = "INBOX";

    private boolean imaPoruka;

    private List<Poruka> odabranePoruke;

    private Poruka odabranaPorukaPregled;

    ResourceBundle bundle;

    /**
     * Creates a new instance of PregledEmailPoruka
     */
    public PregledEmailPoruka() {
    }

    /**
     * Učitavanje konfiguracijskih parametara i incijalno učitavanje poruka.
     */
    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        bundle = context.getApplication().getResourceBundle(context, "m");

        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        Konfiguracija konfigRazno = (Konfiguracija) sc.getAttribute("konfigRazno");

        this.server = konfigRazno.dajPostavku("mail.server");
        this.port = Integer.parseInt(konfigRazno.dajPostavku("mail.port"));
        this.korisnik = konfigRazno.dajPostavku("mail.posiljatelj");
        this.lozinka = konfigRazno.dajPostavku("mail.posiljatelj.lozinka");

        dohvatiMailPoruke();
    }

    private void dohvatiMailPoruke() {
        dohvatiMape();
        dohvatiPoruke();
    }

    /**
     * Spajanje na mail poslužitelj te dohvaćanje popisa mapa s porukama
     */
    private void dohvatiMape() {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundle = context.getApplication().getResourceBundle(context, "m");

        try {
            // Start the session
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", server);
            Session session = Session.getInstance(properties, null);

            // Connect to the store
            Store store = session.getStore("imap");
            store.connect(server, port, korisnik, lozinka);

            // Open the INBOX folder
            Folder osnovnaMapa = store.getDefaultFolder();

            Folder[] podMape = osnovnaMapa.list();
            mape = new HashMap<>();
            for (Folder f : podMape) {
                mape.put(f.getName(), f.getName());
            }

            store.close();
        } catch (AuthenticationFailedException ex) {
            Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
            //porukaGreska = bundle.getString("pregledSvihPoruka_spajanjeKriviPodaci");
        } catch (MessagingException ex) {
            Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
            //porukaGreska = bundle.getString("pregledSvihPoruka_spajanjeGreskaServer");
        }
    }

    /**
     * Spajanje na mail poslužitelj te za izabranu mapu dohvaćanje poruka
     */
    public void dohvatiPoruke() {
        try {
            // Start the session
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", server);
            Session session = Session.getInstance(properties, null);

            // Connect to the store
            Store store = session.getStore("imap");
            store.connect(server, port, korisnik, lozinka);

            // Open the INBOX folder
            Folder folder = store.getFolder(odabranaMapa);
            folder.open(Folder.READ_ONLY);

            int ukupanBrojPoruka = folder.getMessageCount();

            if (ukupanBrojPoruka > 0) {
                Message[] messages = folder.getMessages();

                poruke = new ArrayList<>();
                for (int i = 0; i < ukupanBrojPoruka; i++) {
                    MimeMessage m = (MimeMessage) messages[i];
                    //dohvati tekstualni dio poruke
                    String cistiTekst = null;

                    Object sadrzaj = null;
                    try {
                        sadrzaj = m.getContent();
                        if (sadrzaj instanceof Multipart) {
                            BodyPart tekst = null;
                            Multipart sadrzaj1 = (Multipart) sadrzaj;
                            int brojac = sadrzaj1.getCount();
                            for (int j = 0; j < brojac; j++) {
                                BodyPart dio = sadrzaj1.getBodyPart(j);
                                if (dio.isMimeType("text/plain") || dio.isMimeType("text/html")) {
                                    tekst = dio;
                                    break;
                                }
                            }
                            if (tekst != null) {
                                cistiTekst = (String) tekst.getContent();
                            }
                        } else if (sadrzaj instanceof String) {
                            cistiTekst = (String) sadrzaj;
                        } else {
                            // Nije MIME type
                            cistiTekst = null;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    Poruka p = new Poruka(m.getHeader("Message-ID")[0], m.getSentDate(),
                            m.getFrom()[0].toString(), m.getSubject(), m.getContentType(),
                            m.getSize(), 0, m.getFlags(), null, false, true, cistiTekst);
                    poruke.add(p);
                }
                imaPoruka = true;
            } else {
                poruke = new ArrayList<>();
                //javi da nema poruka
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage("forma:nemaPoruka", new FacesMessage(bundle.getString("email_greskaNemaPoruka")));
                imaPoruka = false;
            }

            folder.close(true);
            store.close();
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Traži odabrane poruke iz pogleda te ih briše sa mail servera.
     */
    public void obirisPoruke() {
        if (odabranePoruke.isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:obrisiPoruke", new FacesMessage(bundle.getString("email_greskaOdabranePorZaBrisanje")));
        } else {
            try {
                // Start the session
                java.util.Properties properties = System.getProperties();
                properties.put("mail.smtp.host", server);
                Session session = Session.getInstance(properties, null);

                // Connect to the store
                Store store = session.getStore("imap");
                store.connect(server, port, korisnik, lozinka);

                // Open the INBOX folder
                Folder folder = store.getFolder(odabranaMapa);
                folder.open(Folder.READ_WRITE);

                int ukupanBrojPoruka = folder.getMessageCount();

                Message[] messages = folder.getMessages();

                for (int i = 0; i < ukupanBrojPoruka; i++) {
                    MimeMessage m = (MimeMessage) messages[i];

                    for (Poruka p : odabranePoruke) {
                        if (m.getHeader("Message-ID")[0].equals(p.getId())) {
                            m.setFlag(Flags.Flag.DELETED, true);
                        }
                    }
                }

                folder.close(true);
                store.close();
            } catch (NoSuchProviderException ex) {
                Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MessagingException ex) {
                Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        dohvatiPoruke();
    }

    public List<Poruka> getPoruke() {
        return poruke;
    }

    public void setPoruke(List<Poruka> poruke) {
        this.poruke = poruke;
    }

    public Map<String, String> getMape() {
        return mape;
    }

    public void setMape(Map<String, String> mape) {
        this.mape = mape;
    }

    public String getOdabranaMapa() {
        return odabranaMapa;
    }

    public void setOdabranaMapa(String odabranaMapa) {
        this.odabranaMapa = odabranaMapa;
    }

    public boolean isImaPoruka() {
        return imaPoruka;
    }

    public void setImaPoruka(boolean imaPoruka) {
        this.imaPoruka = imaPoruka;
    }

    public List<Poruka> getOdabranePoruke() {
        return odabranePoruke;
    }

    public void setOdabranePoruke(List<Poruka> odabranePoruke) {
        this.odabranePoruke = odabranePoruke;
    }

    public Poruka getOdabranaPorukaPregled() {
        return odabranaPorukaPregled;
    }

    public void setOdabranaPorukaPregled(Poruka odabranaPorukaPregled) {
        this.odabranaPorukaPregled = odabranaPorukaPregled;
    }

}
