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
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.foi.nwtis.majugurci.ejb.mdb.AdreseMDB;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.web.podaci.Korisnik;
import org.foi.nwtis.majugurci.web.podaci.MeteoPodaci;

/**
 *
 * @author Mario
 * 
 * Dohvaćanje svih aktivnih korisnika aplikacije 2 preko REST servisa, 
 * dohvaćanje dodanih adresa korisnika preko REST servisa, te 
 * dohvaćanje trenutnih meteo podataka preko socket servera.
 */
@ManagedBean
@ViewScoped
public class RESTRadnje {

    private List<Korisnik> aktivniKorisnici;
    private boolean imaKorisnika = false;
    private Korisnik izabraniKorisnik;

    private List<String> adreseKorisnika;
    private boolean imaAdresa = false;
    private String izabranaAdresa;

    private String server;
    private int port;

    private boolean imaMeteoPodataka = false;
    private MeteoPodaci meteoPodaci;

    /**
     * Creates a new instance of restRadnje
     */
    public RESTRadnje() {
    }

    @PostConstruct
    public void init() {
        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        Konfiguracija konfigRazno = (Konfiguracija) sc.getAttribute("konfigRazno");

        this.server = konfigRazno.dajPostavku("server");
        this.port = Integer.parseInt(konfigRazno.dajPostavku("port"));
    }

    /**
     * Dohvaća korisničke podatke preko REST servisa aplikacije 2.
     */
    public void dohvatiKorPodatke() {
        Client client = ClientBuilder.newClient();
        WebTarget webResource = client.target("http://localhost:8080")
                .path("/majugurci_aplikacija_2_web/webresources/korisnickeAdreseREST");

        String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);

