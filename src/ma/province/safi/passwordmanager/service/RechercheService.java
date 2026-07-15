package ma.province.safi.passwordmanager.service;

import ma.province.safi.passwordmanager.dao.*;
import ma.province.safi.passwordmanager.model.*;
import ma.province.safi.passwordmanager.security.CryptoService;

import java.util.*;

public class RechercheService {

    private final ServeurDAO serveurDAO;
    private final SwitchDAO switchDAO;
    private final SystemeInterneDAO systemeInterneDAO;
    private final SystemeExterneDAO systemeExterneDAO;
    private final CryptoService cryptoService;

    public RechercheService(ServeurDAO serveurDAO, SwitchDAO switchDAO,
                            SystemeInterneDAO systemeInterneDAO, SystemeExterneDAO systemeExterneDAO,
                            CryptoService cryptoService) {
        this.serveurDAO = serveurDAO;
        this.switchDAO = switchDAO;
        this.systemeInterneDAO = systemeInterneDAO;
        this.systemeExterneDAO = systemeExterneDAO;
        this.cryptoService = cryptoService;
    }

    public Map<String, Object> rechercher(String texte, String type) throws Exception {
        Map<String, Object> resultats = new LinkedHashMap<>();

        if (type == null || type.isEmpty() || type.equals("SERVEUR")) {
            List<Serveur> list = serveurDAO.rechercher(texte);
            dechiffrerServeurs(list);
            resultats.put("serveurs", list);
        }
        if (type == null || type.isEmpty() || type.equals("SWITCH")) {
            List<SwitchReseau> list = switchDAO.rechercher(texte);
            dechiffrerSwitches(list);
            resultats.put("switches", list);
        }
        if (type == null || type.isEmpty() || type.equals("SYSTEME_INTERNE")) {
            List<SystemeInterne> list = systemeInterneDAO.rechercher(texte);
            dechiffrerSystemesInternes(list);
            resultats.put("systemesInternes", list);
        }
        if (type == null || type.isEmpty() || type.equals("SYSTEME_EXTERNE")) {
            List<SystemeExterne> list = systemeExterneDAO.rechercher(texte);
            dechiffrerSystemesExternes(list);
            resultats.put("systemesExternes", list);
        }

        return resultats;
    }

    private void dechiffrerServeurs(List<Serveur> list) {
        for (Serveur s : list) {
            try {
                if (s.getMotPasseChiffre() != null)
                    s.setMotDePasseClair(cryptoService.dechiffrer(s.getMotPasseChiffre(), s.getVecteurInitialisation()));
            } catch (Exception e) { /* ignore */ }
        }
    }

    private void dechiffrerSwitches(List<SwitchReseau> list) {
        for (SwitchReseau sw : list) {
            try {
                if (sw.getMotPasseChiffre() != null)
                    sw.setMotDePasseClair(cryptoService.dechiffrer(sw.getMotPasseChiffre(), sw.getVecteurInitialisation()));
            } catch (Exception e) { /* ignore */ }
        }
    }

    private void dechiffrerSystemesInternes(List<SystemeInterne> list) {
        for (SystemeInterne si : list) {
            try {
                if (si.getMotPasseChiffre() != null)
                    si.setMotDePasseClair(cryptoService.dechiffrer(si.getMotPasseChiffre(), si.getVecteurInitialisation()));
            } catch (Exception e) { /* ignore */ }
        }
    }

    private void dechiffrerSystemesExternes(List<SystemeExterne> list) {
        for (SystemeExterne se : list) {
            try {
                if (se.getMotPasseChiffre() != null)
                    se.setMotDePasseClair(cryptoService.dechiffrer(se.getMotPasseChiffre(), se.getVecteurInitialisation()));
            } catch (Exception e) { /* ignore */ }
        }
    }
}
