/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.socketServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.konfiguracije.bp.BP_Konfiguracija;

/**
 *
 * @author Mario
 * 
 * Dretva koja djeluje kao socket server, čeka na zahtjev korisnika
 * te porsljeđuje zahtjev dretvi koja ga obrađuje.
 */
public class SocketServer extends Thread {

    private final MeteoPodaciDretva mpd;
    private final BP_Konfiguracija konfigBP;
    private final Konfiguracija konfigRazno;

    private ServerSocket serverSocket = null;

    private boolean zaustaviServer = false;
    
    private final String cjenik;

    public SocketServer(MeteoPodaciDretva mpd, BP_Konfiguracija konfigBP, Konfiguracija konfigRazno, String cjenik) {
        this.mpd = mpd;
        this.konfigBP = konfigBP;
        this.konfigRazno = konfigRazno;
        this.cjenik = cjenik;
    }

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        int port = Integer.parseInt(konfigRazno.dajPostavku("port"));

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
            Socket socket = null;
            boolean zahtjev;

            System.out.println("Socket server pokrenut");

            while (!zaustaviServer) {
                zahtjev = true;

                try {
                    socket = serverSocket.accept();
                } catch (SocketTimeoutException ex) {
                    // ako u roku sekunde ne stigne zahtjev ponovo pokreni ptelju
                    zahtjev = false;
                }

                if (zahtjev == true) {
                    new ServerObradaDretva(mpd, konfigBP, socket, this, cjenik, konfigRazno).start();
                }
            }
            
            System.out.println("Prekidam rad socket servera");
            serverSocket.close();

        } catch (IOException ex) {
            Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }
    
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public boolean isZaustaviServer() {
        return zaustaviServer;
    }

    public void setZaustaviServer(boolean zaustaviServer) {
        this.zaustaviServer = zaustaviServer;
    }
}
