package bri;

import java.io.*;
import java.net.*;
import java.lang.reflect.Constructor;

public class ManagerAma implements Runnable {

	private Socket client;

	public ManagerAma(Socket socket) {
		client = socket;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);

			// 1. Envoyer le menu des services
			String menu = "Voici la liste des services disponibles : \n" + ServiceRegistry.toStringue() + "\n\nTapez le numero de service desire :";
			// potentiellement mettre le out.println avc replace dans 
			// une autre methode d'un fichier dans un package utils
			out.println(menu.replaceAll("\n", "#space#"));

			// 2. Lire le choix du client
			String line = in.readLine();
			if (line == null)
				return;
			int choix = Integer.parseInt(line);

			// 3. Recuper la classe du service
			Class<?> serviceClass = ServiceRegistry.getServiceClass(choix);

			if (serviceClass != null) {
				// 4. Instanciation dynamique avec constructeur(Socket)
				// La norme impose un constructeur public prenant une Socket
				Constructor<?> constructor = serviceClass.getConstructor(Socket.class);
				Object serviceInstance = constructor.newInstance(this.client);

				// 5. Invocation de la methode service()
				// Puisque la classe implemente l'interface Service, le cast est sur et plus
				// simple que l'invoke
				// simple que l'invoke
				((Service) serviceInstance).service();
			} else {
				out.println("Numero de service invalide.");
			}

		} catch (Exception e) {
			System.err.println("Erreur dans ManagerAma : " + e);
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
}