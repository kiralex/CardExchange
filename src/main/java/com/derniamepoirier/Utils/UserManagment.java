package com.derniamepoirier.Utils;

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

    public static class UserNotLoggedInException extends Exception{
        public UserNotLoggedInException(String str) {
            super(str);
        }
    }

    public static class NoPointsToEarnException extends Exception{
        public NoPointsToEarnException(String str) {
            super(str);
        }
    }

    public static class NotEnoughPointsToSpendException extends Exception{
        public NotEnoughPointsToSpendException(String str) {
            super(str);
        }
    }


    public static UserService getUserService(){
        return UserServiceFactory.getUserService();
    }

    private static Entity intializeUser() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        UserService userService = UserManagment.getUserService();

        if(!userService.isUserLoggedIn())
            throw new UserManagment.UserNotLoggedInException("Utilisateur non connecté");

        User user = userService.getCurrentUser();
        String userId = user.getUserId();

        DatastoreService datastore = DatastoreGetter.getDatastore();

        Entity entity = new Entity("UserInfo", userId);
        entity.setProperty("nextPointEarnDate", DateUtil.serializeDate(new Date()));
        entity.setProperty("nbPoints", 100l);

        datastore.put(entity);
        return entity;
    }

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

    public static long getNbPoints() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        return (Long) getUserInfos().getProperty("nbPoints");
    }

    public static Date getNextPointEarnDate() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        return   DateUtil.deserializeDate((String)getUserInfos().getProperty("nextPointEarnDate"));
    }


    public static boolean canEarnPoints() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        Entity entity = getUserInfos();

        Date nextPointEarnDate = DateUtil.deserializeDate((String)entity.getProperty("nextPointEarnDate"));

        Date d = new Date();

        if(nextPointEarnDate.before(d))
            return true;

        return false;
    }

}
