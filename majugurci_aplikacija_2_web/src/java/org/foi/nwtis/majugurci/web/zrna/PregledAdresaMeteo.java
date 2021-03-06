/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.zrna;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import net.java.dev.jaxb.array.StringArray;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.web.podaci.Korisnik;
import org.foi.nwtis.majugurci.web.podaci.PodaciDodanaAdresa;
import org.foi.nwtis.majugurci.web.podaci.sveAdreseId;
import org.foi.nwtis.majugurci.ws.klijenti.MeteoPodaciWSKlijent;
import org.foi.nwtis.majugurci.ws.serveri.AdreseKorisnikaOmotac;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPodaci;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciListOmotac;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciOmotac;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPrognoza;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPrognozaListOmotac;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPrognozaSati;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPrognozaSatiListOmotac;
import org.foi.nwtis.majugurci.ws.serveri.MeteoStanice;
import org.foi.nwtis.majugurci.ws.serveri.MeteoStaniceListOmotac;
import org.foi.nwtis.majugurci.ws.serveri.RangListaOmotac;

/**
 *
 * @author Mario
 *
 * Klasa koja se bavi dohvaćanjem podataka sa SOAP servisa i njihovim prikazom.
 * Bavi se i slanjem JMS poruke.
 *
 * Te implementirana je JavaScript podrška za prikaz adresa i meteo podataka na
 * Google Maps.
 */
@ManagedBean
@ViewScoped
public class PregledAdresaMeteo implements Serializable {

    private boolean pregledDodajNovuAdresu = false;
    private String adresaDodajNovu;

    private boolean pregledAdresa = false;
    private List<String> sveAdrese = new ArrayList<>();
    private List<sveAdreseId> sveAdreseId = new ArrayList<>();
    private List<sveAdreseId> izabraneAdrese = new ArrayList<>();
    private String izabraneAdreseJS;
    private boolean prikaziMapu = false;
    private boolean noviProzor = false;

    private boolean pregledAdresaKorisnika = false;
    private List<String> sveAdreseKorisnika = new ArrayList<>();

    private boolean pregledTrenutniMeteoPodaci = false;
    private MeteoPodaci trenutniMeteoPodaci = new MeteoPodaci();
    private String adresaZaTrenutneMeteoPodatke;

    private boolean pregledRangListeAdresa = false;
    private List<StringArray> rangListaAdresa = new ArrayList<>();
    private int brojAdresaZaRangListu;

    private boolean pregledMeteoPodatakaInterval = false;
    List<MeteoPodaci> meteoPodaciInterval = new ArrayList<>();
    private String adresaMPInterval;
    private Date pocetniDatum;
    private Date zavrsniDatum;

    private boolean pregledMeteoStaniceUBliziniAdrese = false;
    private List<MeteoStanice> meteoStaniceUBliziniAdrese = new ArrayList<>();
    private String adresaMeteoStaniceUBlizinAdrese;
    private int brojStanicaUBliziniAdrese;

    private boolean pregledPrognozeBrojSati = false;
    private List<MeteoPrognozaSati> meteoPrognozeBrojSati = new ArrayList<>();
    private String adresaPrognozaBrojSati;
    private int brojSatiPrognoza;

    private boolean pregledPrognozeBrojDana = false;
    private List<MeteoPrognoza> meteoPrognozeBrojDana = new ArrayList<>();
    private String adresaPrognozaBrojDana;
    private int brojDanaPrognoza;

    private String korisnickoIme;
    private String lozinka;

    ResourceBundle bundle;

    private String OWMapKey;
    private String GMJSAPIKey;

    /**
     * Creates a new instance of PregledAdresaMeteo
     */
    public PregledAdresaMeteo() {
    }

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        bundle = context.getApplication().getResourceBundle(context, "m");
        HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
        Korisnik k = (Korisnik) session.getAttribute("korisnik");
        if (k == null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(LoginObrada.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            korisnickoIme = k.getKorisnickoIme();
            lozinka = k.getLozinka();
        }

        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        Konfiguracija konfigRazno = (Konfiguracija) sc.getAttribute("konfigRazno");

        this.GMJSAPIKey = konfigRazno.dajPostavku("GoogleMapsJSAPI.key");
        this.OWMapKey = konfigRazno.dajPostavku("openWeatherMap.key");
    }

