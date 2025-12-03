package com.example.bbuniversity;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    // Configuration SMTP
    private static final String SMTP_HOST = "smtp.gmail.com"; // Hôte du serveur SMTP
    private static final String SMTP_PORT = "587";              // Port du serveur SMTP (généralement 587 pour TLS)
    private static final String USERNAME = "";          // Nom d'utilisateur du compte SMTP
    private static final String PASSWORD = "";          // Mot de passe du compte SMTP
    private static final String FROM_EMAIL = ""; // Email de l'expéditeur

    // Méthode pour envoyer un email
    public static void sendEmail(String to, String subject, String body) throws MessagingException {
        // Configuration des propriétés pour la connexion SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");                   // Authentification requise
        props.put("mail.smtp.starttls.enable", "true");         // Activation du chiffrement TLS
        props.put("mail.smtp.host", SMTP_HOST);                // Définition de l'hôte SMTP
        props.put("mail.smtp.port", SMTP_PORT);                 // Définition du port SMTP

        // Création de la session avec authentification
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Fournit les identifiants pour l'authentification SMTP
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        // Création du message email
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));       // Définit l'expéditeur
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // Définit le(s) destinataire(s)
        message.setSubject(subject);                            // Définit le sujet de l'email
        message.setText(body);                                 // Définit le contenu textuel de l'email

        // Envoi de l'email
        Transport.send(message);
    }
}
