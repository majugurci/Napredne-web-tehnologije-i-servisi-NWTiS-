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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.foi.nwtis.majugurci.ejb.mdb.AdreseMDB;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;

/**
 *
 * @author Mario
 * 
 * Zrno koje služi da se pošalje komanda na socket server i prikaže se odgovor.
 */
@ManagedBean
@RequestScoped
public class TestServerSocketa {

    private String server;
    private int port;
    private String komanda;

    /**
     * Creates a new instance of TestServerSocketa
     */
    public TestServerSocketa() {
    }

    @PostConstruct
    public void init() {
        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        Konfiguracija konfigRazno = (Konfiguracija) sc.getAttribute("konfigRazno");

        this.server = konfigRazno.dajPostavku("server");
        this.port = Integer.parseInt(konfigRazno.dajPostavku("port"));
    }

    public void saljiKomandu() {
        if (komanda.trim().equals("")) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:zaOdgovorServera", new FacesMessage("Morate unijeti komandu!"));
        } else {
            String odgovorServera = posaljiZahtjev(komanda);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:zaOdgovorServera", new FacesMessage("Odgovor servera: " + odgovorServera));
        }
    }

    public String getKomanda() {
        return komanda;
    }

    public void setKomanda(String komanda) {
        this.komanda = komanda;
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
