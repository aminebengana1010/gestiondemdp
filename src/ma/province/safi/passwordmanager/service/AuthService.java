package ma.province.safi.passwordmanager.service;

import ma.province.safi.passwordmanager.dao.AuditDAO;
import ma.province.safi.passwordmanager.dao.UtilisateurDAO;
import ma.province.safi.passwordmanager.model.Utilisateur;
import ma.province.safi.passwordmanager.security.PasswordHasher;
import ma.province.safi.passwordmanager.session.Session;
import ma.province.safi.passwordmanager.session.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class AuthService {

    private final UtilisateurDAO utilisateurDAO;
    private final AuditDAO auditDAO;
    private final SessionManager sessionManager;

    public AuthService(UtilisateurDAO utilisateurDAO, AuditDAO auditDAO, SessionManager sessionManager) {
        this.utilisateurDAO = utilisateurDAO;
        this.auditDAO = auditDAO;
        this.sessionManager = sessionManager;
    }

    public Session authentifier(String login, String motDePasse) throws Exception {
        Utilisateur utilisateur = utilisateurDAO.trouverParLogin(login);

        if (utilisateur == null) {
            throw new SecurityException("Identifiants incorrects");
        }

        if (!utilisateur.isActif()) {
            throw new SecurityException("Compte désactivé");
        }

        boolean valide = PasswordHasher.verifier(motDePasse,
            utilisateur.getMotPasseHash(), utilisateur.getSelMotPasse());

        if (!valide) {
            throw new SecurityException("Identifiants incorrects");
        }

        String token = sessionManager.creerSession(
            utilisateur.getIdUtilisateur(),
            utilisateur.getNom(),
            utilisateur.getRoleName()
        );

        auditDAO.enregistrerOld("CONNEXION", "UTILISATEUR", utilisateur.getIdUtilisateur(),
            utilisateur.getIdUtilisateur(), "Connexion de " + utilisateur.getLogin());

        return sessionManager.obtenirSession(token);
    }

    public void deconnecter(String token) {
        sessionManager.detruireSession(token);
    }

    public Session verifierSession(String token) {
        return sessionManager.obtenirSession(token);
    }

    public void creerPremierAdministrateur() throws Exception {
        if (utilisateurDAO.lister().isEmpty()) {
            PasswordHasher.HashResult hr = PasswordHasher.hacher("admin123");
            Utilisateur u = new Utilisateur();
            u.setNom("Administrateur");
            u.setEmail("admin@safi.ma");
            u.setLogin("admin");
            u.setMotPasseHash(hr.hash());
            u.setSelMotPasse(hr.sel());
            u.setIdRole(1); // Administrateur
            u.setEstActif(true);
            utilisateurDAO.ajouter(u);
            System.out.println("[INIT] Administrateur créé: admin / admin123");
        }
    }

    public List<Utilisateur> listerUtilisateurs() throws SQLException {
        return utilisateurDAO.lister();
    }
}
