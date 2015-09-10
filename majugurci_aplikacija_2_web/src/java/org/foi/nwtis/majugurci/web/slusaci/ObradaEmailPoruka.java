/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.slusaci;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.foi.nwtis.majugurci.ejb.eb.MajugurciGrupe;
import org.foi.nwtis.majugurci.ejb.eb.MajugurciKorisnici;
import org.foi.nwtis.majugurci.ejb.sb.MajugurciGrupeFacade;
import org.foi.nwtis.majugurci.ejb.sb.MajugurciKorisniciFacade;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.konfiguracije.bp.BP_Konfiguracija;

/**
 *
 * @author Mario
 * 
 * Dretva koja u pravilnom vremenskom intervalu provjerava poštanski sandučić
 * korisnika iz konfiguracijske datoteke te šalje statističku JMS poruku u
 * red JMS poruka.
 */
public class ObradaEmailPoruka extends Thread {

    MajugurciKorisniciFacade majugurciKorisniciFacade = lookupMajugurciKorisniciFacadeBean();
    MajugurciGrupeFacade majugurciGrupeFacade = lookupMajugurciGrupeFacadeBean();

    private final Konfiguracija konfigRazno;
    private final BP_Konfiguracija konfigBaza;

    //podaci za spajanje na mail server
    private final String server;
    private final int port;
    private final String korisnik;
    private final String lozinka;

    private int interval;   //interval spavanja dretve

    private String NWTiS_predmet;   //predmet mail poruke koju dretva treba obraditi
    private String mapaNWTisPoruke; //mapa u koju se spremaju poruke s ispravnom sintaksom
    private String mapaNeNWTisPoruke;   //mapa u koju se spremaju poruke s neipsravnom sintaksom

    Folder osnovnaMapa;

