package bri;


import java.io.*;
import java.net.*;


public class ServeurBRi implements Runnable {
	private ServerSocket listen_socket;
	private Class<? extends Runnable> serviceClass;
	
	// Cree un serveur TCP - objet de la classe ServerSocket
	public ServeurBRi(int port, Class<? extends Runnable> serviceClass) {
		try {
			listen_socket = new ServerSocket(port);
			this.serviceClass = serviceClass;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// Le serveur ecoute et accepte les connections.
	// pour chaque connection, il cree un ServiceBRi, 
	// qui va la traiter.
	public void run() {
		try {
			while(true)
			{
				Socket client = listen_socket.accept();
				try {
					// Instancie dynamiquement le service avec le client socket puis appelle service()
					Runnable serviceInstance = (Runnable) serviceClass.getConstructor(Socket.class).newInstance(client);
					new Thread(serviceInstance).start();
				} catch (Exception e) {
					System.err.println("Erreur lors du démarrage du service : " + e);
					try { if (!client.isClosed()) client.close(); } catch (IOException e2) {}
				}
			}
		}
		catch (IOException e) { 
			try {this.listen_socket.close();} catch (IOException e1) {}
			System.err.println("Pb sur le port d'ecoute :"+e);
		}
	}

	 // restituer les ressources --> finalize
	protected void finalize() throws Throwable {
		try {this.listen_socket.close();} catch (IOException e1) {}
	}

	// lancement du serveur
	public void lancer() {
		(new Thread(this)).start();		
	}
}
