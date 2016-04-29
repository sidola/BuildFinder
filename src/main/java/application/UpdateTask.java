package application;

import java.util.Optional;

import com.eclipsesource.json.JsonValue;

import application.config.AppProperties;
import application.util.JsonFetcher;
import javafx.concurrent.Task;

public class UpdateTask extends Task<Optional<String>> {

    @Override
    protected Optional<String> call() throws Exception {

        JsonValue jsonValue = JsonFetcher
                .fetchJson(AppProperties.getProperty(AppProperties.RELEASES_API_URL));
        JsonValue firstEntry = jsonValue.asArray().get(0);

        String tagVersion = firstEntry.asObject().get("tag_name").asString().substring(1);
        String body = firstEntry.asObject().get("body").asString();

        int thisVersion = Integer.parseInt(
                AppProperties.getProperty(AppProperties.VERSION).replaceAll("\\.", ""));
        int onlineVersion = Integer.valueOf(tagVersion.replaceAll("\\.", ""));

        if (thisVersion >= onlineVersion) {
            return Optional.empty();
        }

        return Optional.of(body);
    }

}
