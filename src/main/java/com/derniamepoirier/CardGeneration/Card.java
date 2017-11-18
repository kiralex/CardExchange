package com.derniamepoirier.CardGeneration;


import com.derniamepoirier.Utils.DatastoreGetter;
import com.derniamepoirier.Utils.PaintImageUtils;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.*;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.code.appengine.awt.*;
import com.google.code.appengine.awt.geom.RoundRectangle2D;
import com.google.code.appengine.awt.image.BufferedImage;
import com.google.code.appengine.imageio.IIOImage;
import com.google.code.appengine.imageio.ImageIO;
import com.google.code.appengine.imageio.ImageWriteParam;
import com.google.code.appengine.imageio.ImageWriter;
import com.google.code.appengine.imageio.stream.ImageOutputStream;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;

public class Card {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;

    private static final Logger log = Logger.getLogger(Card.class.getName());

    private long id;
    private String tags[];
    private URL pixabayPageURL;

    // URL of the pixabay image
    private URL pixabayImageURL;

    // URL of the card
    private URL cardImageURL;
    private String pixabayAuthorName;
    private double probability;

    /**
     * Constructor of {@link Card}
     *
     * @param id                id of the card
     * @param tags              Tags of the card
     * @param pixabayPageURL    URL of the image page
     * @param pixabayImageURL   URL to download Pixabay image
     * @param pixabayAuthorName Author of the original image
     * @param probability       probability between 0 and 1 to draw the card
     * @param cardImageURL      url of the card image
     */
    public Card(long id, String[] tags, URL pixabayPageURL, URL pixabayImageURL, URL cardImageURL, String pixabayAuthorName, double probability) {
        this(id, tags, pixabayPageURL, pixabayImageURL, pixabayAuthorName);
        this.probability = probability;
        this.cardImageURL = cardImageURL;
    }

    /**
     * Constructor of {@link Card}
     *
     * @param id                id of the card
     * @param tags              Tags of the card
     * @param pixabayPageURL    URL of the image page
     * @param pixabayImageURL   URL to download Pixabay image
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
     * Generate array of {@link Card} from JSON Pixabay API response
     * If the card already exists in {@link DatastoreService}, the {@link Card} won't be returned
     *
     * @param response {@link JSONObject} containing Pixabay API response
     * @param nbCards  max number of cards to generate
     * @return array of {@link Card}
     * @throws DatastoreGetter.DataStoreNotAvailableException Exception throwed if {@link DatastoreService} is not available
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
        Schema schema = SchemaLoader.builder().resolutionScope("file:" + schemasPath).schemaJson(responseJSON).build().load().build();

        try {
            schema.validate(response); // thcardrows a ValidationException if this object is invalid
        } catch (ValidationException e) {
            log.severe("Pixabay api response is not valid : " + e.getMessage());
            e.getCausingExceptions().stream()
                    .map(ValidationException::getMessage)
                    .forEach(log::severe);
            return new Card[0];
        }

        JSONArray images = response.getJSONArray("hits");
        ArrayList<Card> arr = new ArrayList<Card>();

        for (int i = 0; i < images.length() && i <= nbCards; i++) {
            JSONObject obj = images.getJSONObject(i);

            int id = obj.getInt("id");

            if (!Card.existInStore(id)) {
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
     * Restore Card from an entity obtained from a Datastore query
     *
     * @param cardEntity {@link Entity} which contain informations of the card
     * @return
     */
    private static Card restoreFromEntity(Entity cardEntity) {
        if (!cardEntity.getKind().equals("Card"))
            return null;


        String tags[] = ((String) cardEntity.getProperty("tags")).split(",\\s*");
        URL pixabayPageURL = null, pixabayImageURL = null, cardImageURL = null;
        try {
            pixabayPageURL = new URL((String) cardEntity.getProperty("pixabayPageURL"));
            pixabayImageURL = new URL((String) cardEntity.getProperty("pixabayImageURL"));
            cardImageURL = new URL((String) cardEntity.getProperty("cardImageURL"));
        } catch (MalformedURLException e) { /* will not be throwed */ }
        String authorName = (String) cardEntity.getProperty("pixabayAuthorName");
        double probability = ((Double) cardEntity.getProperty("probability")).doubleValue();

        Key k = cardEntity.getKey();

        return new Card(k.getId(), tags, pixabayPageURL, pixabayImageURL, cardImageURL, authorName, probability);
    }

