package bri;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ServiceRegistry {
	// Liste pour stocker les classes de services conformes
	private static List<Class<?>> servicesClasses = new ArrayList<>();

	// Ajoute une classe de service apr�s contr�le de la norme BRi
	// Note: J'ai ajout� l'argument Class<?> c qui manquait dans le squelette
	public static void addService(Class<?> c) throws Exception {
		ClassValidator validator = new ClassValidator();
		if (verifyServiceExists(c)) {
			throw new Exception("Le service " + c.getName() + " existe déjà.");
		}

		if (validator.validation(c)) {
			servicesClasses.add(c);
			System.out.println("Service enregistr� : " + c.getName());
		} else {
			throw new Exception("La classe " + c.getName() + " ne respecte pas la norme BRi.");
		}
	}

	public static void updateService(Class<?> c) throws Exception {
		if (!verifyServiceExists(c)) {
			throw new Exception("Le service " + c.getName() + " n'existe pas et ne peut pas être mis à jour.");
		}
		// Supprimer l'ancienne version si elle existe
		removeService(c);
		addService(c);
	}

	public static void removeService(Class<?> c) throws Exception {
		if (!verifyServiceExists(c)) {
			throw new Exception("Le service " + c.getName() + " n'existe pas et ne peut pas être désinstallé.");
		}
		servicesClasses.remove(c);
		System.out.println("Service désinstallé : " + c.getName());
	}

	public static boolean verifyServiceExists(Class<?> c) {
		return servicesClasses.contains(c);
	}

	// Renvoie la classe de service correspondante (numService - 1 car l'affichage
	// commence � 1)
	public static Class<?> getServiceClass(int numService) {
		if (numService > 0 && numService <= servicesClasses.size()) {
			return servicesClasses.get(numService - 1);
		}
		return null;
	}

	// Liste les activit�s pr�sentes en invoquant la m�thode statique toStringue()
	// de chaque service
	public static String toStringue() {
		StringBuilder result = new StringBuilder("Activites presentes :\n");

		for (int i = 0; i < servicesClasses.size(); i++) {
			Class<?> c = servicesClasses.get(i);
			try {
				// Invocation de la m�thode statique toStringue()
				Method m = c.getMethod("toStringue");
				// null car la m�thode est statique
				String description = (String) m.invoke(null);
				result.append((i + 1)).append(". ").append(description).append("\n");
			} catch (Exception e) {
				result.append((i + 1)).append(". [Erreur description service]\n");
			}
		}
		return result.toString();
	}
}