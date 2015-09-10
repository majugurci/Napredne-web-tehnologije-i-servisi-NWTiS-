/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.kontrole;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.majugurci.socketServer.ServerObradaDretva;

/**
 *
 * @author Mario
 * 
 * Glavni servlet aplikacije kroz koji prolaze svi korinički zahtjevi.
 * Nakon što servlet utvrdi zahtjev radi neku akciju i preusmjerava
 * korisnika na određenu stranicu.
 */
public class Kontroler extends HttpServlet {

    private String server;
    private String baza;
    private String korisnik;
    private String lozinka;
    private String driver;

    private String paginacija;

    public Kontroler() {

    }

    @Override
    public void init() throws ServletException {
        Konfiguracija konfig = (Konfiguracija) getServletContext().getAttribute("konfigRazno");
        BP_Konfiguracija bpk = (BP_Konfiguracija) getServletContext().getAttribute("konfigBP");

        server = bpk.getServer_database();
        baza = server + bpk.getUser_database();
        korisnik = bpk.getUser_username();
        lozinka = bpk.getUser_password();
        driver = bpk.getDriver_database(server);

        paginacija = konfig.dajPostavku("paginacija");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Kontroler.class.getName()).log(Level.SEVERE, null, ex);
            //return null;
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String zahtjev = request.getServletPath();
        String odrediste = null;

        HttpSession hs;
        Korisnik kor;

