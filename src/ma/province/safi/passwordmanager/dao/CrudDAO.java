package ma.province.safi.passwordmanager.dao;

import java.sql.SQLException;
import java.util.List;

public interface CrudDAO<T> {
    void ajouter(T t) throws SQLException;
    void modifier(T t) throws SQLException;
    void supprimer(int id) throws SQLException;
    T trouverParId(int id) throws SQLException;
    List<T> lister() throws SQLException;
}
