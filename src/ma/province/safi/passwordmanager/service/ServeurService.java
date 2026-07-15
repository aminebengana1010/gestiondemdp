package ma.province.safi.passwordmanager.service;

import ma.province.safi.passwordmanager.dao.AuditDAO;
import ma.province.safi.passwordmanager.dao.NotificationDAO;
import ma.province.safi.passwordmanager.dao.ServeurDAO;
import ma.province.safi.passwordmanager.model.Serveur;
import ma.province.safi.passwordmanager.security.CryptoService;
import ma.province.safi.passwordmanager.session.Session;

import java.sql.SQLException;
import java.util.List;

public class ServeurService {

    private final ServeurDAO serveurDAO;
    private final AuditDAO auditDAO;
    private final NotificationDAO notificationDAO;
    private final CryptoService cryptoService;
    private final NotificationBroadcaster broadcaster = NotificationBroadcaster.getInstance();

    public ServeurService(ServeurDAO serveurDAO, AuditDAO auditDAO,
                          NotificationDAO notificationDAO, CryptoService cryptoService) {
        this.serveurDAO = serveurDAO;
        this.auditDAO = auditDAO;
        this.notificationDAO = notificationDAO;
        this.cryptoService = cryptoService;
    }

    public void ajouter(Serveur serveur, String motDePasseClair, Session session) throws Exception {
        CryptoService.ChiffrementResultat cr = cryptoService.chiffrer(motDePasseClair);
        serveurDAO.ajouter(serveur, cr.secretChiffre(), cr.iv());
        auditDAO.enregistrerOld("AJOUT", "SERVEUR", serveur.getId(), session.getIdUtilisateur(),
            "Ajout du serveur: " + serveur.getNom());
        notificationDAO.creer(session.getIdUtilisateur(),
            "Nouveau serveur ajouté: " + serveur.getNom(), "INFORMATION");
        broadcaster.diffuserNotification("Nouveau serveur ajouté: " + serveur.getNom(), "INFO");
    }

    public void modifier(int id, String nom, String adresseIP, String login, Session session) throws Exception {
        Serveur s = serveurDAO.trouverParId(id);
        if (s == null) throw new IllegalArgumentException("Serveur introuvable");
        String ancienNom = s.getNom();
        s.setNom(nom);
        s.setAdresseIP(adresseIP);
        s.setLogin(login);
        serveurDAO.modifier(s);
        auditDAO.enregistrerOld("MODIFICATION", "SERVEUR", id, session.getIdUtilisateur(),
            "Modification du serveur: " + nom);
        broadcaster.diffuserNotification("Serveur modifié: " + nom + " (par " + session.getNomUtilisateur() + ")", "INFO");
    }

    public void modifierMotDePasse(int id, String nouveauMotDePasse, Session session) throws Exception {
        Serveur s = serveurDAO.trouverParId(id);
        if (s == null) throw new IllegalArgumentException("Serveur introuvable");
        verifierForceMotDePasse(nouveauMotDePasse);

        CryptoService.ChiffrementResultat cr = cryptoService.chiffrer(nouveauMotDePasse);
        serveurDAO.mettreAJourSecret(id, cr.secretChiffre(), cr.iv());

        auditDAO.enregistrerOld("MODIFICATION", "SERVEUR", id, session.getIdUtilisateur(),
            "Changement mot de passe du serveur: " + s.getNom());
        notificationDAO.creer(session.getIdUtilisateur(),
            "Mot de passe modifié pour le serveur: " + s.getNom(), "CHANGEMENT_MDP");
        broadcaster.diffuserNotification("🔒 Mot de passe changé - Serveur: " + s.getNom(), "SECURITE");
    }

    public String consulterSecret(int id, Session session) throws Exception {
        verifierDroitConsultation(session);
        Serveur s = serveurDAO.trouverParId(id);
        if (s == null) throw new IllegalArgumentException("Serveur introuvable");
        if (s.getMotPasseChiffre() == null) return null;

        auditDAO.enregistrerOld("CONSULTATION", "SERVEUR", id, session.getIdUtilisateur(),
            "Consultation du mot de passe du serveur: " + s.getNom());
        broadcaster.diffuserNotification("🔑 Secret consulté - Serveur: " + s.getNom() + " par " + session.getNomUtilisateur(), "AUDIT");
        try {
            return cryptoService.dechiffrer(s.getMotPasseChiffre(), s.getVecteurInitialisation());
        } catch (Exception e) {
            throw new RuntimeException("Impossible de déchiffrer le mot de passe. La clé AES a changé (redémarrage ?). Ré-enregistrez le mot de passe.", e);
        }
    }

    public void supprimer(int id, Session session) throws Exception {
        Serveur s = serveurDAO.trouverParId(id);
        String nom = s != null ? s.getNom() : "ID=" + id;
        serveurDAO.supprimer(id);
        auditDAO.enregistrerOld("SUPPRESSION", "SERVEUR", id, session.getIdUtilisateur(),
            "Suppression du serveur: " + nom);
        broadcaster.diffuserNotification("Serveur supprimé: " + nom, "INFO");
    }

    public List<Serveur> lister() throws SQLException {
        List<Serveur> list = serveurDAO.lister();
        for (Serveur s : list) {
            try {
                if (s.getMotPasseChiffre() != null) {
                    s.setMotDePasseClair(cryptoService.dechiffrer(s.getMotPasseChiffre(), s.getVecteurInitialisation()));
                }
            } catch (Exception e) { /* ignore */ }
        }
        return list;
    }
    public Serveur trouverParId(int id) throws SQLException { return serveurDAO.trouverParId(id); }
    public List<Serveur> rechercher(String texte) throws SQLException { return serveurDAO.rechercher(texte); }

    private void verifierForceMotDePasse(String mdp) {
        if (mdp == null || mdp.length() < 8)
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
    }

    private void verifierDroitConsultation(Session session) {
        String role = session.getRole();
        if (!role.equals("Administrateur") && !role.equals("Agent SSICTD"))
            throw new SecurityException("Droits insuffisants pour consulter un secret");
    }
}
