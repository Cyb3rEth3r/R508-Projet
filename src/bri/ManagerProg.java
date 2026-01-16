package bri;

import java.io.*;
import java.net.*;
import java.lang.reflect.Constructor;
import java.net.URLClassLoader;

public class ManagerProg implements Runnable {

    private Socket client;
    private String nom;
    private String lienFtp;

    public ManagerProg(Socket socket) {
        client = socket;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            boolean isConnected = choixConnexion(out, in);

            if (isConnected) {
                System.out.println("[ManagerProg] Programmeur connecté : " + this.nom);
                menuServices(out, in);
            } else {
                out.println("Déconnexion.");
            }

        } catch (Exception e) {
            System.err.println("Erreur dans ManagerProg : " + e);
            e.printStackTrace();
        } finally {
            // On s'assure que la socket est fermee si le service n'a pas pris le relais ou
            // a plantee
            try {
                if (!client.isClosed())
                    client.close();
            } catch (IOException e2) {
            }
        }
    }

    // ... reste du code (finalize, start) inchanger ...
    public void start() {
        (new Thread(this)).start();
    }

    boolean choixConnexion(PrintWriter out, BufferedReader in) throws Exception {
        while (true) {
            String menu = "Bienvenue Programmeur BRi !\n1 - Connexion\n2 - Inscription\n3 - Quitter\n\nEntrez votre choix :";
            out.println(menu.replace("\n", "#space#"));

            String line = in.readLine();

            try {
                int choix = Integer.parseInt(line);
                if (choix == 1) {
                    if (connexion(out, in)) return true;
                } else if (choix == 2) {
                    if (inscription(out, in)) return true;
                } else if (choix == 3) {
                    return false;
                } else {
                    out.println("Choix invalide.");
                }
            } catch (NumberFormatException e) {
                out.println("Veuillez entrer un chiffre.");
            }
        }
    }

    boolean connexion(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Login :");
        String login = in.readLine();
        out.println("Password :");
        String pass = in.readLine();

        if (ProgrammerRegistry.authenticate(login, pass)) {
            this.nom = login;
            this.lienFtp = ProgrammerRegistry.getFtpUrl(login);
            out.println("Authentification réussie !");
            return true;
        } else {
            out.println("Echec : Login ou mot de passe incorrect.");
            return false;
        }
    }

    boolean inscription(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Choisissez un Login (sera votre nom de package) :");
        String login = in.readLine();

        if (ProgrammerRegistry.exists(login)) {
            out.println("Ce login est déjà pris.");
            return false;
        }

        out.println("Choisissez un Password :");
        String pass = in.readLine();

        out.println("Indiquez votre URL FTP (ex: ftp://localhost:2121/) :");
        String ftp = in.readLine();

        if (!ftp.endsWith("/")) ftp += "/";

        ProgrammerRegistry.register(login, pass, ftp);
        this.nom = login;
        this.lienFtp = ftp;
        out.println("Inscription réussie !");
        return true;
    }

    void menuServices(PrintWriter out, BufferedReader in) throws IOException {
        // Menu des services
        // - Fournir un nouveau service ;
        // - Mettre-a-jour un service ;
        // - Declarer un changement d'adresse de son serveur ftp
        // - (Demarrer/arreter un service);
        // - Desinstaller un service.)
        boolean continuer = true;
        while (continuer) {
            String menu = "\n--- MENU PROGRAMMEUR (" + this.nom + ") ---\n" + "1 - Fournir un nouveau service\n" + "2 - Mettre à jour un service\n" + "3 - Changer l'adresse FTP\n" + "4 - Deconnexion\n" + "Votre choix :";
            out.println(menu.replace("\n", "#space#"));
            String line = in.readLine();
            if (line == null) break;
            switch (line) {
                case "1":
                    ajouterOuMajService(out, in);
                    break;
                case "2":
                    //TODO : Implementer la logique de remplacement de service (mise a jour) dans ServiceRegistery
                case "3":
                    changerFtp(out, in);
                    break;
                case "4":
                    continuer = false;
                    break;
                default:
                    out.println("Commande inconnue.");
            }
        }
    }

    private void ajouterOuMajService(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Nom complet de la classe (ex: " + this.nom + ".MaClasse) :");
        String className = in.readLine();

        // CONTROLE DU PACKAGE
        // "Un programmeur doit mettre tout ce qu'il développe dans un package portant comme nom son login"
        if (!className.startsWith(this.nom + ".")) {
            out.println("ERREUR NORME BRi : Votre classe doit être dans le package '" + this.nom + "'.");
            out.println("Attendu : " + this.nom + ".NomDeLaClasse");
            out.println("Reçu : " + className);
            return;
        }

        try {
            out.println("Chargement depuis " + this.lienFtp);

            URL[] urls = new URL[]{new URL(this.lienFtp)};
            URLClassLoader loader = new URLClassLoader(urls);

            // On force le rechargement en créant un nouveau loader à chaque fois
            Class<?> loadedClass = loader.loadClass(className);

            // Validation et Ajout
            // TODO : il faut qu'on fasse en sorte que ServiceRegistry gère l'unicité ou l'écrasement
            ServiceRegistry.addService(loadedClass);

        } catch (ClassNotFoundException e) {
            out.println("ERREUR : Classe introuvable sur votre FTP.");
        } catch (Exception e) {
            out.println("ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void changerFtp(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Nouvelle adresse FTP :");
        String newFtp = in.readLine();
        if (!newFtp.endsWith("/")) newFtp += "/";
        ProgrammerRegistry.updateFtpUrl(this.nom, newFtp);
        this.lienFtp = newFtp;
        out.println("Adresse FTP mise à jour.");
    }

}