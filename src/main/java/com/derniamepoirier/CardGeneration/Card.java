package com.derniamepoirier.CardGeneration;

import com.derniamepoirier.Utils.DatastoreGetter;
import com.google.appengine.api.datastore.*;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;


public class Card {
    private static final Logger log = Logger.getLogger(Card.class.getName());

    private long id;
    private String tags[];
    private URL pixabayPageURL;
    private URL pixabayImageURL;
    private URL cardImageURL;
    private String pixabayAuthorName;
    private double probability;

    /**
     * Generate array of {@link Card} from JSON Pixabay API response
     * If the card already exists in {@link DatastoreService}, the {@link Card} won't be returned
     * @param response {@link JSONObject} containing Pixabay API response
     * @param nbCards max number of cards to generate
     * @throws DatastoreGetter.DataStoreNotAvailableException Exception throwed if {@link DatastoreService} is not available
     * @return array of {@link Card}
     */
    public static Card[] fromPixabayRespond(JSONObject response, int nbCards) throws DatastoreGetter.DataStoreNotAvailableException {
        String schemasPath = System.getProperty("user.dir") + "/schemas/";
        InputStream responseIS = null;
        InputStream imageIS = null;

        try {
            responseIS = new FileInputStream(new File(schemasPath + "PixabayResponse.json"));
            imageIS = new FileInputStream(new File(schemasPath + "PixabayImage.json"));
        } catch (FileNotFoundException e) {
            log.severe("schema " + schemasPath + " not found");
            return new Card[0];
        }

        JSONObject responseJSON = new JSONObject(new JSONTokener(responseIS));
        Schema schema = SchemaLoader.builder().resolutionScope("file:"+schemasPath).schemaJson(responseJSON).build().load().build();

        try {
            schema.validate(response); // thcardrows a ValidationException if this object is invalid
        }catch(ValidationException e){
            log.severe("Pixabay api response is not valid : " + e.getMessage());
            e.getCausingExceptions().stream()
                    .map(ValidationException::getMessage)
                    .forEach(log::severe);
            return new Card[0];
        }

        JSONArray images = response.getJSONArray("hits");
        ArrayList<Card> arr = new ArrayList<Card>();

        for (int i=0; i < images.length(); i++) {
            JSONObject obj = images.getJSONObject(i);

            int id =  obj.getInt("id");

            if(!Card.existInStore(id)){
                String tags[] = obj.getString("tags").split(",\\s*");
                String authorName = obj.getString("user");
                URL pageUrl = null, imageURL = null;
                try {
                    pageUrl = new URL(obj.getString("pageURL"));
                    imageURL = new URL(obj.getString("webformatURL"));
                } catch (MalformedURLException e) { /* will not be catched because already verified by json schemas */ }

                arr.add(new Card(id, tags, pageUrl, imageURL, authorName));
            }
        }

        return arr.toArray(new Card[arr.size()]);
    }

    /**
     * Restore {@link Card} from store with its id
     * @param id id of the card
     * @return <ul>
     *           <li>Return a {@link Card} instance if the id exists in Datastore</li>
     *           <li>Return null if the id does not exists in Datastore</li>
     *          </ul>
     * @throws DatastoreGetter.DataStoreNotAvailableException throwed if the {@link DatastoreService} is not available
     */
    public static Card restoreFromStore(long id) throws DatastoreGetter.DataStoreNotAvailableException {
        DatastoreService service = DatastoreGetter.getDatastore();

        Key k = KeyFactory.createKey("Card", id);
        Entity cardEntity;
        try {
            cardEntity = service.get(k);
        } catch (EntityNotFoundException e) {
            return null;
        }

        String tags[] = ((String) cardEntity.getProperty("tags")).split(",\\s*");
        URL pixabayPageURL = null, pixabayImageURL = null, cardImageURL = null;
        try {
            pixabayPageURL = new URL((String) cardEntity.getProperty("pixabayPageURL"));
            pixabayImageURL = new URL((String) cardEntity.getProperty("pixabayImageURL"));
            cardImageURL = new URL((String) cardEntity.getProperty("cardImageURL"));
        } catch (MalformedURLException e) { /* will not be throwed */ }
        String authorName = (String) cardEntity.getProperty("pixabayAuthorName");
        double probability = ((Double) cardEntity.getProperty("probability")).doubleValue();


        return new Card(id, tags, pixabayPageURL, pixabayImageURL, cardImageURL, authorName, probability);
    }

    /**
     * Get a random {@link Card} from the store store. (Fortune Wheel method). Use their rarity to draw (tirer in French) a card
     * @return random Card
     * @throws DatastoreGetter.DataStoreNotAvailableException Exception throwed if {@link DatastoreService} is not available
     */
    public static Card drawFromStore() throws DatastoreGetter.DataStoreNotAvailableException {
        DatastoreService datastore = DatastoreGetter.getDatastore();

        Query query = new Query("Card");
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> entities = preparedQuery.asList(FetchOptions.Builder.withDefaults());

        log.info("nb entities : " + entities.size());

        double maxProba = 0.0;
        for(Entity e : entities){
            maxProba += ((Double) e.getProperty("probability")).doubleValue();
        }

        Random r = new Random();
        double randomValue = (maxProba) * r.nextDouble();

        double sum = 0;
        int index = 0;
        long id = -1;
        while(sum < randomValue){
            sum += ((Double) entities.get(index).getProperty("probability")).doubleValue();
            index++;
            id = entities.get(index).getKey().getId();
        }

        if(id == -1)
            return null;
        return Card.restoreFromStore(id);

    }

