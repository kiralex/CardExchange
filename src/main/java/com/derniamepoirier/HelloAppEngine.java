package com.derniamepoirier;

import com.derniamepoirier.CardGeneration.Card;
import com.derniamepoirier.CardGeneration.CardGenerator;
import com.derniamepoirier.CardGeneration.PixabayAPIExceptions.PixabayIncorrectParameterException;
import com.derniamepoirier.CardGeneration.PixabayFetcher;
import com.derniamepoirier.Utils.DatastoreGetter;
import com.google.appengine.api.utils.SystemProperty;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import static com.derniamepoirier.CardGeneration.PixabayAPIExceptions.PixabayApiKeyMissingException;
import static com.derniamepoirier.CardGeneration.PixabayAPIExceptions.PixabayResponseCodeException;


@WebServlet(name = "HelloAppEngine", value = "/hello")
public class HelloAppEngine extends HttpServlet {
  private static final Logger log = Logger.getLogger(HelloAppEngine.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Properties properties = System.getProperties();

    //response.setContentType("text/plain");

    response.getWriter().println("<title>Yolo !</title");
    response.getWriter().println("<p>Hello App Engine - Standard using "
        + SystemProperty.version.get() + " Java " + properties.get("java.specification.version") + "</p>");



//    response.getWriter().println(Env.get("API_KEY"));


//    response.getWriter().println("Working Directory = " +
//            System.getProperty("user.dir"));
//
//    URL url = new URL("https://i.pinimg.com/originals/27/97/b5/2797b5ec4f5a9b33212af0596c88b272.jpg");
//
//    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//    InputStream inputStream = url.openStream();
//    int read;
//    while ((read = inputStream.read()) != -1) {
//      baos.write(read);
//    }
//
//    Image image = ImagesServiceFactory.makeImage(baos.toByteArray());
//
//    // transform image
//
//
//
//    GcsFileOptions opt = new GcsFileOptions.Builder().mimeType("image/jpeg").build();
//    GcsService service = GcsServiceFactory.createGcsService();
//    GcsFilename name = new GcsFilename("cardexchangemaven.appspot.com", "toto.jpg");
//
//    response.getWriter().println("En theorie je upload...");
//
//    ByteBuffer buff =  ByteBuffer.wrap(image.getImageData());
//    service.createOrReplace(name, opt, buff);

    PixabayFetcher.PixabayAPIOptions options[] = new PixabayFetcher.PixabayAPIOptions[]{PixabayFetcher.ImageType.PHOTO, PixabayFetcher.Order.POPULAR, PixabayFetcher.Orientation.VERTICAL};

    try {
      Card cards[] = CardGenerator.generate("poney", options, 10);
//
//      for (Card c: cards ) {
//        response.getWriter().println(c);
//      }
      response.getWriter().println("=======================");

      //response.getWriter().println(Card.drawFromStore());
    } catch (DatastoreGetter.DataStoreNotAvailableException e) {
      e.printStackTrace();
    } catch (PixabayIncorrectParameterException e) {
      e.printStackTrace();
    } catch (PixabayApiKeyMissingException e) {
      e.printStackTrace();
    } catch (PixabayResponseCodeException e) {
      e.printStackTrace();
    }


  }

  public static String getInfo() {
    return "Version: " + System.getProperty("java.version")
          + " OS: " + System.getProperty("os.name")
          + " User: " + System.getProperty("user.name");
  }

}
