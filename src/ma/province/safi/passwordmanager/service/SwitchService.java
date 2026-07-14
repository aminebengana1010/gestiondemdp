package ma.province.safi.passwordmanager.service;

import ma.province.safi.passwordmanager.dao.AuditDAO;
import ma.province.safi.passwordmanager.dao.NotificationDAO;
import ma.province.safi.passwordmanager.dao.SwitchDAO;
import ma.province.safi.passwordmanager.model.SwitchReseau;
import ma.province.safi.passwordmanager.security.CryptoService;
import ma.province.safi.passwordmanager.session.Session;

import java.sql.SQLException;
import java.util.List;

public class SwitchService {

    private final SwitchDAO switchDAO;
    private final AuditDAO auditDAO;
    private final NotificationDAO notificationDAO;
    private final CryptoService cryptoService;
    private final NotificationBroadcaster broadcaster = NotificationBroadcaster.getInstance();

    public SwitchService(SwitchDAO switchDAO, AuditDAO auditDAO,
                         NotificationDAO notificationDAO, CryptoService cryptoService) {
        this.switchDAO = switchDAO;
        this.auditDAO = auditDAO;
        this.notificationDAO = notificationDAO;
        this.cryptoService = cryptoService;
    }

    public void ajouter(SwitchReseau sw, String motDePasseClair, Session session) throws Exception {
        CryptoService.ChiffrementResultat cr = cryptoService.chiffrer(motDePasseClair);
        switchDAO.ajouter(sw, cr.secretChiffre(), cr.iv());
        auditDAO.enregistrerOld("AJOUT", "SWITCH", sw.getId(), session.getIdUtilisateur(),
            "Ajout du switch: " + sw.getNom());
        notificationDAO.creer(session.getIdUtilisateur(),
            "Nouveau switch ajouté: " + sw.getNom(), "INFORMATION");
        broadcaster.diffuserNotification("Nouveau switch ajouté: " + sw.getNom(), "INFO");
    }

    public void modifier(SwitchReseau sw, Session session) throws Exception {
        switchDAO.modifier(sw);
        auditDAO.enregistrerOld("MODIFICATION", "SWITCH", sw.getId(), session.getIdUtilisateur(),
            "Modification du switch: " + sw.getNom());
        broadcaster.diffuserNotification("Switch modifié: " + sw.getNom() + " (par " + session.getNomUtilisateur() + ")", "INFO");
    }

    public void modifierMotDePasse(int id, String nouveauMotDePasse, Session session) throws Exception {
        if (nouveauMotDePasse.length() < 8)
            throw new IllegalArgumentException("Minimum 8 caractères");
        CryptoService.ChiffrementResultat cr = cryptoService.chiffrer(nouveauMotDePasse);
        switchDAO.mettreAJourSecret(id, cr.secretChiffre(), cr.iv());
        auditDAO.enregistrerOld("MODIFICATION", "SWITCH", id, session.getIdUtilisateur(),
            "Changement mot de passe du switch ID=" + id);
        notificationDAO.creer(session.getIdUtilisateur(),
            "Mot de passe modifié pour le switch ID=" + id, "CHANGEMENT_MDP");
        broadcaster.diffuserNotification("🔒 Mot de passe changé - Switch ID=" + id, "SECURITE");
    }

    public String consulterSecret(int id, Session session) throws Exception {
        verifierDroitConsultation(session);
        SwitchReseau sw = switchDAO.trouverParId(id);
        if (sw == null) throw new IllegalArgumentException("Switch introuvable");
        if (sw.getMotPasseChiffre() == null) return null;
        auditDAO.enregistrerOld("CONSULTATION", "SWITCH", id, session.getIdUtilisateur(),
            "Consultation mot de passe switch: " + sw.getNom());
        broadcaster.diffuserNotification("🔑 Secret consulté - Switch: " + sw.getNom() + " par " + session.getNomUtilisateur(), "AUDIT");
        return cryptoService.dechiffrer(sw.getMotPasseChiffre(), sw.getVecteurInitialisation());
    }

    public void supprimer(int id, Session session) throws Exception {
        SwitchReseau sw = switchDAO.trouverParId(id);
        String nom = sw != null ? sw.getNom() : "ID=" + id;
        switchDAO.supprimer(id);
        auditDAO.enregistrerOld("SUPPRESSION", "SWITCH", id, session.getIdUtilisateur(),
            "Suppression du switch: " + nom);
        broadcaster.diffuserNotification("Switch supprimé: " + nom, "INFO");
    }

    public List<SwitchReseau> lister() throws SQLException { return switchDAO.lister(); }
    public SwitchReseau trouverParId(int id) throws SQLException { return switchDAO.trouverParId(id); }
    public List<SwitchReseau> rechercher(String texte) throws SQLException { return switchDAO.rechercher(texte); }

    private void verifierDroitConsultation(Session session) {
        String role = session.getRole();
        if (!role.equals("Administrateur") && !role.equals("Agent SSICTD"))
            throw new SecurityException("Droits insuffisants");
    }
}
