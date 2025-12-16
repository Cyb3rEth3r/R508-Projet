package bri;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;

public class ClassValidator {
    public boolean validateInterface(Class<?> c) {
        Class<?>[] interfaces = c.getInterfaces();
        boolean valid = false;
        for (Class<?> i : interfaces) {
            if (i.equals(Service.class)) {
                valid = true;
            }
        }
        return valid;
    }

    public boolean validateAbstract(Class<?> c) {
        return Modifier.isAbstract(c.getModifiers());
    }

    public boolean validatePublic(Class<?> c) {
        return Modifier.isPublic(c.getModifiers());
    }

    //Modifier est un ensemble de flag qui return des bools sur different caratersistque d'une calss
    public boolean validateConstructor(Class<?> c) {
        boolean isValid;
        try {
            Constructor<?> constructor = c.getConstructor(Socket.class);
            isValid = Modifier.isPublic(constructor.getModifiers()) && constructor.getExceptionTypes().length == 0;
        } catch (NoSuchMethodException e) {
            isValid = false;
        }
        return isValid;
    }

    public boolean validateAttribute(Class<?> c) {
        boolean isValid = false;
        Field[] fields = c.getFields();
        for (Field f : fields) {
            if (f.getType().equals(Socket.class) && Modifier.isPrivate(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }

    public boolean validateToStringueMethod(Class<?> c) {
        boolean isValid = false;
        Method[] methods = c.getMethods();
        for (Method m : methods) {
            if (m.getName().equals("toStringue") && Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers()) && m.getExceptionTypes().length == 0) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }

    public boolean validation(Class<?> c) {
        return validateInterface(c) && validateAbstract(c) && validatePublic(c) && validateConstructor(c) && validateAttribute(c) && validateToStringueMethod(c);
    }
}
