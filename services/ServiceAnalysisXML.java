package services;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import bri.Service;

public class ServiceAnalysisXML implements Service {

    private final Socket client;

    // Configuration SMTP
    private static final String SMTP_HOST = "mail.canope.org";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USER = "automation@canope.org";
    private static final String SMTP_PASS = "**********";

    public ServiceAnalysisXML(Socket socket) {
        client = socket;
    }

    @Override
    public void service() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            // Demander l'URL FTP
            out.println("Entrez l'URL FTP du fichier XML (ex: ftp://localhost:2121/fichier.xml) :");
            String ftpUrl = in.readLine();

            if (ftpUrl == null || ftpUrl.trim().isEmpty()) {
                out.println("ERREUR : URL vide.");
                return;
            }

            // Demander l'email du destinataire
            out.println("Entrez l'adresse email pour recevoir le rapport :");
            String emailTo = in.readLine();

            if (emailTo == null || emailTo.trim().isEmpty() || !emailTo.contains("@")) {
                out.println("ERREUR : Adresse email invalide.");
                return;
            }

            System.out.println("[ServiceAnalysisXML] URL reçue : " + ftpUrl);
            System.out.println("[ServiceAnalysisXML] Email destinataire : " + emailTo);

            try {
                // Configuration du parser
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setNamespaceAware(true);
                factory.setFeature("http://xml.org/sax/features/namespaces", false);
                factory.setFeature("http://xml.org/sax/features/validation", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                System.out.println("[ServiceAnalysisXML] Connexion FTP...");

                // Lecture directe depuis l'URL FTP
                Document document = factory.newDocumentBuilder().parse(ftpUrl.trim());
                document.getDocumentElement().normalize();

                System.out.println("[ServiceAnalysisXML] Document charge !");

                Element root = document.getDocumentElement();

                // Construire le rapport
                String report = buildReport(ftpUrl, root);

                System.out.println("[ServiceAnalysisXML] Rapport genere, envoi par email...");

                // Envoyer par email
                boolean emailSent = sendEmail(emailTo.trim(), report);

                if (emailSent) {
                    out.println("Rapport d'analyse envoye avec succes a : " + emailTo);
                    System.out.println("[ServiceAnalysisXML] Email envoye !");
                } else {
                    out.println("ERREUR : Impossible d'envoyer l'email. Voici le rapport :");
                    out.println(report.replace("\n", "#space#"));
                }

            } catch (Exception e) {
                System.err.println("[ServiceAnalysisXML] ERREUR : " + e.getMessage());
                e.printStackTrace();
                out.println("ERREUR : " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Erreur d'E/S dans ServiceAnalysisXML : " + e.getMessage());
        }
    }

    /**
     * Construit le rapport d'analyse
     */
    private String buildReport(String url, Element root) {
        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT D'ANALYSE XML ===\n\n");
        report.append("Groupe : Raphael Desmonts & Emile Rossi\n");
        report.append("Source : ").append(url).append("\n");
        report.append("Date : ").append(new Date()).append("\n\n");

        report.append("[OK] Structure XML bien formee\n");
        report.append("  - Element racine : <").append(root.getTagName()).append(">\n");

        int elementCount = countElements(root);
        int attributeCount = countAttributes(root);
        int maxDepth = getMaxDepth(root);

        report.append("\n[STATISTIQUES]\n");
        report.append("  - Nombre d'elements : ").append(elementCount).append("\n");
        report.append("  - Nombre d'attributs : ").append(attributeCount).append("\n");
        report.append("  - Profondeur maximale : ").append(maxDepth).append("\n");

        List<String> emptyElements = findEmptyElements(root);
        report.append("\n[ELEMENTS VIDES]\n");
        if (emptyElements.isEmpty()) {
            report.append("  Aucun element vide trouve\n");
        } else {
            report.append("  ").append(emptyElements.size()).append(" element(s) vide(s) :\n");
            for (String elem : emptyElements) {
                report.append("    - <").append(elem).append("/>\n");
            }
        }

        Set<String> uniqueTags = new HashSet<>();
        collectTagNames(root, uniqueTags);
        report.append("\n[BALISES UTILISEES]\n");
        for (String tag : uniqueTags) {
            report.append("  - ").append(tag).append("\n");
        }

        report.append("\n=== FIN DU RAPPORT ===\n");

        return report.toString();
    }

    private String readSmtpResponse(BufferedReader reader) throws IOException {
        String line;
        String lastLine = "";
        while ((line = reader.readLine()) != null) {
            System.out.println("[SMTP] " + line);
            lastLine = line;
            // Si la 4ème caractère n'est pas '-', c'est la dernière ligne
            if (line.length() >= 4 && line.charAt(3) != '-') {
                break;
            }
        }
        return lastLine;
    }

    /**
     * Envoie le rapport par email via SMTP
     */
    private boolean sendEmail(String recipient, String reportContent) {
        Socket smtpSocket = null;

        try {
            System.out.println("[SMTP] Connexion a " + SMTP_HOST + ":" + SMTP_PORT);

            smtpSocket = new Socket(SMTP_HOST, SMTP_PORT);
            smtpSocket.setSoTimeout(30000);

            BufferedReader smtpIn = new BufferedReader(new InputStreamReader(smtpSocket.getInputStream()));
            PrintWriter smtpOut = new PrintWriter(smtpSocket.getOutputStream(), true);

            // Lire le message de bienvenue (peut être multi-lignes)
            readSmtpResponse(smtpIn);

            // EHLO
            smtpOut.println("EHLO localhost");
            readSmtpResponse(smtpIn);

            // STARTTLS
            smtpOut.println("STARTTLS");
            String response = readSmtpResponse(smtpIn);

            if (!response.startsWith("220")) {
                System.err.println("[SMTP] STARTTLS non supporte");
                return false;
            }

            // Upgrade vers SSL/TLS
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            javax.net.ssl.SSLSocketFactory sslFactory = sslContext.getSocketFactory();
            javax.net.ssl.SSLSocket sslSocket = (javax.net.ssl.SSLSocket) sslFactory.createSocket(
                    smtpSocket, SMTP_HOST, SMTP_PORT, true);

            // Configurer les protocoles TLS
            sslSocket.setEnabledProtocols(new String[] { "TLSv1.2", "TLSv1.3" });
            sslSocket.startHandshake();

            System.out.println("[SMTP] Connexion TLS etablie");

            // Nouveaux flux sur la connexion sécurisée
            smtpIn = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            smtpOut = new PrintWriter(sslSocket.getOutputStream(), true);

            // EHLO après STARTTLS
            smtpOut.println("EHLO localhost");
            readSmtpResponse(smtpIn);

            // AUTH LOGIN
            smtpOut.println("AUTH LOGIN");
            readSmtpResponse(smtpIn);

            // Envoyer username en Base64
            String userB64 = Base64.getEncoder().encodeToString(SMTP_USER.getBytes());
            smtpOut.println(userB64);
            readSmtpResponse(smtpIn);

            // Envoyer password en Base64
            String passB64 = Base64.getEncoder().encodeToString(SMTP_PASS.getBytes());
            smtpOut.println(passB64);
            response = readSmtpResponse(smtpIn);

            if (!response.startsWith("235")) {
                System.err.println("[SMTP] Authentification echouee: " + response);
                return false;
            }

            System.out.println("[SMTP] Authentification reussie");

            // MAIL FROM
            smtpOut.println("MAIL FROM:<" + SMTP_USER + ">");
            response = readSmtpResponse(smtpIn);
            if (!response.startsWith("250")) {
                System.err.println("[SMTP] MAIL FROM echoue: " + response);
                return false;
            }

            // RCPT TO
            smtpOut.println("RCPT TO:<" + recipient + ">");
            response = readSmtpResponse(smtpIn);
            if (!response.startsWith("250")) {
                System.err.println("[SMTP] RCPT TO echoue: " + response);
                return false;
            }

            // DATA
            smtpOut.println("DATA");
            response = readSmtpResponse(smtpIn);
            if (!response.startsWith("354")) {
                System.err.println("[SMTP] DATA echoue: " + response);
                return false;
            }

            // En-têtes et corps du message
            smtpOut.println("From: " + SMTP_USER);
            smtpOut.println("To: " + recipient);
            smtpOut.println("Subject: Rapport d'analyse XML - BRi");
            smtpOut.println("Content-Type: text/plain; charset=UTF-8");
            smtpOut.println();
            smtpOut.println(reportContent);
            smtpOut.println(".");

            response = readSmtpResponse(smtpIn);

            // QUIT
            smtpOut.println("QUIT");
            sslSocket.close();

            return response.startsWith("250");

        } catch (Exception e) {
            System.err.println("[SMTP] Erreur : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (smtpSocket != null && !smtpSocket.isClosed()) {
                    smtpSocket.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private int countElements(Element element) {
        int count = 1;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                count += countElements((Element) children.item(i));
            }
        }
        return count;
    }

    private int countAttributes(Element element) {
        int count = element.getAttributes().getLength();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                count += countAttributes((Element) children.item(i));
            }
        }
        return count;
    }

    private int getMaxDepth(Element element) {
        int maxChildDepth = 0;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                int childDepth = getMaxDepth((Element) children.item(i));
                maxChildDepth = Math.max(maxChildDepth, childDepth);
            }
        }
        return maxChildDepth + 1;
    }

    private List<String> findEmptyElements(Element element) {
        List<String> emptyElements = new ArrayList<>();

        boolean hasContent = false;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                hasContent = true;
                emptyElements.addAll(findEmptyElements((Element) child));
            } else if (child.getNodeType() == Node.TEXT_NODE && !child.getTextContent().trim().isEmpty()) {
                hasContent = true;
            }
        }

        if (!hasContent) {
            emptyElements.add(element.getTagName());
        }

        return emptyElements;
    }

    private void collectTagNames(Element element, Set<String> tags) {
        tags.add(element.getTagName());
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                collectTagNames((Element) children.item(i), tags);
            }
        }
    }

    protected void finalize() throws Throwable {
        if (!client.isClosed()) {
            client.close();
        }
    }

    public static String toStringue() {
        return "Analyse de fichier XML (FTP)";
    }
}