    /**
     * Restore {@link Card} from store with its id
     *
     * @param id id of the card
     * @return <ul>
     * <li>Return a {@link Card} instance if the id exists in Datastore</li>
     * <li>Return null if the id does not exists in Datastore</li>
     * </ul>
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

        return Card.restoreFromEntity(cardEntity);


    }

    /**
     * Restore several cards from the store
     *
     * @param nbPerPage number of {@link Card}s
     * @param page      page offset
     * @return {@link Card}s Array
     * @throws DatastoreGetter.DataStoreNotAvailableException
     */
    public static Card[] restoreMultipleFromStore(int nbPerPage, int page) throws DatastoreGetter.DataStoreNotAvailableException {
        DatastoreService datastore = DatastoreGetter.getDatastore();

        Query query = new Query("Card");
        PreparedQuery preparedQuery = datastore.prepare(query);

        if (nbPerPage <= 0)
            nbPerPage = 20;
        if (page <= 0)
            page = 1;

        List<Entity> entities;


        if (page == 0)
            entities = preparedQuery.asList(FetchOptions.Builder.withLimit(nbPerPage));
        else
            entities = preparedQuery.asList(FetchOptions.Builder.withLimit(nbPerPage).offset((nbPerPage) * (page - 1)));

        Card cards[] = new Card[entities.size()];
        int cpt = 0;
        for (Entity e : entities) {
            cards[cpt] = Card.restoreFromEntity(e);
            cpt++;
        }
        return cards;
    }

    public static int countAllCards() throws DatastoreGetter.DataStoreNotAvailableException {
        DatastoreService datastore = DatastoreGetter.getDatastore();

        Query query = new Query("Card").setKeysOnly();
        PreparedQuery preparedQuery = datastore.prepare(query);


        return preparedQuery.asList(FetchOptions.Builder.withDefaults()).size();
    }

    /**
     * Get a random {@link Card} from the store store. (Fortune Wheel method). Use their rarity to draw (tirer in French) a card
     *
     * @return random Card
     * @throws DatastoreGetter.DataStoreNotAvailableException Exception throwed if {@link DatastoreService} is not available
     */
    public static Card drawFromStore() throws DatastoreGetter.DataStoreNotAvailableException {
        DatastoreService datastore = DatastoreGetter.getDatastore();

        Query query = new Query("Card");
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> entities = preparedQuery.asList(FetchOptions.Builder.withDefaults());

        double maxProba = 0.0;
        for (Entity e : entities) {
            maxProba += 1 - (Double) e.getProperty("probability");
        }

        Random r = new Random();
        double randomValue = (maxProba) * r.nextDouble();

        double sum = 0;
        int index = 0;
        long id = -1;
        while (sum < randomValue && index < entities.size()) {
            sum += 1 - (Double) entities.get(index).getProperty("probability");
            id = entities.get(index).getKey().getId();
            index++;
        }

        if (id == -1)
            return null;
        return Card.restoreFromStore(id);

    }

    /**
     * Check if a card is already present in Datastore
     *
     * @param id of the card
     * @return <ul>
     * <li>true if the card exists</li>
     * <li>false if not</li>
     * </ul>
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
     * Getter of id
     *
     * @return id of the card
     */
    public long getId() {
        return id;
    }

    /**
     * Getter of tags
     *
     * @return Array of tags ({@link String})
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * Getter of the image page {@link URL}
     *
     * @return image page {@link URL}
     */
    public URL getPixabayPageURL() {
        return pixabayPageURL;
    }

    /**
     * Getter of the source image {@link URL}
     *
     * @return source image {@link URL}
     */
    public URL getPixabayImageURL() {
        return pixabayImageURL;
    }

    /**
     * Getter of the image author name
     *
     * @return name of the author
     */
    public String getPixabayAuthorName() {
        return pixabayAuthorName;
    }

    /**
     * Getter of the {@link Card} image URL
     *
     * @return {@link Card} image URL
     */
    public URL getCardImageURL() {
        return cardImageURL;
    }

