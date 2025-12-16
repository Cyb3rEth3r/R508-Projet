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

		System.out.println("Bienvenue dans votre gestionnaire dynamique d'activité BRi");
		System.out.println("Les clients se connectent au serveur " + PORT_SERVICE + " pour lancer une activité");

		// Lancement du thread serveur pour les clients (Amateurs)
		new Thread(new ServeurBRi(PORT_SERVICE)).start();

		while (true) {
			try {
				// 1. Demande de l'URL du serveur FTP (ex: ftp://127.0.0.1:2121/ ou file://d:/temp/ pour tester localement)
				System.out.println("\n--- Ajout d'un nouveau service ---");
				System.out.println("Entrez l'URL du dossier contenant les classes (ex: ftp://localhost:2121/ ou file:///c:/tmp/) :");
				String urlStr = clavier.next();

				// On s'assure que l'URL finit par un '/' pour désigner un dossier
				if (!urlStr.endsWith("/")) {
					urlStr += "/";
				}

				// 2. Demande du nom complet de la classe (ex: examples.ServiceInversion)
				System.out.println("Entrez le nom complet de la classe (package.NomClasse) :");
				String className = clavier.next();

				System.out.println("Tentative de chargement depuis " + urlStr + " ...");

				// 3. Création du ClassLoader pointant vers l'URL FTP
				URL[] urls = new URL[]{new URL(urlStr)};
				// URLClassLoader va chercher les classes sur le réseau
				URLClassLoader loader = new URLClassLoader(urls);

				// 4. Chargement de la classe
				// Note : La classe n'est pas encore instanciée, juste chargée en mémoire
				Class<?> loadedClass = loader.loadClass(className);

				// 5. Enregistrement et Validation via le ServiceRegistry
				// (Cela appellera ClassValidator et vérifiera la norme BRi)
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