        try {
            JSONObject jo = new JSONObject(odgovor);
            aktivniKorisnici = new ArrayList<>();
            JSONObject aktKor = jo.getJSONObject("Aktivni korisnici");
            String status = aktKor.getString("status");

            if (status.equals("OK")) {
                //ima aktivnih korisnika
                imaKorisnika = true;
                JSONArray sviKorisnici = aktKor.getJSONArray("korisnici");
                int velicinaPolja = sviKorisnici.length();
                for (int i = 0; i < velicinaPolja; i++) {
                    Korisnik k = new Korisnik();

                    JSONObject korisnik = sviKorisnici.getJSONObject(i);

                    k.setIdKorisnika(korisnik.getInt("id"));
                    k.setKorisnickoIme(korisnik.getString("korisnickoIme"));
                    k.setLozinka(korisnik.getString("lozinka"));
                    k.setEmail(korisnik.getString("email"));
                    k.setGrupa(korisnik.getInt("grupaId"));
                    k.setGrupaOpis(korisnik.getString("grupaOpis"));
                    k.setVrijemeRegistracije(korisnik.getString("vrijemeRegistracije"));

                    aktivniKorisnici.add(k);
                }
            } else {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage("forma:zaOdgovorServera", new FacesMessage("Odgovor servera: " + status));
                imaKorisnika = false;
            }
        } catch (JSONException ex) {
            Logger.getLogger(RESTRadnje.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Dohvaća adrese koje je korisnik dodao REST servisa druge aplikacije.
     */
    public void dohvatiAdreseKorisnika() {
        if (izabraniKorisnik == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:zaGreskuAdrese", new FacesMessage("Morate odabrati korisnika iz tablice"));
            imaAdresa = false;
        } else {
            Client client = ClientBuilder.newClient();
            WebTarget webResource = client.target("http://localhost:8080")
                    .path("/majugurci_aplikacija_2_web/webresources/korisnickeAdreseREST/" + izabraniKorisnik.getIdKorisnika());

            String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);

            try {
                JSONObject jo = new JSONObject(odgovor);
                adreseKorisnika = new ArrayList<>();
                JSONObject aktKor = jo.getJSONObject("Adrese");
                String status = aktKor.getString("status");

                if (status.equals("OK")) {
                    //korisnik je dodao adrese
                    imaAdresa = true;
                    JSONArray sveAdrese = aktKor.getJSONArray("adrese");
                    int velicinaPolja = sveAdrese.length();
                    for (int i = 0; i < velicinaPolja; i++) {

                        JSONObject adresa = sveAdrese.getJSONObject(i);

                        adreseKorisnika.add(adresa.getString("adresa"));
                    }
                } else {
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.addMessage("forma:zaOdgovorServera", new FacesMessage("Odgovor servera: " + status));
                    imaAdresa = false;
                }
            } catch (JSONException ex) {
                Logger.getLogger(RESTRadnje.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Dohvaća zadnje spremljene meteopodatke putem socket servera prve aplikacije.
     */
    public void dohvatiMeteoPodatke() {
        if (izabranaAdresa == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:zaGreskuMeteo", new FacesMessage("Morate odabrati adresu za koju želite meteo podatke"));
            imaMeteoPodataka = false;
        } else {
            String korisnik = izabraniKorisnik.getKorisnickoIme();
            String lozinka = izabraniKorisnik.getLozinka();
            String adresa = izabranaAdresa;
            String komanda = "USER " + korisnik + "; PASSWD " + lozinka + "; GET \"" + adresa + "\";";

            String odgovorServera = posaljiZahtjev(komanda);

            if (odgovorServera.startsWith("OK 10;")) {
                //sve je dobro prošlo, prikaži meteo podatke korisniku
                String[] dijelovi = odgovorServera.split(" ");
                
                meteoPodaci = new MeteoPodaci();
                meteoPodaci.setTemperatureValue(Float.parseFloat(dijelovi[3]));
                meteoPodaci.setTemperatureUnit("Celsius");
                
                meteoPodaci.setHumidityValue(Float.parseFloat(dijelovi[5]));
                meteoPodaci.setHumidityUnit("%");
                
                meteoPodaci.setPressureValue(Float.parseFloat(dijelovi[7]));
                meteoPodaci.setPressureUnit("hPa");
                
                meteoPodaci.setLatitude(dijelovi[9]);
                //makni zadnji znak odgovora, točka zarez ;
                meteoPodaci.setLongitude(dijelovi[11].substring(0, dijelovi[11].length()-1));
                
                imaMeteoPodataka = true;
            } else {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage("forma:zaGreskuMeteo", new FacesMessage("Odgovor servera: " + odgovorServera));
            }
        }
    }

    public void zatvoriPregledKorisnika() {
        imaKorisnika = false;
    }

    public void zatvoriPregledAdresa() {
        imaAdresa = false;
    }
    
    public void zatvoriPregledMeteo() {
        imaMeteoPodataka = false;
    }

    public List<Korisnik> getAktivniKorisnici() {
        return aktivniKorisnici;
    }

    public void setAktivniKorisnici(List<Korisnik> aktivniKorisnici) {
        this.aktivniKorisnici = aktivniKorisnici;
    }

    public boolean isImaKorisnika() {
        return imaKorisnika;
    }

    public void setImaKorisnika(boolean imaKorisnika) {
        this.imaKorisnika = imaKorisnika;
    }

    public Korisnik getIzabraniKorisnik() {
        return izabraniKorisnik;
    }

    public void setIzabraniKorisnik(Korisnik izabraniKorisnik) {
        this.izabraniKorisnik = izabraniKorisnik;
    }

    public List<String> getAdreseKorisnika() {
        return adreseKorisnika;
    }

    public void setAdreseKorisnika(List<String> adreseKorisnika) {
        this.adreseKorisnika = adreseKorisnika;
    }

    public boolean isImaAdresa() {
        return imaAdresa;
    }

    public void setImaAdresa(boolean imaAdresa) {
        this.imaAdresa = imaAdresa;
    }

    public String getIzabranaAdresa() {
        return izabranaAdresa;
    }

    public void setIzabranaAdresa(String izabranaAdresa) {
        this.izabranaAdresa = izabranaAdresa;
    }

    public boolean isImaMeteoPodataka() {
        return imaMeteoPodataka;
    }

    public void setImaMeteoPodataka(boolean imaMeteoPodataka) {
        this.imaMeteoPodataka = imaMeteoPodataka;
    }

    public MeteoPodaci getMeteoPodaci() {
        return meteoPodaci;
    }

    public void setMeteoPodaci(MeteoPodaci meteoPodaci) {
        this.meteoPodaci = meteoPodaci;
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
