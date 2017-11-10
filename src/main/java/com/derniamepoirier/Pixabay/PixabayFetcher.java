package com.derniamepoirier.Pixabay;

import com.harium.dotenv.Env;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

public class PixabayFetcher {
    private static final Logger log = Logger.getLogger(PixabayFetcher.class.getName());
    private static final String PIXABAY_API_KEY = Env.get("PIXABAY_API_KEY");

    private PixabayFetcher(){}

    public interface PixabayAPIOptions {}

    public enum Lang implements PixabayAPIOptions {
        CS("cs"), DA("da"), DE("de"), EN("en"), ES("es"), FR("fr"), ID("id"), IT("it"), HU("hu"), NL("nl"), NO("no"), PL("pl"), PT("pt"), RO("ro"), SK("sk"), FI("fi"), SV("sv"), TR("tr"), VI("vi"), TH("th"), BG("bg"), RU("ru"), EL("el"), JA("ja"), KO("ko"), ZH("zk");

        final private String lang;

        Lang(String lang){
            this.lang = lang;
        }

        @Override
        public String toString() {
            return this.lang;
        }
    }

    public enum ImageType implements PixabayAPIOptions {
        ALL("all"), PHOTO("photo"), ILLUSTRATION("illustration"), VECTOR("vector");

        final private String imageType;
        ImageType(String imageType){
            this.imageType = imageType;
        }

        @Override
        public String toString() {
            return this.imageType;
        }
    }

    public enum Orientation implements PixabayAPIOptions {
        ALL("all"), HORIZONTAL("horizontal"), VERTICAL("vertical");

        final private String orientation;

        Orientation(String orientation){
            this.orientation = orientation;
        }

        @Override
        public String toString() {
            return this.orientation;
        }

    }

    public enum Category implements PixabayAPIOptions {
        ALL("all"), FASHION("fashion"), NATURE("nature"), BACKGROUNDS("backgrounds"), SCIENCE("science"), EDUCATION("education"), PEOPLE("people"), FEELINGS("feelings"), RELIGION("religion"), HEALTH("health"), PLACES("places"), ANIMALS("animals"), INDUSTRY("industry"), FOOD("food"), COMPUTER("computer"), SPORTS("sports"), TRANSPORTATION("transportation"), TRAVEL("travel"), BUILDINGS("buildings"), BUSINESS("business"), MUSIC("music");

        final private String category;

        Category(String category){
            this.category = category;
        }
    }

    public enum Order implements PixabayAPIOptions {
        LATEST("latest"), POPULAR("popular");

        final private String order;

        Order(String order){
            this.order = order;
        }

        @Override
        public String toString() {
            return this.order;
        }
    }

    public enum SafeSearch implements PixabayAPIOptions {
        ENABLED("true"), DISABLED("false");

        final private String safeSearch;

        SafeSearch(String safeSearch){
            this.safeSearch = safeSearch;
        }

        @Override
        public String toString() {
            return this.safeSearch;
        }
    }

    public enum EditorChoice implements PixabayAPIOptions {
        ENABLED("true"), DISABLED("false");

        final private String safeSearch;

        EditorChoice(String safeSearch){
            this.safeSearch = safeSearch;
        }

        @Override
        public String toString() {
            return this.safeSearch;
        }
    }

    public static String fetch() throws PixabayIncorrectParameterException, PixabayApiKeyMissingException, PixabayResponseCodeException {
        return PixabayFetcher.fetch("", new PixabayAPIOptions[]{}, 1, 20);
    }

    public static String fetch(String query) throws PixabayIncorrectParameterException, PixabayApiKeyMissingException, PixabayResponseCodeException {
        return PixabayFetcher.fetch(query, new PixabayAPIOptions[]{}, 1, 20);
    }

    public static String fetch(PixabayAPIOptions options[]) throws PixabayIncorrectParameterException, PixabayApiKeyMissingException, PixabayResponseCodeException {
        return PixabayFetcher.fetch("", options, 1, 20);
    }


    public static String fetch(String query, PixabayAPIOptions options[]) throws PixabayIncorrectParameterException, PixabayApiKeyMissingException, PixabayResponseCodeException {
        return PixabayFetcher.fetch(query, options, 1, 20);
    }


