package appli;

import bri.ServeurBRi;
import bri.ManagerAma;
import bri.ManagerProg;

public class BRiLaunch {
	private final static int PORT_AMA = 3000;
	private final static int PORT_PROG = 4000;

	public static void main(String[] args) {

		System.out.println("Gestionnaire dynamique d'activite BRi lancé avec succès");
		
		// Lancement des threads serveurs pour les clients
		new Thread(new ServeurBRi(PORT_AMA, ManagerAma.class)).start();
		new Thread(new ServeurBRi(PORT_PROG, ManagerProg.class)).start();
	}
}