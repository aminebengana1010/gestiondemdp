package ma.province.safi.passwordmanager.service;

import ma.province.safi.passwordmanager.dao.AuditDAO;
import ma.province.safi.passwordmanager.model.*;
import ma.province.safi.passwordmanager.session.Session;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {

    private final ServeurService serveurService;
    private final SwitchService switchService;
    private final SystemeInterneService systemeInterneService;
    private final SystemeExterneService systemeExterneService;
    private final AuditDAO auditDAO;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ExportService(ServeurService serveurService, SwitchService switchService,
                         SystemeInterneService systemeInterneService,
                         SystemeExterneService systemeExterneService, AuditDAO auditDAO) {
        this.serveurService = serveurService;
        this.switchService = switchService;
        this.systemeInterneService = systemeInterneService;
        this.systemeExterneService = systemeExterneService;
        this.auditDAO = auditDAO;
    }

    // ========== CSV ==========

    public String exporterServeursCSV(Session session) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Nom;Adresse IP;Login;Mot de passe;Dernier changement\n");
        List<Serveur> list = serveurService.lister();
        for (Serveur s : list) {
            sb.append(s.getNom()).append(";")
              .append(s.getAdresseIP() != null ? s.getAdresseIP() : "").append(";")
              .append(s.getLogin() != null ? s.getLogin() : "").append(";")
              .append(s.getMotDePasseClair() != null ? s.getMotDePasseClair() : "").append(";")
              .append(s.getDateDernierChangement() != null ? s.getDateDernierChangement().format(DTF) : "")
              .append("\n");
        }
        auditDAO.enregistrerOld("EXPORT", "SERVEUR", null, session.getIdUtilisateur(), "Export CSV serveurs");
        return sb.toString();
    }

    public String exporterSwitchesCSV(Session session) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Nom;Adresse MAC;Emplacement;Login;Mot de passe;Dernier changement\n");
        List<SwitchReseau> list = switchService.lister();
        for (SwitchReseau sw : list) {
            sb.append(sw.getNom()).append(";")
              .append(sw.getAdresseMAC() != null ? sw.getAdresseMAC() : "").append(";")
              .append(sw.getEmplacement() != null ? sw.getEmplacement() : "").append(";")
              .append(sw.getLogin() != null ? sw.getLogin() : "").append(";")
              .append(sw.getMotDePasseClair() != null ? sw.getMotDePasseClair() : "").append(";")
              .append(sw.getDateDernierChangement() != null ? sw.getDateDernierChangement().format(DTF) : "")
              .append("\n");
        }
        auditDAO.enregistrerOld("EXPORT", "SWITCH", null, session.getIdUtilisateur(), "Export CSV switches");
        return sb.toString();
    }

    public String exporterSystemesInternesCSV(Session session) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Nom;URL;Login;Mot de passe;Dernier changement\n");
        List<SystemeInterne> list = systemeInterneService.lister();
        for (SystemeInterne si : list) {
            sb.append(si.getNom()).append(";")
              .append(si.getUrl() != null ? si.getUrl() : "").append(";")
              .append(si.getLogin() != null ? si.getLogin() : "").append(";")
              .append(si.getMotDePasseClair() != null ? si.getMotDePasseClair() : "").append(";")
              .append(si.getDateDernierChangement() != null ? si.getDateDernierChangement().format(DTF) : "")
              .append("\n");
        }
        auditDAO.enregistrerOld("EXPORT", "SYSTEME_INTERNE", null, session.getIdUtilisateur(), "Export CSV systèmes internes");
        return sb.toString();
    }

    public String exporterSystemesExternesCSV(Session session) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Nom;URL;Login;Mot de passe;Dernier changement\n");
        List<SystemeExterne> list = systemeExterneService.lister();
        for (SystemeExterne se : list) {
            sb.append(se.getNom()).append(";")
              .append(se.getUrl() != null ? se.getUrl() : "").append(";")
              .append(se.getLogin() != null ? se.getLogin() : "").append(";")
              .append(se.getMotDePasseClair() != null ? se.getMotDePasseClair() : "").append(";")
              .append(se.getDateDernierChangement() != null ? se.getDateDernierChangement().format(DTF) : "")
              .append("\n");
        }
        auditDAO.enregistrerOld("EXPORT", "SYSTEME_EXTERNE", null, session.getIdUtilisateur(), "Export CSV systèmes externes");
        return sb.toString();
    }

    // ========== HTML (prêt pour impression / PDF navigateur) ==========

    public String exporterServeursHTML(Session session) throws Exception {
        List<Serveur> list = serveurService.lister();
        String rows = buildRows(list, s -> new String[]{
            s.getNom(), s.getAdresseIP(), s.getLogin(),
            s.getMotDePasseClair() != null ? s.getMotDePasseClair() : "",
            s.getDateDernierChangement() != null ? s.getDateDernierChangement().format(DTF) : ""
        });
        String html = htmlPage("Serveurs", new String[]{"Nom", "Adresse IP", "Login", "Mot de passe", "Dernier changement"}, rows);
        auditDAO.enregistrerOld("EXPORT", "SERVEUR", null, session.getIdUtilisateur(), "Export PDF serveurs");
        return html;
    }

    public String exporterSwitchesHTML(Session session) throws Exception {
        List<SwitchReseau> list = switchService.lister();
        String rows = buildRows(list, s -> new String[]{
            s.getNom(), s.getAdresseMAC(), s.getEmplacement(), s.getLogin(),
            s.getMotDePasseClair() != null ? s.getMotDePasseClair() : "",
            s.getDateDernierChangement() != null ? s.getDateDernierChangement().format(DTF) : ""
        });
        String html = htmlPage("Switches", new String[]{"Nom", "Adresse MAC", "Emplacement", "Login", "Mot de passe", "Dernier changement"}, rows);
        auditDAO.enregistrerOld("EXPORT", "SWITCH", null, session.getIdUtilisateur(), "Export PDF switches");
        return html;
    }

    public String exporterSystemesInternesHTML(Session session) throws Exception {
        List<SystemeInterne> list = systemeInterneService.lister();
        String rows = buildRows(list, si -> new String[]{
            si.getNom(), si.getUrl(), si.getLogin(),
            si.getMotDePasseClair() != null ? si.getMotDePasseClair() : "",
            si.getDateDernierChangement() != null ? si.getDateDernierChangement().format(DTF) : ""
        });
        String html = htmlPage("Systèmes Internes", new String[]{"Nom", "URL", "Login", "Mot de passe", "Dernier changement"}, rows);
        auditDAO.enregistrerOld("EXPORT", "SYSTEME_INTERNE", null, session.getIdUtilisateur(), "Export PDF systèmes internes");
        return html;
    }

    public String exporterSystemesExternesHTML(Session session) throws Exception {
        List<SystemeExterne> list = systemeExterneService.lister();
        String rows = buildRows(list, se -> new String[]{
            se.getNom(), se.getUrl(), se.getLogin(),
            se.getMotDePasseClair() != null ? se.getMotDePasseClair() : "",
            se.getDateDernierChangement() != null ? se.getDateDernierChangement().format(DTF) : ""
        });
        String html = htmlPage("Systèmes Externes", new String[]{"Nom", "URL", "Login", "Mot de passe", "Dernier changement"}, rows);
        auditDAO.enregistrerOld("EXPORT", "SYSTEME_EXTERNE", null, session.getIdUtilisateur(), "Export PDF systèmes externes");
        return html;
    }

    private String htmlPage(String titre, String[] headers, String rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"UTF-8\">")
          .append("<title>Export - ").append(titre).append("</title>")
          .append("<style>")
          .append("body{font-family:Arial,sans-serif;margin:40px;}")
          .append("h1{color:#1a237e;margin-bottom:24px;}")
          .append("table{width:100%;border-collapse:collapse;margin-top:16px;}")
          .append("th{background:#1a237e;color:#fff;padding:10px 12px;text-align:left;font-size:13px;}")
          .append("td{padding:8px 12px;border-bottom:1px solid #e0e0e0;font-size:13px;}")
          .append("tr:nth-child(even){background:#f5f5f5;}")
          .append("@media print{body{margin:20px;}th{background:#1a237e!important;color:white!important;-webkit-print-color-adjust:exact;print-color-adjust:exact;}}")
          .append("</style></head><body>")
          .append("<h1>").append(titre).append("</h1>")
          .append("<p style=\"color:#666;font-size:12px;\">Généré le ").append(java.time.LocalDateTime.now().format(DTF)).append("</p>")
          .append("<table><thead><tr>");
        for (String h : headers) sb.append("<th>").append(h).append("</th>");
        sb.append("</tr></thead><tbody>").append(rows).append("</tbody></table></body></html>");
        return sb.toString();
    }

    @FunctionalInterface
    private interface CellExtractor<T> {
        String[] extract(T item);
    }

    private <T> String buildRows(List<T> list, CellExtractor<T> extractor) {
        StringBuilder sb = new StringBuilder();
        for (T item : list) {
            sb.append("<tr>");
            for (String cell : extractor.extract(item)) {
                sb.append("<td>").append(cell != null ? cell : "").append("</td>");
            }
            sb.append("</tr>");
        }
        return sb.toString();
    }
}
