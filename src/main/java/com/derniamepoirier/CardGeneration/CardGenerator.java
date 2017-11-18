package com.derniamepoirier.CardGeneration;

import com.derniamepoirier.CardGeneration.PixabayAPIExceptions.PixabayApiKeyMissingException;
import com.derniamepoirier.CardGeneration.PixabayAPIExceptions.PixabayIncorrectParameterException;
import com.derniamepoirier.CardGeneration.PixabayAPIExceptions.PixabayPageOutValidRangeException;
import com.derniamepoirier.CardGeneration.PixabayAPIExceptions.PixabayResponseCodeException;
import com.derniamepoirier.Utils.DatastoreGetter;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.Logger;

public class CardGenerator {
    private static final Logger log = Logger.getLogger(CardGenerator.class.getName());

    private CardGenerator(){}

    /**
     * Generate array of {@link Card} by using images from Pixabay API and saving them to store.
     * If the API respond images which already exists in {@link com.google.appengine.api.datastore.DatastoreService}, the method will obtain next images, as many as needed
     * @param query String containing keywords
     * @param options array of {@link com.derniamepoirier.CardGeneration.PixabayFetcher.PixabayAPIOptions}
     * @param nbCard number of {@link Card} to generate
     * @return Array of {@link Card}
     * @throws PixabayIncorrectParameterException Exception throwed if parameters are incorects
     * @throws PixabayApiKeyMissingException Exception throwed if API key is missing in environment variables
     * @throws PixabayResponseCodeException Exception throwed if Pixabay respond an error code
     * @throws DatastoreGetter.DataStoreNotAvailableException Exception throwed if {@link com.google.appengine.api.datastore.DatastoreService} is not available
     */
    public static Card[] generate(String query, PixabayFetcher.PixabayAPIOptions options[], int nbCard) throws PixabayIncorrectParameterException, PixabayApiKeyMissingException, PixabayResponseCodeException, DatastoreGetter.DataStoreNotAvailableException {

        int nbGenerated = 0;
        int nbIterations = 1;
        boolean pageOutOfRange = false;

        ArrayList<Card> cards = new ArrayList<Card>();


        while(!pageOutOfRange && nbGenerated < nbCard) {
            JSONObject obj = null;
            try {
                obj = PixabayFetcher.fetch(query, options, nbIterations, Math.min(Math.max(nbCard-nbGenerated, 3), 100));

                Card[] cardsTemp = Card.fromPixabayRespond(obj, nbCard-nbGenerated);



                for(int i = 0; i < cardsTemp.length && nbGenerated < nbCard; i++){
                    Card c = cardsTemp[i];
                    cards.add(c);

                    if(c != null) {
                        c.saveToStore();
                        Queue queue = QueueFactory.getDefaultQueue();
                        queue.add(TaskOptions.Builder.withUrl("/buildImage").param("cardId", Long.toString(c.getId())));
                    }
                    else log.warning("Card id null at iteration " + nbIterations + ". Total Generated = " + nbGenerated);

                    nbGenerated++;
                }

                nbIterations++;
            } catch (PixabayPageOutValidRangeException e) {
                pageOutOfRange = true;
            }

        }

        log.info(nbGenerated + " cards generated from iteration on " + nbIterations + " iterations." );
        return cards.toArray(new Card[cards.size()]);
    }
}
