/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.socketServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.majugurci.rest.klijenti.OWMKlijent;
import org.foi.nwtis.majugurci.web.podaci.Adresa;
import org.foi.nwtis.majugurci.web.podaci.Lokacija;
import org.foi.nwtis.majugurci.web.podaci.MeteoPodaci;

/**
 *
 * @author Mario
 * 
 * Pozadinska dretva koja u pravilnom vremenskom intervalu preuzima meteorološke
 * podatke za adrese spremljenje u bazi podataka.
 * Nakon preuzimanja meteo podataka iste sprema u tablicu u bazi podataka.
 * Na kraju obrade šalje mail poruku s podacima ciklusu obrade.
 */
public class MeteoPodaciDretva extends Thread {

    //podaci za spajanje na bazu podataka
    private final String server;
    private final String baza;
    private final String korisnik;
    private final String lozinka;
    private final String driver;

    private final int vrijemeCiklusa;   //vrijeme ciklusa dretve

    private final String openWeatherMapKey;

    private boolean pauzaRada = false;
    private boolean zavrsiRad = false;
    
    private boolean sleeping = false;
    
    private SocketServer socketServer;

    public MeteoPodaciDretva(BP_Konfiguracija konfigBP, Konfiguracija konfigRazno) {
        server = konfigBP.getServer_database();
        baza = server + konfigBP.getUser_database();
        korisnik = konfigBP.getUser_username();
        lozinka = konfigBP.getUser_password();
        driver = konfigBP.getDriver_database(server);

        vrijemeCiklusa = Integer.parseInt(konfigRazno.dajPostavku("dretva.interval"));

        openWeatherMapKey = konfigRazno.dajPostavku("openWeatherMap.key");
    }

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        while (true) {
            
            System.out.println(new Date() + " dretva pokrenuta");
            
            long pocetak = System.currentTimeMillis();

            if (zavrsiRad) {
                //prekid rada dretve
                System.out.println("Prekidam izvrsavanje meteo podaci dretve");
                socketServer.setZaustaviServer(true);
                break;
            }

            if (!pauzaRada) {

                List<Adresa> adrese = dajSveAdrese();

                OWMKlijent owmk = new OWMKlijent(openWeatherMapKey);

                for (Adresa a : adrese) {
                    MeteoPodaci mp = null;

                    // pokušaj nekoliko puta ako server ne odgovara
                    for (int i = 0; i < 5; i++) {
                        mp = owmk.getRealTimeWeather(a.getGeoloc().getLatitude(),
                                a.getGeoloc().getLongitude());
                        if (mp != null) {
                            break;
                        }
                    }

                    upisiMeteoPodatkeUBazu(a, mp);
                }
                
                System.out.println(new Date() + "Preuzeti meteo podaci");

            }
            
            if (zavrsiRad) {
                //prekid rada dretve
                System.out.println("Prekidam izvrsavanje meteo podaci dretve");
                socketServer.setZaustaviServer(true);
                break;
            }
            
            long kraj = System.currentTimeMillis();

            sleeping = true;
            try {
                sleep(vrijemeCiklusa * 1000 - (kraj - pocetak));
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(MeteoPodaciDretva.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                //Logger.getLogger(MeteoPodaciDretva.class.getName()).log(Level.SEVERE, null, ex);
                //trajni prekid rada
                if (socketServer != null) {
                    socketServer.setZaustaviServer(true);
                }
                break;
            }
            sleeping = false;
        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Spajanje na bazu podataka te dohvaćanje svih adresa
     *
     * @return lista svih adresa
     */
    private List<Adresa> dajSveAdrese() {
        List<Adresa> adrese = new ArrayList<>();

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MeteoPodaciDretva.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        String sql = "SELECT * FROM majugurci_adrese";

        try (Connection veza = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement naredba = veza.createStatement();
                ResultSet odg = naredba.executeQuery(sql);) {
            while (odg.next()) {
                Adresa a = new Adresa(Long.parseLong(odg.getString("id_adresa")),
                        odg.getString("adresa"), new Lokacija(
                                odg.getString("latitude"), odg.getString("longitude")));
                adrese.add(a);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MeteoPodaciDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return adrese;
    }

    /**
     * Zapis meteoroloških podataka u tablicu u bazi podataka
     *
     * @param a Adresa
     * @param mp MeteoPodaci
     */
    private void upisiMeteoPodatkeUBazu(Adresa a, MeteoPodaci mp) {
        if (mp == null) {
            //podaci nisu dohvaceni
        } else {
            //podaci su dohvaceni za adresu, spremi u bazu
            String sqlUpit = "insert into majugurci_meteo (adresa, sunrise, "
                    + "sunset, temperature_value, temperature_min, temperature_max, "
                    + "temperature_unit, humidity_value, humidity_unit, pressure_value, "
                    + "pressure_unit, wind_speed_value, wind_speed_name, wind_direction_value, "
                    + "wind_direction_code, wind_direction_name, clouds_value, clouds_name, "
                    + "visibility, precipitation_value, precipitation_mode, "
                    + "precipitation_unit, weather_number, weather_value, "
                    + "weather_icon, last_update, vrijeme_spremanja) values (?, ?, ?, ?, ?, ?, ?, ?, ?, "
                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                    PreparedStatement ps = con.prepareStatement(sqlUpit);) {

                ps.setInt(1, (int) a.getIdadresa());

                String tekst;
                int intBroj;
                float floatBroj;
                java.util.Date datumJava;

                try {
                    datumJava = mp.getSunRise();
                    java.sql.Timestamp datumSql = new java.sql.Timestamp(datumJava.getTime());
                    ps.setTimestamp(2, datumSql);
                } catch (Exception e) {
                    ps.setNull(2, Types.TIMESTAMP);
                }

                try {
                    datumJava = mp.getSunSet();
                    java.sql.Timestamp datumSql = new java.sql.Timestamp(datumJava.getTime());
                    ps.setTimestamp(3, datumSql);
                } catch (Exception e) {
                    ps.setNull(3, Types.TIMESTAMP);
                }

                try {
                    floatBroj = mp.getTemperatureValue();
                    ps.setFloat(4, floatBroj);
                } catch (Exception e) {
                    ps.setNull(4, Types.FLOAT);
                }

                try {
                    floatBroj = mp.getTemperatureMin();
                    ps.setFloat(5, floatBroj);
                } catch (Exception e) {
                    ps.setNull(5, Types.FLOAT);
                }

                try {
                    floatBroj = mp.getTemperatureMax();
                    ps.setFloat(6, floatBroj);
                } catch (Exception e) {
                    ps.setNull(6, Types.FLOAT);
                }

                try {
                    tekst = mp.getTemperatureUnit();
                    ps.setString(7, tekst);
                } catch (Exception e) {
                    ps.setString(7, "");
                }

                try {
                    floatBroj = mp.getHumidityValue();
                    ps.setFloat(8, floatBroj);
                } catch (Exception e) {
                    ps.setNull(8, Types.FLOAT);
                }

                try {
                    tekst = mp.getHumidityUnit();
                    ps.setString(9, tekst);
                } catch (Exception e) {
                    ps.setString(9, "");
                }

                try {
                    floatBroj = mp.getPressureValue();
                    ps.setFloat(10, floatBroj);
                } catch (Exception e) {
                    ps.setNull(10, Types.FLOAT);
                }

                try {
                    tekst = mp.getPressureUnit();
                    ps.setString(11, tekst);
                } catch (Exception e) {
                    ps.setString(11, "");
                }

                try {
                    floatBroj = mp.getWindSpeedValue();
                    ps.setFloat(12, floatBroj);
                } catch (Exception e) {
                    ps.setNull(12, Types.FLOAT);
                }

                try {
                    tekst = mp.getWindSpeedName();
                    ps.setString(13, tekst);
                } catch (Exception e) {
                    ps.setString(13, "");
                }

                try {
                    floatBroj = mp.getWindDirectionValue();
                    ps.setFloat(14, floatBroj);
                } catch (Exception e) {
                    ps.setNull(14, Types.FLOAT);
                }

                try {
                    tekst = mp.getWindDirectionCode();
                    ps.setString(15, tekst);
                } catch (Exception e) {
                    ps.setString(15, "");
                }

                try {
                    tekst = mp.getWindDirectionName();
                    ps.setString(16, tekst);
                } catch (Exception e) {
                    ps.setString(16, "");
                }

                try {
                    intBroj = mp.getCloudsValue();
                    ps.setInt(17, intBroj);
                } catch (Exception e) {
                    ps.setNull(17, Types.INTEGER);
                }

                try {
                    tekst = mp.getCloudsName();
                    ps.setString(18, tekst);
                } catch (Exception e) {
                    ps.setString(18, "");
                }

                try {
                    tekst = mp.getVisibility();
                    ps.setString(19, tekst);
                } catch (Exception e) {
                    ps.setString(19, "");
                }

                try {
                    floatBroj = mp.getPrecipitationValue();
                    ps.setFloat(20, floatBroj);
                } catch (Exception e) {
                    ps.setNull(20, Types.FLOAT);
                }

                try {
                    tekst = mp.getPrecipitationMode();
                    ps.setString(21, tekst);
                } catch (Exception e) {
                    ps.setString(21, "");
                }

                try {
                    tekst = mp.getPrecipitationUnit();
                    ps.setString(22, tekst);
                } catch (Exception e) {
                    ps.setString(22, "");
                }

                try {
                    intBroj = mp.getWeatherNumber();
                    ps.setInt(23, intBroj);
                } catch (Exception e) {
                    ps.setNull(23, Types.INTEGER);
                }

                try {
                    tekst = mp.getWeatherValue();
                    ps.setString(24, tekst);
                } catch (Exception e) {
                    ps.setString(24, "");
                }

                try {
                    tekst = mp.getWeatherIcon();
                    ps.setString(25, tekst);
                } catch (Exception e) {
                    ps.setString(25, "");
                }

                try {
                    datumJava = mp.getLastUpdate();
                    java.sql.Timestamp datumSql = new java.sql.Timestamp(datumJava.getTime());
                    ps.setTimestamp(26, datumSql);
                } catch (Exception e) {
                    ps.setNull(26, Types.TIMESTAMP);
                }

                Date datum = new Date();

                ps.setTimestamp(27, new java.sql.Timestamp(datum.getTime()));

                if (ps.executeUpdate() == 0) {
                    System.out.println("Dogodila se pogreška pri spremanju podataka u BP");
                } else {
                    //Adresa je uspješno spremljena u BP
                }

            } catch (SQLException ex) {
                Logger.getLogger(MeteoPodaciDretva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean isZavrsiRad() {
        return zavrsiRad;
    }
    
    public void setZavrsiRad(boolean zavrsiRad) {
        this.zavrsiRad = zavrsiRad;
    }

    public boolean isPauzaRada() {
        return pauzaRada;
    }

    public void setPauzaRada(boolean pauzaRada) {
        this.pauzaRada = pauzaRada;
    }

    public boolean isSleeping() {
        return sleeping;
    }

    public void setSleeping(boolean sleeping) {
        this.sleeping = sleeping;
    }

    public void setSocketServer(SocketServer socketServer) {
        this.socketServer = socketServer;
    }
    
    
}
