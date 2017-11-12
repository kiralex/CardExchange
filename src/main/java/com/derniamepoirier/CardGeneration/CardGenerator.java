package com.derniamepoirier.CardGeneration;

import com.derniamepoirier.Utils.DatastoreGetter;
import org.json.JSONObject;

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

        Card cards[] = new Card[nbCard];

        while(nbGenerated < nbCard) {
            JSONObject obj = PixabayFetcher.fetch(query, options, nbIterations, 2*(nbCard-nbGenerated));
            Card[] cardsTemp = Card.fromPixabayRespond(obj, nbCard-nbGenerated);

            for (Card c : cardsTemp) {
                cards[nbGenerated] = c;
                c.saveToSore();

                nbGenerated++;
            }

            log.info(cardsTemp.length + " cards generated from iteration " + nbIterations + "(Total : " + nbGenerated + ")" );

            nbIterations++;
        }

        return cards;
    }
}
