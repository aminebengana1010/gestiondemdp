package ma.province.safi.passwordmanager.model;

public abstract class Systeme extends CompteTechnique {
    protected String url;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
