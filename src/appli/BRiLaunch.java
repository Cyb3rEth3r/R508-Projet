package appli;

import bri.ServeurBRi;
import bri.ManagerAma;
import bri.ManagerProg;

public class BRiLaunch {
	private final static int PORT_AMA = 3000;
	private final static int PORT_PROG = 4000;

	public static void main(String[] args) {

		System.out.println("  ____  _____  _____   _                            _     \n" +
				" |  _ \\|  __ \\|_   _| | |                          | |    \n" +
				" | |_) | |__) | | |   | |     __ _ _   _ _ __   ___| |__  \n" +
				" |  _ <|  _  /  | |   | |    / _` | | | | '_ \\ / __| '_ \\ \n" +
				" | |_) | | \\ \\ _| |_  | |___| (_| | |_| | | | | (__| | | |\n" +
				" |____/|_|  \\_\\_____| |______\\__,_|\\__,_|_| |_|\\___|_| |_|\n" +
				"                                                          \n" +
				"                                                          ");
		System.out.println("Gestionnaire dynamique d'activite BRi lance avec succes");
		
		// Lancement des threads serveurs pour les clients
		new Thread(new ServeurBRi(PORT_AMA, ManagerAma.class)).start();
		new Thread(new ServeurBRi(PORT_PROG, ManagerProg.class)).start();
	}
}