/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.zrna;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Thread.sleep;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.majugurci.ejb.eb.MajugurciGrupe;
import org.foi.nwtis.majugurci.ejb.eb.MajugurciKorisnici;
import org.foi.nwtis.majugurci.ejb.sb.MajugurciGrupeFacade;
import org.foi.nwtis.majugurci.ejb.sb.MajugurciKorisniciFacade;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.web.podaci.Korisnik;

/**
 *
 * @author Mario
 * 
 * Zrno za obradu registracije. Provjerava ispravnost korisničkih te ako su 
 * ispravni šalje zahtjev socket serveru da doda korisnika u svoju bazu. 
 * Ako socket server odgovori pozitivno tada ga dodajemo i u bazu
 * trenutnog sustava.
 */
@ManagedBean
@RequestScoped
public class RegistracijaObrada {

    @EJB
    private MajugurciGrupeFacade majugurciGrupeFacade;

    @EJB
    private MajugurciKorisniciFacade majugurciKorisniciFacade;

    private String korisnickoIme;
    private String lozinka;
    private String ponovljenaLozinka;

    ResourceBundle bundle;

    /**
     * Creates a new instance of RegistracijaObrada
     */
    public RegistracijaObrada() {
    }

    @PostConstruct
    public void init() {
        //ako korisnik rucno upise link a vec je prijavljen, napravi redirekt na index
        FacesContext context = FacesContext.getCurrentInstance();
        bundle = context.getApplication().getResourceBundle(context, "m");
        HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
        Korisnik k = (Korisnik) session.getAttribute("korisnik");
        if (k != null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("indexOdabirAkcije.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(LoginObrada.class.getName()).log(Level.SEVERE, null, ex);
            }
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

    public String getPonovljenaLozinka() {
        return ponovljenaLozinka;
    }

    public void setPonovljenaLozinka(String ponovljenaLozinka) {
        this.ponovljenaLozinka = ponovljenaLozinka;
    }

    /**
     * Provjera korisničkih unosa.
     * Slanje zahtjeva socket serveru.
     * Ako sve prođe u redu tada dodajemo korisnika u trenutnu bazu.
     * @return 
     */
    public String obradiRegistraciju() {
        FacesContext fc = FacesContext.getCurrentInstance();

        if (korisnickoIme.trim().equals("") || lozinka.trim().equals("")
                || ponovljenaLozinka.trim().equals("")) {
            fc.addMessage(null, new FacesMessage(bundle.getString("registracija_greskaNekaPoljaPrazna")));
            return "ERROR";
        }

        if (!lozinka.equals(ponovljenaLozinka)) {
            fc.addMessage(null, new FacesMessage(bundle.getString("registracija_greskaLozinke")));
            return "ERROR";
        }

        //posalji zahtjev na socket server
        String komanda = "ADD " + korisnickoIme + "; PASSWD " + lozinka + ";";
        String odgovor = posaljiZahtjevSocketServeru(komanda);

        System.out.println("Registracija korisnika - odgovor socket servera: " + odgovor);

        if (odgovor.equals("OK 10;")) {

            //upisi korisnika u BP
            List<MajugurciKorisnici> mkList = majugurciKorisniciFacade.findByKorisnik(korisnickoIme);

            if (mkList.isEmpty()) {
                MajugurciKorisnici mk = new MajugurciKorisnici();
                mk.setKorisnickoIme(korisnickoIme);
                mk.setLozinka(lozinka);
                mk.setVrijemeRegistracije(new Date());

                MajugurciGrupe mg = majugurciGrupeFacade.findByGrupa("korisnik").get(0);
                mk.setGrupa(mg);

                mk.setSredstva(new BigDecimal(0));
                mk.setPrvaPrijava(1);

                majugurciKorisniciFacade.create(mk);

            } else {
                fc.addMessage(null, new FacesMessage(bundle.getString("registracija_greskaKorImeZauzeto")));
                return "ERROR";
            }

        } else {
            fc.addMessage(null, new FacesMessage(bundle.getString("registracija_greskaKorImeZauzeto")));
            return "ERROR";
        }

        return "OK";
    }

    /**
     * Slanje zahtjeva socket serveru
     * @param komanda Komanda koja se šalje
     * @return odgovor servera
     */
    private String posaljiZahtjevSocketServeru(String komanda) {
        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        Konfiguracija konfigRazno = (Konfiguracija) sc.getAttribute("konfigRazno");

        String server = konfigRazno.dajPostavku("server");
        int port = Integer.parseInt(konfigRazno.dajPostavku("port"));

        String odgovor = null;

        try {
            Socket socket = new Socket(server, port);

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            os.write(komanda.getBytes("ISO-8859-2"));
            os.flush();

            socket.shutdownOutput();

            StringBuilder sb = new StringBuilder();

            /*
             Klijent ceka do 3 sekunde na odgovor servera
             */
            for (int i = 0; i < 60; i++) {
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

            odgovor = sb.toString();

            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    Logger.getLogger(RegistracijaObrada.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    Logger.getLogger(RegistracijaObrada.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(RegistracijaObrada.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(RegistracijaObrada.class.getName()).log(Level.SEVERE, null, ex);
        }

        return odgovor;
    }
}
