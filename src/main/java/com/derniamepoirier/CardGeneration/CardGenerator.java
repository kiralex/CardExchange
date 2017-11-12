package com.derniamepoirier.CardGeneration;

import com.derniamepoirier.Utils.DatastoreGetter;
import org.json.JSONObject;

import java.util.logging.Logger;

public class CardGenerator {
    private static final Logger log = Logger.getLogger(CardGenerator.class.getName());

    private CardGenerator(){}

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
