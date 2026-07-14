package ma.province.safi.passwordmanager.service;

import ma.province.safi.passwordmanager.dao.AuditDAO;
import ma.province.safi.passwordmanager.dao.NotificationDAO;
import ma.province.safi.passwordmanager.dao.SystemeExterneDAO;
import ma.province.safi.passwordmanager.model.SystemeExterne;
import ma.province.safi.passwordmanager.security.CryptoService;
import ma.province.safi.passwordmanager.session.Session;

import java.sql.SQLException;
import java.util.List;

public class SystemeExterneService {

    private final SystemeExterneDAO dao;
    private final AuditDAO auditDAO;
    private final NotificationDAO notificationDAO;
    private final CryptoService cryptoService;
    private final NotificationBroadcaster broadcaster = NotificationBroadcaster.getInstance();

    public SystemeExterneService(SystemeExterneDAO dao, AuditDAO auditDAO,
                                 NotificationDAO notificationDAO, CryptoService cryptoService) {
        this.dao = dao;
        this.auditDAO = auditDAO;
        this.notificationDAO = notificationDAO;
        this.cryptoService = cryptoService;
    }

    public void ajouter(SystemeExterne se, String motDePasseClair, Session session) throws Exception {
        CryptoService.ChiffrementResultat cr = cryptoService.chiffrer(motDePasseClair);
        dao.ajouter(se, cr.secretChiffre(), cr.iv());
        auditDAO.enregistrerOld("AJOUT", "SYSTEME_EXTERNE", se.getId(), session.getIdUtilisateur(),
            "Ajout SI externe: " + se.getNom());
        notificationDAO.creer(session.getIdUtilisateur(),
            "Nouveau système externe: " + se.getNom(), "INFORMATION");
        broadcaster.diffuserNotification("Nouveau système externe: " + se.getNom(), "INFO");
    }

    public void modifier(SystemeExterne se, Session session) throws Exception {
        dao.modifier(se);
        auditDAO.enregistrerOld("MODIFICATION", "SYSTEME_EXTERNE", se.getId(), session.getIdUtilisateur(),
            "Modification SI externe: " + se.getNom());
        broadcaster.diffuserNotification("Système externe modifié: " + se.getNom(), "INFO");
    }

    public void modifierMotDePasse(int id, String nouveauMotDePasse, Session session) throws Exception {
        if (nouveauMotDePasse.length() < 8)
            throw new IllegalArgumentException("Minimum 8 caractères");
        CryptoService.ChiffrementResultat cr = cryptoService.chiffrer(nouveauMotDePasse);
        dao.mettreAJourSecret(id, cr.secretChiffre(), cr.iv());
        auditDAO.enregistrerOld("MODIFICATION", "SYSTEME_EXTERNE", id, session.getIdUtilisateur(),
            "Changement mot de passe SI externe ID=" + id);
        notificationDAO.creer(session.getIdUtilisateur(),
            "Mot de passe modifié pour le système externe ID=" + id, "CHANGEMENT_MDP");
        broadcaster.diffuserNotification("🔒 Mot de passe changé - Système externe ID=" + id, "SECURITE");
    }

    public String consulterSecret(int id, Session session) throws Exception {
        verifierDroitConsultation(session);
        SystemeExterne se = dao.trouverParId(id);
        if (se == null) throw new IllegalArgumentException("Système externe introuvable");
        if (se.getMotPasseChiffre() == null) return null;
        auditDAO.enregistrerOld("CONSULTATION", "SYSTEME_EXTERNE", id, session.getIdUtilisateur(),
            "Consultation mot de passe SI externe: " + se.getNom());
        broadcaster.diffuserNotification("🔑 Secret consulté - SI externe: " + se.getNom() + " par " + session.getNomUtilisateur(), "AUDIT");
        return cryptoService.dechiffrer(se.getMotPasseChiffre(), se.getVecteurInitialisation());
    }

    public void supprimer(int id, Session session) throws Exception {
        SystemeExterne se = dao.trouverParId(id);
        String nom = se != null ? se.getNom() : "ID=" + id;
        dao.supprimer(id);
        auditDAO.enregistrerOld("SUPPRESSION", "SYSTEME_EXTERNE", id, session.getIdUtilisateur(),
            "Suppression SI externe: " + nom);
        broadcaster.diffuserNotification("Système externe supprimé: " + nom, "INFO");
    }

    public List<SystemeExterne> lister() throws SQLException { return dao.lister(); }
    public SystemeExterne trouverParId(int id) throws SQLException { return dao.trouverParId(id); }

    private void verifierDroitConsultation(Session session) {
        String role = session.getRole();
        if (!role.equals("Administrateur") && !role.equals("Agent SSICTD"))
            throw new SecurityException("Droits insuffisants");
    }
}
