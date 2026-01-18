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
			System.out.println("Service enregistre : " + c.getName());
		} else {
			throw new Exception("La classe " + c.getName() + " ne respecte pas la norme BRi.");
		}
	}

	synchronized public static void updateService(Class<?> c, String className) throws Exception {
		if (!verifyServiceExists(className)) {
			throw new Exception("Le service " + c.getName() + " n'existe pas et ne peut pas être mis à jour.");
		}
		// Supprimer l'ancienne version si elle existe
		removeService(className);
		addService(c);
	}

	public static void removeService(String className) throws Exception {
        synchronized (servicesClasses) {
            Class<?> c = getServiceClassByName(className);
            if (c == null) {
                throw new Exception("Le service " + className + " n'existe pas.");
            }
            servicesClasses.remove(c); // On retire l'objet exact trouvé dans la liste
            System.out.println("Service désinstallé : " + className);
        }
    }

	private static Class<?> getServiceClassByName(String name) {
        synchronized (servicesClasses) {
            for (Class<?> c : servicesClasses) {
                if (c.getName().equals(name)) {
                    return c;
                }
            }
        }
        return null;
    }

	public static boolean verifyServiceExists(Class<?> c) {
		return servicesClasses.contains(c);
	}

	public static boolean verifyServiceExists(String className) {
		for (Class<?> c : servicesClasses) {
			if (c.getName().equals(className)) {
				return true;
			}
		}
		return false;
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