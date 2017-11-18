package com.derniamepoirier.CardGeneration;


import com.derniamepoirier.Utils.DatastoreGetter;
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

        for (int i=0; i < images.length() && i <= nbCards; i++) {
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
     * @param cardEntity {@link Entity} which contain informations of the card
     * @return
     */
    private static Card restoreFromEntity(Entity cardEntity){
        if(!cardEntity.getKind().equals("Card"))
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

        return Card.restoreFromEntity(cardEntity);


    }

    /**
     * Restore several cards from the store
     * @param nbPerPage number of {@link Card}s
     * @param page page offset
     * @return {@link Card}s Array
     * @throws DatastoreGetter.DataStoreNotAvailableException
     */
    public static Card[] restoreMultipleFromStore(int nbPerPage, int page) throws DatastoreGetter.DataStoreNotAvailableException {
        DatastoreService datastore = DatastoreGetter.getDatastore();

        Query query = new Query("Card");
        PreparedQuery preparedQuery = datastore.prepare(query);

        if(nbPerPage <= 0 )
            nbPerPage = 20;
        if(page <= 0)
            page = 1;

        List<Entity> entities;


        if(page == 0)
            entities = preparedQuery.asList(FetchOptions.Builder.withLimit(nbPerPage));
        else
            entities = preparedQuery.asList(FetchOptions.Builder.withLimit(nbPerPage).offset((nbPerPage)*(page-1)));

        Card cards[] = new Card[entities.size()];
        int cpt = 0;
        for (Entity e: entities) {
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
     * @return random Card
     * @throws DatastoreGetter.DataStoreNotAvailableException Exception throwed if {@link DatastoreService} is not available
     */
    public static Card drawFromStore() throws DatastoreGetter.DataStoreNotAvailableException {
        DatastoreService datastore = DatastoreGetter.getDatastore();

        Query query = new Query("Card");
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> entities = preparedQuery.asList(FetchOptions.Builder.withDefaults());

        double maxProba = 0.0;
        for(Entity e : entities){
            maxProba += (Double) e.getProperty("probability");
        }

        Random r = new Random();
        double randomValue = (maxProba) * r.nextDouble();

        double sum = 0;
        int index = 0;
        long id = -1;
        while(sum < randomValue && index < entities.size()){
            sum += (Double) entities.get(index).getProperty("probability");
            id = entities.get(index).getKey().getId();
            index++;
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
    public void saveToStore() throws DatastoreGetter.DataStoreNotAvailableException {
        Entity pixabayImage = new Entity("Card", this.id);
        pixabayImage.setProperty("tags", new JSONArray(this.tags).join(","));
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
        ImageWriter writer = (ImageWriter)iter.next();

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

    // TODO : generate Card image with Graphics2D
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

        this.addImageAndStarToCard (cardGraphics, colorBgCard);

        // Add the rarity text and square
        RoundRectangle2D rect = new RoundRectangle2D.Float(25, 430, 350, 25, 12, 12);
        addSquareAndTextIntoGraphics2D(cardGraphics,this.deAccent("Rareté : "+ String.format("%.2f", this.probability) +"%"), rect,colorBgText, Color.BLACK, fontText);

        // Add the tags square and text
        rect = new RoundRectangle2D.Float(25, 460, 350, 25, 12, 12);
        addSquareAndTextIntoGraphics2D(cardGraphics, "Auteur : " + deAccent(this.pixabayAuthorName), rect,colorBgText, Color.BLACK, fontText);

        // Add Leyenda square and text
        rect = new RoundRectangle2D.Float(25, 500, 350, 70, 12, 12);
        addSquareAndTextIntoGraphics2D(cardGraphics, "Tags : " + this.deAccentStringArray(this.tags) , rect,colorBgText, Color.BLACK, fontText);

        // Add the id text
        rect = new RoundRectangle2D.Float(275, 570, 125, 30, 12, 12);
        addSquareAndTextIntoGraphics2D(cardGraphics, "id : " + this.id, rect,colorBgCard, Color.WHITE, fontText);

//        // Add the card url
//        this.cardImageURL = new URL("https://storage.googleapis.com/cardexchangemaven.appspot.com/"+this.id+".jpg");
      
        GcsFileOptions opt = new GcsFileOptions.Builder().mimeType("image/jpeg").acl("public-read").build();
        GcsService service = GcsServiceFactory.createGcsService();
        GcsFilename name = new GcsFilename("cardexchangemaven.appspot.com", this.id+".jpg");
        Image image = ImagesServiceFactory.makeImage(this.getBytes(bfImg));

        ByteBuffer buff =  ByteBuffer.wrap(image.getImageData());
        service.createOrReplace(name, opt, buff);

        if(SystemProperty.environment.value() == SystemProperty.Environment.Value.Production){
            this.cardImageURL = new URL("https://storage.googleapis.com/" + name.getBucketName() + "/" + name.getObjectName());
        }else{
            ImagesService imagesService = ImagesServiceFactory.getImagesService();
            this.cardImageURL = new URL(imagesService.getServingUrl(ServingUrlOptions.Builder.withGoogleStorageFileName("/gs/" + name.getBucketName() + "/" + name.getObjectName()) ));

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

    /**
     * Return an png img in BufferedImage which can be transform with Graphics2D class
     * @param imageUrl
     * @return BufferedImage
     * @throws IOException
     */
    public BufferedImage urlImageToBufferedImage (URL imageUrl) throws IOException, DatastoreGetter.DataStoreNotAvailableException {
        String tempName = this.getId() + "_temp.png";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream inputStream = imageUrl.openStream();
        int read;
        while ((read = inputStream.read()) != -1) {
            baos.write(read);
        }

        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        Image image = ImagesServiceFactory.makeImage(baos.toByteArray());
        // this throws an exception if data is not image or unsupported format
        // you can wrap this in try..catch and act accordingly
        try {
            image.getFormat();
        } catch (Exception e) {
            log.severe("Card.java, urlImageToBufferedImage : image format not accepted");
        }
        // this is a dummy transform, which do nothing
        Transform transform = ImagesServiceFactory.makeCrop(0.0, 0.0, 1.0, 1.0);
        // setting the output to PNG
        OutputSettings outputSettings = new OutputSettings(ImagesService.OutputEncoding.PNG);
        outputSettings.setQuality(100);
        // apply dummy transform and output settings
        Image newImage = imagesService.applyTransform(transform, image, outputSettings);


        // UPLOAD image
        GcsFileOptions opt = new GcsFileOptions.Builder().mimeType("image/png").acl("public-read").build();
        GcsService service = GcsServiceFactory.createGcsService();
        GcsFilename name = new GcsFilename("cardexchangemaven.appspot.com", tempName);

        ByteBuffer buff =  ByteBuffer.wrap(newImage.getImageData());
        service.createOrReplace(name, opt, buff);


        URL pngImg = new URL("https://storage.googleapis.com/cardexchangemaven.appspot.com/"+tempName);
        BufferedImage newImg = null;

        if(SystemProperty.environment.value() == SystemProperty.Environment.Value.Production){
            newImg = ImageIO.read(new URL("https://storage.googleapis.com/" + name.getBucketName() + "/" + name.getObjectName()));
        }else{
            newImg = ImageIO.read(new URL(imagesService.getServingUrl(ServingUrlOptions.Builder.withGoogleStorageFileName("/gs/" + name.getBucketName() + "/" + name.getObjectName()))));
        }


        service.delete(name);

        return newImg;
    }

    /**
     * Draw a String centered in the middle of a y roundedRectangle at 15px to the begin x of the rectangle.
     *
     * @param g The Graphics instance.
     * @param text The String to draw.
     * @param rect The roundedRectangle to center the text in.
     */
    public static void drawCenteredString(Graphics g, String text, RoundRectangle2D rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = (int) rect.getX() + 15;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = (int) (rect.getY() + rect.getHeight() + metrics.getAscent()/2);
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    public static String deAccentStringArray (String[] strArr) {
        if(strArr.length == 0)
            return "";

        String res = strArr[0];
        for(int i = 1; i < strArr.length; i++){
            res += ", " + strArr[i];
        }

        return res;
    }

    public static String deAccent(String str) {
//        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
//        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//        return pattern.matcher(nfdNormalizedString).replaceAll("");

        String newStr = "";

        for(int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            if(c == 'é'){
                newStr += 'e';
            }else if(c =='è'){
                newStr += 'e';
            }else if(c =='ê'){
                newStr += 'e';
            }else if(c =='à'){
                newStr += 'a';
            }else if(c =='â'){
                newStr += 'a';
            }else if(c =='ä'){
                newStr += 'a';
            }else if(c =='î'){
                newStr += 'i';
            }else if(c =='ï'){
                newStr += 'i';
            }else if(c =='ô'){
                newStr += 'o';
            }else if(c =='ö'){
                newStr += 'o';
            }else if(c =='û'){
                newStr += 'u';
            }else if(c =='ü'){
                newStr += 'u';
            }else{
                newStr += c;
            }
        }

        return newStr;
    }

    /**
     * Add a rounded corner to a picture
     * @param image
     * @param cornerRadius in pixel
     * @return
     */
    public static BufferedImage makeRoundedCornerImage(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    /**
     * Add a square and a text y centering into a graphics2D
     * @param g
     * @param text to draw
     * @param rect rectangle to draw
     * @param colorBgText color of the rectangle
     * @param colorText color of the text
     * @param fontText font of the text
     */
    public static void addSquareAndTextIntoGraphics2D(Graphics2D g, String text, RoundRectangle2D rect, Color colorBgText, Color colorText, Font fontText) {
        g.setColor(colorBgText);
        g.draw(rect);
        g.fill(rect);
        g.setColor(colorText);
        Card.drawCenteredString(g, text, rect, fontText);

    }


    public void addImageAndStarToCard (Graphics2D g, Color colorBgCard) throws IOException, DatastoreGetter.DataStoreNotAvailableException {
        BufferedImage mig = ImageIO.read(new File("newBack.png"));
        g.drawImage(mig, 0, 0, 400, 600, null);

        // Get the image of pixabayImageURL
        BufferedImage newImg = this.urlImageToBufferedImage(this.pixabayImageURL);
        newImg = makeRoundedCornerImage(newImg, 20);
        //        fill the card background
        //g.setColor(colorBgCard);
        //g.fillRect(0, 0, WIDTH, HEIGHT);
        // Add image on the top of the card and resize it


        int widthFenetre = 350;
        int heightFenetre = 400;
        int widthImage = newImg.getWidth();
        int heightImage = newImg.getHeight();

        double ratioImage = (0.0 + widthImage) / heightImage;
        double ratioFenetre = (0.0 + widthFenetre) / heightFenetre;

        int newWidth = 0;
        int newHeight = 0;

        if(ratioFenetre > ratioImage){
            newWidth = (widthImage * heightFenetre) / heightImage;
            newHeight = heightFenetre;
        }else{
            newWidth = widthFenetre;
            newHeight = (heightImage * widthFenetre) / widthImage;
        }

        int top = (heightFenetre - newHeight) / 2;
        int left = (widthFenetre - newWidth) / 2;

//        g.drawImage(newImg, 25, 25, 350, 400, null);


        if(heightImage > widthImage){
            g.drawImage(newImg, 25 + left, 25, newWidth, newHeight, null);
        }else{
            g.drawImage(newImg, 25, 25 + top, newWidth, newHeight, null);
        }



        // Get the star point on top right of the card
//        BufferedImage starImg = ImageIO.read(new URL("https://storage.googleapis.com/cardexchangemaven.appspot.com/_star_point.png"));

        BufferedImage starImg = ImageIO.read(new File("star_point.png"));

        int nbStar = Math.toIntExact(Math.round(10 * this.probability));

        // Add the star to the image
        for (int i = 0; i < nbStar; i++)
            g.drawImage(starImg, 350 - (22 * i), 2, 20, 20, null);

    }

}
