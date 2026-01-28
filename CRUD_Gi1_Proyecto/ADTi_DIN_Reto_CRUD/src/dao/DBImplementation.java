package dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import config.MongoConnection;
import exception.OurException;
import exception.ErrorMessages;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import model.Admin;
import model.Gender;
import model.LoggedProfile;
import model.Profile;
import model.User;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Database implementation of the ModelDAO interface. This class provides the concrete implementation for all data access operations including user registration, authentication, profile management, and administrative functions. It handles database connections, SQL execution, transaction management, and error handling for the entire application data layer.
 *
 * The class implements connection pooling with timeout mechanisms and ensures proper transaction handling for atomic operations.
 *
 * @author Kevin, Alex, Victor, Ekaitz
 */
public class DBImplementation implements ModelDAO
{

    /**
     * Inserts a new user into the database with transaction support. This method performs an atomic operation that inserts user data into both the profile and user tables within a single transaction. If any part fails, the entire transaction is rolled back.
     *
     * @param user the User object containing all user data to be inserted
     * @return the generated user ID if insertion is successful, -1 otherwise
     * @throws OurException if the insertion fails due to SQL errors, constraint violations, or transaction issues
     */
    private String insert(User user) throws OurException
    {
        try {
            MongoCollection<Document> users = MongoConnection.getUsersCollection();

            Document doc = new Document()
                    .append("P_EMAIL", user.getEmail())
                    .append("P_USERNAME", user.getUsername())
                    .append("P_PASSWORD", user.getPassword())
                    .append("P_NAME", user.getName())
                    .append("P_LASTNAME", user.getLastname())
                    .append("P_TELEPHONE", user.getTelephone())
                    .append("U_GENDER", user.getGender().name())
                    .append("U_CARD", user.getCard());

            users.insertOne(doc);

            // MongoDB genera automáticamente el id
            return doc.getObjectId("_id").toHexString();

        } catch (Exception ex) {
            throw new OurException(ErrorMessages.REGISTER_USER);
        }
    }

    /**
     * Retrieves all users from the database. This method executes a query to fetch all user records with their complete profile information including personal details and preferences.
     *
     * @param collection from database to make the queries on it
     * @return an ArrayList containing all User objects from the database
     * @throws OurException if the query execution fails or data retrieval errors occur
     */
    private ArrayList<User> selectUsers(MongoCollection<Document> collection) throws OurException
    {
        ArrayList<User> users = new ArrayList<>();

        for (Document doc : collection.find(Filters.exists("U_GENDER")))
        {
            String genderValue = doc.getString("U_GENDER");
            Gender gender = genderValue != null ? Gender.valueOf(genderValue) : Gender.OTHER;

            User user = new User(
                    doc.getObjectId("_id").toHexString(),
                    doc.getString("P_EMAIL"),
                    doc.getString("P_USERNAME"),
                    doc.getString("P_PASSWORD"),
                    doc.getString("P_NAME"),
                    doc.getString("P_LASTNAME"),
                    doc.getString("P_TELEPHONE"),
                    gender,
                    doc.getString("U_CARD")
            );

            users.add(user);
        }

        return users;
    }

    /**
     * Updates an existing user's information in the database with transaction support. This method performs an atomic operation that updates user data in both the profile and user tables within a single transaction.
     *
     * @param collection from database to make the queries on it
     * @param user the User object containing updated user data
     * @return true if the update operation was successful, false otherwise
     * @throws OurException if the update fails due to SQL errors, constraint violations, or transaction issues
     */
    private boolean update(User user, MongoCollection<Document> collection) throws OurException 
    {

        Document filter = new Document("_id", new ObjectId(user.getId()));

        Document updateFields = new Document()
                .append("P_PASSWORD", user.getPassword())
                .append("P_NAME", user.getName())
                .append("P_LASTNAME", user.getLastname())
                .append("P_TELEPHONE", user.getTelephone())
                .append("U_GENDER", user.getGender().name())
                .append("U_CARD", user.getCard());

        Document update = new Document("$set", updateFields);

        UpdateResult result = collection.updateOne(filter, update);

        if (result.getMatchedCount() == 0) {
            throw new OurException(ErrorMessages.UPDATE_USER);
        }

        return result.getModifiedCount() > 0;
    }