    public String getGMJSAPIKey() {
        return GMJSAPIKey;
    }

    public void setGMJSAPIKey(String GMJSAPIKey) {
        this.GMJSAPIKey = GMJSAPIKey;
    }

    public String getOWMapKey() {
        return OWMapKey;
    }

    public void setOWMapKey(String OWMapKey) {
        this.OWMapKey = OWMapKey;
    }

    public List<sveAdreseId> getIzabraneAdrese() {
        return izabraneAdrese;
    }

    public void setIzabraneAdrese(List<sveAdreseId> izabraneAdrese) {
        this.izabraneAdrese = izabraneAdrese;
    }

    public String getIzabraneAdreseJS() {
        return izabraneAdreseJS;
    }

    public void setIzabraneAdreseJS(String izabraneAdreseJS) {
        this.izabraneAdreseJS = izabraneAdreseJS;
    }

    public boolean isPrikaziMapu() {
        return prikaziMapu;
    }

    public void setPrikaziMapu(boolean prikaziMapu) {
        this.prikaziMapu = prikaziMapu;
    }

    public boolean isNoviProzor() {
        return noviProzor;
    }

    public void setNoviProzor(boolean noviProzor) {
        this.noviProzor = noviProzor;
    }

    /**
     * Metoda koja poziva pripremu JS objekta adresa te postavlja varijable
     */
    public void jsTestFunction() {
        pripremiJSAdreseObjekt();
        
        noviProzor = false;
        prikaziMapu = true;
    }

    /**
     * Metoda koja poziva pripremu JS objekta adresa te postavlja varijable
     */
    public void jsTestFunctionNoviProzor() {
        pripremiJSAdreseObjekt();
        
        noviProzor = true;
    }

