package bri;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ServiceRegistry {
	// Liste pour stocker les classes de services conformes
	private static List<Class<?>> servicesClasses = new ArrayList<>();

	// Ajoute une classe de service après contrôle de la norme BRi
	// Note: J'ai ajouté l'argument Class<?> c qui manquait dans le squelette
	public static void addService(Class<?> c) throws Exception {
		ClassValidator validator = new ClassValidator();

		if (validator.validation(c)) {
			servicesClasses.add(c);
			System.out.println("Service enregistré : " + c.getName());
		} else {
			throw new Exception("La classe " + c.getName() + " ne respecte pas la norme BRi.");
		}
	}

	// Renvoie la classe de service correspondante (numService - 1 car l'affichage commence à 1)
	public static Class<?> getServiceClass(int numService) {
		if (numService > 0 && numService <= servicesClasses.size()) {
			return servicesClasses.get(numService - 1);
		}
		return null;
	}

	// Liste les activités présentes en invoquant la méthode statique toStringue() de chaque service
	public static String toStringue() {
		StringBuilder result = new StringBuilder("Activités présentes :\n");

		for (int i = 0; i < servicesClasses.size(); i++) {
			Class<?> c = servicesClasses.get(i);
			try {
				// Invocation de la méthode statique toStringue()
				Method m = c.getMethod("toStringue");
				// null car la méthode est statique
				String description = (String) m.invoke(null);
				result.append((i + 1)).append(". ").append(description).append("\n");
			} catch (Exception e) {
				result.append((i + 1)).append(". [Erreur description service]\n");
			}
		}
		return result.toString();
	}
}