    public static String fetch(String query, PixabayAPIOptions options[], int page, int nbResultsPerPage) throws PixabayApiKeyMissingException, PixabayIncorrectParameterException, PixabayResponseCodeException {

        // Checking parametters
        if(PIXABAY_API_KEY == null || PIXABAY_API_KEY.isEmpty())
            throw new PixabayApiKeyMissingException("Pixabay API Key is misssing in environment variables");

        if(page <= 0)
            throw new PixabayIncorrectParameterException("Page number is incorrect");

        if(nbResultsPerPage < 3)
            throw new PixabayIncorrectParameterException("Number of results per pages should be greater than or equal than 3");

        if(nbResultsPerPage > 200)
            throw new PixabayIncorrectParameterException("Number of results per pages should be less than or equal to 200");

        // defaults parameters of the pixabay API request
        Lang lang = Lang.FR;
        ImageType imageType = ImageType.ALL;
        Orientation orientation = Orientation.ALL;
        Category category = Category.ALL;
        EditorChoice editorChoice = EditorChoice.DISABLED;
        SafeSearch safeSearch = SafeSearch.ENABLED;
        Order order = Order.LATEST;

        // Replace defaults parameters with user-defined value
        for(PixabayAPIOptions opt : options) {
            if (opt instanceof Lang)
                lang = (Lang) opt;
            else if (opt instanceof ImageType)
                imageType = (ImageType) opt;
            else if (opt instanceof Orientation)
                orientation = (Orientation) opt;
            else if (opt instanceof Category)
                category = (Category) opt;
            else if (opt instanceof EditorChoice)
                editorChoice = (EditorChoice) opt;
            else if (opt instanceof SafeSearch)
                safeSearch = (SafeSearch) opt;
            else if (opt instanceof Order)
                order = (Order) opt;
        }

        // Generating URL
        String urlString = "";
        try {
            URIBuilder uriBuilder = new URIBuilder("https://pixabay.com/api/");

            uriBuilder.addParameter("lang", lang.toString())
                    .addParameter("image_type", imageType.toString())
                    .addParameter("orientation", orientation.toString())
                    .addParameter("category", category.toString())
                    .addParameter("editor_choice", editorChoice.toString())
                    .addParameter("safesearch", safeSearch.toString())
                    .addParameter("order", order.toString())
                    .addParameter("page", String.valueOf(page))
                    .addParameter("per_page", String.valueOf(nbResultsPerPage))
                    .addParameter("key", PIXABAY_API_KEY);

            URL url = uriBuilder.build().toURL();
            urlString = url.toString();


        } catch (URISyntaxException e) {
            log.severe("Error while generating URL for API call : " + e.getMessage() + " " + e.getReason());
            return null;
        }
        catch (MalformedURLException e) {
            log.severe("Error while generating URL for API call : " + e.getMessage());
            return null;
        } catch (IOException e) {
            log.severe("Error while generating URL for API call : " + e.getMessage());
            return null;
        }

        // Making HTTP Request
        HttpClient client = HttpClients.createDefault();
        HttpGet getStubMethod = new HttpGet(urlString);
        HttpResponse getStubResponse = null;


        // Getting answer
        try {
            getStubResponse = client.execute(getStubMethod);
        } catch (IOException e) {
            log.severe("Error while getting API Response: " + e.getMessage());
            return null;
        }


        // Check status code
        int getStubStatusCode = getStubResponse.getStatusLine()
                .getStatusCode();

        String responseBody = "";

        // Get string response
        try {
             responseBody = EntityUtils
                    .toString(getStubResponse.getEntity());

            log.info(responseBody);
        } catch (IOException e) {
            log.severe("Error while getting API Response body: " + e.getMessage());
            return null;
        }

        if(getStubStatusCode == 429)
            throw new PixabayResponseCodeException("Pixabay API rate limit exceeded");

        if (getStubStatusCode < 200 || getStubStatusCode >= 300)
            throw new PixabayResponseCodeException("Pixabay API respond code " + getStubStatusCode + " -> " + responseBody);



        return responseBody;
    }
}
