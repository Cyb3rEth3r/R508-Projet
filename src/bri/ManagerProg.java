package bri;

import java.io.*;
import java.net.*;
import java.lang.reflect.Constructor;

public class ManagerProg implements Runnable {

	private Socket client;
	private String nom;
	private String mdp;
	private String lienFtp;

	public ManagerProg(Socket socket) {
		client = socket;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			
			choixConnexion(out, in);

			// Menu des services
			// - Fournir un nouveau service ;
			// - Mettre-a-jour un service ;
			// - Declarer un changement d'adresse de son serveur ftp
			// - (Demarrer/arreter un service ;
			// - Desinstaller un service.)

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

	void choixConnexion(PrintWriter out, BufferedReader in) {
		try {
			String menu = "1 - Connexion\n2 - Inscription\n\nEntrez votre choix :";
			out.println(menu.replace("\n", "#space#"));
			String line = in.readLine();
			int choixConnexion = Integer.parseInt(line);
			if (choixConnexion == 1) {
				connexion(out, in);
			} else if (choixConnexion == 2) {
				inscription(out, in);
			}
		}
		catch (Exception e) {
			System.err.println("Erreur dans choixConnexion : " + e);
			e.printStackTrace();
		}
	}

	void connexion(PrintWriter out, BufferedReader in) {
		try {
			String menu = "Entrez votre nom :";
			out.println(menu.replace("\n", "#space#"));
			String line = in.readLine();
			this.nom = line;

			menu = "Entrez votre mot de passe :";
			out.println(menu.replace("\n", "#space#"));
			line = in.readLine();
			this.mdp = line;	
			// Authentification(nom, mdp);
		}
		catch (Exception e) {
			System.err.println("Erreur dans connexion : " + e);
			e.printStackTrace();
		}

	}

	void inscription(PrintWriter out, BufferedReader in) {
		try {
			String menu = "Entrez votre nom :";
			out.println(menu.replace("\n", "#space#"));
			String line = in.readLine();
			this.nom = line;

			menu = "Entrez votre mot de passe :";
			out.println(menu.replace("\n", "#space#"));
			line = in.readLine();
			this.mdp = line;

			menu = "Entrez votre lien ftp :";
			out.println(menu.replace("\n", "#space#"));
			line = in.readLine();
			this.lienFtp = line;
			// Inscription(nom, mdp, lienFtp);
			
			choixConnexion(out, in);
		}
		catch (Exception e) {
			System.err.println("Erreur dans inscription : " + e);
			e.printStackTrace();
		}
	}

	void menuServices(PrintWriter out, BufferedReader in) {
	}




}