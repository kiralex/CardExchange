package com.derniamepoirier;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.harium.dotenv.Env;

import javax.imageio.ImageIO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Properties;


@WebServlet(name = "HelloAppEngine", value = "/hello")
public class HelloAppEngine extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Properties properties = System.getProperties();

    response.setContentType("text/plain");
    response.getWriter().println("Hello App Engine - Standard using "
        + SystemProperty.version.get() + " Java " + properties.get("java.specification.version"));



    response.getWriter().println(Env.get("API_KEY"));


    response.getWriter().println("Working Directory = " +
            System.getProperty("user.dir"));

    URL url = new URL("https://i.pinimg.com/originals/27/97/b5/2797b5ec4f5a9b33212af0596c88b272.jpg");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    InputStream inputStream = url.openStream();
    int read;
    while ((read = inputStream.read()) != -1) {
      baos.write(read);
    }

    Image image = ImagesServiceFactory.makeImage(baos.toByteArray());



    GcsFileOptions opt = new GcsFileOptions.Builder().mimeType("image/jpeg").build();
    GcsService service = GcsServiceFactory.createGcsService();
    GcsFilename name = new GcsFilename("cardexchangemaven.appspot.com", "toto.jpg");

    response.getWriter().println("En theorie je upload...");

    ByteBuffer buff =  ByteBuffer.wrap(image.getImageData());

    service.createOrReplace(name, opt, buff);

  }

  public static String getInfo() {
    return "Version: " + System.getProperty("java.version")
          + " OS: " + System.getProperty("os.name")
          + " User: " + System.getProperty("user.name");
  }

}
