package service;

import dao.AuthTokenDao;
import dao.DataAccessException;
import dao.Database;
import dao.UserDao;
import model.AuthToken;
import model.User;
import service.requests.LoginRequest;
import service.results.LoginResult;

import java.util.UUID;

/**
 * A service object for the login API.
 */
public class LoginService {

    /**
     * Create a LoginService object.
     */
    public LoginService() {}

    /**
     * Log a User into the database.
     *
     * @param r a LoginRequest object.
     * @return a LoginResult object.
     */
    public LoginResult login(LoginRequest r) {
        Database db = new Database();
        try {
            db.openConnection();
            // Find username in database
            UserDao uDao = new UserDao(db.getConnection());
            String username = r.getUsername();
            User user = uDao.find(username);

            // Confirm username is registered
            if (user != null) {
                // Confirm password is valid
                if (r.getPassword().equals(user.getPassword())) {
                    // Generate AuthToken and store in database
                    String tokenString = UUID.randomUUID().toString();
                    AuthToken authToken = new AuthToken(tokenString, username);
                    new AuthTokenDao(db.getConnection()).insert(authToken);

                    // Get Person ID associated with this user and then close connection to Database
                    String personID = user.getPersonID();
                    db.closeConnection(true);

                    return new LoginResult(tokenString, username, personID, null, true);
                } else {
                    throw new DataAccessException("Invalid Password.");
                }
            } else {
                throw new DataAccessException("Username is not registered.");
            }
        } catch (DataAccessException ex) {
            ex.printStackTrace();
            try {
                db.closeConnection(false);
            } catch (DataAccessException e) {
                e.printStackTrace();
            }
            return new LoginResult(ex.getMessage(), false);
        }
    }
}
