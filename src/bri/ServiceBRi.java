package bri;

import java.io.*;
import java.net.*;
import java.lang.reflect.Constructor;

class ServiceBRi implements Runnable {

	private Socket client;

	ServiceBRi(Socket socket) {
		client = socket;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);

			// 1. Envoyer le menu des services
			// On remplace \n par ## car le client lit souvent ligne par ligne (readLine)
			String menu = ServiceRegistry.toStringue() + "\nTapez le numéro de service désiré :";
			out.println(menu.replaceAll("\n", "##"));

			// 2. Lire le choix du client
			String line = in.readLine();
			if (line == null) return;
			int choix = Integer.parseInt(line);

			// 3. Récupérer la classe du service
			Class<?> serviceClass = ServiceRegistry.getServiceClass(choix);

			if (serviceClass != null) {
				// 4. Instanciation dynamique avec constructeur(Socket)
				// La norme impose un constructeur public prenant une Socket [cite: 90]
				Constructor<?> constructor = serviceClass.getConstructor(Socket.class);
				Object serviceInstance = constructor.newInstance(this.client);

				// 5. Invocation de la méthode service()
				// Puisque la classe implémente l'interface Service, le cast est sûr et plus simple que l'invoke
				((Service) serviceInstance).service();
			} else {
				out.println("Numéro de service invalide.");
			}

		} catch (Exception e) {
			System.err.println("Erreur dans ServiceBRi : " + e);
			e.printStackTrace();
		} finally {
			// On s'assure que la socket est fermée si le service n'a pas pris le relais ou a planté
			try { if (!client.isClosed()) client.close(); } catch (IOException e2) {}
		}
	}

	// ... reste du code (finalize, start) inchangé ...
	public void start() {
		(new Thread(this)).start();
	}
}