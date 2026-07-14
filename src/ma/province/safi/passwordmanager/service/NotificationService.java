package ma.province.safi.passwordmanager.service;

import ma.province.safi.passwordmanager.dao.NotificationDAO;
import ma.province.safi.passwordmanager.model.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO;

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public List<Notification> listerNonLues(int idUtilisateur) throws SQLException {
        return notificationDAO.listerNonLues(idUtilisateur);
    }

    public List<Notification> listerToutes(int idUtilisateur) throws SQLException {
        return notificationDAO.listerToutes(idUtilisateur);
    }

    public int compterNonLues(int idUtilisateur) throws SQLException {
        return notificationDAO.compterNonLues(idUtilisateur);
    }

    public void marquerLue(long idNotification) throws SQLException {
        notificationDAO.marquerLue(idNotification);
    }

    public void creer(int idUtilisateur, String message, String type) throws SQLException {
        notificationDAO.creer(idUtilisateur, message, type);
    }
}
