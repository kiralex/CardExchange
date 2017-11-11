package com.derniamepoirier.CardClasses;

import com.derniamepoirier.HelloAppEngine;
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
import java.util.Arrays;
import java.util.logging.Logger;


public class Card {
    private static final Logger log = Logger.getLogger(HelloAppEngine.class.getName());

    private int id;
    private String tags[];
    private URL pixabayPageURL;
    private URL pixabayImageURL;
    private String pixabayAuthorName;

    public static Card[] fromPixabayRespond(JSONObject response, int nbCards){
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
            schema.validate(response); // throws a ValidationException if this object is invalid
        }catch(ValidationException e){
            log.severe("Pixabay api response is not valid : " + e.getMessage());
            e.getCausingExceptions().stream()
                    .map(ValidationException::getMessage)
                    .forEach(log::severe);
            return new Card[0];
        }

        JSONArray images = response.getJSONArray("hits");
        Card cards[] = new Card[images.length()];

        for (int i=0; i < images.length(); i++) {
            JSONObject obj = images.getJSONObject(i);

            int id =  obj.getInt("id");
            String tags[] = obj.getString("tags").split(",\\s*");
            String authorName = obj.getString("user");
            URL pageUrl = null, imageURL = null;
            try {
                pageUrl = new URL(obj.getString("pageURL"));
                imageURL = new URL(obj.getString("webformatURL"));
            } catch (MalformedURLException e) { /* will not be catched because already verified by json schemas */ }

            cards[i] = new Card(id, tags, pageUrl, imageURL, authorName);
        }

        return cards;
    }

    public Card(int id, String[] tags, URL pixabayPageURL, URL pixabayImageURL, String pixabayAuthorName) {
        this.id = id;
        this.tags = tags;
        this.pixabayPageURL = pixabayPageURL;
        this.pixabayImageURL = pixabayImageURL;
        this.pixabayAuthorName = pixabayAuthorName;
    }

    public int getId() {
        return id;
    }

    public String[] getTags() {
        return tags;
    }

    public URL getPixabayPageURL() {
        return pixabayPageURL;
    }

    public URL getPixabayImageURL() {
        return pixabayImageURL;
    }

    public String getPixabayAuthorName() {
        return pixabayAuthorName;
    }

    public void saveToSore(){
//        Key groupKey = KeyFactory.createKey("PixabayImage", this.id);

        Entity pixabayImage = new Entity("PixabayImage", this.id);
        pixabayImage.setProperty("tags", new JSONArray(this.tags).toString());
        pixabayImage.setProperty("pixabayPageURL", this.pixabayPageURL.toString());
        pixabayImage.setProperty("pixabayImageURL", this.pixabayImageURL.toString());
        pixabayImage.setProperty("pixabayAuthorName", this.pixabayAuthorName);
        pixabayImage.setProperty("cardURL", null);

        DatastoreService datastore =
                DatastoreServiceFactory.getDatastoreService();
        datastore.put(pixabayImage);
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
        int result = id;
        result = 31 * result + Arrays.hashCode(tags);
        result = 31 * result + pixabayPageURL.hashCode();
        result = 31 * result + pixabayImageURL.hashCode();
        result = 31 * result + pixabayAuthorName.hashCode();
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
