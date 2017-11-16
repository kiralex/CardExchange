package com.derniamepoirier.User;

import com.derniamepoirier.CardGeneration.Card;
import com.derniamepoirier.Utils.DatastoreGetter;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;

import java.util.ArrayList;
import java.util.logging.Logger;

public class CardAssignmentHelper {
    private static final Logger log = Logger.getLogger(CardAssignmentHelper.class.getName());

    private CardAssignmentHelper(){ }

    public static class NullCardPointerException extends Exception{
        public NullCardPointerException(String str){
            super(str);
        }
    }

    public static long getInstanceNumber(Card card, Entity resEntity) throws NullCardPointerException, DatastoreGetter.DataStoreNotAvailableException {
        if(card == null)
            throw new NullCardPointerException("La carte passée en paramètre est nulle");

        UserService userService = UserManagment.getUserService();
        DatastoreService datastore = DatastoreGetter.getDatastore();

        long cardId = card.getId();
        String userId = userService.getCurrentUser().getUserId();

        ArrayList<Query.Filter> filters = new ArrayList<>();
        filters.add(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));
        filters.add(new Query.FilterPredicate("cardId", Query.FilterOperator.EQUAL, cardId));

        Query query = new Query("CardAssignment")
                .setFilter(
                        new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters));

        PreparedQuery pq = datastore.prepare(query);
        Entity entity = pq.asSingleEntity();

        try{
            resEntity = entity;
            return (Long) entity.getProperty("nbInstances");
        }catch (NullPointerException e){
            return 0;
        }
    }

    public static void assignCardInstanceToUser(Card card) throws NullCardPointerException, DatastoreGetter.DataStoreNotAvailableException {
        if(card == null)
            throw new NullCardPointerException("La carte passée en paramètre est nulle");

        UserService userService = UserManagment.getUserService();
        DatastoreService datastore = DatastoreGetter.getDatastore();

        long cardId = card.getId();
        String userId = userService.getCurrentUser().getUserId();
        long nbInstances;

        ArrayList<Query.Filter> filters = new ArrayList<>();
        filters.add(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));
        filters.add(new Query.FilterPredicate("cardId", Query.FilterOperator.EQUAL, cardId));

        Query query = new Query("CardAssignment")
                .setFilter(
                        new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters));

        PreparedQuery pq = datastore.prepare(query);
        Entity cardAssignment = pq.asSingleEntity();

        try {
            nbInstances = (Long) cardAssignment.getProperty("nbInstances");
        }catch (NullPointerException e){
            nbInstances = 0;
            cardAssignment = new Entity("CardAssignment");
        }

        cardAssignment.setProperty("userId", userId);
        cardAssignment.setProperty("cardId", cardId);
        cardAssignment.setProperty("nbInstances", nbInstances+1);
        datastore.put(cardAssignment);


    }
}
