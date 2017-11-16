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
    private static final int NB_POINTS_PER_PERIOD = 20;

    private UserManagment(){}

    public static class UserNotLoggedInException extends Exception{
        public UserNotLoggedInException(String str) {
            super(str);
        }
    }

    public static UserService getUserService(){
        return UserServiceFactory.getUserService();
    }

    public static Entity firstConnection() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        UserService userService = UserManagment.getUserService();

        if(!userService.isUserLoggedIn())
            throw new UserManagment.UserNotLoggedInException("Utilisateur non connecté");

        User user = userService.getCurrentUser();
        String userId = user.getUserId();

        DatastoreService datastore = DatastoreGetter.getDatastore();

        Entity entity = new Entity("UserInfo", userId);
        entity.setProperty("nextPointEarnDate", DateUtil.serializeDate(new Date()));
        entity.setProperty("nbPoints", 100);

        datastore.put(entity);
        return entity;
    }

    public static boolean earnPoints() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        if(!UserManagment.canDrawEarnPoints())
            return false;

        Entity entity = UserManagment.getUserInfos();


        Calendar c = GregorianCalendar.getInstance();
        c.add(Calendar.HOUR, 3);
        Date newDate = c.getTime();

        entity.setProperty("nbPoints", (Long) entity.getProperty("nbPoints") + UserManagment.NB_POINTS_PER_PERIOD);
        entity.setProperty("nextPointEarnDate", DateUtil.serializeDate(newDate));

        DatastoreService datastore = DatastoreGetter.getDatastore();
        datastore.put(entity);

        return true;
    }

    public static Entity getUserInfos() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
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
            return firstConnection();
        }

        return entity;
    }


    public static boolean canDrawEarnPoints() throws UserNotLoggedInException, DatastoreGetter.DataStoreNotAvailableException {
        Entity entity = getUserInfos();

        Date nextPointEarnDate = DateUtil.deserializeDate((String)entity.getProperty("nextPointEarnDate"));

        Date d = new Date();

        if(nextPointEarnDate.before(d))
            return true;

        return false;
    }

}
