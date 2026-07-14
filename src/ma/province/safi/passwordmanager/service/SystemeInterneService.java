package ma.province.safi.passwordmanager.service;

import ma.province.safi.passwordmanager.dao.AuditDAO;
import ma.province.safi.passwordmanager.dao.NotificationDAO;
import ma.province.safi.passwordmanager.dao.SystemeInterneDAO;
import ma.province.safi.passwordmanager.model.SystemeInterne;
import ma.province.safi.passwordmanager.security.CryptoService;
import ma.province.safi.passwordmanager.session.Session;

import java.sql.SQLException;
import java.util.List;

public class SystemeInterneService {

    private final SystemeInterneDAO dao;
    private final AuditDAO auditDAO;
    private final NotificationDAO notificationDAO;
    private final CryptoService cryptoService;
    private final NotificationBroadcaster broadcaster = NotificationBroadcaster.getInstance();

    public SystemeInterneService(SystemeInterneDAO dao, AuditDAO auditDAO,
                                 NotificationDAO notificationDAO, CryptoService cryptoService) {
        this.dao = dao;
        this.auditDAO = auditDAO;
        this.notificationDAO = notificationDAO;
        this.cryptoService = cryptoService;
    }

    public void ajouter(SystemeInterne si, String motDePasseClair, Session session) throws Exception {
        CryptoService.ChiffrementResultat cr = cryptoService.chiffrer(motDePasseClair);
        dao.ajouter(si, cr.secretChiffre(), cr.iv());
        auditDAO.enregistrerOld("AJOUT", "SYSTEME_INTERNE", si.getId(), session.getIdUtilisateur(),
            "Ajout SI interne: " + si.getNom());
        notificationDAO.creer(session.getIdUtilisateur(),
            "Nouveau système interne: " + si.getNom(), "INFORMATION");
        broadcaster.diffuserNotification("Nouveau système interne: " + si.getNom(), "INFO");
    }

    public void modifier(SystemeInterne si, Session session) throws Exception {
        dao.modifier(si);
        auditDAO.enregistrerOld("MODIFICATION", "SYSTEME_INTERNE", si.getId(), session.getIdUtilisateur(),
            "Modification SI interne: " + si.getNom());
        broadcaster.diffuserNotification("Système interne modifié: " + si.getNom(), "INFO");
    }

    public void modifierMotDePasse(int id, String nouveauMotDePasse, Session session) throws Exception {
        if (nouveauMotDePasse.length() < 8)
            throw new IllegalArgumentException("Minimum 8 caractères");
        CryptoService.ChiffrementResultat cr = cryptoService.chiffrer(nouveauMotDePasse);
        dao.mettreAJourSecret(id, cr.secretChiffre(), cr.iv());
        auditDAO.enregistrerOld("MODIFICATION", "SYSTEME_INTERNE", id, session.getIdUtilisateur(),
            "Changement mot de passe SI interne ID=" + id);
        notificationDAO.creer(session.getIdUtilisateur(),
            "Mot de passe modifié pour le système interne ID=" + id, "CHANGEMENT_MDP");
        broadcaster.diffuserNotification("🔒 Mot de passe changé - Système interne ID=" + id, "SECURITE");
    }

    public String consulterSecret(int id, Session session) throws Exception {
        verifierDroitConsultation(session);
        SystemeInterne si = dao.trouverParId(id);
        if (si == null) throw new IllegalArgumentException("Système interne introuvable");
        if (si.getMotPasseChiffre() == null) return null;
        auditDAO.enregistrerOld("CONSULTATION", "SYSTEME_INTERNE", id, session.getIdUtilisateur(),
            "Consultation mot de passe SI interne: " + si.getNom());
        broadcaster.diffuserNotification("🔑 Secret consulté - SI interne: " + si.getNom() + " par " + session.getNomUtilisateur(), "AUDIT");
        return cryptoService.dechiffrer(si.getMotPasseChiffre(), si.getVecteurInitialisation());
    }

    public void supprimer(int id, Session session) throws Exception {
        SystemeInterne si = dao.trouverParId(id);
        String nom = si != null ? si.getNom() : "ID=" + id;
        dao.supprimer(id);
        auditDAO.enregistrerOld("SUPPRESSION", "SYSTEME_INTERNE", id, session.getIdUtilisateur(),
            "Suppression SI interne: " + nom);
        broadcaster.diffuserNotification("Système interne supprimé: " + nom, "INFO");
    }

    public List<SystemeInterne> lister() throws SQLException { return dao.lister(); }
    public SystemeInterne trouverParId(int id) throws SQLException { return dao.trouverParId(id); }

    private void verifierDroitConsultation(Session session) {
        String role = session.getRole();
        if (!role.equals("Administrateur") && !role.equals("Agent SSICTD"))
            throw new SecurityException("Droits insuffisants");
    }
}
