package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bri.Service;

public class ServiceInternalMessaging implements Service {
    private final Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String currentUser;

    // Stockage des utilisateurs (pseudo -> password)
    private static Map<String, String> users = new HashMap<>();
    // Stockage des messages (clé = "user1:user2")
    private static Map<String, List<Message>> conversations = new HashMap<>();

    private static class Message {
        String sender;
        String content;

        Message(String sender, String content) {
            this.sender = sender;
            this.content = content;
        }
    }

    public ServiceInternalMessaging(Socket socket) {
        this.client = socket;
    }

    @Override
    public void service() {
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            if (!authenticate()) {
                return;
            }

            mainLoop();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    protected void finalize() throws Throwable {
        client.close();
    }

    public static String toStringue() {
        return "Messagerie Interne";
    }

    private void send(String message) {
        out.println(message.replace("\n", "#space#"));
    }

    private boolean authenticate() throws IOException {
        send("=== Messagerie Interne ===\n1. Se connecter\n2. S'inscrire\nVotre choix (END pour quitter):");

        String choice = in.readLine();
        if (choice == null || choice.equalsIgnoreCase("END")) {
            send("END");
            return false;
        }

        send("Pseudo:");
        String pseudo = in.readLine();
        if (pseudo == null || pseudo.equalsIgnoreCase("END")) {
            send("END");
            return false;
        }

        send("Mot de passe:");
        String password = in.readLine();
        if (password == null || password.equalsIgnoreCase("END")) {
            send("END");
            return false;
        }

        synchronized (users) {
            if (choice.equals("1")) {
                // Connexion
                if (users.containsKey(pseudo) && users.get(pseudo).equals(password)) {
                    currentUser = pseudo;
                    send("Connexion reussie! Bienvenue " + pseudo + "\nAppuyez sur Entree pour continuer...");
                    in.readLine();
                    return true;
                } else {
                    send("Pseudo ou mot de passe incorrect.\nEND");
                    return false;
                }
            } else if (choice.equals("2")) {
                // Inscription
                if (users.containsKey(pseudo)) {
                    send("Ce pseudo existe deja.\nEND");
                    return false;
                } else {
                    users.put(pseudo, password);
                    currentUser = pseudo;
                    send("Inscription reussie! Bienvenue " + pseudo + "\nAppuyez sur Entree pour continuer...");
                    in.readLine();
                    return true;
                }
            } else {
                send("Choix invalide.\nEND");
                return false;
            }
        }
    }

    private void mainLoop() throws IOException {
        while (true) {
            // Afficher les membres
            StringBuilder sb = new StringBuilder();
            sb.append("=== Utilisateurs ===\n");

            List<String> otherUsers = new ArrayList<>();
            synchronized (users) {
                for (String user : users.keySet()) {
                    if (!user.equals(currentUser)) {
                        otherUsers.add(user);
                    }
                }
            }

            if (otherUsers.isEmpty()) {
                sb.append("Aucun autre utilisateur.\n");
            } else {
                for (int i = 0; i < otherUsers.size(); i++) {
                    sb.append((i + 1) + ". " + otherUsers.get(i) + "\n");
                }
            }
            sb.append("Choisissez un numero (END pour quitter):");

            send(sb.toString());

            String choiceStr = in.readLine();
            if (choiceStr == null || choiceStr.equalsIgnoreCase("END")) {
                send("END");
                return;
            }

            int choiceNum;
            try {
                choiceNum = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                send("Choix invalide.\nAppuyez sur Entree pour continuer:");
                in.readLine();
                continue;
            }

            if (choiceNum < 1 || choiceNum > otherUsers.size()) {
                send("Choix invalide.\nAppuyez sur Entree pour continuer:");
                in.readLine();
                continue;
            }

            String selectedUser = otherUsers.get(choiceNum - 1);
            conversation(selectedUser);
        }
    }

    private void conversation(String otherUser) throws IOException {
        String conversationKey = getConversationKey(currentUser, otherUser);
        boolean justSentMessage = false;

        while (true) {
            StringBuilder sb = new StringBuilder();
            if (justSentMessage) {
                sb.append("\n\n\n\n");
                justSentMessage = false;
            }
            sb.append("=== Conversation avec " + otherUser + " ===\n");

            synchronized (conversations) {
                List<Message> messages = conversations.get(conversationKey);
                if (messages == null || messages.isEmpty()) {
                    sb.append("Aucun message.\n");
                } else {
                    for (Message msg : messages) {
                        if (msg.sender.equals(currentUser)) {
                            sb.append("Moi : " + msg.content + "\n");
                        } else {
                            sb.append(msg.sender + " : " + msg.content + "\n");
                        }
                    }
                }
            }

            sb.append("Votre message (RELOAD pour actualiser, BACK pour revenir, END pour quitter):");
            send(sb.toString());

            String message = in.readLine();
            if (message == null || message.equalsIgnoreCase("END")) {
                send("END");
                return;
            }

            if (message.equalsIgnoreCase("RELOAD")) {
                continue;
            }

            if (message.equalsIgnoreCase("BACK")) {
                send("Retour au menu principal.\nAppuyez sur Entree pour continuer...");
                in.readLine();
                return;
            }

            if (!message.trim().isEmpty()) {
                synchronized (conversations) {
                    List<Message> messages = conversations.computeIfAbsent(conversationKey, k -> new ArrayList<>());
                    messages.add(new Message(currentUser, message));
                }
                justSentMessage = true;
            }
        }
    }

    private String getConversationKey(String user1, String user2) {
        if (user1.compareTo(user2) < 0) {
            return user1 + ":" + user2;
        } else {
            return user2 + ":" + user1;
        }
    }
}