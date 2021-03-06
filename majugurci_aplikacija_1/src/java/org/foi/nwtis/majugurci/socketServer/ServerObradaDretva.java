/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.socketServer;

import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.majugurci.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.majugurci.rest.klijenti.GMKlijent;
import org.foi.nwtis.majugurci.web.podaci.Lokacija;
import org.foi.nwtis.majugurci.web.podaci.MeteoPodaci;

/**
 *
 * @author Mario
 * 
 * Dretva socket servera koja obrađuje jedan korisnički zahtjev.
 * Dretva čita zahtjev te ovisno o zahtjevu provodi određenu obradu.
 * Na kraju obrade šalje odgovor korisniku.
 */
public class ServerObradaDretva extends Thread {

    private final MeteoPodaciDretva mpd;
    private final BP_Konfiguracija konfigBP;
    private final Socket socket;
    private final SocketServer ss;
    private final String cjenik;
    private Konfiguracija konfigCjenik;

    //podaci za spajanje na bazu podataka
    private final String server;
    private final String baza;
    private final String korisnik;
    private final String lozinka;
    private final String driver;

    //podaci za spajanje na mail posluzitelj
    private final String mailServer;
    private final int mailPort;
    private final String mailPosiljatelj;
    private final String mailPosiljateljLozinka;
    private final String mailPrimatelj;
    private final String mailPorukaPredmet;