    /**
     * Deletes a user from the database by their unique identifier. This method removes a user record from the system based on the provided user ID.
     *
     * @param collection from database to make the queries on it
     * @param userId the unique identifier of the user to be deleted
     * @return true if the deletion was successful, false if no user was found with the specified ID
     * @throws OurException if the deletion operation fails due to SQL errors or database constraints
     */
    private DeleteResult delete(MongoCollection<Document> collection, String userId) throws OurException
    {
        ObjectId objectId = new ObjectId(userId);
        
        return collection.deleteOne(Filters.eq("_id", objectId));
    }

    /**
     * Authenticates a user by verifying credentials against the database. This method checks if the provided credential (email or username) and password match an existing user record and returns the appropriate profile type (User or Admin) upon successful authentication.
     *
     * @param credential the user's email or username for identification
     * @param password the user's password for authentication
     * @return the authenticated user's Profile object (User or Admin) if credentials are valid, null otherwise
     * @throws OurException if the authentication process fails due to SQL errors or data retrieval issues
     */
    private Profile loginProfile(String credential, String password) throws OurException
    {
        try
        {
            MongoCollection<Document> users =
                    MongoConnection.getUsersCollection();
            
            Document query = new Document("$and", Arrays.asList(
                    new Document("$or", Arrays.asList(
                            new Document("P_EMAIL", credential),
                            new Document("P_USERNAME", credential)
                    )),
                    new Document("P_PASSWORD", password)
            ));
            
            Document doc = users.find(query).first();
            if (doc == null) return null;
            
            String gender = doc.getString("U_GENDER");
            String admin = doc.getString("A_CURRENT_ACCOUNT");
            
            String pId = doc.getObjectId("_id").toHexString();
            
            if (gender != null) {
                return new User(
                    pId,
                    doc.getString("P_EMAIL"),
                    doc.getString("P_USERNAME"),
                    doc.getString("P_PASSWORD"),
                    doc.getString("P_NAME"),
                    doc.getString("P_LASTNAME"),
                    doc.getString("P_TELEPHONE"),
                    Gender.valueOf(gender),
                    doc.getString("U_CARD")
                );
            } else if (admin != null) {
                return new Admin(
                    pId,
                    doc.getString("P_EMAIL"),
                    doc.getString("P_USERNAME"),
                    doc.getString("P_PASSWORD"),
                    doc.getString("P_NAME"),
                    doc.getString("P_LASTNAME"),
                    doc.getString("P_TELEPHONE"),
                    admin
                );
            }
            
            return null;
        } 
        catch (Exception ex) 
        {
            throw new OurException(ErrorMessages.LOGIN);
        }
    }

    /**
     * Checks if the provided email or username already exists in the database. This method verifies the uniqueness of user credentials during registration to prevent duplicate accounts.
     *
     * @param email the email address to check for existence
     * @param username the username to check for existence
     * @return a HashMap indicating which credentials already exist with keys "email" and "username" and boolean values
     * @throws OurException if the verification process fails due to SQL errors
     */
    private HashMap<String, Boolean> checkCredentialsExistence(String email, String username) throws OurException
    {
        HashMap<String, Boolean> exists = new HashMap<>();
        exists.put("email", false);
        exists.put("username", false);

        try {
            MongoCollection<Document> users = MongoConnection.getUsersCollection();

            // email
            if (users.find(new Document("P_EMAIL", email)).first() != null) {
                exists.put("email", true);
            }

            // username
            if (users.find(new Document("P_USERNAME", username)).first() != null) {
                exists.put("username", true);
            }

            return exists;

        } catch (Exception ex) {
            throw new OurException(ErrorMessages.VERIFY_CREDENTIALS);
        }
    }