    public ObradaEmailPoruka(Konfiguracija konfigRazno, BP_Konfiguracija konfigBaza) {
        this.konfigRazno = konfigRazno;
        this.konfigBaza = konfigBaza;

        this.server = konfigRazno.dajPostavku("mail.server");
        this.port = Integer.parseInt(konfigRazno.dajPostavku("mail.port"));
        this.korisnik = konfigRazno.dajPostavku("mail.posiljatelj");
        this.lozinka = konfigRazno.dajPostavku("mail.posiljatelj.lozinka");

        NWTiS_predmet = konfigRazno.dajPostavku("mail.poruka.predmet");
        mapaNWTisPoruke = konfigRazno.dajPostavku("mail.poruke.ispravne");
        mapaNeNWTisPoruke = konfigRazno.dajPostavku("mail.poruke.ostale");

        interval = Integer.parseInt(konfigRazno.dajPostavku("dretva.interval"));
    }

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        while (true) {
            long pocetak = System.currentTimeMillis();

            int brojProcitanihPoruka = 0;
            int brojNWTiSPoruka = 0;
            Date pocetakObrade = new Date(pocetak);
            Date krajObrade;
            List<String> dodaniKorisnici = new ArrayList<>();
            List<String> neupjesniKorisnici = new ArrayList<>();

            try {

                // Start the session
                java.util.Properties properties = System.getProperties();
                properties.put("mail.smtp.host", server);
                Session session = Session.getInstance(properties, null);
                // Connect to the store
                Store store = session.getStore("imap");
                store.connect(server, port, korisnik, lozinka);
                // Open the INBOX folder
                osnovnaMapa = store.getDefaultFolder();

                if (!osnovnaMapa.getFolder(mapaNWTisPoruke).exists()) {
                    osnovnaMapa.getFolder(mapaNWTisPoruke).create(Folder.HOLDS_MESSAGES);
                }

                if (!osnovnaMapa.getFolder(mapaNeNWTisPoruke).exists()) {
                    osnovnaMapa.getFolder(mapaNeNWTisPoruke).create(Folder.HOLDS_MESSAGES);
                }

                Folder folder = store.getFolder("INBOX");
                folder.open(Folder.READ_WRITE);

                Message[] messages = folder.getMessages();

                brojProcitanihPoruka = messages.length;

                for (int i = 0; i < messages.length; ++i) {
                    MimeMessage m = (MimeMessage) messages[i];
                    String tip = m.getContentType().toLowerCase();
                    String[] tipovi = tip.split(";");
                    for (String t : tipovi) {
                        //ako je neki dio text/plain
                        if (t.trim().equals("text/plain")) {

                            if (m.getSubject().startsWith(NWTiS_predmet)) {

                                brojNWTiSPoruka++;

                                String sadrzaj = (String) m.getContent();
                                String[] recenice = sadrzaj.split("\r\n");

                                Matcher matcher = provjeraParametara(recenice[0]);
                                if (matcher != null) {

                                    String korisnik = matcher.group(1);
                                    String lozinka = matcher.group(2);

                                    List<MajugurciKorisnici> korisnici = majugurciKorisniciFacade.findByKorisnik(korisnik);

                                    if (korisnici.size() > 0) {
                                        //korisnik vec postoji u bazi
                                        neupjesniKorisnici.add(korisnik);
                                        //System.out.println("Korisnik: " + korisnici.get(0).getKorisnickoIme() + " već postoji u BP");
                                    } else {
                                        MajugurciKorisnici korisnikNovi = new MajugurciKorisnici();
                                        MajugurciGrupe grupa = new MajugurciGrupe(2);
                                        korisnikNovi.setGrupa(grupa);
                                        korisnikNovi.setKorisnickoIme(korisnik);
                                        korisnikNovi.setLozinka(lozinka);
                                        korisnikNovi.setVrijemeRegistracije(new Date());

                                        majugurciKorisniciFacade.create(korisnikNovi);

                                        dodaniKorisnici.add(korisnik);
                                    }
                                } else {
                                    //komanda ne odgovara
                                }

                                //prebaci poruku u folder s ispravnim nwtis porukama
                                prebaciPoruku(folder, mapaNWTisPoruke, messages[i]);
                            } else {
                                //prebaci poruku u folder s neispravnim nwtis porukama
                                prebaciPoruku(folder, mapaNeNWTisPoruke, messages[i]);
                            }

                            break;
                        } else if (t.trim().equals("text/html")) {
                            //prebaci poruku u folder s neispravnim nwtis porukama
                            prebaciPoruku(folder, mapaNeNWTisPoruke, messages[i]);
                        }
                    }
                }

                folder.close(true);
                store.close();

            } catch (NoSuchProviderException ex) {
                Logger.getLogger(ObradaEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MessagingException ex) {
                Logger.getLogger(ObradaEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ObradaEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }

            long kraj = System.currentTimeMillis();
            long trajanjeObrade = kraj - pocetak;

            krajObrade = new Date(kraj);
            
            long id = System.currentTimeMillis();

            //send JMS message
            PodaciObrade po = new PodaciObrade(id, pocetakObrade, krajObrade, brojProcitanihPoruka, brojNWTiSPoruka, dodaniKorisnici, neupjesniKorisnici);
            try {
                this.sendJMSMessageToNWTiS_majugurci_1(po);
            } catch (JMSException ex) {
                Logger.getLogger(ObradaEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NamingException ex) {
                Logger.getLogger(ObradaEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                sleep((this.interval * 1000) - trajanjeObrade);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(ObradaEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
                //vrijeme trajanja dretve je vece od vremena intervala
            } catch (InterruptedException ex) {
                //Logger.getLogger(ObradaEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }

        }

    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Provjera pruženog string-a u odnosu na sintaksu u metodi
     *
     * @param p string
     * @return objekt tipa Matcher ili null ako ne odgovara
     */
    public Matcher provjeraParametara(String p) {
        String sintaksa = "^ADD ([\\w-_]+); PASSWD ([\\w-_#!]+);$";

        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(p);
        boolean status = m.matches();
        if (status) {
            return m;
        } else {
            return null;
        }
    }

    private MajugurciGrupeFacade lookupMajugurciGrupeFacadeBean() {
        try {
            Context c = new InitialContext();
            return (MajugurciGrupeFacade) c.lookup("java:global/majugurci_aplikacija_2/majugurci_aplikacija_2_ejb/MajugurciGrupeFacade!org.foi.nwtis.majugurci.ejb.sb.MajugurciGrupeFacade");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    private MajugurciKorisniciFacade lookupMajugurciKorisniciFacadeBean() {
        try {
            Context c = new InitialContext();
            return (MajugurciKorisniciFacade) c.lookup("java:global/majugurci_aplikacija_2/majugurci_aplikacija_2_ejb/MajugurciKorisniciFacade!org.foi.nwtis.majugurci.ejb.sb.MajugurciKorisniciFacade");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    /**
     * Premješta poruku iz izvorišnog foldera u odredišni
     *
     * @param izvoriste - izvorišni folder
     * @param odrediste - odredišnifolder
     * @param m - poruka
     * @throws MessagingException
     */
    private void prebaciPoruku(Folder izvoriste, String odrediste, Message m) throws MessagingException {
        Folder odredisniFolder = osnovnaMapa.getFolder(odrediste);
        odredisniFolder.open(Folder.READ_WRITE);
        odredisniFolder.appendMessages(new Message[]{m});
        izvoriste.setFlags(new Message[]{m}, new Flags(Flags.Flag.DELETED), true);
        izvoriste.expunge();
        odredisniFolder.close(true);
    }

    private javax.jms.Message createJMSMessageForjmsNWTiS_majugurci_1(javax.jms.Session session, PodaciObrade messageData) throws JMSException {
        // TODO create and populate message to send
        ObjectMessage om = session.createObjectMessage();
        om.setObject(messageData);
        return om;
    }

    private void sendJMSMessageToNWTiS_majugurci_1(PodaciObrade messageData) throws JMSException, NamingException {
        Context c = new InitialContext();
        ConnectionFactory cf = (ConnectionFactory) c.lookup("java:comp/env/jms/NWTiS_QF_majugurci_1");
        Connection conn = null;
        javax.jms.Session s = null;
        try {
            conn = cf.createConnection();
            s = conn.createSession(false, s.AUTO_ACKNOWLEDGE);
            Destination destination = (Destination) c.lookup("java:comp/env/jms/NWTiS_majugurci_1");
            MessageProducer mp = s.createProducer(destination);
            mp.send(createJMSMessageForjmsNWTiS_majugurci_1(s, messageData));
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (JMSException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Cannot close session", e);
                }
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

}