    //svi mogući regex izrazi ispravnih komandi
    private final String sintaksa2Provjera = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+);$";
    private final String sintaksa2Dodavanje = "^ADD ([\\w-_]+); PASSWD ([\\w-_#!]+);$";
    private final String sintaksa3AdminPause = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); PAUSE;$";
    private final String sintaksa3AdminStart = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); START;$";
    private final String sintaksa3AdminStop = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); STOP;$";
    private final String sintaksa3AdminAdmin = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); ADMIN ([\\w-_]+);$";
    private final String sintaksa3AdminNoAdmin = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); NOADMIN ([\\w-_]+);$";
    private final String sintaksa3AdminDownload = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); DOWNLOAD;$";
    private final String sintaksa3UserAdd = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); ADD \"([^\"]*)\";$";
    private final String sintaksa3UserTest = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); TEST \"([^\"]*)\";$";
    private final String sintaksa3UserGet = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); GET \"([^\"]*)\";$";
    private final String sintaksa3UserType = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); TYPE;$";
    private final String sintaksa4AdminUpload = "^USER ([\\w-_]+); PASSWD ([\\w-_#!]+); UPLOAD (\\d+);\r\n(.+)$";

    private Matcher m = null;
    private String odgovor = "";
    String komanda = "";
    String ipAdresa = "";
    
    long pocetak;

    ServerObradaDretva(MeteoPodaciDretva mpd, BP_Konfiguracija konfigBP, Socket socket, SocketServer ss, String cjenik, Konfiguracija konfigRazno) {
        this.mpd = mpd;
        this.konfigBP = konfigBP;
        this.socket = socket;
        this.ss = ss;
        this.cjenik = cjenik;

        server = konfigBP.getServer_database();
        baza = server + konfigBP.getUser_database();
        korisnik = konfigBP.getUser_username();
        lozinka = konfigBP.getUser_password();
        driver = konfigBP.getDriver_database(server);

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        mailServer = konfigRazno.dajPostavku("mail.server");
        mailPort = Integer.parseInt(konfigRazno.dajPostavku("mail.port"));
        mailPosiljatelj = konfigRazno.dajPostavku("mail.posiljatelj");
        mailPosiljateljLozinka = konfigRazno.dajPostavku("mail.posiljatelj.lozinka");
        mailPrimatelj = konfigRazno.dajPostavku("mail.primatelj");
        mailPorukaPredmet = konfigRazno.dajPostavku("mail.poruka.predmet");

        //preuzmi cjenik
        try {
            konfigCjenik = KonfiguracijaApstraktna.preuzmiKonfiguraciju(cjenik);
        } catch (Exception ex) {
            //ne postoji datoteka cjenika, svi zatjevi su besplatni
            System.out.println("Cjenik ne postoji!");
        }
    }

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        pocetak = System.currentTimeMillis();

        InputStream is = null;
        OutputStream os = null;

        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();

            StringBuilder sb = new StringBuilder();

            // ovaj sleep je potreban jer metoda available za inputStream dobije
            // podatke prekasno i vraća rezultat 0 prije nego su podaci
            // uopće dospjeli na stream te se nikad i ne izvrsi citanje streama,
            // ovako uz malu odgodu pomocu sleep-a se uredno cita sa stream-a
            try {
                sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(is, "ISO-8859-2"));

            String linija;
            List<String> linije = new ArrayList<>();

            while ((linija = in.readLine()) != null) {
                linije.add(linija);
            }

            if (linije.size() > 1) {
                //ima vise linija, UPLOAD
                int velicina = linije.size();
                for (int i=0; i<linije.size(); i++) {
                    String l = linije.get(i);
                    komanda += l;
                    if (i != (velicina-1)) {
                        komanda += "\r\n";
                    }
                }
            } else {
                //samo jedna linija
                komanda = linije.get(0);
            }

            System.out.println(komanda);

            String[] zahtjev = komanda.split(";");
            int duzina = zahtjev.length;

            ipAdresa = this.socket.getInetAddress().toString();

            if (duzina == 2) {
                if (zahtjev[0].startsWith("USER")) {

                    obradaSintaksaUser();

                } else if (zahtjev[0].startsWith("ADD")) {

                    obradaSintaksaAdd();

                }
            } else if (duzina == 3) {

                if (zahtjev[2].equals(" PAUSE")) {

                    obradaSintaksaPause();

                } else if (zahtjev[2].equals(" START")) {

                    obradaSintakseStart();

                } else if (zahtjev[2].equals(" STOP")) {

                    obradaSintakseStop();

                } else if (zahtjev[2].startsWith(" ADMIN")) {

                    obradaSintakseAdmin();

                } else if (zahtjev[2].startsWith(" NOADMIN")) {

                    obradaSintakseNoAdmin();

                } else if (zahtjev[2].equals(" DOWNLOAD")) {

                    obradaSintakseDownload();

                } else if (zahtjev[2].startsWith(" ADD")) {

                    obradaSintakseAdd();

                } else if (zahtjev[2].startsWith(" TEST")) {

                    obradaSintakseTest();

                } else if (zahtjev[2].startsWith(" GET")) {

                    obradaSintakseGet();

                } else if (zahtjev[2].equals(" TYPE")) {

                    obradaSintakseType();

                }
            } else if (duzina == 4) {
                obradaSintakseUpload();

            }

            if (m == null) {
                odgovor = "ERR 99; Neipsravna komanda";
            }

            os.write(odgovor.getBytes());
            
            System.out.println("Odgovor servera: " + odgovor);
 
        } catch (IOException ex) {
            //exception za is i os
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (os != null) {
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Provjera pruženog string-a u odnosu pruženu sintaksu
     *
     * @param p string
     * @param sintaksa regex izraz
     * @return objekt tipa Matcher ili null ako ne odgovara
     */
    public Matcher provjeraParametara(String p, String sintaksa) {
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(p);
        boolean status = m.matches();
        if (status) {
            return m;
        } else {
            return null;
        }
    }

    /**
     * Provjerava postoji li korisnik u bazi podataka i je li lozinka točna
     *
     * @param korisnik ime korisnika
     * @param lozinka lozinka korisnika
     * @return true - uspjesna autentikacija, false inace
     */
    private boolean provjeriKorImeILozinku(String korisnik, String lozinka) {

        String sqlUpit = "SELECT k.korisnicko_ime, g.opis FROM majugurci_korisnici k "
                + "JOIN majugurci_grupe g ON k.grupa = g.id_grupa "
                + "WHERE korisnicko_ime=? AND lozinka=?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);
            ps.setString(2, lozinka);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Daje uloga korisnika u sustavi
     *
     * @param korisnik ime korisnika
     * @return 0 - nepostojeci korisnik, 1 - admin, 2 - obican korisnik
     */
    private int provjeriGrupuKorisnika(String korisnik) {

        String sqlUpit = "SELECT k.korisnicko_ime, g.opis FROM majugurci_korisnici k "
                + "JOIN majugurci_grupe g ON k.grupa = g.id_grupa "
                + "WHERE korisnicko_ime=?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getString("opis").equals("admin")) {
                        return 1;
                    } else if (rs.getString("opis").equals("korisnik")) {
                        return 2;
                    } else {
                        //ako se doda neka korisnicka grupa u buducnosti
                    }
                } else {
                    return 0;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /**
     * Mijenja ulogu korisnika u sustavu
     *
     * @param korisnik ime korisnika
     * @param zeljenaUloga ime korisnika
     */
    private void promijeniUloguKorisnika(String korisnik, int zeljenaUloga) {

        String sqlUpit = "UPDATE majugurci_korisnici SET grupa = ? WHERE korisnicko_ime = ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setLong(1, zeljenaUloga);
            ps.setString(2, korisnik);

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Daje id korisnika na temelju korisnickog imena
     *
     * @param korisnik ime korisnika
     * @return id korisnika, 0 ako ne postoji
     */
    private int dajKorisnickiID(String korisnik) {

        String sqlUpit = "SELECT id_korisnik FROM majugurci_korisnici "
                + "WHERE korisnicko_ime = ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (rs.getInt("id_korisnik"));
                } else {
                    return 0;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /**
     * Daje broj korisnika iz baze podataka
     *
     * @return broj korisnika
     */
    private int dajBrojKorisnika() {

        String sqlUpit = "SELECT COUNT(*) as broj_korisnika FROM majugurci_korisnici;";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);
                ResultSet rs = ps.executeQuery();) {

            if (rs.next()) {
                return (rs.getInt("broj_korisnika"));
            } else {
                return 0;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /**
     * Dodaj korisnika u BP
     *
     * @param korisnik ime korisnika
     * @param lozinka lozinka
     */
    private void dodajKorisnikaUBP(String korisnik, String lozinka) {

        String sqlUpit = "INSERT INTO majugurci_korisnici (grupa, korisnicko_ime, lozinka, vrijeme_registracije) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setInt(1, 2);
            ps.setString(2, korisnik);
            ps.setString(3, lozinka);

            java.util.Date utilDate = new Date();
            java.sql.Timestamp date = new java.sql.Timestamp(utilDate.getTime());
            ps.setTimestamp(4, date);

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Provjera posotji li adresa u bazi podataka
     *
     * @param korisnik adresa
     * @return true - adresa postoji, false inace
     */
    private boolean provjeriAdresu(String adresa) {

        String sqlUpit = "SELECT adresa FROM majugurci_adrese WHERE adresa = ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, adresa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Dodaje adresu u bazu podataka
     *
     * @param korisnik ime korisnika
     * @param adresa ime adrese
     * @param lokacija geolokacijski podaci
     */
    private void dodajAdresuUBP(String korisnik, String adresa, Lokacija lokacija) {

        String sqlUpit = "INSERT INTO majugurci_adrese (korisnik, adresa, latitude, longitude) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setInt(1, dajKorisnickiID(korisnik));
            ps.setString(2, adresa);
            ps.setString(3, lokacija.getLatitude());
            ps.setString(4, lokacija.getLongitude());

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Daje id adrese na temelju njenog naziva
     *
     * @param korisnik naziv adrese
     * @return id adrese, 0 ako ne postoji
     */
    private int dajIDAdrese(String adresa) {

        String sqlUpit = "SELECT id_adresa FROM majugurci_adrese "
                + "WHERE adresa = ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, adresa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (rs.getInt("id_adresa"));
                } else {
                    return 0;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /**
     * Daje zadnje spremljene meteo podatke
     *
     * @param korisnik naziv adrese
     * @return null ako nema podataka, inace objekt MeteoPodaci
     */
    private MeteoPodaci dajZadnjeMeteoPodatke(String adresa) {

        int idAdrese = dajIDAdrese(adresa);

        String sqlUpit = "SELECT temperature_value, humidity_value, pressure_value FROM majugurci_meteo "
                + "WHERE adresa = ? AND id_meteo_podaci = (SELECT max(id_meteo_podaci) from majugurci_meteo "
                + "WHERE adresa = ?)";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setInt(1, idAdrese);
            ps.setInt(2, idAdrese);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MeteoPodaci mp = new MeteoPodaci();
                    mp.setTemperatureValue(rs.getFloat("temperature_value"));
                    mp.setHumidityValue(rs.getFloat("humidity_value"));
                    mp.setPressureValue(rs.getFloat("pressure_value"));

                    return mp;
                } else {
                    return null;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private Lokacija dajLokacijuRest(String adresa) {
        GMKlijent gmk = new GMKlijent();
        Lokacija l = gmk.getGeoLocation(adresa);
        return l;
    }

    /**
     * Daje spremljene geolokacijske podatke iz baze podataka
     *
     * @param adresa naziv adrese
     * @return null ako nema podataka, inace objekt MeteoPodaci
     */
    private Lokacija dajlokacijuBP(String adresa) {

        String sqlUpit = "SELECT latitude, longitude FROM majugurci_adrese "
                + "WHERE adresa = ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, adresa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Lokacija l = new Lokacija();
                    l.setLatitude(rs.getString("latitude"));
                    l.setLongitude(rs.getString("longitude"));
                    return l;
                } else {
                    return null;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Zapisuje podatke u dnevnik rada u BP
     *
     * @param korisnik korisnik iz komande
     * @param trajanje trajanje obrade zahtjeva
     */
    private void zapisiPodatkeUDnevnik(String korisnik, long trajanje) {
        String sqlUpit = "INSERT INTO majugurci_dnevnik (korisnik, komanda, "
                + "odgovor, ipadresa, vrijeme, trajanje) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setInt(1, dajKorisnickiID(korisnik));
            ps.setString(2, komanda);
            ps.setString(3, odgovor);
            ps.setString(4, ipAdresa);
            java.util.Date utilDate = new Date();
            java.sql.Timestamp date = new java.sql.Timestamp(utilDate.getTime());
            ps.setTimestamp(5, date);
            ps.setInt(6, (int) trajanje);

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Ako je jedini sadržaj komande USER i PASSWD provjeri korisnika u BP
     */
    private void obradaSintaksaUser() {

        m = provjeraParametara(komanda, sintaksa2Provjera);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {
                odgovor = "OK 10;";
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande za dodavanje novog korisnika
     */
    private void obradaSintaksaAdd() {
        Date vrijeme = new Date();

        m = provjeraParametara(komanda, sintaksa2Dodavanje);

        if (m != null) {
            String user = m.group(1);
            String pass = m.group(2);

            int postoji = dajKorisnickiID(user);

            if (postoji != 0) {
                //korisnik vec postoji
                odgovor = "ERR 50;";
            } else {
                dodajKorisnikaUBP(user, pass);
                odgovor = "OK 10;";
            }

            int brojKorisnika = dajBrojKorisnika();

            System.setProperty("java.net.preferIPv4Stack", "true");
            System.out.println("Mail server: " + mailServer);
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", mailServer);
            Session session = Session.getInstance(properties, null);
            String poruka = "";
            poruka += komanda;
            poruka += "\r\nVrijeme primanja: " + vrijeme;
            poruka += "\r\nTrenutni broj korisnika: " + brojKorisnika;
            try {
                // Connect to the store
                Store store = session.getStore("imap");
                store.connect(mailServer, mailPort, mailPosiljatelj, mailPosiljateljLozinka);

                MimeMessage message = new MimeMessage(session);
                Address fromAddress = new InternetAddress(mailPosiljatelj);
                message.setFrom(fromAddress);

                Address[] toAddresses = InternetAddress.parse(mailPrimatelj);
                message.setRecipients(Message.RecipientType.TO, toAddresses);

                String vrsta = "text/plain; charset=UTF-8";

                message.setSubject(mailPorukaPredmet, "utf-8");
                message.setText(poruka, "utf-8");
                message.setHeader("Content-Type", vrsta);

                message.setSentDate(new Date());

                Transport.send(message);

            } catch (NoSuchProviderException ex) {
                Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MessagingException ex) {
                Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Obrada komande PAUSE Pauzira izvršavanje dretve koja skuplja meteo
     * podatke
     */
    private void obradaSintaksaPause() {

        m = provjeraParametara(komanda, sintaksa3AdminPause);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {
                int grupaKorisnika = provjeriGrupuKorisnika(m.group(1));
                if (grupaKorisnika == 1) {
                    //admin
                    if (mpd.isPauzaRada()) {
                        odgovor = "ERR 30;";
                    } else {
                        mpd.setPauzaRada(true);
                        odgovor = "OK 10;";
                    }
                } else if (grupaKorisnika == 2) {
                    //korisnik
                    odgovor = "ERR 21;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande START pokrece dretvu koja skuplja meteo podatke a prije je
     * bila u stanju pause
     */
    private void obradaSintakseStart() {

        m = provjeraParametara(komanda, sintaksa3AdminStart);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {

                int grupaKorisnika = provjeriGrupuKorisnika(m.group(1));
                if (grupaKorisnika == 1) {
                    //admin
                    if (!mpd.isPauzaRada()) {
                        odgovor = "ERR 31;";
                    } else {
                        mpd.setPauzaRada(false);
                        odgovor = "OK 10;";
                    }
                } else if (grupaKorisnika == 2) {
                    //korisnik
                    odgovor = "ERR 21;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande STOP Zaustavlja izvršavanje dretve koja skuplja meteo
     * podatke i zaustavlja rad socket servera
     */
    private void obradaSintakseStop() {

        m = provjeraParametara(komanda, sintaksa3AdminStop);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {

                int grupaKorisnika = provjeriGrupuKorisnika(m.group(1));
                if (grupaKorisnika == 1) {
                                //admin

                    //serverSocket se zaustavlja iz dretve
                    // ako je u postupku zaustavljanja
                    if (mpd.isZavrsiRad()) {
                        odgovor = "ERR 32;";
                    } else {    //pozovi prekidanje dretve i servera
                        if (mpd.isSleeping()) {
                            //ako dretva spava pozovi interrup
                            mpd.setSocketServer(ss);
                            mpd.interrupt();
                        } else {
                            //ako ne spava postavi zastavicu
                            mpd.setSocketServer(ss);
                            mpd.setZavrsiRad(true);
                        }

                        odgovor = "OK 10;";
                    }

                } else if (grupaKorisnika == 2) {
                    //korisnik
                    odgovor = "ERR 21;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande ADMIN Korisniku pridjeljuje ulogu administratora
     */
    private void obradaSintakseAdmin() {

        m = provjeraParametara(komanda, sintaksa3AdminAdmin);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {

                int grupaKorisnika = provjeriGrupuKorisnika(m.group(1));
                if (grupaKorisnika == 1) {
                    //admin

                    int grupaKorisnikaIzKomande = provjeriGrupuKorisnika(m.group(3));

                    if (grupaKorisnikaIzKomande == 0) {
                        //nepostojeci korisnik
                        odgovor = "ERR 33;";
                    } else if (grupaKorisnikaIzKomande == 1) {
                        odgovor = "ERR 34;";
                    } else if (grupaKorisnikaIzKomande == 2) {
                        promijeniUloguKorisnika(m.group(3), 1);
                        odgovor = "OK 10;";
                    }

                } else if (grupaKorisnika == 2) {
                    //korisnik
                    odgovor = "ERR 21;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande NOADMIN Korisniku oduzima ulogu administratora
     */
    private void obradaSintakseNoAdmin() {

        m = provjeraParametara(komanda, sintaksa3AdminNoAdmin);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {

                int grupaKorisnika = provjeriGrupuKorisnika(m.group(1));
                if (grupaKorisnika == 1) {
                    //admin

                    int grupaKorisnikaIzKomande = provjeriGrupuKorisnika(m.group(3));

                    if (grupaKorisnikaIzKomande == 0) {
                        //nepostojeci korisnik                    
                        odgovor = "ERR 33;";
                    } else if (grupaKorisnikaIzKomande == 1) {
                        promijeniUloguKorisnika(m.group(3), 2);
                        odgovor = "OK 10;";
                    } else if (grupaKorisnikaIzKomande == 2) {
                        odgovor = "ERR 35;";
                    }

                } else if (grupaKorisnika == 2) {
                    //korisnik
                    odgovor = "ERR 21;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande DOWNLOAD Daje cjenik koji se nalazi na poslužitelju
     */
    private void obradaSintakseDownload() {

        m = provjeraParametara(komanda, sintaksa3AdminDownload);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {

                int grupaKorisnika = provjeriGrupuKorisnika(m.group(1));
                if (grupaKorisnika == 1) {
                    //admin

                    File cjenikFile = new File(cjenik);

                    if (cjenikFile.exists()) {

                        try {
                            byte[] mybytearray = new byte[(int) cjenikFile.length()];
                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(cjenikFile));
                            bis.read(mybytearray, 0, mybytearray.length);
                            String procitanaDatoteka = Arrays.toString(mybytearray);
                            int velicinaDatoteke = mybytearray.length;

                            odgovor = "OK 10; DATA " + velicinaDatoteke + ";\r\n" + procitanaDatoteka;
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        //cjenik ne postoji
                        odgovor = "ERR 36;";
                    }

                } else if (grupaKorisnika == 2) {
                    //korisnik
                    odgovor = "ERR 21;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande ADD Dodaje novu adresu u BP za koju će se sakupljati meteo
     * podaci
     */
    private void obradaSintakseAdd() {

        m = provjeraParametara(komanda, sintaksa3UserAdd);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {

                int grupaKorisnika = provjeriGrupuKorisnika(m.group(1));

                boolean imaSredstva = true;

                float cijenaZahtjeva = 0;

                String zahtjev = "addAdresa";

                if (grupaKorisnika == 2) {
                    //obican korisnik
                    if (konfigCjenik != null) {
                        try {
                            cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                            imaSredstva = korisnikImaSredstva(m.group(1), cijenaZahtjeva);
                        } catch (Exception e) {
                            //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                            imaSredstva = true;
                        }

                    } else {
                        //nema cjenika, zahtjev je besplatan
                        imaSredstva = true;
                    }
                }

                // ako je admin uvijek se izvrsava if, nikad else
                if (imaSredstva) {
                    String adresa = m.group(3);
                    Lokacija l = dajLokacijuRest(adresa);
                    if (l == null) {
                        //korisnik je unio nepostojecu adresu
                        odgovor = "ERR 99; Unijeli ste nepostojeću adresu;";
                    } else {
                        if (provjeriAdresu(adresa)) {
                            //adresa postoji u BP
                            odgovor = "ERR 41;";
                        } else {
                            //dodaj adresu u bazu
                            dodajAdresuUBP(m.group(1), adresa, l);
                            odgovor = "OK 10;";
                        }
                    }

                    if (grupaKorisnika == 2 && konfigCjenik != null) {
                        umanjiKorisnickaSredstva(m.group(1), cijenaZahtjeva);
                        upisiTransakcijuUEvidenciju(m.group(1), cijenaZahtjeva, zahtjev);
                    }
                } else {
                    odgovor = "ERR 40;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande TEST Provjerava postoji li adresa spremljena u BP
     */
    private void obradaSintakseTest() {

        m = provjeraParametara(komanda, sintaksa3UserTest);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {

                int grupaKorisnika = provjeriGrupuKorisnika(m.group(1));

                boolean imaSredstva = true;

                float cijenaZahtjeva = 0;

                String zahtjev = "testAdresa";

                if (grupaKorisnika == 2) {
                    //obican korisnik
                    if (konfigCjenik != null) {
                        try {
                            cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                            imaSredstva = korisnikImaSredstva(m.group(1), cijenaZahtjeva);
                        } catch (Exception e) {
                            //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                            imaSredstva = true;
                        }

                    } else {
                        //nema cjenika, zahtjev je besplatan
                        imaSredstva = true;
                    }
                }

                // ako je admin uvijek se izvrsava if, nikad else
                if (imaSredstva) {
                    String adresa = m.group(3);
                    if (provjeriAdresu(adresa)) {
                        //adresa postoji u BP
                        odgovor = "OK 10;";
                    } else {
                        //adresa ne postoji u BP
                        odgovor = "ERR 42;";
                    }

                    if (grupaKorisnika == 2 && konfigCjenik != null) {
                        umanjiKorisnickaSredstva(m.group(1), cijenaZahtjeva);
                        upisiTransakcijuUEvidenciju(m.group(1), cijenaZahtjeva, zahtjev);
                    }
                } else {
                    odgovor = "ERR 40;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande GET Daje zadnje spremljene meteo podatke za adresu
     */
    private void obradaSintakseGet() {

        m = provjeraParametara(komanda, sintaksa3UserGet);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {

                int grupaKorisnika = provjeriGrupuKorisnika(m.group(1));

                boolean imaSredstva = true;

                float cijenaZahtjeva = 0;

                String zahtjev = "getAdresa";

                if (grupaKorisnika == 2) {
                    //obican korisnik
                    if (konfigCjenik != null) {
                        try {
                            cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                            imaSredstva = korisnikImaSredstva(m.group(1), cijenaZahtjeva);
                        } catch (Exception e) {
                            //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                            imaSredstva = true;
                        }

                    } else {
                        //nema cjenika, zahtjev je besplatan
                        imaSredstva = true;
                    }
                }

                // ako je admin uvijek se izvrsava if, nikad else
                if (imaSredstva) {
                    String adresa = m.group(3);
                    MeteoPodaci mp = dajZadnjeMeteoPodatke(adresa);

                    if (mp == null) {
                        //podaci se ne preuzimaju za adrese
                        odgovor = "ERR 43;";
                    } else {
                        Lokacija l = dajlokacijuBP(adresa);

                        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en_US"));
                        DecimalFormat df = (DecimalFormat) nf;

                        odgovor = "OK 10;";

                        String pattern = "00.00";
                        df.applyPattern(pattern);

                        String temp = df.format(mp.getTemperatureValue());
                        odgovor += " TEMP " + temp;

                        String vlaga = df.format(mp.getHumidityValue());
                        odgovor += " VLAGA " + vlaga;

                        pattern = "0000.00";
                        df.applyPattern(pattern);

                        String tlak = df.format(mp.getPressureValue());
                        odgovor += " TLAK " + tlak;

                        pattern = "000.000000";
                        df.applyPattern(pattern);

                        String lat = df.format(Double.parseDouble(l.getLatitude()));
                        odgovor += " GEOSIR " + lat;

                        String lon = df.format(Double.parseDouble(l.getLongitude()));
                        odgovor += " GEODUZ " + lon + ";";
                    }

                    if (grupaKorisnika == 2 && konfigCjenik != null) {
                        umanjiKorisnickaSredstva(m.group(1), cijenaZahtjeva);
                        upisiTransakcijuUEvidenciju(m.group(1), cijenaZahtjeva, zahtjev);
                    }
                } else {
                    odgovor = "ERR 40;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande TYPE Daje korisniku informaciju o njegovoj ulozi u sustavu
     * (admin ili korisnik)
     */
    private void obradaSintakseType() {

        m = provjeraParametara(komanda, sintaksa3UserType);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {

                int vrstaKorisnika = provjeriGrupuKorisnika(m.group(1));

                if (vrstaKorisnika == 1) {
                    odgovor = "OK 11;";
                } else if (vrstaKorisnika == 2) {
                    odgovor = "OK 10;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Obrada komande UPLOAD Postavlja novi cjenik na poslužitelj
     */
    private void obradaSintakseUpload() {

        m = provjeraParametara(komanda, sintaksa4AdminUpload);

        if (m != null) {
            boolean autentikacija = provjeriKorImeILozinku(m.group(1), m.group(2));

            if (autentikacija) {

                try {

                    int grupaKorisnika = provjeriGrupuKorisnika(m.group(1));
                    if (grupaKorisnika == 1) {
                        //admin

                        boolean uspjeh = true;

                        File cjenikFile = new File(cjenik);

                        if (!cjenikFile.exists()) {
                            cjenikFile.createNewFile();
                        }

                        String[] dijelovi = cjenik.split("\\.xml");
                        String nazivDatotekeZaSpremiti = dijelovi[0] + "privremeni.xml";
                        System.out.println(nazivDatotekeZaSpremiti);

                        File cjenikPrivremeni = new File(nazivDatotekeZaSpremiti);

                        int brojZnakova = Integer.parseInt(m.group(3));
                        String datotekaUStringu = m.group(4);

                        String[] byteValues = datotekaUStringu.substring(1, datotekaUStringu.length() - 1).split(",");
                        byte[] bytes = new byte[byteValues.length];

                        for (int i = 0, len = bytes.length; i < len; i++) {
                            try {
                                bytes[i] = Byte.parseByte(byteValues[i].trim());
                            } catch (NumberFormatException e) {
                                odgovor = "ERROR 37;";
                                return;
                            }
                        }

                        FileOutputStream fos = new FileOutputStream(nazivDatotekeZaSpremiti);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);

                        bos.write(bytes, 0, brojZnakova);
                        bos.close();

                        if (bytes.length != brojZnakova) {
                            File f = new File(nazivDatotekeZaSpremiti);
                            f.delete();
                            //razlikuje se velicina datoteke
                            uspjeh = false;
                            odgovor = "ERROR 37;";
                        }

                        //provjeri je li file u preperties obliku
                        File ucitani = new File(nazivDatotekeZaSpremiti);
                        FileInputStream fileInput = new FileInputStream(ucitani);
                        Properties properties = new Properties();
                        try {
                            properties.loadFromXML(fileInput);
                        } catch (Exception e) {
                            //file nije u dobrom obliku
                            ucitani.delete();
                            uspjeh = false;
                            odgovor = "ERROR 37;";
                        }
                        fileInput.close();

                        //ako je sve u redu privremeni file zapisi u stvarni cjenik
                        //i obrisi privremeni
                        if (uspjeh) {
                            File privremeni = new File(nazivDatotekeZaSpremiti);
                            Files.copy(privremeni, cjenikFile);
                            privremeni.delete();
                            odgovor = "OK 10;";
                        }

                    } else if (grupaKorisnika == 2) {
                        //korisnik
                        odgovor = "ERR 21;";
                    }

                }catch (IOException ex) {
                    Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
                    odgovor = "ERR 37;";
                }
            } else {
                odgovor = "ERR 20;";
            }

            long trajanje = System.currentTimeMillis() - pocetak;
            zapisiPodatkeUDnevnik(m.group(1), trajanje);
        }
    }

    /**
     * Daje podatak ima li korisnik dovoljno sredstava za izvršavanje zahtjeva
     * ili ne
     *
     * @param korisnik korisnicko ime
     * @param cijenaUsluge cijena usluge iz cjenika
     * @return true ako ima, false inace
     */
    private boolean korisnikImaSredstva(String korisnik, float cijenaUsluge) {
        String sqlUpit = "SELECT korisnicko_ime, sredstva FROM majugurci_korisnici "
                + "WHERE korisnicko_ime= ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    float korisnickaSredstva = rs.getFloat("sredstva");
                    if ((korisnickaSredstva - cijenaUsluge) >= 0) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    //ovaj dio se ne bi trbao izvrsavati
                    return false;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Metoda koja smanjuje korisnicki iznos u BP za cijenu usluge
     *
     * @param korisnik korisnicko_ime
     * @param cijenaUsluge iznos za koji se smanjuju kor. sredstva
     */
    private void umanjiKorisnickaSredstva(String korisnik, float cijenaUsluge) {
        String sqlUpit = "UPDATE majugurci_korisnici SET sredstva = sredstva - ? WHERE korisnicko_ime = ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setFloat(1, cijenaUsluge);
            ps.setString(2, korisnik);

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda za upisivanje evidencije transakcija
     *
     * @param korisnik korisnicko ime
     * @param cijenaZahtjeva cijena zahtjva
     * @param zahtjev opis zahtjeva
     */
    private void upisiTransakcijuUEvidenciju(String korisnik, float cijenaZahtjeva, String zahtjev) {
        cijenaZahtjeva = -cijenaZahtjeva;

        String sqlUpit = "INSERT INTO majugurci_transakcije "
                + "(korisnik, iznos_promjena, novo_stanje, datum, usluga) "
                + "VALUES ("
                + "(SELECT id_korisnik FROM majugurci_korisnici WHERE korisnicko_ime = ?), "
                + "?, "
                + "(SELECT sredstva FROM majugurci_korisnici WHERE korisnicko_ime = ?), "
                + "?, ?);";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);
            ps.setFloat(2, cijenaZahtjeva);
            ps.setString(3, korisnik);

            java.util.Date utilDate = new Date();
            java.sql.Timestamp date = new java.sql.Timestamp(utilDate.getTime());
            ps.setTimestamp(4, date);
            ps.setString(5, zahtjev);

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