    /**
     * Authenticates a user with the provided credentials. This method verifies user identity by checking the provided credential and password against stored user data and sets the logged-in profile upon successful authentication.
     *
     * @param credential the user's username or email address used for identification
     * @param password the user's password for authentication
     * @return the authenticated user's Profile object containing user information and access privileges, or null if authentication fails
     * @throws OurException if authentication fails due to database errors or system issues
     */
    @Override
    public Profile login(String credential, String password) throws OurException
    {
        Profile profile = loginProfile(credential, password);
        
        if (profile != null)
        {
            LoggedProfile.getInstance().setProfile(profile);
        }
        
        return profile;
    }

    /**
     * Registers a new user in the system with duplicate credential checking. This method validates credential uniqueness, creates a new user account, and returns the registered user with their identifier.
     *
     * @param user the User object containing all registration information
     * @return the registered User object with the generated ID and system-assigned values
     * @throws OurException if registration fails due to duplicate credentials, database constraints, or system errors
     */
    @Override
    public User register(User user) throws OurException {

        Map<String, Boolean> existing =
                checkCredentialsExistence(user.getEmail(), user.getUsername());

        if (existing.get("email") && existing.get("username")) {
            throw new OurException("Both email and username already exist");
        } else if (existing.get("email")) {
            throw new OurException("Email already exists");
        } else if (existing.get("username")) {
            throw new OurException("Username already exists");
        }

        String id = insert(user);

        if (id == null) {
            throw new OurException(ErrorMessages.REGISTER_USER);
        }

        user.setId(id);
        return user;
    }

    /**
     * Retrieves a list of all users from the system. This method provides access to the complete user database, typically used by administrative interfaces for user management operations.
     *
     * @return an ArrayList containing all User objects in the system
     * @throws OurException if the user retrieval operation fails due to database connectivity issues or data access errors
     */
    @Override
    public ArrayList<User> getUsers() throws OurException
    {
        try
        {
            MongoCollection<Document> collection = MongoConnection.getUsersCollection();
            ArrayList<User> users = selectUsers(collection);

            return users;
        }
        catch (OurException ex)
        {
            throw new OurException(ErrorMessages.GET_USERS);
        }
    }

    /**
     * Updates an existing user's information in the system. This method persists changes made to a user's profile data, ensuring that modifications are saved to the database.
     *
     * @param user the User object containing updated information to be saved
     * @return true if the update operation was successful, false if no changes were made or the operation did not affect any records
     * @throws OurException if the update operation fails due to validation errors, database constraints violations, or data access issues
     */
    @Override
    public boolean updateUser(User user) throws OurException 
    {
        try {
            MongoCollection<Document> collection = MongoConnection.getUsersCollection();
            return update(user, collection);
        } catch (Exception e) {
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }


    /**
     * Deletes a user from the system by their unique identifier. This method permanently removes a user record from the database based on the provided user ID.
     *
     * @param id the unique identifier of the user to be deleted
     * @return true if the deletion was successful, false if no user was found with the specified ID or the operation did not affect any records
     * @throws OurException if the deletion operation fails due to database constraints, referential integrity issues, or data access errors
     */
    @Override
    public boolean deleteUser(String id) throws OurException {
        try {
            MongoCollection<Document> collection = MongoConnection.getUsersCollection();
            
            DeleteResult resultt = delete(collection, id);

            return resultt.getDeletedCount() > 0;
        }
        catch (IllegalArgumentException ex) {
            // id no válido (no es ObjectId)
            throw new OurException(ErrorMessages.DELETE_USER);
        }
        catch (MongoException ex) {
            throw new OurException(ErrorMessages.DELETE_USER);
        }
    }
}