    /**
     * Getter of the {@link Card} probability
     *
     * @return {@link Card} probability
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Save an instance of {@link Card} to Store
     *
     * @throws DatastoreGetter.DataStoreNotAvailableException error throwed if {@link DatastoreService} is not available
     */
    public void saveToStore() throws DatastoreGetter.DataStoreNotAvailableException {
        Entity pixabayImage = new Entity("Card", this.id);

        pixabayImage.setProperty("tags", String.join(", ", (this.tags)));
        pixabayImage.setProperty("pixabayPageURL", this.pixabayPageURL.toString());
        pixabayImage.setProperty("pixabayImageURL", this.pixabayImageURL.toString());
        pixabayImage.setProperty("pixabayAuthorName", this.pixabayAuthorName);
        pixabayImage.setProperty("cardImageURL", this.cardImageURL != null ? this.cardImageURL.toString() : null);
        pixabayImage.setProperty("probability", this.probability);

        DatastoreService datastore = DatastoreGetter.getDatastore();
        datastore.put(pixabayImage);
    }

    private byte[] getBytes(BufferedImage image) throws IOException {
        Iterator iter = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer = (ImageWriter) iter.next();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(bytes);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            // NOTE: Any method named [set|get]Compression.* throws UnsupportedOperationException if false
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.9f);
        }


        IIOImage iioImage = new IIOImage(image, null, null);
        writer.write(null, iioImage, param);
        ios.close();
        writer.dispose();
        return bytes.toByteArray();
    }

    public void generateCardImage() throws IOException, DatastoreGetter.DataStoreNotAvailableException {
        // Making image
        BufferedImage bfImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D cardGraphics = bfImg.createGraphics();

        cardGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        cardGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        cardGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        cardGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Color colorBgText = new Color(254, 212, 149);
        Color colorBgCard = new Color(164, 107, 20);
        Font fontText = new Font("Monospaced", Font.PLAIN, 15);

        PaintImageUtils.addImageAndStarToCard(this.id, this.pixabayImageURL, this.probability, cardGraphics, colorBgCard);

        // Add the rarity text and square
        RoundRectangle2D rect = new RoundRectangle2D.Float(25, 430, 350, 25, 12, 12);
        PaintImageUtils.addSquareAndTextIntoGraphics2D(cardGraphics, PaintImageUtils.deAccent("Rareté : " + String.format("%.2f", this.probability) + "%"), rect, colorBgText, Color.BLACK, fontText);

        // Add the tags square and text
        rect = new RoundRectangle2D.Float(25, 460, 350, 25, 12, 12);
        PaintImageUtils.addSquareAndTextIntoGraphics2D(cardGraphics, "Auteur : " + PaintImageUtils.deAccent(this.pixabayAuthorName), rect, colorBgText, Color.BLACK, fontText);

        // Add Leyenda square and text
        rect = new RoundRectangle2D.Float(25, 500, 350, 70, 12, 12);
        PaintImageUtils.addSquareAndTextIntoGraphics2D(cardGraphics, "Tags : " + PaintImageUtils.deAccentStringArray(this.tags), rect, colorBgText, Color.BLACK, fontText);

        // Add the id text
        rect = new RoundRectangle2D.Float(275, 570, 125, 30, 12, 12);
        PaintImageUtils.addSquareAndTextIntoGraphics2D(cardGraphics, "id : " + this.id, rect, colorBgCard, Color.WHITE, fontText);

//        // Add the card url
//        this.cardImageURL = new URL("https://storage.googleapis.com/cardexchangemaven.appspot.com/"+this.id+".jpg");

        GcsFileOptions opt = new GcsFileOptions.Builder().mimeType("image/jpeg").acl("public-read").build();
        GcsService service = GcsServiceFactory.createGcsService();
        GcsFilename name = new GcsFilename("cardexchangemaven.appspot.com", this.id + ".jpg");
        Image image = ImagesServiceFactory.makeImage(this.getBytes(bfImg));

        ByteBuffer buff = ByteBuffer.wrap(image.getImageData());
        service.createOrReplace(name, opt, buff);

        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            this.cardImageURL = new URL("https://storage.googleapis.com/" + name.getBucketName() + "/" + name.getObjectName());
        } else {
            ImagesService imagesService = ImagesServiceFactory.getImagesService();
            this.cardImageURL = new URL(imagesService.getServingUrl(ServingUrlOptions.Builder.withGoogleStorageFileName("/gs/" + name.getBucketName() + "/" + name.getObjectName())));

        }

        this.saveToStore();
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
