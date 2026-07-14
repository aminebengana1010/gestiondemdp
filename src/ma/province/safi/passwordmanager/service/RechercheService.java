package ma.province.safi.passwordmanager.service;

import ma.province.safi.passwordmanager.dao.*;
import ma.province.safi.passwordmanager.model.*;

import java.util.*;

public class RechercheService {

    private final ServeurDAO serveurDAO;
    private final SwitchDAO switchDAO;
    private final SystemeInterneDAO systemeInterneDAO;
    private final SystemeExterneDAO systemeExterneDAO;

    public RechercheService(ServeurDAO serveurDAO, SwitchDAO switchDAO,
                            SystemeInterneDAO systemeInterneDAO, SystemeExterneDAO systemeExterneDAO) {
        this.serveurDAO = serveurDAO;
        this.switchDAO = switchDAO;
        this.systemeInterneDAO = systemeInterneDAO;
        this.systemeExterneDAO = systemeExterneDAO;
    }

    public Map<String, Object> rechercher(String texte, String type) throws Exception {
        Map<String, Object> resultats = new LinkedHashMap<>();

        if (type == null || type.isEmpty() || type.equals("SERVEUR")) {
            resultats.put("serveurs", serveurDAO.rechercher(texte));
        }
        if (type == null || type.isEmpty() || type.equals("SWITCH")) {
            resultats.put("switches", switchDAO.rechercher(texte));
        }
        if (type == null || type.isEmpty() || type.equals("SYSTEME_INTERNE")) {
            resultats.put("systemesInternes", systemeInterneDAO.rechercher(texte));
        }
        if (type == null || type.isEmpty() || type.equals("SYSTEME_EXTERNE")) {
            resultats.put("systemesExternes", systemeExterneDAO.rechercher(texte));
        }

        return resultats;
    }
}
