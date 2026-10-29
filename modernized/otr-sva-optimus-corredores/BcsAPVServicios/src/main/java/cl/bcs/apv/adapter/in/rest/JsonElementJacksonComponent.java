package cl.bcs.apv.adapter.in.rest;

import org.springframework.boot.jackson.JacksonComponent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

/**
 * The typed outcomes of {@code DevolverPagoRezagos} and {@code DevolverBonificacionEntidadRecaudadora} carry the opaque Tesoreria payloads as Gson
 * {@link JsonObject}s (BcsAPVApi depends on gson only). Jackson cannot (de)serialise a Gson tree, so it is written and read as plain JSON text.
 */
@JacksonComponent
public class JsonElementJacksonComponent {

    public static class Serializador extends ValueSerializer<JsonElement> {
        @Override
        public void serialize(JsonElement valor, JsonGenerator gen, SerializationContext contexto) {
            gen.writeRawValue(valor.toString());
        }

        @Override
        public Class<JsonElement> handledType() {
            return JsonElement.class;
        }
    }

    public static class Deserializador extends ValueDeserializer<JsonObject> {
        @Override
        public JsonObject deserialize(JsonParser parser, DeserializationContext contexto) {
            return com.google.gson.JsonParser.parseString(contexto.readTree(parser).toString()).getAsJsonObject();
        }
    }
}
