package appli;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Scanner;

import bri.ServeurBRi;
import bri.ServiceRegistry;

public class BRiLaunch {
	private final static int PORT_SERVICE = 3000;

	public static void main(String[] args) {
		Scanner clavier = new Scanner(System.in);

		System.out.println("Bienvenue dans votre gestionnaire dynamique d'activit� BRi");
		System.out.println("Les clients se connectent au serveur " + PORT_SERVICE + " pour lancer une activit�");

		// Lancement du thread serveur pour les clients (Amateurs)
		new Thread(new ServeurBRi(PORT_SERVICE)).start();

		while (true) {
			try {
				System.out.println("\n--- Ajout d'un nouveau service ---");
				System.out.println("Entrez l'URL :");
				String urlStr = clavier.next().trim();

				System.out.println("Entrez le nom complet de la classe (package.NomClasse) :");
				String className = clavier.next().trim();

				System.out.println("Tentative de chargement depuis " + urlStr + " ...");

				URL[] urls = new URL[] { new URL(urlStr) };
				// URLClassLoader va chercher les classes sur le réseau
				URLClassLoader urlcl = new URLClassLoader(urls);

				// CHargement de la classe
				Class<?> loadedClass = urlcl.loadClass(className);

				// Enregistrement + validation via le ServiceRegistry
				ServiceRegistry.addService(loadedClass);

			} catch (ClassNotFoundException e) {
				System.err.println("Erreur : Classe introuvable à cette adresse. Vérifiez le nom et l'URL.");
			} catch (Exception e) {
				System.err.println("Erreur lors de l'ajout du service : " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}