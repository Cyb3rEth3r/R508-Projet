package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ApplicationAma {
	private final static int PORT_AMA = 3000;
	private final static String HOST = "localhost";

	public static void main(String[] args) {
		Socket s = null;

		try {

			s = new Socket(HOST, PORT_AMA);

			BufferedReader sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter sout = new PrintWriter(s.getOutputStream(), true);
			BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));

			String in = "";

			System.out.println(
					"Connecte au serveur " + s.getInetAddress() + ":" + s.getPort() + "\nTapez 'quit' pour quitter");

			while (!in.equals("quit")) {
				try {
					String line;
					// menu et choix du service
					line = sin.readLine().replace("#space#", "\n");
					System.out.println(line);
					// saisie/envoie du choix
					in = clavier.readLine();
					sout.println(in);
				} catch (IOException e) {
					System.err.println("Fin de la connexion");
					break;
				}
			}

		} catch (IOException e) {
			System.err.println("Fin de la connexion");
		}
		// Refermer dans tous les cas la socket
		try {
			if (s != null)
				s.close();
		} catch (IOException e2) {
			;
		}
	}
}
