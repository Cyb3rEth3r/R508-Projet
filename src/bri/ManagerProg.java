package bri;

import java.io.*;
import java.net.*;
import java.lang.reflect.Constructor;

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
        String msg;

        if (ProgrammerRegistry.authenticate(login, pass)) {
            this.nom = login;
            this.lienFtp = ProgrammerRegistry.getFtpUrl(login);
            msg = "Authentification réussie !\nAppuyez sur Entrée pour continuer...";
            out.println(msg.replace("\n", "#space#"));
            return true;
        } else {
            out.println("Echec : Login ou mot de passe incorrect.");
            return false;
        }
    }

    boolean inscription(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Choisissez un Login (sera votre nom de package) :");
        String login = in.readLine();
        String msg;

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
        msg = "Inscription réussie !\nAppuyez sur Entrée pour continuer...";
        out.println(msg.replace("\n", "#space#"));
        in.readLine();
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
            String menu = "\n--- MENU PROGRAMMEUR (" + this.nom + ") ---\n" + "1 - Fournir un nouveau service\n" + "2 - Mettre à jour un service\n" + "3 - Changer l'adresse FTP\n" + "4 - Desinstaller un service\n" + "5 - Déconnexion\n" + "Votre choix :";
            out.println(menu.replace("\n", "#space#"));
            String line = in.readLine();
            if (line == null) break;
            switch (line) {
                case "1":
                    ajouterService(out, in);
                    break;
                case "2":
                    mettreAJourService(out, in);
                    break;
                case "3":
                    changerFtp(out, in);
                    break;
                case "4":
                    desinstallerService(out, in);
                    break;
                case "5":
                    continuer = false;
                    break;
                default:
                    out.println("Commande inconnue.");
            }
        }
    }

    private void ajouterService(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Nom complet de la classe (ex: " + this.nom + ".MaClasse) :");
        String className = in.readLine();
        String msg;

        // CONTROLE DU PACKAGE
        // "Un programmeur doit mettre tout ce qu'il développe dans un package portant comme nom son login"
        if (!className.startsWith(this.nom + ".")) {
            msg = "ERREUR NORME BRi : Votre classe doit être dans le package '" + this.nom + "'.\nAttendu : " + this.nom + ".NomDeLaClasse\nReçu : " + className + "\nAppuyez sur Entrée pour continuer...";
            out.println(msg.replace("\n", "#space#"));
            in.readLine();
            return;
        }

        try {

            URL[] urls = new URL[]{new URL(this.lienFtp)};
            URLClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());

            // On force le rechargement en créant un nouveau loader à chaque fois
            Class<?> loadedClass = loader.loadClass(className);

            // Validation et Ajout
            ServiceRegistry.addService(loadedClass);

            msg = "Charger depuis :" + this.lienFtp + "\nAppuyez sur Entrée pour continuer...";
            out.println(msg.replace("\n", "#space#"));
            in.readLine();

        } catch (ClassNotFoundException e) {
            out.println("ERREUR : Classe introuvable sur votre FTP.");
        } catch (Exception e) {
            out.println("ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mettreAJourService(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Nom complet de la classe à mettre à jour (ex: " + this.nom + ".MaClasse) :");
        String className = in.readLine();
        String msg;

        // CONTROLE DU PACKAGE
        if (!className.startsWith(this.nom + ".")) {
            msg = "ERREUR NORME BRi : Votre classe doit être dans le package '" + this.nom + "'.\nAttendu : " + this.nom + ".NomDeLaClasse\nReçu : " + className + "\nAppuyez sur Entrée pour continuer...";
            out.println(msg.replace("\n", "#space#"));
            in.readLine();
            return;
        }

        try {

            URL[] urls = new URL[]{new URL(this.lienFtp)};
            URLClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());

            // On force le rechargement en créant un nouveau loader à chaque fois
            Class<?> loadedClass = loader.loadClass(className);

            // Validation et Mise à jour
            ServiceRegistry.updateService(loadedClass, className);

            msg = "Service mis à jour depuis :" + this.lienFtp + "\nAppuyez sur Entrée pour continuer...";
            out.println(msg.replace("\n", "#space#"));
            in.readLine();

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

    private void desinstallerService(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Nom complet de la classe à désinstaller (ex: " + this.nom + ".MaClasse) :");
        String className = in.readLine();
        String msg;

        // CONTROLE DU PACKAGE
        if (!className.startsWith(this.nom + ".")) {
            out.println("ERREUR : Mauvais package.\nAppuyez sur Entrée...");
            in.readLine();
            return;
        }

        try {
            // CORRECTION : On passe le NOM (String) directement au registre
            ServiceRegistry.removeService(className);

            msg = "Service désinstallé avec succès.\nAppuyez sur Entrée pour continuer...";
            out.println(msg.replace("\n", "#space#"));
            in.readLine();

        } catch (Exception e) {
            out.println("ERREUR : " + e.getMessage());
        }
    }

}