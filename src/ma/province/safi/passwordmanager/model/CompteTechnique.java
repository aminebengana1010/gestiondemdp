package ma.province.safi.passwordmanager.model;

import java.time.LocalDateTime;

public abstract class CompteTechnique implements EntiteAvecSecret {
    protected int id;
    protected String nom;
    protected String login;
    protected String motPasseChiffre;
    protected String vecteurInitialisation;
    protected LocalDateTime dateDernierChangement;
    protected String motDePasseClair; // transient, non persisté

    @Override
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @Override
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotPasseChiffre() { return motPasseChiffre; }
    public void setMotPasseChiffre(String motPasseChiffre) { this.motPasseChiffre = motPasseChiffre; }

    public String getVecteurInitialisation() { return vecteurInitialisation; }
    public void setVecteurInitialisation(String vecteurInitialisation) { this.vecteurInitialisation = vecteurInitialisation; }

    public LocalDateTime getDateDernierChangement() { return dateDernierChangement; }
    public void setDateDernierChangement(LocalDateTime dateDernierChangement) { this.dateDernierChangement = dateDernierChangement; }

    public String getMotDePasseClair() { return motDePasseClair; }
    public void setMotDePasseClair(String motDePasseClair) { this.motDePasseClair = motDePasseClair; }
}
