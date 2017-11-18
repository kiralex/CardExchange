package com.derniamepoirier.User;

import com.derniamepoirier.CardGeneration.Card;
import com.derniamepoirier.Utils.DatastoreGetter;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static HashMap<Card, Long> getAllCards() throws DatastoreGetter.DataStoreNotAvailableException, UserManagment.UserNotLoggedInException {
        UserService userService = UserManagment.getUserService();
        DatastoreService datastore = DatastoreGetter.getDatastore();


        if(!userService.isUserLoggedIn())
            throw new UserManagment.UserNotLoggedInException("Utilisateur non connecté");
        String userId = userService.getCurrentUser().getUserId();



        Query query = new Query("CardAssignment")
                .setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));

        PreparedQuery pq = datastore.prepare(query);
        List<Entity> cardAssignment = pq.asList(FetchOptions.Builder.withDefaults());

        HashMap<Card, Long> cards = new HashMap<Card, Long>();
        int i = 0;
        for (Entity entity: cardAssignment ) {
            long cardId = (Long) entity.getProperty("cardId");
            Long nbInstances = (Long) entity.getProperty("nbInstances");
            Card c = Card.restoreFromStore(cardId);
            cards.put(c, nbInstances);
            i++;
        }

        return cards;
    }

    public static void assignCardInstanceToUser(Card card) throws NullCardPointerException, DatastoreGetter.DataStoreNotAvailableException, UserManagment.UserNotLoggedInException {
        if(card == null)
            throw new NullCardPointerException("La carte passée en paramètre est nulle");

        UserService userService = UserManagment.getUserService();
        DatastoreService datastore = DatastoreGetter.getDatastore();


        if(!userService.isUserLoggedIn())
            throw new UserManagment.UserNotLoggedInException("Utilisateur non connecté");

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

    public static long sellCardInstance(long cardId, long nbInstancesToSell) throws DatastoreGetter.DataStoreNotAvailableException, UserManagment.UserNotLoggedInException {

        UserService userService = UserManagment.getUserService();
        DatastoreService datastore = DatastoreGetter.getDatastore();

        if(!userService.isUserLoggedIn())
            throw new UserManagment.UserNotLoggedInException("Utilisateur non connecté");

        String userId = userService.getCurrentUser().getUserId();

        ArrayList<Query.Filter> filters = new ArrayList<>();
        filters.add(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));
        filters.add(new Query.FilterPredicate("cardId", Query.FilterOperator.EQUAL, cardId));

        Query query = new Query("CardAssignment")
                .setFilter(
                        new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters));

        PreparedQuery pq = datastore.prepare(query);
        Entity cardAssignment = pq.asSingleEntity();

        long oldNumberOfInstances;

        try {
            oldNumberOfInstances = (Long) cardAssignment.getProperty("nbInstances");
        }catch (NullPointerException e){
            return 0;
        }

        long oldMinusNb = Math.min(oldNumberOfInstances, oldNumberOfInstances-nbInstancesToSell);
        long valueToStore = Math.max(0, oldMinusNb);
        long nbCardReallySelled = (oldMinusNb >=  0) ? nbInstancesToSell : -oldMinusNb;
        long nbPointsToEarn = ((long) Math.ceil(Card.restoreFromStore(cardId).getProbability()*10) )* nbCardReallySelled;


        if(valueToStore > 0 ){
            cardAssignment.setProperty("userId", userId);
            cardAssignment.setProperty("cardId", cardId);
            cardAssignment.setProperty("nbInstances", valueToStore);
            datastore.put(cardAssignment);
        }else{
            datastore.delete(cardAssignment.getKey());
        }

        UserManagment.earnPointsWithSell(nbPointsToEarn);

        return nbPointsToEarn;
    }
}