    /**
     * Check if a card is already present in Datastore
     * @param id of the card
     * @return  <ul>
     *              <li>true if the card exists</li>
     *              <li>false if not</li>
     *          </ul>
     *
     * @throws DatastoreGetter.DataStoreNotAvailableException Exception throwed if {@link DatastoreService} is not available
     */
    public static boolean existInStore(long id) throws DatastoreGetter.DataStoreNotAvailableException {
        DatastoreService service = DatastoreGetter.getDatastore();

        Key k = KeyFactory.createKey("Card", id);
        Entity cardEntity;
        try {
            cardEntity = service.get(k);
        } catch (EntityNotFoundException e) {
            return false;
        }

        return true;
    }

    /**
     * Constructor of {@link Card}
     * @param id id of the card
     * @param tags Tags of the card
     * @param pixabayPageURL URL of the image page
     * @param pixabayImageURL URL to download Pixabay image
     * @param pixabayAuthorName Author of the original image
     * @param probability probability between 0 and 1 to draw the card
     * @param cardImageURL url of the card image
     */
    public Card(long id, String[] tags, URL pixabayPageURL, URL pixabayImageURL, URL cardImageURL, String pixabayAuthorName, double probability) {
        this(id, tags, pixabayPageURL, pixabayImageURL, pixabayAuthorName);
        this.probability = probability;
        this.cardImageURL = cardImageURL;
    }

    /**
     * Constructor of {@link Card}
     * @param id id of the card
     * @param tags Tags of the card
     * @param pixabayPageURL URL of the image page
     * @param pixabayImageURL URL to download Pixabay image
     * @param pixabayAuthorName Author of the original image
     */
    private Card(long id, String[] tags, URL pixabayPageURL, URL pixabayImageURL, String pixabayAuthorName) {
        this.id = id;
        this.tags = tags;
        this.pixabayPageURL = pixabayPageURL;
        this.pixabayImageURL = pixabayImageURL;
        this.pixabayAuthorName = pixabayAuthorName;
        this.probability = Math.random();
    }

    /**
     * Getter of id
     * @return id of the card
     */
    public long getId() {
        return id;
    }

    /**
     * Getter of tags
     * @return Array of tags ({@link String})
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * Getter of the image page {@link URL}
     * @return image page {@link URL}
     */
    public URL getPixabayPageURL() {
        return pixabayPageURL;
    }

    /**
     * Getter of the source image {@link URL}
     * @return source image {@link URL}
     */
    public URL getPixabayImageURL() {
        return pixabayImageURL;
    }

    /**
     * Getter of the image author name
     * @return name of the author
     */
    public String getPixabayAuthorName() {
        return pixabayAuthorName;
    }

    /**
     * Getter of the {@link Card} image URL
     * @return {@link Card} image URL
     */
    public URL getCardImageURL() { return cardImageURL; }

    /**
     * Getter of the {@link Card} probability
     * @return {@link Card} probability
     */
    public double getProbability() { return probability; }

    /**
     * Save an instance of {@link Card} to Store
     * @throws DatastoreGetter.DataStoreNotAvailableException error throwed if {@link DatastoreService} is not available
     */
    public void saveToSore() throws DatastoreGetter.DataStoreNotAvailableException {
        Entity pixabayImage = new Entity("Card", this.id);
        pixabayImage.setProperty("tags", new JSONArray(this.tags).join(","));
        pixabayImage.setProperty("pixabayPageURL", this.pixabayPageURL.toString());
        pixabayImage.setProperty("pixabayImageURL", this.pixabayImageURL.toString());
        pixabayImage.setProperty("pixabayAuthorName", this.pixabayAuthorName);
        pixabayImage.setProperty("cardImageURL", this.cardImageURL);
        pixabayImage.setProperty("probability", this.probability);

        DatastoreService datastore = DatastoreGetter.getDatastore();
        datastore.put(pixabayImage);
    }

    // TODO : generate Card image with Graphics2D
    public void generateCardImage(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;

        Card card = (Card) o;

        if (id != card.id) return false;
        if (pixabayAuthorName != card.pixabayAuthorName) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(tags, card.tags)) return false;
        if (!pixabayPageURL.equals(card.pixabayPageURL)) return false;
        return pixabayImageURL.equals(card.pixabayImageURL);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + Arrays.hashCode(tags);
        result = 31 * result + pixabayPageURL.hashCode();
        result = 31 * result + pixabayImageURL.hashCode();
        result = 31 * result + cardImageURL.hashCode();
        result = 31 * result + pixabayAuthorName.hashCode();
        temp = Double.doubleToLongBits(probability);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", tags=" + Arrays.toString(tags) +
                ", pixabayPageURL=" + pixabayPageURL +
                ", pixabayImageURL=" + pixabayImageURL +
                ", pixabayAuthorName='" + pixabayAuthorName + '\'' +
                '}';
    }
}
