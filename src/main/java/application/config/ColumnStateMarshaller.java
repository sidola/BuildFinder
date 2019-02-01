package application.config;

import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import application.gui.model.BuildTableColumn;
import application.gui.model.BuildTableColumnState;

/**
 * Util class to help serialize/deserialize column state to the preference file.
 */
public class ColumnStateMarshaller {

    /**
     * Takes a list of states and returns a JSON array as a string.
     */
    public static String marshallToString(List<BuildTableColumnState> columnStates) {
        JsonArray jsonArray = Json.array();

        columnStates.stream().map(columnState -> {

            return Json.object().add("column", columnState.getColumn().name)
                    .add("width", columnState.getWidth())
                    .add("index", columnState.getIndex())
                    .add("isVisible", columnState.isVisible());

        }).forEach(jsonArray::add);

        return jsonArray.toString();
    }

    /**
     * Takes a JSON array and parses it to a list of state objects.
     */
    public static List<BuildTableColumnState> unmarshallFromString(String jsonData) {
        JsonArray jsonArray = Json.parse(jsonData).asArray();
        List<BuildTableColumnState> columnStates = new ArrayList<>();

        for (JsonValue jsonValue : jsonArray) {
            JsonObject jsonObject = jsonValue.asObject();

            BuildTableColumn column = BuildTableColumn
                    .fromName(jsonObject.get("column").asString());

            int size = jsonObject.getInt("width", 150);
            int index = jsonObject.getInt("index", 0);
            boolean isVisible = jsonObject.getBoolean("isVisible", true);

            columnStates.add(new BuildTableColumnState(column, index, size, isVisible));
        }

        return columnStates;
    }

}
