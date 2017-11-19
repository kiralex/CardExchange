package com.derniamepoirier.Utils;

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
import com.google.code.appengine.imageio.ImageIO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class PaintImageUtils {
    private static final Logger log = Logger.getLogger(PaintImageUtils.class.getName());

    private PaintImageUtils() {
    }

    /**
     * Return an png img in BufferedImage which can be transform with Graphics2D class
     *
     * @param imageUrl
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage urlImageToBufferedImage(long id, URL imageUrl) throws IOException, DatastoreGetter.DataStoreNotAvailableException {
        String tempName = id + "_temp.png";
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

        ByteBuffer buff = ByteBuffer.wrap(newImage.getImageData());
        service.createOrReplace(name, opt, buff);


        URL pngImg = new URL("https://storage.googleapis.com/cardexchangemaven.appspot.com/" + tempName);
        BufferedImage newImg = null;

        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            newImg = ImageIO.read(new URL("https://storage.googleapis.com/" + name.getBucketName() + "/" + name.getObjectName()));
        } else {
            newImg = ImageIO.read(new URL(imagesService.getServingUrl(ServingUrlOptions.Builder.withGoogleStorageFileName("/gs/" + name.getBucketName() + "/" + name.getObjectName()))));
        }


        service.delete(name);

        return newImg;
    }

    /**
     * Draw a String centered in the middle of a y roundedRectangle at 15px to the begin x of the rectangle.
     *
     * @param g    The Graphics instance.
     * @param text The String to draw.
     * @param rect The roundedRectangle to center the text in.
     */
    public static void drawCenteredString(Graphics g, String text, RoundRectangle2D rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = (int) rect.getX() + 15;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = (int) (rect.getY() + rect.getHeight() + metrics.getAscent() / 2);
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    public static String deAccentStringArray(String[] strArr) {
        if (strArr.length == 0)
            return "";

        String res = strArr[0];
        for (int i = 1; i < strArr.length; i++) {
            res += ", " + strArr[i];
        }

        return res;
    }

    public static String deAccent(String str) {
//        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
//        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//        return pattern.matcher(nfdNormalizedString).replaceAll("");

        String newStr = "";

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == 'é') {
                newStr += 'e';
            } else if (c == 'è') {
                newStr += 'e';
            } else if (c == 'ë') {
                newStr += 'e';
            } else if (c == 'ê') {
                newStr += 'e';
            } else if (c == 'à') {
                newStr += 'a';
            } else if (c == 'â') {
                newStr += 'a';
            } else if (c == 'ä') {
                newStr += 'a';
            } else if (c == 'î') {
                newStr += 'i';
            } else if (c == 'ï') {
                newStr += 'i';
            } else if (c == 'ô') {
                newStr += 'o';
            } else if (c == 'ö') {
                newStr += 'o';
            } else if (c == 'û') {
                newStr += 'u';
            } else if (c == 'ü') {
                newStr += 'u';
            } else {
                newStr += c;
            }
        }

        return newStr;
    }

    /**
     * Add a rounded corner to a picture
     *
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
     *
     * @param g
     * @param text        to draw
     * @param rect        rectangle to draw
     * @param colorBgText color of the rectangle
     * @param colorText   color of the text
     * @param fontText    font of the text
     */
    public static void addSquareAndTextIntoGraphics2D(Graphics2D g, String text, RoundRectangle2D rect, Color colorBgText, Color colorText, Font fontText) {
        g.setColor(colorBgText);
        g.draw(rect);
        g.fill(rect);
        g.setColor(colorText);
        PaintImageUtils.drawCenteredString(g, text, rect, fontText);

    }


    public static void addImageAndStarToCard(long id, URL pixabayImageURL, double probability, Graphics2D g, Color colorBgCard) throws IOException, DatastoreGetter.DataStoreNotAvailableException {
        BufferedImage mig = ImageIO.read(new File("newBack.png"));
        mig.flush();
        g.drawImage(mig, 0, 0, 400, 600, null);

        // Get the image of pixabayImageURL
        BufferedImage newImg = PaintImageUtils.urlImageToBufferedImage(id, pixabayImageURL);
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

        if (ratioFenetre > ratioImage) {
            newWidth = (widthImage * heightFenetre) / heightImage;
            newHeight = heightFenetre;
        } else {
            newWidth = widthFenetre;
            newHeight = (heightImage * widthFenetre) / widthImage;
        }

        int top = (heightFenetre - newHeight) / 2;
        int left = (widthFenetre - newWidth) / 2;

//        g.drawImage(newImg, 25, 25, 350, 400, null);


        if (heightImage > widthImage) {
            g.drawImage(newImg, 25 + left, 25, newWidth, newHeight, null);
        } else {
            g.drawImage(newImg, 25, 25 + top, newWidth, newHeight, null);
        }


        // Get the star point on top right of the card
//        BufferedImage starImg = ImageIO.read(new URL("https://storage.googleapis.com/cardexchangemaven.appspot.com/_star_point.png"));

        BufferedImage starImg = ImageIO.read(new File("star_point.png"));

        int nbStar = (int) Math.ceil(10.0 * probability);

        // Add the star to the image
        for (int i = 0; i < nbStar; i++)
            g.drawImage(starImg, 350 - (22 * i), 2, 20, 20, null);

    }
}
