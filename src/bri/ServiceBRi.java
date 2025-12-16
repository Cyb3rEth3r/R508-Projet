package bri;


import java.io.*;
import java.net.*;


class ServiceBRi implements Runnable {
	
	private Socket client;
	
	ServiceBRi(Socket socket) {
		client = socket;
	}

	public void run() {
		try {BufferedReader in = new BufferedReader (new InputStreamReader(client.getInputStream ( )));
			PrintWriter out = new PrintWriter (client.getOutputStream ( ), true);
			String menu = ServiceRegistry.toStringue()+"\nTapez le numéro de service désiré :";
			out.println (menu.replaceAll("\n","##"));
			int choix = Integer.parseInt(in.readLine());
			
			// instancier le service numéro "choix" en lui passant la socket "client"
			// invoquer service () pour cette instance 
				
			}
		catch (IOException e) {
			//Fin du service
		}

		try {client.close();} catch (IOException e2) {}
	}
	
	protected void finalize() throws Throwable {
		 client.close(); 
	}

	// lancement du service
	public void start() {
		(new Thread(this)).start();		
	}

}
