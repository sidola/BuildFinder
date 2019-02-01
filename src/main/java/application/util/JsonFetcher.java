package application.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

/**
 * Utility class to fetch JSON data.
 */
public class JsonFetcher {

    /**
     * Downloads JSON from the given URL.
     * 
     * @return A {@link JsonValue} of the downloaded JSON data.
     */
    public static JsonValue fetchJson(String urlAsString) {

        try {

            URL url = new URL(urlAsString);
            InputStream urlInputStream = null;

            try {
                urlInputStream = url.openStream();
            } catch (IOException urlStreamException) {
                throw new RuntimeException("Could not establish a network connection",
                        urlStreamException);
            }

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlInputStream, "UTF-8"));

            StringBuilder html = new StringBuilder();
            bufferedReader.lines().forEach(html::append);
            bufferedReader.close();

            return Json.parse(html.toString());

        } catch (IOException e) {
            throw new RuntimeException("Could not fetch the JSON data", e);
        }

    }

}
