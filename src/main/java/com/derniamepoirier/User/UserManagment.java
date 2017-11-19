package com.derniamepoirier.User;

import com.derniamepoirier.Utils.DatastoreGetter;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.search.DateUtil;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

public class UserManagment {
    private static final Logger log = Logger.getLogger(UserManagment.class.getName());
    public static final int NB_POINTS_PER_PERIOD = 30;
    public static final int NB_POINTS_PER_CARD = 10;

    private UserManagment(){}

    /**
     * Error throwed when user is not connected while it should do
     */
    public static class UserNotLoggedInException extends Exception{
        public UserNotLoggedInException(String str) {
            super(str);
        }
    }

    /**
     * Error throwed when calling earn points method while user have to wait
     */
    public static class NoPointsToEarnException extends Exception{
        public NoPointsToEarnException(String str) {
            super(str);
        }
    }

    /**
     * Error throwed when calling spend points methods while user can not
     */
    public static class NotEnoughPointsToSpendException extends Exception{
        public NotEnoughPointsToSpendException(String str) {
            super(str);
        }
    }

    /**
     * Get {@link UserService} from Google APIs
     * @return
     */
    public static UserService getUserService(){
        return UserServiceFactory.getUserService();
    }

    /**
     * Initialize user with an initial number of points (10) and let him draw a card immediatly
     * @return the created entity
     * @throws UserNotLoggedInException Error throwed when user is not connected
     * @throws DatastoreGetter.DataStoreNotAvailableException Error throwed when {@link com.google.api.client.util.store.DataStore} is not available
     */
    private static Entity intializeUser() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        UserService userService = UserManagment.getUserService();

        if(!userService.isUserLoggedIn())
            throw new UserManagment.UserNotLoggedInException("Utilisateur non connecté");

        User user = userService.getCurrentUser();
        String userId = user.getUserId();

        DatastoreService datastore = DatastoreGetter.getDatastore();

        Entity entity = new Entity("UserInfo", userId);
        entity.setProperty("nextPointEarnDate", DateUtil.serializeDate(new Date()));
        entity.setProperty("nbPoints", 300l);

        datastore.put(entity);
        return entity;
    }

    /**
     * Method to make a user earn points if he does not have to wait
     * @throws UserNotLoggedInException Error throwed when user is not connected
     * @throws DatastoreGetter.DataStoreNotAvailableException Error throwed when {@link com.google.api.client.util.store.DataStore} is not available
     * @throws NoPointsToEarnException Error throwed when user have to wait to earn points
     */
    public static void earnPoints() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException, NoPointsToEarnException {
        if(!UserManagment.canEarnPoints())
            throw new NoPointsToEarnException("Vous devez attendre pour récupérer des points");

        Entity entity = UserManagment.getUserInfos();

        Calendar c = GregorianCalendar.getInstance();
        c.add(Calendar.HOUR, 3);
        Date newDate = c.getTime();

        entity.setProperty("nbPoints", (Long) entity.getProperty("nbPoints") + UserManagment.NB_POINTS_PER_PERIOD);
        entity.setProperty("nextPointEarnDate", DateUtil.serializeDate(newDate));

        DatastoreService datastore = DatastoreGetter.getDatastore();
        datastore.put(entity);
    }

    /**
     * Method which increment user number of points
     * @throws UserNotLoggedInException Error throwed when user is not connected
     * @throws DatastoreGetter.DataStoreNotAvailableException Error throwed when {@link com.google.api.client.util.store.DataStore} is not available
     */
    public static void earnPointsWithSell(long nbPoints) throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException{
        Entity entity = UserManagment.getUserInfos();

        entity.setProperty("nbPoints", (Long) entity.getProperty("nbPoints") + nbPoints);

        DatastoreService datastore = DatastoreGetter.getDatastore();
        datastore.put(entity);
    }

    /**
     * Method to make a user buy cards if he have a sufficient number of points
     * @throws UserNotLoggedInException Error throwed when user is not connected
     * @throws DatastoreGetter.DataStoreNotAvailableException Error throwed when {@link com.google.api.client.util.store.DataStore} is not available
     * @throws NotEnoughPointsToSpendException Error throwed when user does not have enough points to buy cards
     */
    public static void spendPoints() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException, NotEnoughPointsToSpendException {
        Entity entity = UserManagment.getUserInfos();
        DatastoreService datastore = DatastoreGetter.getDatastore();

        long oldPointsTotal = (Long) entity.getProperty("nbPoints");
        if(oldPointsTotal < UserManagment.NB_POINTS_PER_CARD)
            throw new NotEnoughPointsToSpendException("Pas assez de points pour acheter une carte");

        long newPointsTotal = oldPointsTotal - UserManagment.NB_POINTS_PER_CARD;
        entity.setProperty("nbPoints", newPointsTotal);
        datastore.put(entity);

    }

    /**
     * Get {@link Entity} of the current user
     * If the user was not initialized, it will before returning its entity
     * @return Entity containing informations about the user (nb of points, delay to wait)
     * @throws UserNotLoggedInException Error throwed when user is not connected
     * @throws DatastoreGetter.DataStoreNotAvailableException Error throwed when {@link com.google.api.client.util.store.DataStore} is not available
     */
    private static Entity getUserInfos() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        UserService userService = UserManagment.getUserService();

        if(!userService.isUserLoggedIn())
            throw new UserManagment.UserNotLoggedInException("Utilisateur non connecté");


        User user = userService.getCurrentUser();
        String userId = user.getUserId();

        DatastoreService datastore = DatastoreGetter.getDatastore();

        Key key = KeyFactory.createKey("UserInfo", userId);
        Entity entity;

        try {
            entity = datastore.get(key);
        } catch (EntityNotFoundException e) {
            return intializeUser();
        }

        return entity;
    }

    /**
     * Get number of points of the current user
     * @return number of points of the current user
     * @throws UserNotLoggedInException Error throwed when user is not connected
     * @throws DatastoreGetter.DataStoreNotAvailableException Error throwed when {@link com.google.api.client.util.store.DataStore} is not available
     */
    public static long getNbPoints() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        return (Long) getUserInfos().getProperty("nbPoints");
    }

    /**
     * Get the next {@link Date} when the user will be able to earn points
     * @return the next date when the user will be able to earn points
     * @throws UserNotLoggedInException Error throwed when user is not connected
     * @throws DatastoreGetter.DataStoreNotAvailableException Error throwed when {@link com.google.api.client.util.store.DataStore} is not available
     */
    public static Date getNextPointEarnDate() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        return   DateUtil.deserializeDate((String)getUserInfos().getProperty("nextPointEarnDate"));
    }


    /**
     * Determine if the user can earn points
     * @return boolean
     * @throws UserNotLoggedInException Error throwed when user is not connected
     * @throws DatastoreGetter.DataStoreNotAvailableException Error throwed when {@link com.google.api.client.util.store.DataStore} is not available
     */
    public static boolean canEarnPoints() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        Entity entity = getUserInfos();

        Date nextPointEarnDate = DateUtil.deserializeDate((String)entity.getProperty("nextPointEarnDate"));

        Date d = new Date();

        if(nextPointEarnDate.before(d))
            return true;

        return false;
    }

}