    /**
     * Priprema JS objekta tipa array
     */
    private void pripremiJSAdreseObjekt() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < izabraneAdrese.size(); i++) {
            sb.append("\"").append(izabraneAdrese.get(i).getAdresa()).append("\"");
            if (i + 1 < izabraneAdrese.size()) {
                sb.append(",");
            }
        }
        sb.append("]");
        
        izabraneAdreseJS = sb.toString();
    }

    public void zatvoriMapu() {
        prikaziMapu = false;
    }

    public boolean isPregledDodajNovuAdresu() {
        return pregledDodajNovuAdresu;
    }

    public void setPregledDodajNovuAdresu(boolean pregledDodajNovuAdresu) {
        this.pregledDodajNovuAdresu = pregledDodajNovuAdresu;
    }

    public String getAdresaDodajNovu() {
        return adresaDodajNovu;
    }

    public void setAdresaDodajNovu(String adresaDodajNovu) {
        this.adresaDodajNovu = adresaDodajNovu;
    }

    public boolean isPregledAdresa() {
        return pregledAdresa;
    }

    public void setPregledAdresa(boolean pregledAdresa) {
        this.pregledAdresa = pregledAdresa;
    }

    public List<String> getSveAdrese() {
        return sveAdrese;
    }

    public void setSveAdrese(List<String> sveAdrese) {
        this.sveAdrese = sveAdrese;
    }

    public List<sveAdreseId> getSveAdreseId() {
        return sveAdreseId;
    }

    public void setSveAdreseId(List<sveAdreseId> sveAdreseId) {
        this.sveAdreseId = sveAdreseId;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public boolean isPregledAdresaKorisnika() {
        return pregledAdresaKorisnika;
    }

    public void setPregledAdresaKorisnika(boolean pregledAdresaKorisnika) {
        this.pregledAdresaKorisnika = pregledAdresaKorisnika;
    }

    public List<String> getSveAdreseKorisnika() {
        return sveAdreseKorisnika;
    }

    public void setSveAdreseKorisnika(List<String> sveAdreseKorisnika) {
        this.sveAdreseKorisnika = sveAdreseKorisnika;
    }

    public boolean isPregledTrenutniMeteoPodaci() {
        return pregledTrenutniMeteoPodaci;
    }

    public void setPregledTrenutniMeteoPodaci(boolean pregledTrenutniMeteoPodaci) {
        this.pregledTrenutniMeteoPodaci = pregledTrenutniMeteoPodaci;
    }

    public MeteoPodaci getTrenutniMeteoPodaci() {
        return trenutniMeteoPodaci;
    }

    public void setTrenutniMeteoPodaci(MeteoPodaci trenutniMeteoPodaci) {
        this.trenutniMeteoPodaci = trenutniMeteoPodaci;
    }

    public String getAdresaZaTrenutneMeteoPodatke() {
        return adresaZaTrenutneMeteoPodatke;
    }

    public void setAdresaZaTrenutneMeteoPodatke(String adresaZaTrenutneMeteoPodatke) {
        this.adresaZaTrenutneMeteoPodatke = adresaZaTrenutneMeteoPodatke;
    }

    public boolean isPregledRangListeAdresa() {
        return pregledRangListeAdresa;
    }

    public void setPregledRangListeAdresa(boolean pregledRangListeAdresa) {
        this.pregledRangListeAdresa = pregledRangListeAdresa;
    }

    public List<StringArray> getRangListaAdresa() {
        return rangListaAdresa;
    }

    public void setRangListaAdresa(List<StringArray> rangListaAdresa) {
        this.rangListaAdresa = rangListaAdresa;
    }

    public int getBrojAdresaZaRangListu() {
        return brojAdresaZaRangListu;
    }

    public void setBrojAdresaZaRangListu(int brojAdresaZaRangListu) {
        this.brojAdresaZaRangListu = brojAdresaZaRangListu;
    }

    public boolean isPregledMeteoPodatakaInterval() {
        return pregledMeteoPodatakaInterval;
    }

    public void setPregledMeteoPodatakaInterval(boolean pregledMeteoPodatakaInterval) {
        this.pregledMeteoPodatakaInterval = pregledMeteoPodatakaInterval;
    }

    public List<MeteoPodaci> getMeteoPodaciInterval() {
        return meteoPodaciInterval;
    }

    public void setMeteoPodaciInterval(List<MeteoPodaci> meteoPodaciInterval) {
        this.meteoPodaciInterval = meteoPodaciInterval;
    }

    public String getAdresaMPInterval() {
        return adresaMPInterval;
    }

    public void setAdresaMPInterval(String adresaMPInterval) {
        this.adresaMPInterval = adresaMPInterval;
    }

    public Date getPocetniDatum() {
        return pocetniDatum;
    }

    public void setPocetniDatum(Date pocetniDatum) {
        this.pocetniDatum = pocetniDatum;
    }

    public Date getZavrsniDatum() {
        return zavrsniDatum;
    }

    public void setZavrsniDatum(Date zavrsniDatum) {
        this.zavrsniDatum = zavrsniDatum;
    }

    public boolean isPregledMeteoStaniceUBliziniAdrese() {
        return pregledMeteoStaniceUBliziniAdrese;
    }

    public void setPregledMeteoStaniceUBliziniAdrese(boolean pregledMeteoStaniceUBliziniAdrese) {
        this.pregledMeteoStaniceUBliziniAdrese = pregledMeteoStaniceUBliziniAdrese;
    }

    public List<MeteoStanice> getMeteoStaniceUBliziniAdrese() {
        return meteoStaniceUBliziniAdrese;
    }

    public void setMeteoStaniceUBliziniAdrese(List<MeteoStanice> meteoStaniceUBliziniAdrese) {
        this.meteoStaniceUBliziniAdrese = meteoStaniceUBliziniAdrese;
    }

    public int getBrojStanicaUBliziniAdrese() {
        return brojStanicaUBliziniAdrese;
    }

    public void setBrojStanicaUBliziniAdrese(int brojStanicaUBliziniAdrese) {
        this.brojStanicaUBliziniAdrese = brojStanicaUBliziniAdrese;
    }

    public String getAdresaMeteoStaniceUBlizinAdrese() {
        return adresaMeteoStaniceUBlizinAdrese;
    }

    public void setAdresaMeteoStaniceUBlizinAdrese(String adresaMeteoStaniceUBlizinAdrese) {
        this.adresaMeteoStaniceUBlizinAdrese = adresaMeteoStaniceUBlizinAdrese;
    }

    public boolean isPregledPrognozeBrojSati() {
        return pregledPrognozeBrojSati;
    }

    public void setPregledPrognozeBrojSati(boolean pregledPrognozeBrojSati) {
        this.pregledPrognozeBrojSati = pregledPrognozeBrojSati;
    }

    public List<MeteoPrognozaSati> getMeteoPrognozeBrojSati() {
        return meteoPrognozeBrojSati;
    }

    public void setMeteoPrognozeBrojSati(List<MeteoPrognozaSati> meteoPrognozeBrojSati) {
        this.meteoPrognozeBrojSati = meteoPrognozeBrojSati;
    }

    public String getAdresaPrognozaBrojSati() {
        return adresaPrognozaBrojSati;
    }

    public void setAdresaPrognozaBrojSati(String adresaPrognozaBrojSati) {
        this.adresaPrognozaBrojSati = adresaPrognozaBrojSati;
    }

    public int getBrojSatiPrognoza() {
        return brojSatiPrognoza;
    }

    public void setBrojSatiPrognoza(int brojSatiPrognoza) {
        this.brojSatiPrognoza = brojSatiPrognoza;
    }

    public boolean isPregledPrognozeBrojDana() {
        return pregledPrognozeBrojDana;
    }

    public void setPregledPrognozeBrojDana(boolean pregledPrognozeBrojDana) {
        this.pregledPrognozeBrojDana = pregledPrognozeBrojDana;
    }

    public List<MeteoPrognoza> getMeteoPrognozeBrojDana() {
        return meteoPrognozeBrojDana;
    }

    public void setMeteoPrognozeBrojDana(List<MeteoPrognoza> meteoPrognozeBrojDana) {
        this.meteoPrognozeBrojDana = meteoPrognozeBrojDana;
    }

    public String getAdresaPrognozaBrojDana() {
        return adresaPrognozaBrojDana;
    }

    public void setAdresaPrognozaBrojDana(String adresaPrognozaBrojDana) {
        this.adresaPrognozaBrojDana = adresaPrognozaBrojDana;
    }

    public int getBrojDanaPrognoza() {
        return brojDanaPrognoza;
    }

    public void setBrojDanaPrognoza(int brojDanaPrognoza) {
        this.brojDanaPrognoza = brojDanaPrognoza;
    }

    /**
     * Šalje JMS poruku aplikaciji 3 za dodavanje nove adrese
     */
    public void dodajNovuAdresu() {

        if (adresaDodajNovu.trim().isEmpty()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("forma:zaGreskuDodajNovuAdresu", new FacesMessage(bundle.getString("pregledAdresa_greskaUnesiteAdresu")));
            pregledDodajNovuAdresu = false;
        } else {
            //posalji JMS poruku
            pregledDodajNovuAdresu = true;

            long id = System.currentTimeMillis();

            PodaciDodanaAdresa pda = new PodaciDodanaAdresa(id, korisnickoIme, lozinka, adresaDodajNovu);
            try {
                this.sendJMSMessageToNWTiS_majugurci_2(pda);
            } catch (JMSException ex) {
                Logger.getLogger(PregledAdresaMeteo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NamingException ex) {
                Logger.getLogger(PregledAdresaMeteo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Pomoćna metoda koja zatvara pregled dodane adrese
     */
    public void zatvoriDodavanjeNoveAdrese() {
        pregledDodajNovuAdresu = false;
        adresaDodajNovu = "";
    }

    /**
     * Poziva SOAP servis i dohvaća sve adrese za koje se prikupljaju meteo
     * podaci
     */
    public void dajPregledSvihAdresa() {

        AdreseKorisnikaOmotac ako = MeteoPodaciWSKlijent.dajSveAdrese(korisnickoIme, lozinka);

        if (ako.getPoruka() != null) {
            //dogodila se greska u pozivu i nema podataka
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("forma:zaGreskuSveAdrese", new FacesMessage(ako.getPoruka()));
            pregledAdresa = false;
        } else {
            sveAdrese = ako.getAdrese();
            for (int i = 0; i < sveAdrese.size(); i++) {
                sveAdreseId.add(new sveAdreseId(i, sveAdrese.get(i)));
            }
            pregledAdresa = true;
        }

    }

    /**
     * Pomoćna metoda koja zatvara pregled svih adresa
     */
    public void zatvoriPregledSvihAdresa() {
        pregledAdresa = false;
        sveAdrese.clear();
    }

    /**
     * Poziva SOAP servis i dohvaća sve adrese koje je korisnik dodao i za koje
     * se prikupljaju meteo podaci
     */
    public void dajPregledSvihAdresaKorisnika() {

        AdreseKorisnikaOmotac ako = MeteoPodaciWSKlijent.dajAdreseKorisnika(korisnickoIme, lozinka);

        if (ako.getPoruka() != null) {
            //dogodila se greska u pozivu i nema podataka
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("forma:zaGreskuSveAdreseKorisnika", new FacesMessage(ako.getPoruka()));
            pregledAdresaKorisnika = false;
        } else {
            sveAdreseKorisnika = ako.getAdrese();
            pregledAdresaKorisnika = true;
        }

    }

    /**
     * Pomoćna metoda koja zatvara pregled svih adresa koje je korisnik dodao a
     * za koje se prikupljaju meteo podaci
     */
    public void zatvoriPregledSvihAdresaKorisnika() {
        pregledAdresaKorisnika = false;
        sveAdreseKorisnika.clear();
    }

    /**
     * Poziva SOAP servis i dohvaća sve adrese koje je korisnik dodao i za koje
     * se prikupljaju meteo podaci
     */
    public void dajPregledTrenutnihMeteoPodataka() {

        MeteoPodaciOmotac mpo = MeteoPodaciWSKlijent.dajTrenutneMeteoPodatkeZaAdresu(adresaZaTrenutneMeteoPodatke, korisnickoIme, lozinka);

        if (mpo.getPoruka() != null) {
            //dogodila se greska u pozivu i nema podataka
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("forma:zaGreskuTrenutniMeteoPodaci", new FacesMessage(mpo.getPoruka()));
            pregledTrenutniMeteoPodaci = false;
        } else {
            trenutniMeteoPodaci = mpo.getMeteoPodaci();
            pregledTrenutniMeteoPodaci = true;
        }

    }

    /**
     * Pomoćna metoda koja zatvara pregled trenutnih meteo podataka za adresu
     */
    public void zatvoriPregledTrenutnihMeteoPodataka() {
        pregledTrenutniMeteoPodaci = false;
        trenutniMeteoPodaci = new MeteoPodaci();
    }

    /**
     * Poziva SOAP servis i dohvaća rang listu adresa za koje je prikupljeno
     * najviše meteo podataka
     */
    public void dajPregledRangListeAdresa() {

        RangListaOmotac rlo = MeteoPodaciWSKlijent.dajRangListu(brojAdresaZaRangListu, korisnickoIme, lozinka);

        if (rlo.getPoruka() != null) {
            //dogodila se greska u pozivu i nema podataka
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("forma:zaGreskuRangListaAdresa", new FacesMessage(rlo.getPoruka()));
            pregledRangListeAdresa = false;
        } else {
            rangListaAdresa = rlo.getAdrese();
            pregledRangListeAdresa = true;
        }

    }

    /**
     * Pomoćna metoda koja zatvara pregled trenutnih meteo podataka za adresu
     */
    public void zatvoriPregledRangListeAdresa() {
        pregledRangListeAdresa = false;
        rangListaAdresa.clear();
    }

    /**
     * Metoda koja javlja pogrešku ako se ne unese broj elemenata rang liste,
     * odnosno ako je taj broj manji od 1
     *
     *
     * @param facesContext
     * @param arg1
     * @param value
     * @throws ValidatorException
     */
    public void validatorBrojElemenataRang(FacesContext facesContext, UIComponent arg1, Object value) throws ValidatorException {

        int vrijednost = 0;
        boolean nijeBroj = false;

        if (value instanceof Integer) {
            vrijednost = (int) value;
        } else {
            nijeBroj = true;
        }

        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();

        String nazivGumba = params.get("odabraniGumb");

        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_ERROR);

        if (nazivGumba == null) {
            //System.out.println("Ovaj dio se ne izvrsava");
        } else if (nazivGumba.equals("Dohvati rang listu")) {
            if (value == null) {
                //ako korisnicki odabir adrese nije inicijaliziran
                message.setSummary(bundle.getString("pregledAdresa_greksaBrojElemenata"));
                message.setDetail(bundle.getString("pregledAdresa_greksaBrojElemenata"));
                facesContext.addMessage("brojElemenata", message);

                throw new ValidatorException(message);
            } else if (nijeBroj) {
                message.setSummary(bundle.getString("pregledAdresa_greskaCjelobrojnaVrijednost"));
                message.setDetail(bundle.getString("pregledAdresa_greskaCjelobrojnaVrijednost"));
                facesContext.addMessage("brojElemenata", message);
                throw new ValidatorException(message);
            } else if (vrijednost < 1) {
                message.setSummary(bundle.getString("pregledAdresa_greskaBrojVeciOdNule"));
                message.setDetail(bundle.getString("pregledAdresa_greskaBrojVeciOdNule"));
                facesContext.addMessage("brojElemenata", message);
                throw new ValidatorException(message);
            }
        }
    }

    /**
     * Poziva SOAP servis i meteo podatke za adresu u vremenskom intervalu
     */
    public void dajPregledMPInterval() {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        String pocetniDatum = df.format(this.pocetniDatum);
        String zavrsniDatum = df.format(this.zavrsniDatum);

        MeteoPodaciListOmotac mplo = MeteoPodaciWSKlijent.dajMeteoPodatkeUDatumskomIntervalu(adresaMPInterval,
                pocetniDatum, zavrsniDatum, korisnickoIme, lozinka);

        if (mplo.getPoruka() != null) {
            //dogodila se greska u pozivu i nema podataka
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("forma:zaGreskuMeteoPodaciUIntervalu", new FacesMessage(mplo.getPoruka()));
            pregledMeteoPodatakaInterval = false;
        } else {
            meteoPodaciInterval = mplo.getMeteoPodaci();
            pregledMeteoPodatakaInterval = true;
        }

    }

    /**
     * Pomoćna metoda koja zatvara pregled meteo podataka za adresu i vremenskom
     * intervalu
     */
    public void zatvoriPregledMPUIntervalu() {
        pregledMeteoPodatakaInterval = false;
        meteoPodaciInterval.clear();
    }

    /**
     * Metoda koja javlja pogrešku ako se ne unesu datumi za pregled meteo
     * podataka u vremenskom intervalu
     *
     *
     * @param facesContext
     * @param arg1
     * @param value
     * @throws ValidatorException
     */
    public void validatorVremenskiInterval(FacesContext facesContext, UIComponent arg1, Object value) throws ValidatorException {

        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();

        String nazivGumba = params.get("odabraniGumb");

        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_ERROR);

        if (nazivGumba == null) {
            //System.out.println("Ovaj dio se ne izvrsava");
        } else if (nazivGumba.equals("dohvatiMeteoPodatkeUDatInt")) {
            if (value == null) {
                //ako korisnicki odabir adrese nije inicijaliziran
                message.setSummary(bundle.getString("pregledAdresa_greskaDatum"));
                message.setDetail(bundle.getString("pregledAdresa_greskaDatum"));
                facesContext.addMessage("datumPocetak", message);

                throw new ValidatorException(message);
            }
        }
    }

    /**
     * Poziva SOAP servis i dohvaća meteo stanice u blizini adrese
     */
    public void dajPregledMeteoStanicaUBliziniAdrese() {

        MeteoStaniceListOmotac mso = MeteoPodaciWSKlijent.dajVremenskeStaniceUBliziniAdrese(adresaMeteoStaniceUBlizinAdrese,
                brojStanicaUBliziniAdrese, korisnickoIme, lozinka);

        if (mso.getPoruka() != null) {
            //dogodila se greska u pozivu i nema podataka
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("forma:zaGreskuMeteoStaniceUBliziniAdrese", new FacesMessage(mso.getPoruka()));
            pregledMeteoStaniceUBliziniAdrese = false;
        } else {
            meteoStaniceUBliziniAdrese = mso.getMeteoStanice();
            pregledMeteoStaniceUBliziniAdrese = true;
        }

    }

    /**
     * Pomoćna metoda koja zatvara pregled meteo stanica u blizini adrese
     */
    public void zatvoriPregledMeteoStaniceUBliziniAdrese() {
        pregledMeteoStaniceUBliziniAdrese = false;
        meteoStaniceUBliziniAdrese.clear();
    }

    /**
     * Metoda koja javlja pogrešku ako se ne unese broj meteo stanica, odnosno
     * ako je taj broj manji od 1
     *
     *
     * @param facesContext
     * @param arg1
     * @param value
     * @throws ValidatorException
     */
    public void validatorBrojStanica(FacesContext facesContext, UIComponent arg1, Object value) throws ValidatorException {

        int vrijednost = 0;
        boolean nijeBroj = false;

        if (value instanceof Integer) {
            vrijednost = (int) value;
        } else {
            nijeBroj = true;
        }

        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();

        String nazivGumba = params.get("odabraniGumb");

        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_ERROR);

        if (nazivGumba == null) {
            //System.out.println("Ovaj dio se ne izvrsava");
        } else if (nazivGumba.equals("dohvatiMeteoStanice")) {
            if (value == null) {
                //ako korisnicki odabir adrese nije inicijaliziran
                message.setSummary(bundle.getString("pregledAdresa_greskaBrojStanica"));
                message.setDetail(bundle.getString("pregledAdresa_greskaBrojStanica"));
                facesContext.addMessage("brojStanica", message);

                throw new ValidatorException(message);
            } else if (nijeBroj) {
                message.setSummary(bundle.getString("pregledAdresa_greskaCjelobrojnaVrijednost"));
                message.setDetail(bundle.getString("pregledAdresa_greskaCjelobrojnaVrijednost"));
                facesContext.addMessage("brojStanica", message);
                throw new ValidatorException(message);
            } else if (vrijednost < 1) {
                message.setSummary(bundle.getString("pregledAdresa_greskaBrojVeciOdNule"));
                message.setDetail(bundle.getString("pregledAdresa_greskaBrojVeciOdNule"));
                facesContext.addMessage("brojStanica", message);
                throw new ValidatorException(message);
            }
        }
    }

    /**
     * Poziva SOAP servis i dohvaća meteo prognozu za adresu za iducih n sati
     */
    public void dajPregledMeteoPrognozePoSatima() {

        MeteoPrognozaSatiListOmotac mpslo = MeteoPodaciWSKlijent.dajPrognozuZaBrojSati(adresaPrognozaBrojSati,
                brojSatiPrognoza, korisnickoIme, lozinka);

        if (mpslo.getPoruka() != null) {
            //dogodila se greska u pozivu i nema podataka
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("forma:zaGreskuPrognozaBrojSati", new FacesMessage(mpslo.getPoruka()));
            pregledPrognozeBrojSati = false;
        } else {
            meteoPrognozeBrojSati = mpslo.getMeteoPrognozaSati();
            //miče sve prazne zapise
            meteoPrognozeBrojSati.removeAll(Collections.singleton(null));
            pregledPrognozeBrojSati = true;
        }

    }

    /**
     * Pomoćna metoda koja zatvara pregled prognoze po broju sati
     */
    public void zatvoriPregledPrognozeBrojSati() {
        pregledPrognozeBrojSati = false;
        meteoPrognozeBrojSati.clear();
    }

    /**
     * Metoda koja javlja pogrešku ako se ne unese broj sati, odnosno ako je taj
     * broj manji od 1
     *
     *
     * @param facesContext
     * @param arg1
     * @param value
     * @throws ValidatorException
     */
    public void validatorBrojSati(FacesContext facesContext, UIComponent arg1, Object value) throws ValidatorException {

        int vrijednost = 0;
        boolean nijeBroj = false;

        if (value instanceof Integer) {
            vrijednost = (int) value;
        } else {
            nijeBroj = true;
        }

        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();

        String nazivGumba = params.get("odabraniGumb");

        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_ERROR);

        if (nazivGumba == null) {
            //System.out.println("Ovaj dio se ne izvrsava");
        } else if (nazivGumba.equals("dohvatiMeteoPrognozuSati")) {
            if (value == null) {
                //ako korisnicki odabir adrese nije inicijaliziran
                message.setSummary(bundle.getString("pregledAdresa_greskaBrojSati"));
                message.setDetail(bundle.getString("pregledAdresa_greskaBrojSati"));
                facesContext.addMessage("brojSati", message);

                throw new ValidatorException(message);
            } else if (nijeBroj) {
                message.setSummary(bundle.getString("pregledAdresa_greskaCjelobrojnaVrijednost"));
                message.setDetail(bundle.getString("pregledAdresa_greskaCjelobrojnaVrijednost"));
                facesContext.addMessage("brojSati", message);
                throw new ValidatorException(message);
            } else if (vrijednost < 1) {
                message.setSummary(bundle.getString("pregledAdresa_greskaBrojVeciOdNule"));
                message.setDetail(bundle.getString("pregledAdresa_greskaBrojVeciOdNule"));
                facesContext.addMessage("brojSati", message);
                throw new ValidatorException(message);
            }
        }
    }

    /**
     * Poziva SOAP servis i dohvaća meteo prognozu za adresu za iducih n dana
     */
    public void dajPregledMeteoPrognozePoDanima() {

        MeteoPrognozaListOmotac mplo = MeteoPodaciWSKlijent.dajPrognozuZaBrojDana(adresaPrognozaBrojDana,
                brojDanaPrognoza, korisnickoIme, lozinka);

        if (mplo.getPoruka() != null) {
            //dogodila se greska u pozivu i nema podataka
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("forma:zaGreskuPrognozaBrojDana", new FacesMessage(mplo.getPoruka()));
            pregledPrognozeBrojDana = false;
        } else {
            meteoPrognozeBrojDana = mplo.getMeteoPrognoza();
            //miče sve prazne zapise
            //meteoPrognozeBrojSati.removeAll(Collections.singleton(null));
            pregledPrognozeBrojDana = true;
        }

    }

    /**
     * Pomoćna metoda koja zatvara pregled prognoze za broj dana
     */
    public void zatvoriPregledPrognozeBrojDana() {
        pregledPrognozeBrojDana = false;
        meteoPrognozeBrojDana.clear();
    }

    /**
     * Metoda koja javlja pogrešku ako se ne unese broj dana, odnosno ako je taj
     * broj manji od 1
     *
     *
     * @param facesContext
     * @param arg1
     * @param value
     * @throws ValidatorException
     */
    public void validatorBrojDana(FacesContext facesContext, UIComponent arg1, Object value) throws ValidatorException {

        int vrijednost = 0;
        boolean nijeBroj = false;

        if (value instanceof Integer) {
            vrijednost = (int) value;
        } else {
            nijeBroj = true;
        }

        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();

        String nazivGumba = params.get("odabraniGumb");

        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_ERROR);

        if (nazivGumba == null) {
            //System.out.println("Ovaj dio se ne izvrsava");
        } else if (nazivGumba.equals("dohvatiMeteoPrognozuDani")) {
            if (value == null) {
                //ako korisnicki odabir adrese nije inicijaliziran
                message.setSummary(bundle.getString("pregledAdresa_greskaBrojDana"));
                message.setDetail(bundle.getString("pregledAdresa_greskaBrojDana"));
                facesContext.addMessage("brojDana", message);

                throw new ValidatorException(message);
            } else if (nijeBroj) {
                message.setSummary(bundle.getString("pregledAdresa_greskaCjelobrojnaVrijednost"));
                message.setDetail(bundle.getString("pregledAdresa_greskaCjelobrojnaVrijednost"));
                facesContext.addMessage("brojDana", message);
                throw new ValidatorException(message);
            } else if (vrijednost < 1) {
                message.setSummary(bundle.getString("pregledAdresa_greskaBrojVeciOdNule"));
                message.setDetail(bundle.getString("pregledAdresa_greskaBrojVeciOdNule"));
                facesContext.addMessage("brojDana", message);
                throw new ValidatorException(message);
            }
        }
    }

    private javax.jms.Message createJMSMessageForjmsNWTiS_majugurci_2(javax.jms.Session session, PodaciDodanaAdresa messageData) throws JMSException {
        // TODO create and populate message to send
        ObjectMessage om = session.createObjectMessage();
        om.setObject(messageData);
        return om;
    }

    private void sendJMSMessageToNWTiS_majugurci_2(PodaciDodanaAdresa messageData) throws JMSException, NamingException {
        Context c = new InitialContext();
        ConnectionFactory cf = (ConnectionFactory) c.lookup("java:comp/env/jms/NWTiS_QF_majugurci_2");
        Connection conn = null;
        javax.jms.Session s = null;
        try {
            conn = cf.createConnection();
            s = conn.createSession(false, s.AUTO_ACKNOWLEDGE);
            Destination destination = (Destination) c.lookup("java:comp/env/jms/NWTiS_majugurci_2");
            MessageProducer mp = s.createProducer(destination);
            mp.send(createJMSMessageForjmsNWTiS_majugurci_2(s, messageData));
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