        switch (zahtjev) {
            case "/Kontroler":
                odrediste = "/jsp/index.jsp";
                break;
            case "/PrijavaKorisnika":
                odrediste = "/jsp/login.jsp";
                break;
            case "/ProvjeraKorisnika":
                String korisnik = request.getParameter("korisnik");
                String lozinka = request.getParameter("lozinka");
                if (korisnik == null || korisnik.trim().isEmpty()
                        || lozinka == null || lozinka.trim().isEmpty()) {
                    throw new NeuspjesnaPrijava("Nisu upisani potrebni podaci.");
                } else {
                    //provjera u bazi podataka
                    Korisnik k = dajKorisnickePodatke(korisnik, lozinka);
                    if (k == null) {
                        throw new NeuspjesnaPrijava("Problem pri spajanju na bazu podataka.");
                    } else {
                        if (k.getKorisnickoIme() == null) {
                            throw new NeuspjesnaPrijava("Nisu upisani ispravni podaci.");
                        } else {
                            hs = request.getSession(true);
                            kor = (Korisnik) hs.getAttribute("korisnik");
                            if (kor != null) {
                                throw new NeuspjesnaPrijava("Već ste prijavljeni u sustav.");
                            } else {
                                hs.setAttribute("korisnik", k);
                                odrediste = "/privatno/index.jsp";
                            }
                        }
                    }
                }
                break;
            case "/OdjavaKorisnika":
                hs = request.getSession(false);
                kor = (Korisnik) hs.getAttribute("korisnik");
                if (kor != null) {
                    hs.removeAttribute("korisnik");
                    hs.invalidate();
                } else {
                    throw new NeuspjesnaPrijava("Samo prijavljeni korisnici se mogu odjaviti.");
                }
                odrediste = "/Kontroler";
                break;
            case "/PregledFinancija":
                hs = request.getSession(false);
                kor = (Korisnik) hs.getAttribute("korisnik");
                if (kor != null) {
                    String azuriranje = (String) request.getParameter("azuriraj");
                    String porukaGreska = null;
                    String porukaUspjeh = null;
                    if (azuriranje != null) {
                        //ako je korisnik preuspmjeren sa stranice za azuriranje sredstava
                        boolean uspjesnaKonverzija = true;
                        float novoStanje = 0;
                        try {
                            novoStanje = Float.parseFloat((String) request.getParameter("korSredstvaNovo"));
                        } catch (Exception e) {
                            uspjesnaKonverzija = false;
                        }

                        float staroStanje = Float.parseFloat((String) request.getParameter("korSredstva"));

                        if (novoStanje != staroStanje) {

                            if (uspjesnaKonverzija) {

                                boolean uspjesno = azurirajSredstvaKorisnika(kor.getKorisnickoIme(), novoStanje);

                                if (uspjesno) {
                                    request.setAttribute("korSredstva", novoStanje);
                                    porukaUspjeh = "Sredstva su uspješno ažurirana";

                                    //upisi transkaciju u evidenciju
                                    upisiTransakcijuUEvidenciju(kor.getKorisnickoIme(), novoStanje - staroStanje, "Ažuriranje sredstava");
                                } else {
                                    request.setAttribute("korSredstva", staroStanje);
                                    porukaGreska = "Pogrešan iznos sredstava, ažuriranje nije obavljeno";
                                }
                            } else {
                                // korisnik je unio broj nevaljanog formata
                                request.setAttribute("korSredstva", staroStanje);
                                porukaGreska = "Pogrešan iznos sredstava, ažuriranje nije obavljeno";
                            }

                        } else {
                            //korisnik je unio vec postojeci iznos racuna
                            request.setAttribute("korSredstva", staroStanje);
                            porukaGreska = "Unijeli ste postojeće stanje računa";
                        }
                    } else {
                        //ako korisnik pozove straniciu za pregled financijskih podataka
                        float korSredstva = dajKorisnickaSredstva(kor.getKorisnickoIme());
                        request.setAttribute("korSredstva", korSredstva);
                    }
                    request.setAttribute("porukaUspjeh", porukaUspjeh);
                    request.setAttribute("porukaGreska", porukaGreska);
                    request.setAttribute("paginacija", paginacija);

                    //dohvati transkacije
                    List<Transakcija> transakcije = dajZapiseTranskacija(kor.getKorisnickoIme());
                    request.setAttribute("transakcije", transakcije);

                    odrediste = "/privatno/pregledFinancija.jsp";
                } else {
                    throw new NeuspjesnaPrijava("Samo prijavljeni korisnici mogu pristupiti ovoj stranici.");
                }

                break;
            case "/AzurirajStanje":
                hs = request.getSession(false);
                kor = (Korisnik) hs.getAttribute("korisnik");
                if (kor != null) {
                    float korSredstva = Float.parseFloat((String) request.getParameter("korSredstva"));
                    request.setAttribute("korSredstva", korSredstva);
                    odrediste = "/privatno/azurirajStanje.jsp";
                } else {
                    throw new NeuspjesnaPrijava("Samo prijavljeni korisnici mogu pristupiti ovoj stranici.");
                }
                break;
            case "/PregledZahtjeva":
                hs = request.getSession(false);
                kor = (Korisnik) hs.getAttribute("korisnik");
                if (kor != null) {

                    String porukaGreska = null;
                    String porukaUspjeh = null;

                    String filterKomanda = request.getParameter("filterKomanda");
                    String filterOdgovor = request.getParameter("filterOdgovor");
                    String filteripAdresa = request.getParameter("filteripAdresa");
                    String filterVrijemePocetak = request.getParameter("filterVrijemePocetak");
                    String filterVrijemeKraj = request.getParameter("filterVrijemeKraj");
                    String filterTrajanje = request.getParameter("filterTrajanje");

                    DateFormat format = new SimpleDateFormat("dd.MM.yyyy hh.mm.ss");

                    Date vrijemePocetak = null;
                    Date vrijemeKraj = null;

                    boolean datumPocetakDobar = false;
                    boolean datumKrajDobar = false;

                    boolean datumiDobri = true;

                    if (filterVrijemePocetak == null) {
                        try {
                            vrijemePocetak = format.parse("01.01.2000 12.00.00");
                        } catch (ParseException ex) {
                            Logger.getLogger(Kontroler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (filterVrijemePocetak.trim().equals("")) {
                        try {
                            vrijemePocetak = format.parse("01.01.2000 12.00.00");
                        } catch (ParseException ex) {
                            Logger.getLogger(Kontroler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        try {
                            datumPocetakDobar = true;
                            vrijemePocetak = format.parse(filterVrijemePocetak);
                        } catch (ParseException ex) {
                            //Logger.getLogger(Kontroler.class.getName()).log(Level.SEVERE, null, ex);
                            porukaGreska = "Krivi format datuma pocetka (ispravan: dd.MM.yyyy hh.mm.ss)";
                            datumiDobri = false;
                        }
                    }

                    if (filterVrijemeKraj == null) {
                        try {
                            vrijemeKraj = format.parse("01.01.2020 12.00.00");
                        } catch (ParseException ex) {
                            Logger.getLogger(Kontroler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (filterVrijemeKraj.trim().equals("")) {
                        try {
                            vrijemeKraj = format.parse("01.01.2020 12.00.00");
                        } catch (ParseException ex) {
                            Logger.getLogger(Kontroler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        try {
                            datumKrajDobar = true;
                            vrijemeKraj = format.parse(filterVrijemeKraj);
                        } catch (ParseException ex) {
                            //Logger.getLogger(Kontroler.class.getName()).log(Level.SEVERE, null, ex);
                            porukaGreska = "\nKrivi format datuma zavrsetka (ispravan: dd.MM.yyyy hh.mm.ss)";
                            datumiDobri = false;
                        }
                    }

                    List<Dnevnik> dnevnik = new ArrayList<>();

                    if (datumiDobri) {
                        dnevnik = dajZapiseDnevnika(kor.getKorisnickoIme(), filterKomanda,
                                filterOdgovor, filteripAdresa, vrijemePocetak, vrijemeKraj, filterTrajanje);
                    }

                    request.setAttribute("dnevnik", dnevnik);

                    request.setAttribute("porukaUspjeh", porukaUspjeh);
                    request.setAttribute("porukaGreska", porukaGreska);
                    request.setAttribute("paginacija", paginacija);

                    request.setAttribute("filterKomanda", filterKomanda);
                    request.setAttribute("filterOdgovor", filterOdgovor);
                    request.setAttribute("filteripAdresa", filteripAdresa);
                    if (datumPocetakDobar) {
                        request.setAttribute("filterVrijemePocetak", filterVrijemePocetak);
                    } else {
                        request.setAttribute("filterVrijemePocetak", "");
                    }
                    if (datumKrajDobar) {
                        request.setAttribute("filterVrijemeKraj", filterVrijemeKraj);
                    } else {
                        request.setAttribute("filterVrijemeKraj", "");
                    }
                    request.setAttribute("filterTrajanje", filterTrajanje);

                    odrediste = "/privatno/pregledZahtjeva.jsp";
                } else {
                    throw new NeuspjesnaPrijava("Samo prijavljeni korisnici mogu pristupiti ovoj stranici.");
                }
                break;
        }
        if (odrediste == null) {
            throw new ServletException("Zahtjev: '" + zahtjev + "' nije poznat.");
        }
        RequestDispatcher rd = this.getServletContext().getRequestDispatcher(odrediste);
        rd.forward(request, response);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    /**
     * Daje korisničke podatke iz baze podataka
     *
     * @param korisnik korisničko ime
     * @param lozinka lozinka korisnika
     * @return objetk tipa Korisnik
     */
    private Korisnik dajKorisnickePodatke(String korisnik, String lozinka) {

        Korisnik k = new Korisnik();

        String sqlUpit = "SELECT * FROM majugurci_korisnici "
                + "WHERE korisnicko_ime= ? AND lozinka = ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);
            ps.setString(2, lozinka);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    k.setKorisnickoIme(rs.getString("korisnicko_ime"));
                    k.setGrupa(rs.getInt("grupa"));
                    k.setEmail(rs.getString("email"));
                    k.setSredstva(rs.getFloat("sredstva"));
                    java.sql.Timestamp vrijemeSql = rs.getTimestamp("vrijeme_registracije");
                    java.util.Date vrijemeJava = new java.sql.Date(vrijemeSql.getTime());
                    k.setVrijemeRegistracije(vrijemeJava);

                    return k;
                } else {
                    return new Korisnik();
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Metoda koja vraća iznos korisničkih sredstava
     *
     * @param korisnik korisničko ime
     * @return iznos sredstava
     */
    private float dajKorisnickaSredstva(String korisnik) {
        String sqlUpit = "SELECT sredstva FROM majugurci_korisnici "
                + "WHERE korisnicko_ime= ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("sredstva");
                } else {
                    //return null;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            //return null;
        }

        return (float) 0.0;
    }

    private boolean azurirajSredstvaKorisnika(String korisnickoIme, float novoStanje) {
        String sqlUpit = "UPDATE majugurci_korisnici SET sredstva = ? WHERE korisnicko_ime = ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setFloat(1, novoStanje);
            ps.setString(2, korisnickoIme);

            try {
                ps.executeUpdate();
                return true;
            } catch (Exception ex) {
                return false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Metoda za upisivanje evidencije transakcija
     *
     * @param korisnik korisnicko ime
     * @param cijenaZahtjeva cijena zahtjva
     * @param zahtjev opis zahtjeva
     */
    private void upisiTransakcijuUEvidenciju(String korisnik, float cijenaZahtjeva, String zahtjev) {

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

    /**
     * Metoda koja vraća zapise transakcija
     *
     * @param korisnik korisničko ime
     * @return lista transakcija
     */
    private List<Transakcija> dajZapiseTranskacija(String korisnik) {

        List<Transakcija> transkacije = new ArrayList<>();

        String sqlUpit = "SELECT t.iznos_promjena, t.novo_stanje, t.datum, t.usluga "
                + "FROM majugurci_transakcije t JOIN majugurci_korisnici k "
                + "ON t.korisnik = k.id_korisnik "
                + "WHERE k.korisnicko_ime = ? ORDER by id_transakcije DESC";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transakcija t = new Transakcija();
                    t.setIznosPromjena(rs.getFloat("iznos_promjena"));
                    t.setNovoStanje(rs.getFloat("novo_stanje"));

                    Timestamp datumSql = rs.getTimestamp("datum");
                    Date datumJava = new java.util.Date(datumSql.getTime());

                    t.setDatum(datumJava);
                    t.setUsluga(rs.getString("usluga"));

                    transkacije.add(t);
                }
                return transkacije;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            //return null;
        }

        return null;
    }

    /**
     * Metoda koja vraća filtirane zapise dnevnika
     *
     * @param korisnickoIme korisnicko ime
     * @param filterKomanda komanda ili null
     * @param filterOdgovor odgovor ili null
     * @param filteripAdresa ipAdresa ili null
     * @param filterVrijeme vrijeme ili null
     * @param filterTrajanje trajanje ili null
     * @return lista dnevnika
     */
    private List<Dnevnik> dajZapiseDnevnika(String korisnickoIme, String filterKomanda, String filterOdgovor,
            String filteripAdresa, Date filterVrijemePocetak, Date filterVrijemeKraj, String filterTrajanje) {

        List<Dnevnik> dnevnik = new ArrayList<>();

        String sqlUpit = "SELECT d.komanda, d.odgovor, d.ipadresa, d.vrijeme, d.trajanje "
                + "FROM majugurci_dnevnik d JOIN majugurci_korisnici k "
                + "ON d.korisnik = k.id_korisnik "
                + "WHERE (k.korisnicko_ime = ?) "
                + "AND (? IS NULL OR d.komanda = ?) "
                + "AND (? IS NULL OR d.odgovor = ?) "
                + "AND (? IS NULL OR d.ipadresa = ?) "
                + "AND (d.vrijeme BETWEEN ? AND ?) "
                + "AND (? IS NULL OR d.trajanje = ?) "
                + "ORDER by d.id_dnevnik DESC";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinka);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnickoIme);

            if (filterKomanda == null) {
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
            } else {
                if (filterKomanda.trim().equals("")) {
                    ps.setNull(2, Types.VARCHAR);
                    ps.setNull(3, Types.VARCHAR);
                } else {
                    ps.setString(2, filterKomanda);
                    ps.setString(3, filterKomanda);
                }
            }

            if (filterOdgovor == null) {
                ps.setNull(4, Types.VARCHAR);
                ps.setNull(5, Types.VARCHAR);
            } else {
                if (filterOdgovor.trim().equals("")) {
                    ps.setNull(4, Types.VARCHAR);
                    ps.setNull(5, Types.VARCHAR);
                } else {
                    ps.setString(4, filterOdgovor);
                    ps.setString(5, filterOdgovor);
                }
            }

            if (filteripAdresa == null) {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
            } else {
                if (filteripAdresa.trim().equals("")) {
                    ps.setNull(6, Types.VARCHAR);
                    ps.setNull(7, Types.VARCHAR);
                } else {
                    ps.setString(6, filteripAdresa);
                    ps.setString(7, filteripAdresa);
                }
            }

            ps.setTimestamp(8, new Timestamp(filterVrijemePocetak.getTime()));
            ps.setTimestamp(9, new Timestamp(filterVrijemeKraj.getTime()));

            if (filterTrajanje == null) {
                ps.setNull(10, Types.TIMESTAMP);
                ps.setNull(11, Types.TIMESTAMP);
            } else {
                if (filterTrajanje.trim().equals("")) {
                    ps.setNull(10, Types.TIMESTAMP);
                    ps.setNull(11, Types.TIMESTAMP);
                } else {
                    ps.setString(10, filterTrajanje);
                    ps.setString(11, filterTrajanje);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Dnevnik d = new Dnevnik();

                    d.setKomanda(rs.getString("komanda"));
                    d.setOdgovor(rs.getString("odgovor"));
                    d.setIpAdresa(rs.getString("ipadresa"));

                    Timestamp datumSql = rs.getTimestamp("vrijeme");
                    Date datumJava = new java.util.Date(datumSql.getTime());

                    d.setDatum(datumJava);
                    d.setTrajanje(rs.getInt("trajanje"));

                    dnevnik.add(d);
                }
                return dnevnik;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            //return null;
        }

        return null;
    }

}
