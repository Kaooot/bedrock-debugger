package dev.kaooot.debugger.util.protocoldocs.model.deserializer;

import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockArray;
import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockCondition;
import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockEnum;
import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockMap;
import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockMapEntry;
import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockProperty;
import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockRef;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class PropertyDeserializer implements JsonDeserializer<BedrockProperty> {

    private static final Gson GSON = new Gson();

    @Override
    public BedrockProperty deserialize(JsonElement jsonElement, Type type,
                                       JsonDeserializationContext context)
        throws JsonParseException {
        if (!jsonElement.isJsonObject()) {
            return null;
        }
        final JsonObject object = jsonElement.getAsJsonObject();
        Class<? extends BedrockProperty> clazz = BedrockProperty.class;
        if (object.has("items")) {
            final BedrockArray array = GSON.fromJson(object, BedrockArray.class);
            array.setItems(this.deserialize(object.get("items"), type, context));
            return array;
        } else if (object.has("oneOf")) {
            final BedrockCondition condition = GSON.fromJson(object, BedrockCondition.class);
            final JsonArray array = object.getAsJsonArray("oneOf");
            final List<BedrockProperty> oneOf = new ObjectArrayList<>();
            for (final JsonElement element : array) {
                oneOf.add(this.deserialize(element, type, context));
            }
            condition.setOneOf(oneOf);
            return condition;
        } else if (object.has("enum")) {
            clazz = BedrockEnum.class;
        } else if (object.has("additionalProperties")) {
            final BedrockMap map = GSON.fromJson(object, BedrockMap.class);
            final JsonObject additionalProperties = object.getAsJsonObject("additionalProperties");
            if (additionalProperties.has("properties")) {
                final JsonObject properties = additionalProperties.getAsJsonObject("properties");
                final BedrockProperty key = this.deserialize(
                    properties.getAsJsonObject("key"),
                    type,
                    context
                );
                final BedrockProperty value = this.deserialize(
                    properties.getAsJsonObject("value"),
                    type,
                    context
                );
                final BedrockMapEntry entry = GSON.fromJson(
                    additionalProperties,
                    BedrockMapEntry.class
                );
                entry.setProperties(new BedrockMapEntry.Properties(key, value));
                map.setAdditionalProperties(entry);
            } else {
                map.setAdditionalProperties(
                    this.deserialize(additionalProperties, type, context)
                );
            }
            return map;
        } else if (object.has("$ref")) {
            clazz = BedrockRef.class;
        }
        return GSON.fromJson(object, clazz);
    }
}