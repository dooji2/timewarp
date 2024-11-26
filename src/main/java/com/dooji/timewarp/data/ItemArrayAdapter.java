package com.dooji.timewarp.data;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ItemArrayAdapter implements JsonDeserializer<Item[]>, JsonSerializer<Item[]> {

    @Override
    public Item[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonArray()) {
            throw new JsonParseException("Expected an array for items");
        }

        List<Item> items = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray()) {
            Identifier id = Identifier.of(element.getAsString());
            Item item = Registries.ITEM.get(id);
            if (item == null) {
                throw new JsonParseException("Unknown item ID: " + id);
            }
            items.add(item);
        }

        return items.toArray(new Item[0]);
    }

    @Override
    public JsonElement serialize(Item[] items, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jsonArray = new JsonArray();
        for (Item item : items) {
            Identifier id = Registries.ITEM.getId(item);
            if (id != null) {
                jsonArray.add(id.toString());
            }
        }
        return jsonArray;
    }
}